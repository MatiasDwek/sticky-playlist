package connectors

import akka.actor.ActorSystem
import dataobjects.PlaylistId
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

trait ApplicationDatabase {
  def followPlaylist(userId: UserId, playlistId: PlaylistId): Future[Unit]

  def addUser(userId: UserId): Future[Unit]
}

class ApplicationDatabaseImpl @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext)
  extends ApplicationDatabase {
  var userFollowedPlaylists: Map[UserId, Vector[PlaylistId]] = Map()

  def updateSomething(): Unit = {
    Future {
      db.withConnection { conn =>
        // do whatever you need with the db connection
      }
    }(databaseExecutionContext)
  }

  override def addUser(userId: UserId): Future[Unit] = {
    userFollowedPlaylists += userId -> Vector()
    Future.successful(None)
  }

  override def followPlaylist(userId: UserId, playlistId: PlaylistId): Future[Unit] = {
    val userPlaylists: Vector[PlaylistId] = userFollowedPlaylists.getOrElse(userId, throw new RuntimeException(s"User $userId not " +
      s"registered"))
    userFollowedPlaylists += userId -> (userPlaylists :+ playlistId)
    Future.successful(None)
  }
}

/**
 * This class is a pointer to an execution context configured to point to "database.dispatcher"
 * in the "application.conf" file.
 */
@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")
