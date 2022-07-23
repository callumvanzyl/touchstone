package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.configuration.DockerConfiguration
import com.callumvanzyl.touchstone.util.Outcome
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Ports
import com.github.dockerjava.api.model.Volume
import com.github.dockerjava.core.command.ExecStartResultCallback
import com.github.dockerjava.core.command.LogContainerResultCallback
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern
import org.apache.http.conn.HttpHostConnectException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

interface DockerService {
    fun createContainer(name: String, image: Image, bindedPorts: Pair<Int, Int>? = null, environment: Map<String, String>?): Outcome<CreateContainerResponse>
    fun startContainer(id: String): Outcome<Boolean>
    fun destroyContainer(id: String): Outcome<Boolean>
    fun destroyDanglingContainers(): Outcome<Boolean>
    fun connectContainerToNetwork(containerId: String, networkName: String): Outcome<Boolean>
    fun restoreMongoDump(containerId: String, dumpName: String): Outcome<Boolean>
    fun findDanglingContainers(): Outcome<List<Container>>
    fun findImageById(id: String): Outcome<Image>
    fun findImageByNameAndTagRegex(name: String, tagRegex: String): Outcome<Image>
    fun findExposedPorts(image: Image): Outcome<List<ExposedPort>>
    fun findAllPublicPorts(): Outcome<List<Int>>
    fun findContainersOnPublicPorts(lower: Int, higher: Int): Outcome<List<Container>>
    fun getLogs(containerId: String): Outcome<List<String>>
    fun isRuntimeDockerised(): Outcome<Boolean>
}

@Service("DockerService")
@ConditionalOnProperty(value = ["docker.enabled"], havingValue = "true")
class DockerServiceImpl(
    @Value("\${touchstone.data-path}") private val touchstoneDataPath: String,

    private val dockerClient: DockerClient
) : DockerService {

    override fun createContainer(name: String, image: Image, bindedPorts: Pair<Int, Int>?, environment: Map<String, String>?): Outcome<CreateContainerResponse> =
        try {
            val cmd = dockerClient
                .createContainerCmd(image.id)
                .withName(name)
                .withLabels(mapOf(Pair("com.docker.compose.project", DockerConfiguration.TEST_CONTAINER_LABEL)))

            if (bindedPorts != null) {
                val tcp = ExposedPort.tcp(bindedPorts.first)
                val ports = Ports().also { it.bind(tcp, Ports.Binding.bindPort(bindedPorts.second)) }
                cmd
                    .withExposedPorts(tcp)
                    .withHostConfig(
                        HostConfig()
                            .withPortBindings(ports)
                            .withBinds(Bind(DockerConfiguration.DATA_MOUNT_VOLUME, Volume(DockerConfiguration.DATA_MOUNT_PATH)))
                    )
            } else {
                cmd
                    .withHostConfig(
                        HostConfig()
                            .withBinds(Bind(DockerConfiguration.DATA_MOUNT_VOLUME, Volume(DockerConfiguration.DATA_MOUNT_PATH)))
                    )
            }

            if (environment != null) {
                cmd
                    .withEnv(environment.map { "${it.key}=${it.value}" })
            }

            Outcome.Success(cmd.exec())
        } catch (err: HttpHostConnectException) {
            Outcome.Error("Could not connect to the Docker daemon")
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun startContainer(id: String): Outcome<Boolean> =
        try {
            dockerClient
                .startContainerCmd(id)
                .exec()
            Outcome.Success(true)
        } catch (err: NotFoundException) {
            Outcome.Error("Container $id could not be found")
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun destroyContainer(id: String): Outcome<Boolean> =
        try {
            dockerClient
                .removeContainerCmd(id)
                .withForce(true)
                .withRemoveVolumes(true)
                .exec()
            Outcome.Success(true)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun destroyDanglingContainers(): Outcome<Boolean> =
        try {
            dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(mapOf(Pair("com.docker.compose.project", DockerConfiguration.TEST_CONTAINER_LABEL)))
                .exec()
                .forEach { destroyContainer(it.id) }
            Outcome.Success(true)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun connectContainerToNetwork(containerId: String, networkName: String): Outcome<Boolean> =
        try {
            dockerClient
                .connectToNetworkCmd()
                .withContainerId(containerId)
                .withNetworkId(networkName)
                .exec()
            Outcome.Success(true)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun restoreMongoDump(containerId: String, dumpName: String): Outcome<Boolean> {
        if (!dumpName.endsWith(".bson.gz")) { return Outcome.Error("Dump $dumpName is not a valid dump") }
        val dumps = File("$touchstoneDataPath/dumps/")
        if (!dumps.listFiles().any { it.name == dumpName }) { return Outcome.Error("Dump $dumpName does not exist") }
        return try {
            val cmd = dockerClient
                .execCreateCmd(containerId)
                .withCmd(
                    "sh", "-c",
                    "mongorestore -u ${DockerConfiguration.DEFAULT_DB_USERNAME} -p ${DockerConfiguration.DEFAULT_DB_PASSWORD} --archive=${DockerConfiguration.DATA_MOUNT_PATH}/dumps/$dumpName --gzip"
                )
                .exec()
                .id
            dockerClient.execStartCmd(cmd).exec(ExecStartResultCallback()).awaitCompletion()
            Outcome.Success(true)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }
    }

    override fun findDanglingContainers(): Outcome<List<Container>> =
        try {
            val containers = dockerClient
                .listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(mapOf(Pair("com.docker.compose.project", DockerConfiguration.TEST_CONTAINER_LABEL)))
                .exec()
            Outcome.Success(containers)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun findImageById(id: String): Outcome<Image> =
        dockerClient
            .listImagesCmd()
            .exec()
            .firstOrNull { it.id.contains(id) }
            ?.let { Outcome.Success(it) }
            ?: Outcome.Error("Image $id could not be found")

    override fun findImageByNameAndTagRegex(name: String, tagRegex: String): Outcome<Image> {
        lateinit var pattern: Pattern
        try { pattern = Pattern.compile(tagRegex) } catch (_: Exception) { return Outcome.Error<Nothing>("Invalid regex provided") }
        dockerClient
            .listImagesCmd()
            .withImageNameFilter(name)
            .exec()
            .filter { image -> image.repoTags.any { tag -> (tag.split(":")[0] == name && pattern.matcher(tag.split(":")[1]).matches()) } }
            .maxByOrNull { it.created }
            ?.let { return Outcome.Success(it) }
            ?: return Outcome.Error("No images meet these criteria")
    }

    override fun findExposedPorts(image: Image): Outcome<List<ExposedPort>> =
        try {
            Outcome.Success(
                dockerClient
                        .inspectImageCmd(image.id)
                        .exec()
                        .config
                        ?.exposedPorts?.toList() ?: emptyList()
            )
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun findAllPublicPorts(): Outcome<List<Int>> =
        try {
            Outcome.Success(
                dockerClient.listContainersCmd().withShowAll(true).exec().map { container ->
                    val ports = mutableListOf<Int>()
                    container.ports.forEach {
                        val port = it.publicPort
                        if (port != null) {
                            ports.add(port)
                        }
                    }
                    ports
                }.flatten().toSet().toList()
            )
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun findContainersOnPublicPorts(lower: Int, higher: Int): Outcome<List<Container>> =
        try {
            val containers = mutableListOf<Container>()
            dockerClient.listContainersCmd().withShowAll(true).exec().forEach { container ->
                container.ports.forEach {
                    val port = it.publicPort
                    if (port != null && port > lower && port < higher) {
                        containers.add(container)
                    }
                }
            }
            Outcome.Success(containers)
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun getLogs(containerId: String): Outcome<List<String>> =
        try {
            val logs = mutableListOf<String>()
            val cmd = dockerClient.logContainerCmd(containerId).withStdErr(true).withStdOut(true).withTimestamps(true)
            cmd.exec(object : LogContainerResultCallback() {
                override fun onNext(item: Frame) {
                    logs.add(item.toString())
                }
            }).awaitCompletion()
            Outcome.Success(logs.toList())
        } catch (err: Exception) {
            Outcome.Error(err.localizedMessage)
        }

    override fun isRuntimeDockerised(): Outcome<Boolean> =
        try {
            Files
                .lines(Paths.get("/proc/1/cgroup"))
                .use { stream -> Outcome.Success(stream.anyMatch { line -> line.contains("/docker") }) }
        } catch (err: IOException) {
            Outcome.Success(false)
        }
}
