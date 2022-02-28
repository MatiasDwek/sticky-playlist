package v1.playlist

import net.logstash.logback.marker.LogstashMarker
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.{Logger, MarkerContext}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * A wrapped request for playlist resources.
 *
 * This is commonly used to hold request-specific information like
 * security credentials, and useful shortcut methods.
 */
trait PlaylistRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider

class PlaylistRequest[A](request: Request[A], val messagesApi: MessagesApi)
  extends WrappedRequest(request)
    with PlaylistRequestHeader

/**
 * Provides an implicit marker that will show the request in all logger statements.
 */
trait RequestMarkerContext {

  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(
                                             implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress)
    }
  }

}

/**
 * The action builder for the playlist.Playlist resource.
 *
 * This is the place to put logging, metrics, to augment
 * the request with contextual data, and manipulate the result.
 */
class PlaylistActionBuilder @Inject()(messagesApi: MessagesApi,
                                      playBodyParsers: PlayBodyParsers)(
                                       implicit val executionContext: ExecutionContext)
  extends ActionBuilder[PlaylistRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  type PlaylistRequestBlock[A] = PlaylistRequest[A] => Future[Result]
  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent
  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A],
                              block: PlaylistRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext =
      requestHeaderToMarkerContext(request)
    logger.trace(s"invokeBlock: ")

    val future = block(new PlaylistRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case _ =>
          result
      }
    }
  }
}

/**
 * Packages up the component dependencies for the playlist controller.
 *
 * This is a good way to minimize the surface area exposed to the controller, so the
 * controller only has to have one thing injected.
 */
case class PlaylistControllerComponents @Inject()(
                                                   playlistActionBuilder: PlaylistActionBuilder,
                                                   playlistResourceHandler: PlaylistResourceHandler,
                                                   actionBuilder: DefaultActionBuilder,
                                                   parsers: PlayBodyParsers,
                                                   messagesApi: MessagesApi,
                                                   langs: Langs,
                                                   fileMimeTypes: FileMimeTypes,
                                                   executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents

/**
 * Exposes actions and handler to the PlaylistController by wiring the injected state into the base class.
 */
class PlaylistBaseController @Inject()(pcc: PlaylistControllerComponents)
  extends BaseController
    with RequestMarkerContext {
  def PlaylistAction: PlaylistActionBuilder = pcc.playlistActionBuilder

  def playlistResourceHandler: PlaylistResourceHandler = pcc.playlistResourceHandler

  override protected def controllerComponents: ControllerComponents = pcc
}
