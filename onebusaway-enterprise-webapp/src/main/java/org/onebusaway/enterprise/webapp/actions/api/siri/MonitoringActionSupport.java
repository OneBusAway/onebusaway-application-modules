package org.onebusaway.enterprise.webapp.actions.api.siri;

import javax.servlet.http.HttpServletRequest;

import org.onebusaway.util.services.configuration.ConfigurationService;

import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import com.dmurph.tracking.VisitorData;

public class MonitoringActionSupport {

  protected JGoogleAnalyticsTracker _googleAnalytics = null;

  public MonitoringActionSupport() {
  }

  protected void setupGoogleAnalytics(HttpServletRequest request, ConfigurationService configService) {
    GoogleAnalyticsApiHelper gaApiHelper = new GoogleAnalyticsApiHelper(configService);

    String googleAnalyticsSiteId = 
        configService.getConfigurationValueAsString("display.googleAnalyticsSiteId", null);
    
    try {
      if ((googleAnalyticsSiteId != null) && (gaApiHelper.reportToGoogleAnalytics(request.getParameter("key")))) { 
        VisitorData visitorData =  VisitorData.newVisitor();
        visitorData.newRequest();
        AnalyticsConfigData config = new AnalyticsConfigData(googleAnalyticsSiteId, visitorData);
        _googleAnalytics = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);
      }
    } catch(Exception e) {
      // discard
    }
  }

  protected void reportToGoogleAnalytics(HttpServletRequest request, String event, String gaLabel, ConfigurationService configService) {
    GoogleAnalyticsApiHelper gaApiHelper = new GoogleAnalyticsApiHelper(configService);
    if (_googleAnalytics != null && gaApiHelper.reportToGoogleAnalytics(request.getParameter("key"))) {
      try {
      _googleAnalytics.trackEvent("API", event, gaLabel);
      } catch(Exception e) {
        //discard
      }
    }
  }

}