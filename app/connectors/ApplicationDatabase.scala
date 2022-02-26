package connectors

import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext
import v1.playlist.{PlaylistId, UserId}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

class ApplicationDatabase @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) {
  var userFollowedPlaylists: Map[UserId, Vector[PlaylistId]] = Map()

  def updateSomething(): Unit = {
    Future {
      db.withConnection { conn =>
        // do whatever you need with the db connection
      }
    }(databaseExecutionContext)
  }

  def addUser(userId: UserId): Unit = {
    userFollowedPlaylists += userId -> Vector()
  }

  def followPlaylist(userId: UserId, playlistId: PlaylistId): Unit = {
    val userPlaylists: Vector[PlaylistId] = userFollowedPlaylists.getOrElse(userId, throw new RuntimeException(s"User $userId not " +
      s"registered"))
    userFollowedPlaylists += userId -> (userPlaylists :+ playlistId)
  }
}

/**
 * This class is a pointer to an execution context configured to point to "database.dispatcher"
 * in the "application.conf" file.
 */
@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")
