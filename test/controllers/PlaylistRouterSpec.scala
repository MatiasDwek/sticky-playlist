import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import v1.playlist.PlaylistResource

import scala.concurrent.Future

class PlaylistRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "PlaylistRouter" should {

    "render the list of playlists" in {
      val request = FakeRequest(GET, "/v1/playlists").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(home)).get
      playlists.filter(_.id == "abc").head mustBe PlaylistResource("abc", "/v1/playlists/abc", "title 1", "blog playlist 1")
    }

    "render the list of playlists when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/playlists/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(home)).get
      playlists.filter(_.id == "abc").head mustBe (PlaylistResource("abc", "/v1/playlists/abc", "title 1", "blog playlist 1"))
    }
  }

}