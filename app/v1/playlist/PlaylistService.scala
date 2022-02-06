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
  extends CustomExecutionContext(actorSystem, "service.dispatcher")

/**
 * A pure non-blocking interface for the PlaylistRepository.
 */
trait PlaylistService {
  def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId]

  def list()(implicit mc: MarkerContext): Future[Iterable[PlaylistData]]

  def get(id: PlaylistId)(implicit mc: MarkerContext): Future[Option[PlaylistData]]

  def followPlaylist(id: PlaylistId)(implicit mc: MarkerContext): Future[Unit]
}

/**
 * A custom execution context is used here to establish that blocking operations should be
 * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
 * such as rendering.
 */
@Singleton
class PlaylistServiceImpl @Inject()(streamingServiceProxy: StreamingServiceProxy)(implicit ec: PlaylistExecutionContext)
  extends PlaylistService {
  private val logger = Logger(this.getClass)

  override def list()(
    implicit mc: MarkerContext): Future[Iterable[PlaylistData]] = {
    logger.trace(s"list: ")
    val dummyUserId = UserId("1")
    streamingServiceProxy.listPlaylistsOfUser(dummyUserId)
  }

  override def get(id: PlaylistId)(
    implicit mc: MarkerContext): Future[Option[PlaylistData]] = {
    logger.trace(s"get: id = $id")
    val dummyUserId = UserId("1")
    streamingServiceProxy.getPlaylist(id, dummyUserId)
  }

  override def followPlaylist(id: PlaylistId)(implicit mc: MarkerContext): Future[Unit] = {
    Future {
      logger.trace(s"following playlist = $id")
      Future.successful(None)
    }
  }

  def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }
}
