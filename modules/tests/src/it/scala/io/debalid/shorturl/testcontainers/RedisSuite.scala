package io.debalid.shorturl.testcontainers

import java.nio.file.Paths

import com.dimafeng.testcontainers.DockerComposeContainer.ComposeFile
import com.dimafeng.testcontainers.munit.TestContainersSuite
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import munit.CatsEffectSuite

trait RedisSuite { self: CatsEffectSuite with TestContainersSuite =>

  // Redis is not directly supported in TC yet, therefore Compose
  override type Containers = DockerComposeContainer

  private val RedisService = "redis_1"
  private val RedisPort    = 6379

  override def startContainers(): DockerComposeContainer = {
    val composeFile = ComposeFile(Left(Paths.get(getClass.getResource("/redis/docker-compose.yml").toURI).toFile))
    DockerComposeContainer
      .Def(
        composeFiles = composeFile,
        exposedServices = ExposedService(name = RedisService, port = RedisPort) :: Nil
      )
      .start()
  }

  def redisAddress: String = withContainers(cnt => s"redis://localhost:${cnt.getServicePort(RedisService, RedisPort)}")
}
