package hclu.hreg.dao.sql

import java.net.URI

import com.typesafe.config.ConfigValueFactory._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import hclu.hreg.dao.DatabaseConfig
import hclu.hreg.dao.DatabaseConfig._
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.{ClassLoaderResourceAccessor, CompositeResourceAccessor, FileSystemResourceAccessor, ResourceAccessor}
import org.joda.time.{DateTime, DateTimeZone}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._

case class SqlDatabase(
    db: slick.jdbc.JdbcBackend.Database,
    driver: JdbcProfile,
    connectionString: JdbcConnectionString
) {

  import driver.api._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  def close() {
    db.close()
  }
}

case class JdbcConnectionString(url: String, username: String = "", password: String = "")

object SqlDatabase extends LazyLogging {

  def embeddedConnectionStringFromConfig(config: DatabaseConfig): String = {
    val url = config.dbH2Url
    val fullPath = url.split(":")(3)
    logger.info(s"Using an embedded database, with data files located at: $fullPath")
    url
  }

  def create(config: DatabaseConfig): SqlDatabase = {
    val envDatabaseUrl = System.getenv("DATABASE_URL")

    if (config.dbPostgresServerName.length > 0)
      createPostgresFromConfig(config)
    else if (envDatabaseUrl != null)
      createPostgresFromEnv(envDatabaseUrl)
    else
      createEmbedded(config)
  }

  def createPostgresFromEnv(envDatabaseUrl: String) = {
    /*
      The DATABASE_URL is set by Heroku (if deploying there) and must be converted to a proper object
      of type Config (for Slick). Expected format:
      postgres://<username>:<password>@<host>:<port>/<dbname>
    */
    val dbUri = new URI(envDatabaseUrl)
    val username = dbUri.getUserInfo.split(":")(0)
    val password = dbUri.getUserInfo.split(":")(1)
    val intermediaryConfig = new DatabaseConfig {
      override def rootConfig: Config = ConfigFactory.empty()
        .withValue(PostgresDSClass, fromAnyRef("org.postgresql.ds.PGSimpleDataSource"))
        .withValue(PostgresServerNameKey, fromAnyRef(dbUri.getHost))
        .withValue(PostgresPortKey, fromAnyRef(dbUri.getPort))
        .withValue(PostgresDbNameKey, fromAnyRef(dbUri.getPath.tail))
        .withValue(PostgresUsernameKey, fromAnyRef(username))
        .withValue(PostgresPasswordKey, fromAnyRef(password))
    }
    createPostgresFromConfig(intermediaryConfig)
  }

  def postgresUrl(host: String, port: String, dbName: String) =
    s"jdbc:postgresql://$host:$port/$dbName"

  def postgresConnectionString(config: DatabaseConfig) = {
    val host = config.dbPostgresServerName
    val port = config.dbPostgresPort
    val dbName = config.dbPostgresDbName
    val username = config.dbPostgresUsername
    val password = config.dbPostgresPassword
    JdbcConnectionString(postgresUrl(host, port, dbName), username, password)
  }

  def createPostgresFromConfig(config: DatabaseConfig) = {
    val db = Database.forConfig("hreg.db.postgres", config.rootConfig)
    SqlDatabase(db, slick.driver.PostgresDriver, postgresConnectionString(config))
  }

  private def createEmbedded(config: DatabaseConfig): SqlDatabase = {
    val db = Database.forConfig("hreg.db.h2")
    SqlDatabase(db, slick.driver.H2Driver, JdbcConnectionString(embeddedConnectionStringFromConfig(config)))
  }

  def createEmbedded(connectionString: String): SqlDatabase = {
    val db = Database.forURL(connectionString)
    SqlDatabase(db, slick.driver.H2Driver, JdbcConnectionString(connectionString))
  }

  def updateSchema(connectionString: JdbcConnectionString) {

    val threadClFO: ResourceAccessor = new ClassLoaderResourceAccessor(Thread.currentThread.getContextClassLoader)
    val clFO: ResourceAccessor = new ClassLoaderResourceAccessor
    val fsFO: ResourceAccessor = new FileSystemResourceAccessor

    val resourceAccessor = new CompositeResourceAccessor(fsFO, clFO, threadClFO)

    val database: liquibase.database.Database = DatabaseFactory.getInstance().openDatabase(connectionString.url, connectionString.username, connectionString.password, null, resourceAccessor);

    //    val database: liquibase.database.Database = DatabaseFactory.getInstance.findCorrectDatabaseImplementation(new JdbcConnection(db.createSession().conn))

    database.setDefaultSchemaName("public")

    val changeLogFile = "liquibase/changelog-master.xml"

    val liqui: Liquibase = new Liquibase(changeLogFile, new CompositeResourceAccessor(clFO, fsFO, threadClFO), database)
    liqui.update("production")

    //    val flyway = new Flyway()
    //    flyway.setDataSource(connectionString.url, connectionString.username, connectionString.password)
    //    flyway.migrate()
  }
}
