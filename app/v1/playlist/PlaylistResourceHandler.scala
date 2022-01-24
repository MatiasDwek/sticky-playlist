package v1.playlist

import play.api.MarkerContext
import play.api.libs.json._

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
                                        playlistRepository: PlaylistService)
                                       (implicit ec: ExecutionContext) {

  def create(playlistInput: PlaylistFormInput)(
    implicit mc: MarkerContext): Future[PlaylistResource] = {
    val data = PlaylistData(PlaylistId("999"), playlistInput.title, playlistInput.body)
    // We don't actually create the playlist, so return what we have
    playlistRepository.create(data).map { _ =>
      createPlaylistResource(data)
    }
  }
  private def createPlaylistResource(p: PlaylistData): PlaylistResource = {
    PlaylistResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }
  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[PlaylistResource]] = {
    val playlistFuture = playlistRepository.get(PlaylistId(id))
    playlistFuture.map { maybePlaylistData =>
      maybePlaylistData.map { playlistData =>
        createPlaylistResource(playlistData)
      }
    }
  }
  def find(implicit mc: MarkerContext): Future[Iterable[PlaylistResource]] = {
    playlistRepository.list().map { playlistDataList =>
      playlistDataList.map(playlistData => createPlaylistResource(playlistData))
    }
  }

}
