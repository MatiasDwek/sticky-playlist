import com.google.inject.AbstractModule
import connectors._
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}

import javax.inject._

/**
 * Sets up custom components for Play.
 *
 * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
 */
class Module(environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure() = {
    bind[SecretFetcher].to[SecretFetcherImpl].in[Singleton]()
    bind[StreamingServiceProxy].to[SpotifyProxy].in[Singleton]()
    bind[ApplicationDatabase].to[ApplicationDatabaseImpl].in[Singleton]()
  }
}
