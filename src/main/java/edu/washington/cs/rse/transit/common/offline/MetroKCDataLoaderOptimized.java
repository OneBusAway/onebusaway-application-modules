/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.offline;

import edu.washington.cs.rse.text.CSVLibrary;
import edu.washington.cs.rse.text.CSVListener;
import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.model.BlockTrip;
import edu.washington.cs.rse.transit.common.model.BlockTripKey;
import edu.washington.cs.rse.transit.common.model.ChangeDate;
import edu.washington.cs.rse.transit.common.model.IdentityBean;
import edu.washington.cs.rse.transit.common.model.OrderedPatternStops;
import edu.washington.cs.rse.transit.common.model.PatternEventSequence;
import edu.washington.cs.rse.transit.common.model.PatternTimepoints;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.StreetName;
import edu.washington.cs.rse.transit.common.model.TPI;
import edu.washington.cs.rse.transit.common.model.TPIPath;
import edu.washington.cs.rse.transit.common.model.TPIPathKey;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.TransLink;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePoint;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePointKey;
import edu.washington.cs.rse.transit.common.model.TransNode;
import edu.washington.cs.rse.transit.common.model.Trip;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetroKCDataLoaderOptimized {

  public static void main(String[] args) throws Exception {
    ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
    MetroKCDataLoaderOptimized loader = (MetroKCDataLoaderOptimized) ctx.getBean("metroKCDataLoaderOptimized");
    loader.run();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private static SimpleDateFormat _format = new SimpleDateFormat(
      "dd-MMM-yyyy HH:mm:ss");

  private static Date _startEpoch;

  private static Date _endEpoch;

  private static GeometryFactory _factory = new GeometryFactory(
      new PrecisionModel(PrecisionModel.FLOATING), 2285);

  /*****************************************************************************
   * 
   ****************************************************************************/

  private static final String[] TIMEPOINT_FIELDS = {
      "id", "name8", "name20", "name40", "dbModDate", "timepointStatus",
      "timepointType", "transNode", "scheduledTimepointType"};

  private static final String[] TRANS_NODE_FIELDS = {
      "id", "dbModDate", "status", "x", "y", "ignore=z", "city", "ignore",
      "ignore", "ignore"};

  private static final String[] TRANS_LINK_FIELDS = {
      "id", "dbModDate", "streetName", "transNodeFrom", "transNodeTo",
      "ignore=aboveBelowFlag", "addrLeftFrom", "addrLeftTo", "addrRightFrom",
      "addrRightTo", "countyClass", "ignore=hovFlag", "linkLen",
      "ignore=transLinkStatus", "ignore=tigerClass", "ignore=trafficFlow",
      "ignore=transitFlag", "ignore=weightUsage", "zipLeft", "zipRight"};

  private static final String[] TRANS_LINK_SHAPE_POINT_FIELDS = {
      "id", "sequence", "dbModDate", "x", "y"};

  private static final String[] TPI_PATH_FIELDS = {
      "id", "sequence", "effectiveDate", "transLink", "dbModDate",
      "flowDirection", "status"};

  private static final String[] TPI_FIELDS = {
      "id", "effectiveDate", "dbModDate", "fromTimepoint", "toTimepoint",
      "status", "ignore=endDate"};

  private static String[] ORDERED_PATTERN_STOPS_FIELDS = {
      "sequence", "dbModDate", "ignore=sequence2", "ignore=sequence3",
      "pptFlag", "signOfDestination", "signOfDash", "assignedToOns",
      "assignedToOffs", "route", "routePartCode", "showThroughRouteNum",
      "localExpressCode", "schedulePatternId", "directionCode",
      "effectiveBeginDate", "ignore=patternEventTypeId", "stop"};

  private static String[] ROUTES_FIELDS = {
      "id", "number", "code", "ignore=description", "ignore=dbModDate",
      "ignore=effectiveBeginDate", "ignore=effectiveEndDate", "transitAgencyId"};

  private static String[] STOP_TIME_FIELDS = {
      "changeId", "trip", "stopTimePosition", "dbModeDate", "passingTime",
      "patternId", "timepoint", "patternTimepointPosition", "firstLastFlag"};

  private static String[] TRIPS_FIELDS = {
      "changeId", "id", "dbModDate", "directionName", "liftFlag", "patternId",
      "peakFlag", "scheduleTripId", "scheduleType", "exceptionCode",
      "ignore=forwardLayover", "ignore=schedTripType", "updateDate",
      "controlPointTime", "ignore=changePrior", "ignore=changeNumFollowing",
      "patternIdFollowing", "patternIdPrior", "tripLink"};

  private static String[] BLOCK_TRIPS_FIELDS = {
      "changeDate", "id", "trip", "tripPosition", "dbModDate", "tripEndTime",
      "tripStartTime"};

  private static String[] CHANGE_DATE_FIELDS = {
      "id", "bookingId", "startDate", "dbModDate", "minorChangeDate",
      "endDate", "currentNextCode", "effectiveBeginDate", "effectiveEndDate",
      "ignore", "ignore"};

  private static String[] SERVICE_PATTERN_FIELDS = {
      "id", "changeId", "dbModDate", "direction", "ignore", "ignore", "route",
      "ignore", "serviceType", "schedulePatternId", "patternType"};

  private static String[] PATTERN_EVENT_SEQUENCE_FIELDS = {
      "dbModDate", "directionCode", "effectiveBeginDate", "effectiveEndDate",
      "route", "routePartCode", "localExpressCode", "schedulePatternId"};

  private static final String[] STOP_LOCATION_FIELDS = {
      "bay", "ignore", "ignore", "ignore=createdBy", "crossStreetName",
      "ignore=dateCreated", "ignore=dateMapped", "ignore=dateModified",
      "displacement", "effectiveBeginDate", "effectiveEndDate",
      "fromCrossCurb", "fromIntersectionCenter", "gisJurisdictionCode",
      "gisZipCode", "ignore", "ignore", "ignore", "ignore", "ignore",
      "mappedLinkLen", "mappedPercentFrom", "mappedTransNodeFrom", "ignore",
      "ignore=modifiedBy", "ignore=rfaManualOverride", "rideFreeArea", "side",
      "sideCross", "sideOn", "id", "ignore", "ignore=streetAddress",
      "streetAddressComment", "transLink", "x", "xOffset", "y", "yOffset"};

  private static final String[] STREET_NAME_FIELDS = {
      "id", "status", "dbModDate", "prefix", "name", "type", "suffix"};

  private static final String[] PATTERN_TIMEPOINTS_FIELDS = {
      "patternId", "changeId", "patternTimepointPosition", "dbModDate",
      "timepoint", "tpi", "effectiveDate", "firstLastFlag"};

  private SessionFactory _sessionFactory;

  private Session _session;

  private File _dataDirectory;

  private List<CSVSchema> _schemas = new ArrayList<CSVSchema>();

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  public void setDataDirectory(File dataDirectory) {
    System.out.println(this);
    System.out.println("dataDirectory===" + dataDirectory);
    _dataDirectory = dataDirectory;
  }

  public void run() throws Exception {
    System.out.println(this);
    System.out.println("Loading Data Directory===" + _dataDirectory);

    add("routes.csv", Route.class, ROUTES_FIELDS);
    add("change_dates.csv", ChangeDate.class, CHANGE_DATE_FIELDS);

    // Depends on ChangeDate and Route
    CSVSchema sp = add("service_patterns.csv", ServicePattern.class,
        SERVICE_PATTERN_FIELDS);
    sp.addModifier(new ServicePatternKeyPropertyModifier());

    add("street_names.csv", StreetName.class, STREET_NAME_FIELDS);

    CSVSchema tn = add("trans_node.csv", TransNode.class, TRANS_NODE_FIELDS);
    tn.addModifier(new PointPropertyModifier("x", "y", "location"));

    // Depends on StreetName, TransNode
    add("trans_link.csv", TransLink.class, TRANS_LINK_FIELDS);

    // Depends on StreetName, TransLink
    CSVSchema sl = add("stop_locations.csv", StopLocation.class,
        STOP_LOCATION_FIELDS);
    sl.addModifier(new PointPropertyModifier("x", "y", "location"));
    sl.addModifier(new PointPropertyModifier("xOffset", "yOffset",
        "offsetLocation"));

    // Depends on StopLocation and Route
    add("ordered_pattern_stops.csv", OrderedPatternStops.class,
        ORDERED_PATTERN_STOPS_FIELDS);

    // Depends on TransNode
    add("timepoints.csv", Timepoint.class, TIMEPOINT_FIELDS);

    // Depends on ServicePattern
    CSVSchema trips = add("trips.csv", Trip.class, TRIPS_FIELDS);
    trips.addModifier(new ServicePatternPropertyModifier());

    // Depends on ServicePattern, Trip, Timepoint
    CSVSchema st = add("stop_times.csv", StopTime.class, STOP_TIME_FIELDS);
    st.addModifier(new ServicePatternPropertyModifier());

    // Depends on Timepoints
    add("tpi.csv", TPI.class, TPI_FIELDS);

    // Depends on ServicePattern, Timepoint, TPI
    CSVSchema pt = add("pattern_timepoints.csv", PatternTimepoints.class,
        PATTERN_TIMEPOINTS_FIELDS);
    pt.addModifier(new ServicePatternPropertyModifier());

    // Depends on TransLink
    CSVSchema tlsp = add("trans_link_shape_point.csv",
        TransLinkShapePoint.class, TRANS_LINK_SHAPE_POINT_FIELDS);
    tlsp.addModifier(new PointPropertyModifier("x", "y", "location"));
    tlsp.addModifier(new TransLinkShapePointKeyPropertyModifier());

    // Depends on TransLink
    CSVSchema tpip = add("tpi_path.csv", TPIPath.class, TPI_PATH_FIELDS);
    tpip.addModifier(new TPIPathKeyPropertyModifier());

    // Depends on ChangeDate, Trip
    CSVSchema bt = add("block_trips.csv", BlockTrip.class, BLOCK_TRIPS_FIELDS);
    bt.addModifier(new BlockTripKeyPropertyModifier());

    // Depends on Route
    add("pattern_event_sequences.csv", PatternEventSequence.class,
        PATTERN_EVENT_SEQUENCE_FIELDS);

    runInternal();
  }

  private CSVSchema add(String filename, Class<?> c, String[] fields)
      throws Exception {
    File file = new File(_dataDirectory, filename);
    CSVSchema schema = new CSVSchema(file, c, fields);
    schema.addModifier(new EntityPropertyModifier());
    schema.addModifier(new DatePropertyModifier());
    schema.addModifier(new NumberPropertyModifier());
    _schemas.add(schema);
    return schema;
  }

  private void runInternal() throws Exception {

    for (CSVSchema schema : _schemas) {

      System.out.println(schema.getFile());

      _session = _sessionFactory.openSession();
      Transaction tx = _session.beginTransaction();

      CSVToEntityHandler handler = new CSVToEntityHandler(schema);

      try {
        CSVLibrary.parse(schema.getFile(), handler);
      } catch (StopException ex) {

      }

      handler.clear();

      tx.commit();
      _session.close();
    }
  }

  private Date parseMetroTime(String value) throws ParseException {

    if (_startEpoch == null)
      _startEpoch = _format.parse("31-JAN-1970 00:00:00");

    if (_endEpoch == null)
      _endEpoch = _format.parse("31-JAN-2020 00:00:00");

    Date d = _format.parse(value);

    if (d.getTime() < _startEpoch.getTime())
      return _startEpoch;

    if (d.getTime() > _endEpoch.getTime())
      return _endEpoch;

    return d;
  }

  private class CSVSchema implements PropertyModifier {

    private File _file;

    private Class<?> _beanType;

    private String[] _fields;

    private List<PropertyModifier> _modifiers = new ArrayList<PropertyModifier>();

    public CSVSchema(File file, Class<?> beanType, String[] fields) {
      _file = file;
      _beanType = beanType;
      _fields = fields;
    }

    public File getFile() {
      return _file;
    }

    public Class<?> getBeanType() {
      return _beanType;
    }

    public String[] getFields() {
      return _fields;
    }

    public void addModifier(PropertyModifier modifier) {
      _modifiers.add(modifier);
    }

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      for (PropertyModifier modifier : _modifiers)
        modifier.modify(wrapper, values);
    }
  }

  private class CSVToEntityHandler implements CSVListener {

    private static final int FLUSH_SIZE = 50;

    private Class<?> _beanType;

    private String[] _fields;

    private CSVSchema _schema;

    private int _count = 0;

    private Set<Object> _ids = new HashSet<Object>();

    public CSVToEntityHandler(CSVSchema schema) {
      _beanType = schema.getBeanType();
      _fields = schema.getFields();
      _schema = schema;
    }

    public void clear() {
      _ids.clear();
    }

    public void handleLine(List<String> in) throws Exception {

      if (in.size() != _fields.length)
        throw new IllegalArgumentException("field mismatch: expected="
            + _fields.length + " actual=" + in.size());

      int index = 0;

      Object bean = _beanType.newInstance();

      BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);

      Map<String, Object> values = new HashMap<String, Object>();

      for (String value : in) {
        String field = _fields[index++];
        if (!field.startsWith("ignore"))
          values.put(field, value);
      }

      _schema.modify(wrapper, values);
      try {
        wrapper.setPropertyValues(values);
      } catch (Exception ex) {
        System.err.println(values);
        throw ex;
      }

      //Object id = wrapper.getPropertyValue("id");

      /*
      if (id == null
          || ((id instanceof Integer) && ((Integer) id).intValue() == 0)
          || _ids.add(id)) {
        try {
        */
          _session.save(bean);
          /*
        } catch (NonUniqueObjectException ex) {
          System.err.println("  " + ex.getMessage());
        }
      }
      */

      _count++;

      if (_count % FLUSH_SIZE == 0) {
        _session.flush();
        _session.clear();
      }

      if (_count % 1000 == 0)
        System.out.println("  count=" + _count);
    }
  }

  private interface PropertyModifier {
    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception;
  }

  private class EntityPropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {

      Set<String> toRemove = new HashSet<String>();

      for (Map.Entry<String, Object> entry : values.entrySet()) {
        String field = entry.getKey();
        Class<?> type = wrapper.getPropertyType(field);
        if (type == null)
          continue;
        if (IdentityBean.class.isAssignableFrom(type)) {

          try {
            String value = entry.getValue().toString();
            if (value.length() > 0) {
              int id = Integer.parseInt(entry.getValue().toString());
              Object v = _session.get(type, id);
              values.put(field, v);
            } else {
              toRemove.add(field);
            }
          } catch (NumberFormatException ex) {
            throw new Exception("Error loading entity field=" + field);
          }
        }
      }

      values.keySet().removeAll(toRemove);
    }
  }

  private class DatePropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {

      Set<String> toRemove = new HashSet<String>();

      for (Map.Entry<String, Object> entry : values.entrySet()) {
        String field = entry.getKey();
        Class<?> type = wrapper.getPropertyType(field);

        if (type == null)
          continue;

        if (type.equals(Date.class)) {
          String value = entry.getValue().toString();
          if (value.length() == 0)
            toRemove.add(field);
          else {
            try {
              Object v = parseMetroTime(value);
              values.put(field, v);
            } catch (ParseException ex) {
              throw new Exception("error for field=" + field + " value="
                  + entry.getValue(), ex);
            }
          }

        }

      }

      values.keySet().removeAll(toRemove);
    }
  }

  private class NumberPropertyModifier implements PropertyModifier {

    private Set<Class<?>> _types = new HashSet<Class<?>>();

    public NumberPropertyModifier() {
      _types.add(Integer.TYPE);
      _types.add(Integer.class);
      _types.add(Long.TYPE);
      _types.add(Long.class);
      _types.add(Float.TYPE);
      _types.add(Float.class);
      _types.add(Double.TYPE);
      _types.add(Double.class);
    }

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      for (Map.Entry<String, Object> entry : values.entrySet()) {

        String field = entry.getKey();
        Class<?> type = wrapper.getPropertyType(field);

        if (type == null)
          continue;

        try {
          if (_types.contains(type)) {
            String value = values.get(field).toString();
            if (value.length() == 0) {
              values.put(field, "0");
            }
          }
        } catch (Exception ex) {
          throw new Exception("field=" + field, ex);
        }
      }
    }

  }

  private class PointPropertyModifier implements PropertyModifier {

    private String _xField;

    private String _yField;

    private String _outputField;

    public PointPropertyModifier(String xField, String yField,
        String outputField) {
      _xField = xField;
      _yField = yField;
      _outputField = outputField;
    }

    public void modify(BeanWrapper wrapper, Map<String, Object> values) {
      String xValue = (String) values.remove(_xField);
      String yValue = (String) values.remove(_yField);
      double x = Double.parseDouble(xValue);
      double y = Double.parseDouble(yValue);
      Point point = _factory.createPoint(new Coordinate(x, y));
      values.put(_outputField, point);
    }
  }

  private class ServicePatternKeyPropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      int patternId = Integer.parseInt(values.remove("id").toString());
      int changeId = Integer.parseInt(values.remove("changeId").toString());

      ChangeDate changeDate = (ChangeDate) _session.get(ChangeDate.class,
          changeId);
      values.put("id", new ServicePatternKey(changeDate, patternId));
    }
  }

  private class ServicePatternPropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      int patternId = Integer.parseInt(values.remove("patternId").toString());
      int changeId = Integer.parseInt(values.remove("changeId").toString());

      ChangeDate changeDate = (ChangeDate) _session.get(ChangeDate.class,
          changeId);
      ServicePatternKey id = new ServicePatternKey(changeDate, patternId);
      values.put("servicePattern", _session.get(ServicePattern.class, id));
    }
  }

  private class TransLinkShapePointKeyPropertyModifier implements
      PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      int id = Integer.parseInt(values.remove("id").toString());
      int sequence = Integer.parseInt(values.remove("sequence").toString());

      TransLink link = (TransLink) _session.get(TransLink.class, id);

      values.put("id", new TransLinkShapePointKey(link, sequence));
    }
  }

  private class TPIPathKeyPropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      int id = Integer.parseInt(values.remove("id").toString());
      int sequence = Integer.parseInt(values.remove("sequence").toString());

      TPI tpi = (TPI) _session.get(TPI.class, id);

      values.put("id", new TPIPathKey(tpi, sequence));
    }
  }

  private class BlockTripKeyPropertyModifier implements PropertyModifier {

    public void modify(BeanWrapper wrapper, Map<String, Object> values)
        throws Exception {
      int changeDateId = Integer.parseInt(values.remove("changeDate").toString());
      int tripId = Integer.parseInt(values.remove("trip").toString());
      int id = Integer.parseInt(values.remove("id").toString());

      ChangeDate changeDate = (ChangeDate) _session.get(ChangeDate.class,
          changeDateId);
      Trip trip = (Trip) _session.get(Trip.class, tripId);

      values.put("id", new BlockTripKey(changeDate, trip, id));
    }
  }

  private static final class StopException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

  }
}
