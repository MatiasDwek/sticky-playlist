import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, MarkerContext, inject}
import v1.playlist.{PlaylistData, PlaylistId, PlaylistResource, PlaylistService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlaylistRouterSpec extends PlaySpec with GuiceOneAppPerTest {
  override def fakeApplication(): Application =
    GuiceApplicationBuilder().overrides(inject.bind[PlaylistService].to[MockedService]).build()

  "PlaylistRouter" should {
    "render the list of playlists" in {
      val request = FakeRequest(GET, "/v1/playlists").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(response)).get
      playlists.filter(_.id == "456").head mustBe PlaylistResource("456", "/v1/playlists/456", "winter playlist", "tunes for " +
        "winter")
    }

    "render the list of playlists when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/playlists/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(response)).get
      playlists.filter(_.id == "456").head mustBe (PlaylistResource("456", "/v1/playlists/456", "winter playlist", "tunes for" +
        " winter"))
    }

    "render a playlist" in {
      val request = FakeRequest(GET, "/v1/playlists/abc").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlist: PlaylistResource = Json.fromJson[PlaylistResource](contentAsJson(response)).get
      playlist mustBe PlaylistResource("abc", "/v1/playlists/abc", "title", "description")
    }

    "follow a playlist" in {
      val request = FakeRequest(POST, "/v1/playlists/followed/abc").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get
      assert(status(response) == 200)
    }

    "return a 404 when following a non existing playlist" in {
      val request = FakeRequest(POST, "/v1/playlists/followed/not-a-playlist").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get
      assert(status(response) == 404)
    }
  }
}

class MockedService extends PlaylistService {
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
  override def followPlaylist(id: PlaylistId)(implicit mc: MarkerContext): Future[Unit] = {
    if (id.underlying == "abc") Future.successful((): Unit)
    else Future.failed(new RuntimeException("Not found"))
  }
}