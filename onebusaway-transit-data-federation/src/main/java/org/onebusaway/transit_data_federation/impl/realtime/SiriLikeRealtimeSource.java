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
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphDaoImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
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
  private XPathExpression vehicleActivityExpression;
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
    factory = DocumentBuilderFactory.newInstance();
    builder = factory.newDocumentBuilder();
    
    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();

    vehicleActivityExpression = xpath.compile("/Siri/VehicleMonitoringDelivery/VehicleActivity");
    tripIdExpression = xpath.compile("MonitoredVehicleJourney/FramedVehicleJourneyRef/DatedVehicleJourneyRef/text()");
    serviceDateExpression = xpath.compile("MonitoredVehicleJourney/FramedVehicleJourneyRef/DataFrameRef/text()");
    recordedAtExpression = xpath.compile("RecordedAtTime/text()");
    vehicleIdExpression = xpath.compile("MonitoredVehicleJourney/VehicleRef/text()");
    latExpression = xpath.compile("MonitoredVehicleJourney/VehicleLocation/Latitude/text()");
    lonExpression = xpath.compile("MonitoredVehicleJourney/VehicleLocation/Longitude/text()");
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
    for (String route : getRoutes()) {
      for (Node n : parseVehicles(new URL(constructUrl(route, getApiKey(), getUrl())))) {
        VehicleLocationRecord vlr = parse(n);
        if (vlr != null) {
          result.addMatchedTripId(vlr.getTripId().getId());
          _vehicleLocationListener.handleVehicleLocationRecord(vlr);
        }
      }
    }
  }
  
  private String constructUrl(String route, String apiKey, String url) {
    return url + "?route=" + route + "&usertoken=" + apiKey;
  }

  // process URL into a series of fragments representing vehicle activity
  public List<Node> parseVehicles(URL url) throws Exception {
    ArrayList<Node> vehicles = new ArrayList<Node>();
    Document doc = builder.parse(url.toString());
    NodeList nl = (NodeList) this.vehicleActivityExpression.evaluate(doc, XPathConstants.NODESET);
    if (nl ==null) return vehicles;
    for (int i = 0; i < nl.getLength(); i++) {
      vehicles.add(nl.item(i));
    }
    return vehicles;
  }
  
  public VehicleLocationRecord parse(Node node) throws Exception {
    String tripId = (String) tripIdExpression.evaluate(node, XPathConstants.STRING);
    if (tripId == null) {
      _log.error("no trip for node=" + node);
      return null;
    }
    
    String serviceDateStr = (String) serviceDateExpression.evaluate(node, XPathConstants.STRING);
    Date serviceDate = parseServiceDate(serviceDateStr);
    
    VehicleLocationRecord vlr = new VehicleLocationRecord();
    try {
      vlr.setTimeOfLocationUpdate(parseDate((String)recordedAtExpression.evaluate(node, XPathConstants.STRING)).getTime());
      vlr.setTimeOfRecord(vlr.getTimeOfLocationUpdate());
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
      return null;
    }
    return vlr;
  }


  protected Integer calculateScheduleDeviation(VehicleLocationRecord vlr) {
    
    TripEntry tripEntry = this._transitGraphDao.getTripEntryForId(vlr.getTripId());
 // unit tests don't have a populated transit graph so fall back on scheduled time from feed
    if (tripEntry != null) {
      long time = vlr.getTimeOfLocationUpdate();
      double lat = vlr.getCurrentLocationLat();
      double lon = vlr.getCurrentLocationLon();
      long serviceDateTime = vlr.getServiceDate();
      long effectiveScheduleTimeSeconds = getEffectiveScheduleTime(tripEntry, lat, lon, time, serviceDateTime);
      long effectiveScheduleTime = effectiveScheduleTimeSeconds + (serviceDateTime/1000);
      return (int)(time - effectiveScheduleTime);
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

  private Date parseDate(String s) throws Exception {
    if (StringUtils.isBlank(s)) {
      return beginningOfDay();
    }
    return ISO_DATE_FORMAT.parse(s);
  }

  public Date parseServiceDate(String serviceDateStr) throws Exception {
    if (StringUtils.isBlank(serviceDateStr)) {
      return beginningOfDay();
    }
    return ISO_DATE_SHORT_FORMAT.parse(serviceDateStr);
  }

  private Date beginningOfDay() {
    return new ServiceDate().getAsDate();
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
}
