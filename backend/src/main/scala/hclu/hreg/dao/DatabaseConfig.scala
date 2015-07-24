package hclu.hreg.dao

import hclu.hreg.common.config.ConfigWithDefault
import com.typesafe.config.Config
import DatabaseConfig._

trait DatabaseConfig extends ConfigWithDefault {
  def rootConfig: Config

  // format: OFF
  lazy val dbH2Url              = getString(s"hreg.db.h2.properties.url", "jdbc:h2:file:./data/hreg")
  lazy val dbPostgresServerName = getString(PostgresServerNameKey, "")
  lazy val dbPostgresPort       = getString(PostgresPortKey, "5432")
  lazy val dbPostgresDbName     = getString(PostgresDbNameKey, "")
  lazy val dbPostgresUsername   = getString(PostgresUsernameKey, "")
  lazy val dbPostgresPassword   = getString(PostgresPasswordKey, "")
}

object DatabaseConfig {
  val PostgresDSClass       = "hreg.db.postgres.dataSourceClass"
  val PostgresServerNameKey = "hreg.db.postgres.properties.serverName"
  val PostgresPortKey       = "hreg.db.postgres.properties.portNumber"
  val PostgresDbNameKey     = "hreg.db.postgres.properties.databaseName"
  val PostgresUsernameKey   = "hreg.db.postgres.properties.user"
  val PostgresPasswordKey   = "hreg.db.postgres.properties.password"
  // format: ON
}