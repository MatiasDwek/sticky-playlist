package v1.playlist

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

/**
 * Routes and URLs to the PlaylistResource controller.
 */
class PlaylistRouter @Inject()(controller: PlaylistController) extends SimpleRouter {
  val prefix = "/v1/playlists"

  def link(id: PlaylistId): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)
  }

}
