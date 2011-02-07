package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.ItineraryV2BeanFactory;
import org.onebusaway.api.model.transit.tripplanning.GraphResultV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItinerariesV2Bean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class StreetGraphForRegionController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  private TransitDataService _transitDataService;

  private double _latFrom;

  private double _lonFrom;

  private double _latTo;

  private double _lonTo;

  public StreetGraphForRegionController() {
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

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    ListBean<VertexBean> vertices = _transitDataService.getStreetGraphForRegion(
        _latFrom, _lonFrom, _latTo, _lonTo);

    BeanFactoryV2 factory = getBeanFactoryV2();
    ItineraryV2BeanFactory itineraryFactory = new ItineraryV2BeanFactory(
        factory);

    GraphResultV2Bean bean = itineraryFactory.getGraphResult(vertices.getList());
    return setOkResponse(factory.entry(bean));
  }

}
