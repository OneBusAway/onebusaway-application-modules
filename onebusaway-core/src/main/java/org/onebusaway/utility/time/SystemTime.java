package org.onebusaway.utility.time;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.utility.restful.RestApiLibrary;

/**
 * Single point of time manipulation for OBANYC. Intended for development only
 * -- not for use in production environments. Hence the defaults are OFF!
 */
public class SystemTime {

	public static final String HOST_KEY = "oba.systemTime.host";
	public static final String PORT_KEY = "oba.systemTime.port";
	public static final String API_ADJUSTMENT_KEY = "oba.systemTime.adjustment.apiPath";
	public static final String API_ENABLED_KEY = "oba.systemTime.enabled.apiPath";
	public static final String TIME_ENABLED_KEY = "oba.systemTime.enabled";
	private static final int POLL_INTERVAL = 60; // seconds

	// construct a single instance per JVM
	private static SystemTime INSTANCE = new SystemTime();
	private JsonParser parser = new JsonParser();

	// private ConfigurationService _configurationService = null;
	private RestApiLibrary _apiLibrary = null;

	private String _host = null;
	private Integer _port = null;

	private String _apiAdjustmentPath = null;
	private String _apiEnabledPath = null;

	private URL _apiAdjustmentUrl = null;
	private URL _apiEnabledUrl = null;

	private String _enabled = "false"; // turned off by default
	private long _adjustment = 0; // by default there is no adjustment

	private static Logger _log = LoggerFactory.getLogger(SystemTime.class);

	private SystemTime() {
		UpdateWorker uw = new UpdateWorker();
		new Thread(uw).start();
	}

	public static long currentTimeMillis() {
		return System.currentTimeMillis() + INSTANCE._adjustment;
	}

	public static void setEnabled(String enabledFlag) {
		INSTANCE._enabled = enabledFlag;
	}

	public static void setAdjustment(long adjustmentFromNowInMillis) {
		INSTANCE._adjustment = adjustmentFromNowInMillis;
	}

	public static long getAdjustment() {
		return INSTANCE._adjustment;
	}

	private long refreshAdjustment() throws Exception {

		refreshConfigValues();

		if (!isEnabled()) {
			return 0;
		}

		return getAdjustment();
	}

	private void refreshConfigValues() throws Exception {

		if (_apiLibrary == null) {
			_apiLibrary = new RestApiLibrary(getHost(), getPort(), "/");
			_apiAdjustmentUrl = _apiLibrary.buildUrl(getAdjustmentApiPath());
			_apiEnabledUrl = _apiLibrary.buildUrl(getEnabledApiPath());
		}

		String apiEnabledResult = _apiLibrary
				.getContentsOfUrlAsString(_apiEnabledUrl);
		JsonObject apiEnabledResultAsJson = parser.parse(apiEnabledResult)
				.getAsJsonObject();
		if (apiEnabledResultAsJson != null) {
			setEnabled(apiEnabledResultAsJson.get("value").getAsString());
		}

		String apiAdjustmentResult = _apiLibrary
				.getContentsOfUrlAsString(_apiAdjustmentUrl);
		JsonObject apiAdjustmentResultAsJson = parser
				.parse(apiAdjustmentResult).getAsJsonObject();
		setAdjustment(getStringAsLong(apiAdjustmentResultAsJson.get("value")
				.getAsString()));
	}

	private String getHost() {
		String host = null;

		// option 1: read tdm.host from system env
		host = getStringFromSystemProperty(HOST_KEY);
		if (StringUtils.isNotBlank(host))
			return host;

		// option 2: spring injection
		if (StringUtils.isNotBlank(_host)) {
			return _host;
		}

		// option 3: let if fail -- the default is not to configure this
		return null;
	}

	private Integer getPort() {
		Integer port = null;

		// option 1: read tdm.port from system env
		port = getIntegerFromSystemProperty(PORT_KEY);
		if (port != null && port > 0)
			return port;

		// option 2: spring injection
		if (_port != null && port > 0) {
			return _port;
		}

		// option 3: let if fail -- the default is not to configure this
		return null;
	}

	private String getAdjustmentApiPath() {
		String api_adjustment = null;

		// option 1: read tdm.host from system env
		api_adjustment = getStringFromSystemProperty(API_ADJUSTMENT_KEY);
		if (StringUtils.isNotBlank(api_adjustment))
			return api_adjustment;

		// option 2: spring injection
		if (StringUtils.isNotBlank(_apiAdjustmentPath)) {
			return _apiAdjustmentPath;
		}

		// option 3: let if fail -- the default is not to configure this
		return null;
	}

	private String getEnabledApiPath() {
		String api_enabled = null;

		// option 1: read tdm.host from system env
		api_enabled = getStringFromSystemProperty(API_ENABLED_KEY);
		if (StringUtils.isNotBlank(api_enabled))
			return api_enabled;

		// option 2: spring injection
		if (StringUtils.isNotBlank(_apiEnabledPath)) {
			return _apiEnabledPath;
		}

		// option 3: let if fail -- the default is not to configure this
		return null;
	}

	/**
	 * determine if time skew adjustment is enabled. In production environments
	 * IT SHOULD NOT BE. FOR DEVELOPMENT ONLY!
	 * 
	 * @return
	 */
	public static boolean isEnabled() {
		String systemProperty = System.getProperty(TIME_ENABLED_KEY);
		if (systemProperty != null) {
			return "true".equals(systemProperty);
		}

		// if endpoint was injected, assume we are turned on
		return "true".equalsIgnoreCase(INSTANCE._enabled);
	}

	private String getStringFromSystemProperty(String key) {
		return System.getProperty(key);
	}

	private Integer getIntegerFromSystemProperty(String key) {
		try {
			Integer port = Integer.valueOf(System.getProperty(key));
			return port;
		} catch (Exception e) {
			return null;
		}
	}

	private Long getStringAsLong(String value) {
		try {
			Long StringAsLong = Long.parseLong(value);
			return StringAsLong;
		} catch (NumberFormatException nfe) {
			_log.error("failed to convert config value into Long", nfe);
			return 0L;
		}
	}

	public class UpdateWorker implements Runnable {

		@Override
		public void run() {

			try {
				refreshConfigValues();
			} catch (Exception e) {
				_log.error("Unable to retrieve TDM configuration values", e);
			}

			if (INSTANCE.isEnabled()) {
				_log.warn("SystemTime Adjustment enabled.  Polling configuration service every "
						+ POLL_INTERVAL + " seconds");
			} else {
				_log.info("SystemTime Adjustment disabled.  Exiting.");
				return;
			}

			while (!Thread.interrupted()) {
				try {
					int rc = sleep(POLL_INTERVAL);
					if (rc < 0) {
						_log.info("caught SIGHUP, exiting");
						return;
					}
					_log.debug("check for instance=" + INSTANCE.toString());
					long adj = refreshAdjustment();
					if (adj != _adjustment) {
						_log.info("updated adjustment to " + adj);
						_adjustment = adj;
					}
				} catch (Exception e) {
					_log.error("refresh failed:" + e);
				}
			}
		}

		private int sleep(int i) {
			try {
				Thread.sleep(i * 1000);
			} catch (InterruptedException e) {
				return -1;
			}
			return 0;
		}

	}

}
