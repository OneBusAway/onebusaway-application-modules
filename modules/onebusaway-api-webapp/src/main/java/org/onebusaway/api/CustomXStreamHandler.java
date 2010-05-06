package org.onebusaway.api;

import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.TripStatusBean;

import com.thoughtworks.xstream.XStream;

import org.apache.struts2.rest.handler.XStreamHandler;

public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.alias("response", ResponseBean.class);
    xstream.alias("stop", StopBean.class);
    xstream.alias("route", RouteBean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureBean.class);
    xstream.alias("encodedPolyline", EncodedPolylineBean.class);
    xstream.alias("encodedPolygon", EncodedPolygonBean.class);
    xstream.alias("stopGrouping",StopGroupingBean.class);
    xstream.alias("stopGroup",StopGroupBean.class);
    xstream.alias("agency-with-coverage",AgencyWithCoverageBean.class);
    xstream.alias("trip-status",TripStatusBean.class);
    return xstream;
  }

}
