package org.onebusaway.util.impl.analytics;

import javax.annotation.PostConstruct;

import org.onebusaway.util.services.analytics.GoogleAnalyticsService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.brsanthu.googleanalytics.DefaultRequest;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;

@Component
public class GoogleAnalyticsServiceImpl extends GoogleAnalytics implements GoogleAnalyticsService{
	
	public GoogleAnalyticsServiceImpl() {
		super(new GoogleAnalyticsConfig(), new DefaultRequest());
	}
	
	@Autowired
	 private ConfigurationService _configurationService;
	
	@PostConstruct
	private void setup(){
		this.getDefaultRequest().trackingId(getGoogleAnalyticsSiteId());
		/*this.setDefaultRequest(new DefaultRequest().trackingId(getGoogleAnalyticsSiteId));
		this(new GoogleAnalyticsConfig(), new DefaultRequest().trackingId(trackingId));*/
	}
	
	private String getGoogleAnalyticsSiteId() {
		 return _configurationService.getConfigurationValueAsString("display.googleAnalyticsSiteId", "");
	}

}
