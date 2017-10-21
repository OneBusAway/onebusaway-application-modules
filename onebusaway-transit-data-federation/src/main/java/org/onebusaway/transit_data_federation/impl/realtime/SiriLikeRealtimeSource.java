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
package org.onebusaway.transit_data_federation.impl.realtime;

// todo refactor this
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.SiriLikeRealtimeSource.NodesAndTimestamp;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphDaoImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Inject data that is in a SIRI like format, but has enough differences
 * that a custom parser is required.
 *
 */
public class SiriLikeRealtimeSource {

  private static final Logger _log = LoggerFactory.getLogger(SiriLikeRealtimeSource.class);
  private static SimpleDateFormat ISO_DATE_SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
  private static SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");
  public List<String> _routeList = new ArrayList<String>();
  private XPathExpression mvjExpression;
  private XPathExpression tripIdExpression;
  private XPathExpression serviceDateExpression;
  private XPathExpression recordedAtExpression;
  private XPathExpression vehicleIdExpression;
  private XPathExpression latExpression;
  private XPathExpression lonExpression;
  private TransitGraphDaoImpl _transitGraphDao;
  private BlockGeospatialService _blockGeospatialService;
  private VehicleLocationListener _vehicleLocationListener;
  private ScheduledExecutorService _scheduledExecutorService;
  private ScheduledFuture<?> _refreshTask;
  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;
  private String _agency;
  private String _baseUrl;
  private String _apiKey;
  
  private int _refreshInterval = 30;
  
  public void setApiKey(String key) {
    _apiKey = key;
  }
  
  public String getApiKey() {
    return _apiKey;
  }
  
  public void setBaseUrl(String url) {
    _baseUrl = url;
  }
  
  public String getUrl() {
    return _baseUrl;
  }
  
  public void setAgency(String agencyId) {
    _agency = agencyId;
  }
  
  private String getAgency() {
    return _agency;
  }

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }
  
  @Autowired
  public void setTransitGraphDao(TransitGraphDaoImpl transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }
  
  @Autowired
  public void setBlockGeospatialService(BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  @Autowired
  public void setVehicleLocationListener(
      VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }
  
  @Autowired
  public void setScheduledExecutorService(
      ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  @PostConstruct
  public void setup() throws Exception {
    ISO_DATE_FORMAT.setLenient(false);
    ISO_DATE_SHORT_FORMAT.setLenient(false);
    factory = DocumentBuilderFactory.newInstance();
    builder = factory.newDocumentBuilder();
    
    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();

    mvjExpression = xpath.compile("/Siri/VehicleMonitoringDelivery/VehicleActivity/MonitoredVehicleJourney");
    tripIdExpression = xpath.compile("FramedVehicleJourneyRef/DatedVehicleJourneyRef/text()");
    serviceDateExpression = xpath.compile("FramedVehicleJourneyRef/DataFrameRef/text()");
    recordedAtExpression = xpath.compile("/Siri/VehicleMonitoringDelivery/VehicleActivity/RecordedAtTime/text()");
    vehicleIdExpression = xpath.compile("VehicleRef/text()");
    latExpression = xpath.compile("VehicleLocation/Latitude/text()");
    lonExpression = xpath.compile("VehicleLocation/Longitude/text()");
    if (_refreshInterval > 0) {
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }
  }
  
  @PreDestroy
  public void stop() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
  }

  public void setRoutes(List<String> routes) {
    _routeList = routes;
  }
  
  public List<String> getRoutes() {
    // for now routes are statically configured via spring
    return _routeList;
  }
  
  public void refresh() throws Exception {
    MonitoredResult result = new MonitoredResult();
    handleUpdates(result);
  }
  
  private synchronized void handleUpdates(MonitoredResult result) throws Exception {
    int vehicles = 0;
    int trips = 0;
    for (String route : getRoutes()) {
      NodesAndTimestamp nodesAndTimestamp = parseVehicles(new URL(constructUrl(route, getApiKey(), getUrl())));
      _log.debug("found " + nodesAndTimestamp.getNodes().size() + " nodes");
      for (Node n : nodesAndTimestamp.getNodes()) {
        vehicles ++;
        VehicleLocationRecord vlr = parse(n, nodesAndTimestamp.getTimestamp());
        if (vlr != null) {
          trips++;
          result.addMatchedTripId(vlr.getTripId().getId());
          try {
            _vehicleLocationListener.handleVehicleLocationRecord(vlr);
          } catch (Exception any) {
            // bury
          }
        }
      }
    }
    _log.info("updated " + trips + " of " + vehicles + " for agency " + getAgency());
  }
  
  private String constructUrl(String route, String apiKey, String url) {
    return url + "?route=" + route + "&usertoken=" + apiKey;
  }

  // process URL into a series of fragments representing vehicle activity
  public NodesAndTimestamp parseVehicles(URL url) throws Exception {
    List<Node> vehicles = new ArrayList<Node>();
    Document doc = builder.parse(url.toString());
    String recordedAtStr = (String)recordedAtExpression.evaluate(doc, XPathConstants.STRING);
    long timestamp = parseDate(recordedAtStr).getTime();
    _log.debug("timestamp=" + new Date(timestamp) + " for date " + recordedAtStr);
    NodeList nl = (NodeList) this.mvjExpression.evaluate(doc, XPathConstants.NODESET);
    if (nl ==null || nl.getLength() == 0) {
      _log.debug("no nodes found");
      return new NodesAndTimestamp(vehicles, timestamp);
    }
    for (int i = 0; i < nl.getLength(); i++) {
      vehicles.add(nl.item(i));
    }
    return new NodesAndTimestamp(vehicles, timestamp);
  }
  
  
  public VehicleLocationRecord parse(Node node, long timestamp) throws Exception {
    String tripId = (String) tripIdExpression.evaluate(node, XPathConstants.STRING);
    if (tripId == null) {
      _log.error("no trip for node=" + node);
      return null;
    }
    if (timestamp == 0) {
      timestamp = SystemTime.currentTimeMillis();
    }
    
    String serviceDateStr = (String) serviceDateExpression.evaluate(node, XPathConstants.STRING);
    Date serviceDate = parseServiceDate(serviceDateStr);
    
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    try {
      vlr.setTimeOfLocationUpdate(timestamp);
      vlr.setTimeOfRecord(timestamp);
      vlr.setTripId(new AgencyAndId(getAgency(), tripId));
      vlr.setServiceDate(serviceDate.getTime());
      vlr.setVehicleId(new AgencyAndId(getAgency(), (String)vehicleIdExpression.evaluate(node, XPathConstants.STRING)));
      vlr.setCurrentLocationLat(asDouble(latExpression.evaluate(node, XPathConstants.STRING)));
      vlr.setCurrentLocationLon(asDouble(lonExpression.evaluate(node, XPathConstants.STRING)));
      Integer scheduleDeviation = calculateScheduleDeviation(vlr);
      if (scheduleDeviation != null) {
        vlr.setScheduleDeviation(scheduleDeviation);
      }
    } catch (NumberFormatException nfe) {
      _log.info("caught nfe", nfe);
      return null;
    }
    _log.debug("return vlr=" + vlr);
    return vlr;
  }


  protected Integer calculateScheduleDeviation(VehicleLocationRecord vlr) {
    
    TripEntry tripEntry = this._transitGraphDao.getTripEntryForId(vlr.getTripId());
 // unit tests don't have a populated transit graph so fall back on scheduled time from feed
    if (tripEntry != null) {
      // todo this is a side effect
      vlr.setBlockId(tripEntry.getBlock().getId());
      long time = vlr.getTimeOfLocationUpdate()/1000;
      double lat = vlr.getCurrentLocationLat();
      double lon = vlr.getCurrentLocationLon();
      long serviceDateTime = vlr.getServiceDate();
      long effectiveScheduleTimeSeconds = getEffectiveScheduleTime(tripEntry, lat, lon, time, serviceDateTime);
      long effectiveScheduleTime = effectiveScheduleTimeSeconds + (serviceDateTime/1000);
      int deviation =  (int)(time - effectiveScheduleTime);
      _log.debug("deviation(" + vlr.getVehicleId() + " is " + deviation + " time=" + new Date(time*1000) 
          + ", effectiveScheduleTime=" + new Date(effectiveScheduleTime*1000));
      return deviation;
    }
    
    return null;
  }

private long getEffectiveScheduleTime(TripEntry trip, double lat, double lon, long timestamp, long serviceDate) {
    
    ServiceIdActivation serviceIds = new ServiceIdActivation(trip.getServiceId());
    // todo!
    BlockConfigurationEntry blockConfig = blockConfiguration(trip.getBlock(), serviceIds, trip);
    BlockInstance block = new BlockInstance(blockConfig, serviceDate);
    CoordinatePoint location = new CoordinatePoint(lat, lon);
     
    ScheduledBlockLocation loc = _blockGeospatialService.getBestScheduledBlockLocationForLocation(
        block, location, timestamp, 0, trip.getTotalTripDistance());
    
    return loc.getScheduledTime();
  }
  
  private double asDouble(Object obj) {
    String s = (String) obj;
    return Double.parseDouble(s);
  }

  public Date parseShortDate(String s) throws Exception {
    return ISO_DATE_SHORT_FORMAT.parse(s);
  }

  public Date parseDate(String s) throws Exception {
    int endPos = "yyyy-MM-ddTHH:mm:ss.SSS".length();
    // we can't convince Java's Simple Date to parse millisecond to 7 digit precision
    s = s.substring(0, endPos-1) + s.substring((s.length() - 7), s.length());
    
    return ISO_DATE_FORMAT.parse(s);
  }

  public Date parseServiceDate(String serviceDateStr) throws Exception {
    Date d = ISO_DATE_SHORT_FORMAT.parse(serviceDateStr);
    return new ServiceDate(d).getAsDate();
  }

  
  private class RefreshTask implements Runnable {

    @Override
    public void run() {
      try {
        refresh();
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources", ex);
      }
    }
  }
  
  public static class NodesAndTimestamp {
    private List<Node> _nodes;
    private long _timestamp;
    public NodesAndTimestamp(List<Node> nodes, long timestamp) {
      this._nodes = nodes;
      this._timestamp = timestamp;
    }
    public List<Node> getNodes() {
      return _nodes;
    }
    public long getTimestamp() {
      return _timestamp;
    }
  }

}
