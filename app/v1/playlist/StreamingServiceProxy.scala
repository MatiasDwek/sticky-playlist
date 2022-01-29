package v1.playlist

import akka.actor.ActorSystem
import com.wrapper.spotify.SpotifyApi
import play.api.Logger
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.jdk.FutureConverters.CompletionStageOps


class StreamingServiceProxyExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "streaming-proxy.dispatcher")

trait StreamingServiceProxy {
  def listPlaylistsOfUser(userId: UserId): Future[Iterable[PlaylistData]]

  def getPlaylist(id: PlaylistId, userId: UserId): Future[Option[PlaylistData]]
}

@Singleton
class SpotifyProxy @Inject()(secretFetcher: SecretFetcher)(implicit ec: StreamingServiceProxyExecutionContext) extends
  StreamingServiceProxy {

  private val logger = Logger(this.getClass)

  override def listPlaylistsOfUser(userId: UserId): Future[Iterable[PlaylistData]] = {
    logger.info(s"listing playlists of user=$userId from Spotify")
    val authToken = secretFetcher.getAuthorizationToken(userId)
    authToken flatMap { t =>
      val spotifyApi = new SpotifyApi.Builder().setAccessToken(t.token).build
      val getPlaylistsBuilder = spotifyApi.getListOfCurrentUsersPlaylists
        .limit(10) // TODO pass this params
        .offset(0)
        .build
      val getPlaylistsRequest = getPlaylistsBuilder.executeAsync.asScala

      getPlaylistsRequest map { playlists =>
        playlists.getItems map { p =>
          PlaylistData(PlaylistId(p.getId), p.getName, p.getUri)
        }
      }
    }
  }

  override def getPlaylist(id: PlaylistId, userId: UserId): Future[Option[PlaylistData]] = {
    logger.info(s"fetching playlist with id = $id for user=$userId from Spotify")
    val authToken = secretFetcher.getAuthorizationToken(userId)
    authToken flatMap { t =>
      val spotifyApi = new SpotifyApi.Builder().setAccessToken(t.token).build
      val getPlaylistsBuilder = spotifyApi.getPlaylist(id.underlying)
        .build
      val getPlaylistsRequest = getPlaylistsBuilder.executeAsync.asScala
      getPlaylistsRequest.map { p =>
        Option(p).map { p =>
          PlaylistData(PlaylistId(p.getId), p.getName, p.getUri)
        }
      }
    }
  }

}
