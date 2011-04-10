package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.ItineraryV2BeanFactory;
import org.onebusaway.api.model.transit.tripplanning.ItinerariesV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItineraryV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.StreetLegV2Bean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@AddToStack("constraints")
public class PlanTripController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private double _latFrom;

  private double _lonFrom;

  private double _latTo;

  private double _lonTo;

  private long _time;

  private long _currentTime;
  
  private String _includeSpecificItinerary;

  private ConstraintsBean _constraints = new ConstraintsBean();

  public PlanTripController() {
    super(V2);
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setLatFrom(double latFrom) {
    _latFrom = latFrom;
  }

  public void setLonFrom(double lonFrom) {
    _lonFrom = lonFrom;
  }

  public void setLatTo(double latTo) {
    _latTo = latTo;
  }

  public void setLonTo(double lonTo) {
    _lonTo = lonTo;
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public void setDateAndTime(String value) throws ParseException {
    SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    Date time = f.parse(value);
    setTime(time);
  }
  
  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setCurrentTime(Date time) {
    _currentTime = time.getTime();
  }


  public void setIncludeSpecificItinerary(String includeSpecificItinerary) {
    _includeSpecificItinerary = includeSpecificItinerary;
  }
  
  public String getIncludeSpecificItinerary() {
    return _includeSpecificItinerary;
  }

  public void setConstraints(ConstraintsBean constraints) {
    _constraints = constraints;
  }

  public ConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setMode(List<String> modes) {
    _constraints.setModes(new HashSet<String>(modes));
  }
  
  public DefaultHttpHeaders create() throws IOException, ServiceException {
    return index();
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (_time == 0)
      _time = System.currentTimeMillis();
    if( _currentTime == 0)
      _currentTime = System.currentTimeMillis();

    CoordinatePoint from = new CoordinatePoint(_latFrom, _lonFrom);
    CoordinatePoint to = new CoordinatePoint(_latTo, _lonTo);

    BeanFactoryV2 factory = getBeanFactoryV2();
    ItineraryV2BeanFactory itineraryFactory = new ItineraryV2BeanFactory(
        factory);

    parseAdditionalItinerary(itineraryFactory);

    ItinerariesBean itineraries = _transitDataService.getItinerariesBetween(
        from, to, _time, _currentTime, _constraints);

    ItinerariesV2Bean bean = itineraryFactory.getItineraries(itineraries);
    return setOkResponse(factory.entry(bean));
  }

  private void parseAdditionalItinerary(ItineraryV2BeanFactory itineraryFactory) {

    if (_includeSpecificItinerary == null || _includeSpecificItinerary.isEmpty())
      return;

    ItineraryV2Bean bean = new ItineraryV2Bean();
    JSONObject jsonObject = JSONObject.fromObject(_includeSpecificItinerary);
    
    JsonConfig config = new JsonConfig();
    
    Map<Object,Object> classMap = new HashMap<Object, Object>();
    classMap.put("legs", LegV2Bean.class);
    classMap.put("streetLegs", StreetLegV2Bean.class);
    classMap.put("situationIds", String.class);
    config.setClassMap(classMap);
    
    JSONObject.toBean(jsonObject, bean, config);

    ItineraryBean itinerary = itineraryFactory.reverseItinerary(bean);
    _constraints.setIncludeItinerary(itinerary);
  }
}
