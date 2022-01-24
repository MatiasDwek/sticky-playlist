import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, MarkerContext, inject}
import v1.playlist.{PlaylistData, PlaylistId, PlaylistRepository, PlaylistResource}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlaylistRouterSpec extends PlaySpec with GuiceOneAppPerTest {
  override def fakeApplication(): Application =
    GuiceApplicationBuilder().overrides(inject.bind[PlaylistRepository].to[MockedRepository]).build()

  "PlaylistRouter" should {
    "render the list of playlists" in {
      val request = FakeRequest(GET, "/v1/playlists").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(home)).get
      playlists.filter(_.id == "456").head mustBe PlaylistResource("456", "/v1/playlists/456", "winter playlist", "tunes for " +
        "winter")
    }

    "render the list of playlists when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/playlists/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(home)).get
      playlists.filter(_.id == "456").head mustBe (PlaylistResource("456", "/v1/playlists/456", "winter playlist", "tunes for" +
        " winter"))
    }

    "render a playlist" in {
      val request = FakeRequest(GET, "/v1/playlists/abc").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home: Future[Result] = route(app, request).get

      val playlist: PlaylistResource = Json.fromJson[PlaylistResource](contentAsJson(home)).get
      playlist mustBe PlaylistResource("abc", "/v1/playlists/abc", "title", "description")
    }
  }
}

class MockedRepository extends PlaylistRepository {
  override def create(data: PlaylistData)(implicit mc: MarkerContext): Future[PlaylistId] = Future {
    PlaylistId("abc")
  }
  override def list()(implicit mc: MarkerContext): Future[Iterable[PlaylistData]] = Future {
    Iterable(
      PlaylistData(PlaylistId("123"), "summer playlist", "tunes for summer"),
      PlaylistData(PlaylistId("456"), "winter playlist", "tunes for winter"))
  }
  override def get(id: PlaylistId)(implicit mc: MarkerContext): Future[Option[PlaylistData]] = Future {
    Option(PlaylistData(PlaylistId("abc"), "title", "description"))
  }
}