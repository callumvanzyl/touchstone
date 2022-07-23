package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.configuration.DockerConfiguration
import com.callumvanzyl.touchstone.model.ContainerType
import com.callumvanzyl.touchstone.model.DatabaseDriver
import com.callumvanzyl.touchstone.model.DeploymentPlan
import com.callumvanzyl.touchstone.model.TestRun
import com.callumvanzyl.touchstone.util.Outcome
import com.github.dockerjava.api.model.Image
import java.io.File
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

interface OrchestrationService {
    fun deployPlans(plans: List<DeploymentPlan>, testRun: TestRun): Outcome<TestRun>
    fun generateLogs(testRun: TestRun): Outcome<Boolean>
    fun tidy(): Outcome<Boolean>
}

@Service("OrchestrationService")
@ConditionalOnProperty(value = ["docker.enabled"], havingValue = "true")
class OrchestrationServiceImpl(
    @Value("\${touchstone.data-path}") private val touchstoneDataPath: String,

    private val dockerService: DockerService?
) : OrchestrationService {

    override fun deployPlans(plans: List<DeploymentPlan>, testRun: TestRun): Outcome<TestRun> {
        var updated = testRun

        tidy()

        val networkConfigurationsResult = generateNetworkConfigurations(plans)
        when (networkConfigurationsResult) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = networkConfigurationsResult.reason)
        }
        networkConfigurationsResult.data.forEach { config ->
            updated = updated.copy(
                additionalScriptVariables = updated.additionalScriptVariables + mapOf(
                    Pair("${config.key}-host", config.value.host),
                    Pair("${config.key}-port", "${config.value.port ?: config.value.exposedPort}"),
                    Pair("${config.key}-url", "http://${config.value.host}:${config.value.port ?: config.value.exposedPort}")
                )
            )
        }
        plans.forEach {
            when (val deployResult = deployPlan(it, networkConfigurationsResult.data)) {
                is Outcome.Success -> {}
                is Outcome.Error -> return Outcome.Error(reason = deployResult.reason)
            }
        }
        return Outcome.Success(updated)
    }

    override fun generateLogs(testRun: TestRun): Outcome<Boolean> {
        when (val containersResult = dockerService!!.findDanglingContainers()) {
            is Outcome.Success -> {
                containersResult.data.forEach { container ->
                    when (val logsResult = dockerService.getLogs(container.id)) {
                        is Outcome.Success -> {
                            val destination = File("$touchstoneDataPath/logs/${testRun.id!!}")
                            destination.mkdirs()
                            val out = File(destination, "${container.names.first()}.txt")
                            logsResult.data.forEach { out.appendText("$it\n") }
                        }
                        is Outcome.Error -> return Outcome.Error(reason = logsResult.reason)
                    }
                }
            }
            is Outcome.Error -> return Outcome.Error(reason = containersResult.reason)
        }
        return Outcome.Success(true)
    }

    override fun tidy(): Outcome<Boolean> =
        when (val tidyResult = dockerService!!.destroyDanglingContainers()) {
            is Outcome.Success -> Outcome.Success(true)
            is Outcome.Error -> Outcome.Error(reason = tidyResult.reason)
        }

    fun deployPlan(plan: DeploymentPlan, networkConfigurations: Map<String, NetworkConfiguration>): Outcome<Boolean> {
        val isDockerRuntime = (dockerService!!.isRuntimeDockerised() as Outcome.Success).data

        val imageResult = fetchImage(plan)
        when (imageResult) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = imageResult.reason)
        }

        var environment = when (plan.type) {
            ContainerType.APPLICATION -> plan.environment
            ContainerType.DATABASE -> {
                if (plan.databaseSettings == null) {
                    return Outcome.Error(reason = "Database settings cannot be empty in a database deployment plan")
                }
                when (plan.databaseSettings.driver) {
                    DatabaseDriver.MONGO -> {
                        plan.environment + mapOf(
                            Pair("MONGO_INITDB_ROOT_USERNAME", DockerConfiguration.DEFAULT_DB_USERNAME),
                            Pair("MONGO_INITDB_ROOT_PASSWORD", DockerConfiguration.DEFAULT_DB_PASSWORD)
                        )
                    }
                }
            }
        }

        environment = environment.mapValues {
            var env = it.value
            networkConfigurations.forEach { config ->
                env = env.replace("&${config.key}-host&", config.key)
                env = env.replace("&${config.key}-port&", config.value.exposedPort.toString())
                env = env.replace("&${config.key}-url&", "http://${config.key}:${config.value.exposedPort}")
            }
            env
        }

        val networkConfiguration = networkConfigurations[plan.name] ?: return Outcome.Error(reason = "TODO")

        val containerResult = dockerService.createContainer(
            name = plan.name,
            image = imageResult.data,
            bindedPorts = if (isDockerRuntime) { null } else { Pair(networkConfiguration.exposedPort, networkConfiguration.port!!) },
            environment = environment
        )
        when (containerResult) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = containerResult.reason)
        }

        when (val networkResult = dockerService.connectContainerToNetwork(containerResult.data.id, DockerConfiguration.NETWORK_NAME)) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = networkResult.reason)
        }

        when (val startResult = dockerService.startContainer(containerResult.data.id)) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = startResult.reason)
        }

        if (plan.type == ContainerType.DATABASE) {
            when (val restoreResult = dockerService.restoreMongoDump(containerResult.data.id, plan.databaseSettings!!.dumpName)) {
                is Outcome.Success -> {}
                is Outcome.Error -> return Outcome.Error(reason = restoreResult.reason)
            }
        }

        return Outcome.Success(true)
    }

    fun fetchImage(plan: DeploymentPlan): Outcome<Image> = when (plan.type) {
        ContainerType.APPLICATION -> dockerService!!.findImageByNameAndTagRegex(
            plan.imageName,
            plan.imageTagRegex
        )
        ContainerType.DATABASE -> {
            plan.databaseSettings?.let {
                when (it.driver) {
                    DatabaseDriver.MONGO -> dockerService!!.findImageByNameAndTagRegex(
                        "mongo",
                        "latest"
                    )
                }
            } ?: Outcome.Error(reason = "Database settings cannot be empty in a database deployment plan")
        }
    }

    fun generateNetworkConfigurations(plans: List<DeploymentPlan>): Outcome<Map<String, NetworkConfiguration>> {
        val isDockerRuntime = (dockerService!!.isRuntimeDockerised() as Outcome.Success).data
        return Outcome.Success(
            plans.mapIndexed { index, plan -> Pair(
                plan.name,
                NetworkConfiguration(
                    if (isDockerRuntime) { plan.name } else { "localhost" },

                    if (!isDockerRuntime) {
                        val portsResult = dockerService.findAllPublicPorts()
                        when (portsResult) {
                            is Outcome.Success -> {}
                            is Outcome.Error -> return Outcome.Error(reason = portsResult.reason)
                        }

                        val target = index + DockerConfiguration.PORT_RANGE_LOWER
                        if (target < DockerConfiguration.PORT_RANGE_HIGHER && !portsResult.data.contains(target)) {
                            target
                        } else {
                            return Outcome.Error(reason = "There are no open ports between ${DockerConfiguration.PORT_RANGE_LOWER} and ${DockerConfiguration.PORT_RANGE_HIGHER}")
                        }
                    } else { null },

                    if (plan.port != null) {
                        plan.port
                    } else {
                        val imageResult = fetchImage(plan)
                        when (imageResult) {
                            is Outcome.Success -> {}
                            is Outcome.Error -> return Outcome.Error(reason = imageResult.reason)
                        }

                        val exposedPortsResult = dockerService.findExposedPorts(imageResult.data)
                        when (exposedPortsResult) {
                            is Outcome.Success -> {}
                            is Outcome.Error -> return Outcome.Error(reason = exposedPortsResult.reason)
                        }
                        when (exposedPortsResult.data.size) {
                            1 -> {}
                            else -> return Outcome.Error(reason = "The Docker image does not expose exactly one port")
                        }
                        exposedPortsResult.data.first().port
                    }
                )
            ) }.toMap()
        )
    }
}

class NetworkConfiguration(
    val host: String,
    val port: Int?,
    val exposedPort: Int
)
