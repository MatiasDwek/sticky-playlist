package connectors

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

final case class AuthorizationToken(token: String)

final case class UserId(id: String)

class SecretFetcherExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "secret.dispatcher")

trait SecretFetcher {
  def getAuthorizationToken(userId: UserId)(implicit mc: MarkerContext): Future[AuthorizationToken]
}

@Singleton
class SecretFetcherImpl @Inject()()(implicit ec: SecretFetcherExecutionContext)
  extends SecretFetcher {

  private val logger = Logger(this.getClass)

  def getAuthorizationToken(userId: UserId)(implicit mc: MarkerContext): Future[AuthorizationToken] = {
    Future {
      logger.trace(s"getting access token for user ${userId.id}")
      val token = sys.env(s"user_token_${userId.id}")
      AuthorizationToken(token)
    }
  }
}
