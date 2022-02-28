package v1.playlist

import connectors.{ApplicationDatabase, StreamingServiceProxy, UserId}
import dataobjects.{PlaylistData, PlaylistId}
import play.api.libs.json._
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
 * DTO for displaying playlist information.
 */
case class PlaylistResource(id: String, link: String, title: String, body: String)

object PlaylistResource {
  /**
   * Mapping to read/write a PlaylistResource out as a JSON value.
   */
  implicit val format: Format[PlaylistResource] = Json.format
}


/**
 * Controls access to the backend data, returning [[PlaylistResource]]
 */
class PlaylistResourceHandler @Inject()(routerProvider: Provider[PlaylistRouter],
                                        streamingServiceProxy: StreamingServiceProxy,
                                        applicationDatabase: ApplicationDatabase)
                                       (implicit ec: ExecutionContext) {
  val dummyUserId: UserId = UserId("1")
  private val logger = Logger(this.getClass)

  def create(playlistInput: PlaylistFormInput)(
    implicit mc: MarkerContext): Future[PlaylistResource] = {
    val data = PlaylistData(PlaylistId("999"), playlistInput.title, playlistInput.body)
    // We don't actually create the playlist, so return what we have
    Future {
      logger.trace(s"create: data = $data")
      createPlaylistResource(data)
    }
  }

  def getPlaylist(id: String)(
    implicit mc: MarkerContext): Future[Option[PlaylistResource]] = {
    logger.trace(s"get: id = $id")
    streamingServiceProxy.getPlaylist(PlaylistId(id), dummyUserId).map { maybePlaylistData =>
      maybePlaylistData.map { playlistData =>
        createPlaylistResource(playlistData)
      }
    }
  }
  private def createPlaylistResource(p: PlaylistData): PlaylistResource = {
    PlaylistResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }
  def followPlaylist(id: String)(implicit mc: MarkerContext): Future[Unit] = {
    logger.trace(s"following playlist = $id")
    val dummyUserId = UserId("1")
    applicationDatabase.followPlaylist(dummyUserId, PlaylistId(id))
  }
  def listUserPlaylists(implicit mc: MarkerContext): Future[Iterable[PlaylistResource]] = {
    logger.trace(s"list: ")
    streamingServiceProxy.listPlaylistsOfUser(dummyUserId).map { playlistDataList =>
      playlistDataList.map(playlistData => createPlaylistResource(playlistData))
    }
  }
}
