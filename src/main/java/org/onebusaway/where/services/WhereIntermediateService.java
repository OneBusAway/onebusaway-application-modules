package org.onebusaway.where.services;

import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;

import java.util.Date;
import java.util.List;

public interface WhereIntermediateService {

  public StopBean getStop(String stopId) throws ServiceException;

  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      String stopId, Date date) throws ServiceException;
  
  public List<StopCalendarDayBean> getCalendarForStop(String stopId) throws ServiceException;

  public StopSelectionTree getStopSelectionTreeForRoute(String route)
      throws ServiceException;

  public List<DepartureBean> getDeparturesForStop(String stopId)
      throws ServiceException;

  public List<StopSequenceBlockBean> getServicePatternBlocksByRoute(String route)
      throws ServiceException;
}
