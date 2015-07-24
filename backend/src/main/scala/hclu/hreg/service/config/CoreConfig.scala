package hclu.hreg.service.config

import hclu.hreg.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait CoreConfig extends ConfigWithDefault {
  def rootConfig: Config

  private lazy val topLevelConfig = rootConfig.getConfig("hreg")

  lazy val resetLinkPattern = getString("hreg.reset-link-pattern", "http://localhost:8080/#/password-reset?code=%s")
}
