package v1.playlist

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class PlaylistFormInput(title: String, body: String)

/**
 * Takes HTTP requests and produces JSON.
 */
class PlaylistController @Inject()(cc: PlaylistControllerComponents)(
  implicit ec: ExecutionContext)
  extends PlaylistBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PlaylistFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(PlaylistFormInput.apply)(PlaylistFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = PlaylistAction.async { implicit request =>
    logger.trace("index: ")
    playlistResourceHandler.listUserPlaylists.map { playlists =>
      Ok(Json.toJson(playlists))
    }
  }

  def process: Action[AnyContent] = PlaylistAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPlaylist()
  }

  private def processJsonPlaylist[A]()(implicit request: PlaylistRequest[A]): Future[Result] = {
    def failure(badForm: Form[PlaylistFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PlaylistFormInput) = {
      playlistResourceHandler.create(input).map { playlist =>
        Created(Json.toJson(playlist)).withHeaders(LOCATION -> playlist.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  def show(id: String): Action[AnyContent] = PlaylistAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      playlistResourceHandler.getPlaylist(id).map { playlist =>
        Ok(Json.toJson(playlist))
      }
  }

  def follow(id: String): Action[AnyContent] = PlaylistAction.async {
    implicit request =>
      logger.trace(s"follow: id = $id")
      playlistResourceHandler.followPlaylist(id).transform {
        case Success(_) => Try(Ok)
        case Failure(_) => Try(NotFound)
      }
  }
}
