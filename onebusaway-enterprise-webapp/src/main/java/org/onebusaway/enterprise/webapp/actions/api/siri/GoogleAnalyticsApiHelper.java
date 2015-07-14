package org.onebusaway.enterprise.webapp.actions.api.siri;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

public class GoogleAnalyticsApiHelper {

  @Autowired
  private ConfigurationService _config;

  public GoogleAnalyticsApiHelper(ConfigurationService configurationService) {
    this._config = configurationService;
  }
  
  public boolean reportToGoogleAnalytics(final String key) {
    if ((key == null) || key.isEmpty())
      return false;
    String[] excludeKeys = StringUtils.split(_config.getConfigurationValueAsString("display.obaApiExcludeGaKey", "OBANYC"), ",");
    for (String excludeKey: excludeKeys) {
      if (StringUtils.equals(excludeKey, key)) 
        return false;
    }
    return true;
  }

}
