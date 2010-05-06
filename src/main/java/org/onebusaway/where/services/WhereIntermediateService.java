package org.onebusaway.where.services;

import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchStopServiceException;

import java.util.Date;
import java.util.List;

public interface WhereIntermediateService {

  public StopBean getStop(String stopId) throws ServiceException;

  public StopBean getStopWithNearbyStopRadius(String stopId, double nearbyStopSearchDistance) throws ServiceException;

  public NearbyRoutesBean getNearbyRoutes(String stopId, double nearbyRouteSearchDistance) throws NoSuchStopServiceException;

  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(String stopId, Date date)
      throws ServiceException;

  public List<StopCalendarDayBean> getCalendarForStop(String stopId) throws ServiceException;

  public StopSelectionTree getStopSelectionTreeForRoute(String route) throws ServiceException;

  public List<DepartureBean> getDeparturesForStop(String stopId) throws ServiceException;

  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String route) throws ServiceException;

}
