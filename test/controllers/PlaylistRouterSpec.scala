import connectors.{ApplicationDatabase, StreamingServiceProxy, UserId}
import dataobjects.{PlaylistData, PlaylistId}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, inject}
import v1.playlist.PlaylistResource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlaylistRouterSpec extends PlaySpec with GuiceOneAppPerTest {
  override def fakeApplication(): Application =
    GuiceApplicationBuilder().overrides(inject.bind[StreamingServiceProxy].to[MockedStreamingService], inject.bind[ApplicationDatabase]
      .to[MockedDatabaseImpl]).build()

  "PlaylistRouter" should {
    "render the list of playlists" in {
      val request = FakeRequest(GET, "/api/v1/playlists").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(response)).get
      playlists.filter(_.id == "456").head mustBe PlaylistResource("456", "/api/v1/playlists/456", "winter playlist", "tunes for " +
        "winter")
    }

    "render the list of playlists when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/api/v1/playlists/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlists: Seq[PlaylistResource] = Json.fromJson[Seq[PlaylistResource]](contentAsJson(response)).get
      playlists.filter(_.id == "456").head mustBe (PlaylistResource("456", "/api/v1/playlists/456", "winter playlist", "tunes for" +
        " winter"))
    }

    "render a playlist" in {
      val request = FakeRequest(GET, "/api/v1/playlists/abc").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get

      val playlist: PlaylistResource = Json.fromJson[PlaylistResource](contentAsJson(response)).get
      playlist mustBe PlaylistResource("abc", "/api/v1/playlists/abc", "title", "description")
    }

    // TODO make this test user aware
    "follow a playlist" in {
      val request = FakeRequest(POST, "/api/v1/playlists/followed/abc").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get
      assert(status(response) == 200)
    }

    // TODO make this test user aware
    "return a 404 when following a non existing playlist" in {
      val request = FakeRequest(POST, "/api/v1/playlists/followed/not-a-playlist").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val response: Future[Result] = route(app, request).get
      assert(status(response) == 404)
    }
  }
}

class MockedStreamingService extends StreamingServiceProxy {
  override def listPlaylistsOfUser(userId: UserId): Future[Iterable[PlaylistData]] = Future {
    Iterable(
      PlaylistData(PlaylistId("123"), "summer playlist", "tunes for summer"),
      PlaylistData(PlaylistId("456"), "winter playlist", "tunes for winter"))
  }

  override def getPlaylist(id: PlaylistId, userId: UserId): Future[Option[PlaylistData]] = Future {
    Option(PlaylistData(PlaylistId("abc"), "title", "description"))
  }
}

class MockedDatabaseImpl extends ApplicationDatabase {
  override def addUser(userId: UserId): Future[Unit] = Future.successful(None)

  override def followPlaylist(userId: UserId, playlistId: PlaylistId): Future[Unit] = {
    if (playlistId.underlying == "abc")
      Future.successful(None)
    else
      Future.failed(new RuntimeException("Not found"))
  }
}