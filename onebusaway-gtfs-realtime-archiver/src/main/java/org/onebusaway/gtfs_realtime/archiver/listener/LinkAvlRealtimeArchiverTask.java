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
package org.onebusaway.gtfs_realtime.archiver.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import org.onebusaway.gtfs_realtime.archiver.model.LinkAVLData;
import org.onebusaway.gtfs_realtime.archiver.model.StopUpdate;
import org.onebusaway.gtfs_realtime.archiver.model.TripInfo;
import org.onebusaway.gtfs_realtime.archiver.service.GtfsPersistor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Entry point for archiving Link AVL realtime data. Configure one of these (via spring
 * configuration) for your Link AVL realtime source.
 */
public class LinkAvlRealtimeArchiverTask extends RealtimeArchiverTask {

  private URL _linkAvlUrl;
  private String _avlFeedId = "Link";
  private GtfsPersistor _persistor;
  private boolean _disabledCertificateVerification; // Allow HTTPS connection
                                                    // without verification

  public URL get_linkAvlUrl() {
    return _linkAvlUrl;
  }
  public void setLinkAvlUrl(URL linkAvlUrl) {
    _linkAvlUrl = linkAvlUrl;
  }
  public String getAvlFeedId() {
    return _avlFeedId;
  }
  public void setAvlFeedId(String avlFeedId) {
    _avlFeedId = avlFeedId;
  }
  @Autowired
  public void setGtfsPersistor(GtfsPersistor persistor) {
    _persistor = persistor;
  }
  public boolean isDisabledCertificateVerification() {
    return _disabledCertificateVerification;
  }
  public void setDisabledCertificateVerification(
      boolean disabledCertificateVerification) {
    _disabledCertificateVerification = disabledCertificateVerification;
  }

  @Override
  protected void init() {
    if (_linkAvlUrl == null) {
      _log.warn(
          "no linkAvlUrl configured.  This is most likely a configuration issue");
    }

    if (_refreshInterval > 0) {
      _log.info("scheduling executor for refresh=" + _refreshInterval);
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new UpdateTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }
  }

  public void update() throws IOException {
    // Read feed
    String avlJson = _linkAvlUrl == null ? "" : readFeedFromUrl(_linkAvlUrl);

    // Parse into model classes
    LinkAVLData avlData = deserializeAvlJson(avlJson);

    // Persist
    _persistor.persist(avlData); 

    return;
  }

  /**
   * @param url the {@link URL} to read from
   * @return a json String with the avl data 
   * @throws IOException
   */
  private String readFeedFromUrl(URL url) throws IOException {
    InputStream in = null;
    String avlData = "";
    try {
      URLConnection conn = url.openConnection();
      if(conn instanceof HttpsURLConnection && _disabledCertificateVerification) {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] { new OpenTrustManager() },
                new java.security.SecureRandom());
        ((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
        ((HttpsURLConnection)conn).setHostnameVerifier(
            new HostnameVerifier(){
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            }
        );
      }
      conn.setRequestProperty("Accept", "application/json");
      InputStreamReader isr = new InputStreamReader(conn.getInputStream());
      BufferedReader br = new BufferedReader(isr);
      avlData = br.readLine();
    } catch (Exception ex) {
      _log.error(ex.getMessage());
    }
    return avlData;
  }

  protected class UpdateTask implements Runnable {
    @Override
    public void run() {
      try {
        update();
      } catch (Throwable ex) {
        _log.warn("Error updating from AVL-realtime data sources", ex);
      }
    }
  }

  private LinkAVLData deserializeAvlJson(String avlJson) {
    LinkAVLData avlData = new LinkAVLData();
    ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
    try {
      avlData = mapper.readValue(avlJson, LinkAVLData.class);
    } 
    catch (JsonParseException e) {
      _log.error("JsonParseException trying to parse feed data.");
    } catch (JsonMappingException e) {
      _log.error("JsonMappingException: " + e.getMessage());
    } catch (IOException e) {
      _log.error("IOException trying to parse feed data.");
    } catch (Exception e) {
      _log.error("Exception trying to parse feed data: " + e.getMessage());
    }

    avlData.setAvlSource(_avlFeedId);
    if (avlData.getTrips() == null) return avlData;
    for (TripInfo tripInfo : avlData.getTrips()) {
      tripInfo.setLinkAVLData(avlData);
      for (StopUpdate stopUpdate : tripInfo.getStopUpdates()) {
        stopUpdate.setTripInfo(tripInfo);
      }
    }
    return avlData;
  }

  /*
   * This class is needed for connecting using HTTPS without certificate
   * verification.
   */
  private class OpenTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
    }
    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
  }
}
