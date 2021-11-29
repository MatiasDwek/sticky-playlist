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
class PlaylistRepositoryImpl @Inject()()(implicit ec: PlaylistExecutionContext)
  extends PlaylistRepository {

  private val logger = Logger(this.getClass)

  private val playlistList = List(
    PlaylistData(PlaylistId("abc"), "title 1", "blog playlist 1"),
    PlaylistData(PlaylistId("def"), "title 2", "blog playlist 2"),
    PlaylistData(PlaylistId("ghi"), "title 3", "blog playlist 3"),
    PlaylistData(PlaylistId("fge"), "title 4", "blog playlist 4"),
    PlaylistData(PlaylistId("qws"), "title 5", "blog playlist 5")
  )

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[PlaylistData]] = {
    Future {
      logger.trace(s"list: ")
      playlistList
    }
  }

  override def get(id: PlaylistId)(
    implicit mc: MarkerContext): Future[Option[PlaylistData]] = {
    Future {
      logger.trace(s"get: id = $id")
      playlistList.find(playlist => playlist.id == id)
    }
  }

  def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
