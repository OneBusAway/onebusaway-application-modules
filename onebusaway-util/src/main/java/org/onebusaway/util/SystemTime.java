/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single point of time manipulation for OBA.  Intended for development
 * only -- not for use in production environments.  Hence the defaults
 * are OFF!
 */
public class SystemTime {

  public static final java.lang.String TIME_ADJUSTMENT_KEY = "org.onebusaway.util.SystemTime.enabled";

  private static final String TDS_PATH_KEY = "tds.path";
  private static final String API_CALL = "/adjustment.do";

  private static final int POLL_INTERVAL = 10; // seconds
  private static final int CONNECT_TIMEOUT = 5 * 1000; // 5 seconds
  private static final int READ_TIMEOUT = 5 * 1000; // 5 seconds;
  // construct a single instance per JVM
  private static SystemTime INSTANCE = new SystemTime();

  private static Logger _log = LoggerFactory.getLogger(SystemTime.class);
  private String _endpoint = null; // no path configured for TDS by default
  private String _enabled = "false"; // turned off by default
  private long adjustment = 0; // by default there is no adjustment


  private SystemTime() {
    UpdateWorker uw = new UpdateWorker();
    new Thread(uw).start();
  }
  public static long currentTimeMillis() {
    return System.currentTimeMillis() + INSTANCE.adjustment;
  }

  public static void setEndpoint(String url) {
    INSTANCE._endpoint = url;
  }

  public static void setEnabled(String enabledFlag) {
    INSTANCE._enabled = enabledFlag;
  }

  public static void setAdjustment(long adjustmentFromNowInMillis) {
    INSTANCE.adjustment = adjustmentFromNowInMillis;
    _log.info("updated adjustment to " + adjustmentFromNowInMillis);
  }

  public static long getAdjustment() {
    return INSTANCE.adjustment;
  }

  private long refreshAdjustment() throws Exception {

    if (!isEnabled()) {
      return 0;
    }
    if (getEndpointUrl() == null) {
      return 0;
    }

    // deliberately keep this simple java to keep dependencies light
    String endpoint = getEndpointUrl();
    URL url = new URL(endpoint);
    URLConnection connection = url.openConnection();
    HttpURLConnection httpConnection = (HttpURLConnection)connection;
    httpConnection.setRequestMethod("GET");
    httpConnection.setDoInput(true);
    httpConnection.setDoOutput(false);
    httpConnection.setConnectTimeout(CONNECT_TIMEOUT);
    httpConnection.setReadTimeout(READ_TIMEOUT);
    InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
    BufferedReader in = new BufferedReader(isr);
    String responseString;
    StringBuffer sb = new StringBuffer();
    while ((responseString = in.readLine()) != null) {
      sb.append(responseString);
    }

    return Long.parseLong(sb.toString());
  }

  private String getEndpointUrl() {
    String endpoint = null;
    // option 1:  read tds.path from system env
    endpoint = getEndpointUrlFromSystemProperty();
    if (StringUtils.isNotBlank(endpoint))
      return endpoint + API_CALL;
    // option 2:  spring injection
    if (StringUtils.isNotBlank(_endpoint)) {
      return _endpoint;
    }

    // option 3:  let if fail -- the default is not to configure this
    return null;
  }

  private String getEndpointUrlFromSystemProperty() {
    return System.getProperty(TDS_PATH_KEY);
  }


  /**
   * determine if time skew adjustment is enabled.  In production
   * environments IT SHOULD NOT BE.  FOR DEVELOPMENT ONLY!
   * @return
   */
  public static boolean isEnabled() {
    String systemProperty = System.getProperty(TIME_ADJUSTMENT_KEY);
    if (systemProperty != null) {
      return "true".equals(systemProperty);
    }

    // if endpoint was injected, assume we are turned on
    return "true".equalsIgnoreCase(INSTANCE._enabled);
  }

  public class UpdateWorker implements Runnable {

    @Override
    public void run() {

      if (INSTANCE.isEnabled()) {
        if (StringUtils.isNotBlank(getEndpointUrl())) {
          _log.warn("SystemTime Adjustment enabled.  Polling "
                  + getEndpointUrl() + " every " + POLL_INTERVAL + " seconds");
        } else {
          _log.error("SystemTime Adjustment enabled but no endpoint ULR configured!"
          + "  Please set -Dtds.path=http://localhost:8080/onebusaway-transit-data-federation-webapp"
          + " or equivalent.  Exiting.");
          return;
        }
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
          if (adj != adjustment) {
            _log.info("updated adjustment to " + adj);
            adjustment = adj;
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
