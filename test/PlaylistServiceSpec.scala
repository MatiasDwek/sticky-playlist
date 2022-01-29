import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.MarkerContext
import v1.playlist._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PlaylistServiceSpec extends PlaySpec with MockitoSugar {

  "PlaylistService" should {
    implicit val actorSystem: ActorSystem = ActorSystem("testActorSystem", ConfigFactory.load())

    "get the list of playlists" in {
      val secretFetcher = mock[SecretFetcher]
      when(secretFetcher.getAuthorizationToken(any[UserId])(any[MarkerContext])).thenReturn(Future {
        AuthorizationToken("aa")
      })

      val playlistService = new PlaylistServiceImpl(secretFetcher)(new PlaylistExecutionContext(actorSystem))
      print(Await.result(playlistService.list(), Duration.Inf))
    }
  }

}
