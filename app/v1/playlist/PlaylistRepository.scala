package v1.playlist

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

final case class PlaylistData(id: PlaylistId, title: String, body: String)


class PlaylistId private(val underlying: String) extends AnyVal {
  override def toString: String = underlying
}

object PlaylistId {
  def apply(raw: String): PlaylistId = {
    require(raw != null)
    new PlaylistId(raw)
  }
}

class PlaylistExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
 * A pure non-blocking interface for the PlaylistRepository.
 */
trait PlaylistRepository {
  def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId]

  def list()(implicit mc: MarkerContext): Future[Iterable[PlaylistData]]

  def get(id: PlaylistId)(implicit mc: MarkerContext): Future[Option[PlaylistData]]
}

/**
 * A trivial implementation for the Playlist Repository.
 *
 * A custom execution context is used here to establish that blocking operations should be
 * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
 * such as rendering.
 */
@Singleton
class PlaylistRepositoryImpl @Inject()(secretFetcher: SecretFetcher)(implicit ec: PlaylistExecutionContext)
  extends PlaylistRepository {

  import com.wrapper.spotify.SpotifyApi

  import scala.jdk.FutureConverters._

  private val logger = Logger(this.getClass)
  
  override def list()(
    implicit mc: MarkerContext): Future[Iterable[PlaylistData]] = {
    logger.trace(s"list: ")
    val authToken = secretFetcher.getAuthorizationToken(UserId("1"))
    authToken flatMap { t =>
      val spotifyApi = new SpotifyApi.Builder().setAccessToken(t.token).build
      val getPlaylistsBuilder = spotifyApi.getListOfCurrentUsersPlaylists
        .limit(10)
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

  override def get(id: PlaylistId)(
    implicit mc: MarkerContext): Future[Option[PlaylistData]] = {

    logger.trace(s"get: id = $id")
    val authToken = secretFetcher.getAuthorizationToken(UserId("1"))
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

  def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }
}
