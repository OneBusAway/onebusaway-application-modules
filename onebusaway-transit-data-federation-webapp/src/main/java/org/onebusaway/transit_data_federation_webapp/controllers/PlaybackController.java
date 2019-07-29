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
package org.onebusaway.transit_data_federation_webapp.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.transit_data_federation.impl.bundle.RealtimeSourceServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation_webapp.model.RealtimeSourceDetail;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PlaybackController {

  private static final String DEFAULT_ARCHIVE_URL = "http://localhost:9999/onebusaway-gtfs-realtime-archiver/gtfs-realtime/trip-updates";
  private static final int CONNECTION_TIMEOUT = 5 * 1000; // 5 seconds
  private static final int READ_TIMEOUT = 5 * 1000; // 5 seconds
  private static Logger _log = LoggerFactory.getLogger(PlaybackController.class);
  private static long adjustment = 0;
  
  @Autowired
  private RealtimeSourceServiceImpl _sourceService;

  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;

  @RequestMapping("/playback.do")
  public ModelAndView index() throws Exception {
    return new ModelAndView("playback.jspx", "source", _sourceService);
  }

  @RequestMapping("/adjustment.do")
  @ResponseBody
  public String adjustment() throws Exception {
    return "" + adjustment;
  }

  
  @RequestMapping("/playback!datasource-detail.do")
  public ModelAndView datasourceDetail(int index) throws Exception {
    RealtimeSourceDetail detail = new RealtimeSourceDetail();
    detail.setSource(_sourceService.getSources().get(index));
    detail.setIndex(index);
    return new ModelAndView("datasource-detail.jspx", "detail", detail);
  }

  
  @RequestMapping("/playback!playback-detail.do")
  public ModelAndView playbackDetail(int index) throws Exception {
    RealtimeSourceDetail detail = new RealtimeSourceDetail();
    detail.setSource(_sourceService.getSources().get(index));
    detail.setIndex(index);
    detail.setBaseUrl(getDefaultArchiveUrlConfig());
    return new ModelAndView("playback-detail.jspx", "detail", detail);
  }
  
  @RequestMapping("/playback!datasource-toggle")
  public ModelAndView datasourceToggle(int index) {
    GtfsRealtimeSource source = _sourceService.getSources().get(index); 
    boolean enabled = source.getEnabled();
    source.setEnabled(!enabled);
    return new ModelAndView("redirect:/playback.do");
  }

  @RequestMapping("/playback!playback-disable")
  public ModelAndView playbackDisable() {
    adjustment = 0;
    SystemTime.setAdjustment(0);
    return new ModelAndView("redirect:/playback.do");
  }


  @RequestMapping("/playback!set-time")
  public ModelAndView setTime(String time) throws Exception {
    _log.info("input date of " + time);
    if (time != null || time != "") {
      Date selectedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
      adjustment = selectedDate.getTime() - System.currentTimeMillis();
      _log.info("setting adjustment to " + adjustment + " based on time " + selectedDate);
      SystemTime.setAdjustment(adjustment);
    }
    return new ModelAndView("redirect:/playback.do");
  }

  @RequestMapping("/playback!playback.do")
  public ModelAndView playback(int index,
     String date,
      String time, 
      String baseUrl, 
      String apiKey, 
      String refresh,
      String command) throws Exception {
    String dateAndTimeStr = date + " " + time;
    _log.debug("playback with dateAndTime=|" + dateAndTimeStr + "|");
    long millisSinceEpoch = parseTime(dateAndTimeStr);
    long localAdjustment = System.currentTimeMillis() - millisSinceEpoch;
    // set the values that came off the page
    if ("clear".equals(command)) {
      String resetUrl = constructResetUrl(baseUrl, apiKey);
      _log.info("calling reset...");
      callReset(resetUrl);
    } else {
      _log.info("update command to time=" + dateAndTimeStr);
    }
    String tripUrl = constructTripUrl(baseUrl, dateAndTimeStr, refresh, apiKey);
    GtfsRealtimeSource source = _sourceService.getSources().get(index); 
    source.setTripUpdatesUrl(new URL(tripUrl));
    // turn off date validation
    source.getGtfsRealtimeTripLibrary().setValidateCurrentTime(false);
    source.setEnabled(true);
    source.reset();
    adjustment = localAdjustment * -1;
    SystemTime.setAdjustment(adjustment);
    return new ModelAndView("redirect:/playback.do");
  }

  private void callReset(String endpoint) throws Exception {
      URL url = new URL(endpoint);
      URLConnection connection = url.openConnection();
      HttpURLConnection httpConnection = (HttpURLConnection)connection;
      httpConnection.setRequestMethod("GET");
      httpConnection.setDoInput(true);
      httpConnection.setDoOutput(false);
      httpConnection.setConnectTimeout(CONNECTION_TIMEOUT);
      httpConnection.setReadTimeout(READ_TIMEOUT);
      InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
      BufferedReader in = new BufferedReader(isr);
      String responseString;
      StringBuffer sb = new StringBuffer();
      while ((responseString = in.readLine()) != null) {
        sb.append(responseString);
      }
      if (!"SUCCESS".equals(sb.toString())) {
        throw new RuntimeException("Unexpected response=|" + sb.toString() + "|"
        + " for call=" + url);
      }
    }
 

  private String constructResetUrl(String baseUrl, String apiKey) {
    return baseUrl.replace("trip-updates",  "clear") + "?"
        + "key=" + apiKey;
  }

  private long parseTime(String time) throws Exception {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return format.parse(time).getTime();
  }

  private String constructTripUrl(String baseUrl, String time, String refresh,
      String apiKey) {
    
    return baseUrl + "?"
        + "time=" + URLEncoder.encode(time)
        + "&interval=" + refresh
        + "&key=" + apiKey;
  }


  private String getDefaultArchiveUrlConfig() {

    String value;
    try {
      value = _configurationServiceClient.getItem("tds", "archiveUrl");
    } catch (Exception any) {
      value = DEFAULT_ARCHIVE_URL;
    }
    if (StringUtils.isBlank(value)) {
      return DEFAULT_ARCHIVE_URL;
    }
    return value;
  }

}
