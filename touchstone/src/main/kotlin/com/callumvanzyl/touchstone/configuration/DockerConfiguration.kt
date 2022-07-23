package com.callumvanzyl.touchstone.configuration

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(value = ["docker.enabled"], havingValue = "true")
class DockerConfiguration(
    @Value("\${docker.daemon.url}") private val dockerDaemonUrl: String,
    @Value("\${docker.daemon.cert-path}") private val dockerDaemonCertPath: String,
    @Value("\${docker.registry.username}") private val dockerRegistryUsername: String,
    @Value("\${docker.registry.password}") private val dockerRegistryPassword: String,
    @Value("\${docker.registry.url}") private val dockerRegistryUrl: String
) {

    fun dockerClientConfig(): DefaultDockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost(dockerDaemonUrl)
        .withDockerTlsVerify(dockerDaemonCertPath.isNotBlank())
        .withDockerCertPath(dockerDaemonCertPath)
        .withRegistryUsername(dockerRegistryUsername)
        .withRegistryPassword(dockerRegistryPassword)
        .withRegistryUrl(dockerRegistryUrl)
        .build()

    @Bean
    fun dockerClient(): DockerClient = DockerClientBuilder.getInstance(dockerClientConfig()).build()

    companion object {
        const val NETWORK_NAME = "touchstone-network"
        const val TEST_CONTAINER_LABEL = "touchstone-test-containers--DO-NOT-TOUCH"

        const val DEFAULT_DB_USERNAME = "touchstone"
        const val DEFAULT_DB_PASSWORD = "touchstone"

        const val DATA_MOUNT_PATH = "/touchstone-data"
        const val DATA_MOUNT_VOLUME = "tch-app-data"

        const val PORT_RANGE_LOWER = 3400
        const val PORT_RANGE_HIGHER = 3420
    }
}
