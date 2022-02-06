import akka.actor.ActorSystem
import play.api.db.Database
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

class ApplicationDatabase @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) {
  def updateSomething(): Unit = {
    Future {
      db.withConnection { conn =>
        // do whatever you need with the db connection
      }
    }(databaseExecutionContext)
  }
}

/**
 * This class is a pointer to an execution context configured to point to "database.dispatcher"
 * in the "application.conf" file.
 */
@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")
