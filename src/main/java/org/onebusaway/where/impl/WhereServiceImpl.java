package org.onebusaway.where.impl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.model.SelectionName;
import org.onebusaway.where.model.StopSelectionTree;
import org.onebusaway.where.services.WhereIntermediateService;
import org.onebusaway.where.web.actions.GeocoderAccuracyToBounds;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.NameBean;
import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.model.StopRouteScheduleBean;
import org.onebusaway.where.web.common.client.model.StopScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.rpc.InvalidSelectionServiceException;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
class WhereServiceImpl implements WhereService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private GtdfDao _gtdfDao;

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private WhereIntermediateService _whereIntermediate;

  /***************************************************************************
   * {@link WhereService} Interface
   **************************************************************************/

  public StopsBean getStopsByLocationAndAccuracy(double lat, double lon,
      int accuracy) {
    int r = GeocoderAccuracyToBounds.getBoundsInFeetByAccuracy(accuracy);
    Point p = _projection.getLatLonAsPoint(lat, lon);
    Geometry boundary = p.buffer(r).getEnvelope();
    return getStopsByGeometry(boundary);
  }

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2) throws ServiceException {
    Point p1 = _projection.getLatLonAsPoint(lat1, lon1);
    Point p2 = _projection.getLatLonAsPoint(lat2, lon2);
    Geometry g = p1.union(p2);
    g = g.getEnvelope();
    return getStopsByGeometry(g);
  }

  public StopBean getStop(String stopId) throws ServiceException {
    return _whereIntermediate.getStop(stopId);
  }

  public StopWithArrivalsBean getArrivalsByStopId(String stopId)
      throws ServiceException {

    StopBean stopBean = _whereIntermediate.getStop(stopId);
    List<DepartureBean> departures = _whereIntermediate.getDeparturesForStop(stopId);
    long now = System.currentTimeMillis();
    long from = now - 5 * 60 * 1000;
    long to = now + 35 * 60 * 1000;
    List<DepartureBean> filtered = new ArrayList<DepartureBean>();
    for (DepartureBean bean : departures) {
      long time = bean.getBestTime();
      if (from <= time && time <= to)
        filtered.add(bean);
    }
    StopWithArrivalsBean arrivalsBean = new StopWithArrivalsBean(stopBean,
        filtered);

    return arrivalsBean;
  }

  public NameTreeBean getStopByRoute(String route, List<Integer> selection)
      throws ServiceException {
    NameTreeBean bean = new NameTreeBean();
    StopSelectionTree tree = _whereIntermediate.getStopSelectionTreeForRoute(route);
    visitTree(tree, bean, selection, 0);
    return bean;
  }

  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String route)
      throws ServiceException {
    return _whereIntermediate.getServicePatternBlocksByRoute(route);
  }

  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {

    StopScheduleBean bean = new StopScheduleBean();
    bean.setDate(date);
    
    StopBean stopBean = _whereIntermediate.getStop(stopId);
    bean.setStop(stopBean);

    List<StopRouteScheduleBean> routes = _whereIntermediate.getScheduledArrivalsForStopAndDate(
        stopId, date);
    bean.setRoutes(routes);

    List<StopCalendarDayBean> calendarDays = _whereIntermediate.getCalendarForStop(stopId);
    bean.setCalendarDays(calendarDays);

    return bean;
  }

  /***************************************************************************
   * 
   **************************************************************************/

  private StopsBean getStopsByGeometry(Geometry g) {

    List<Stop> stops = _gtdfDao.getStopsByLocation(g);

    List<StopBean> stopBeans = new ArrayList<StopBean>();

    for (Stop stop : stops) {
      StopBean sb = ApplicationBeanLibrary.getStopAsBean(stop);
      stopBeans.add(sb);
    }

    StopsBean stopsBean = new StopsBean();
    stopsBean.setStopBeans(stopBeans);
    return stopsBean;
  }

  /***************************************************************************
   * Tree Methods
   **************************************************************************/

  private void visitTree(StopSelectionTree tree, NameTreeBean bean,
      List<Integer> selection, int index)
      throws InvalidSelectionServiceException {

    // If we have a stop, we have no choice but to return
    if (tree.hasStop()) {
      bean.setStop(ApplicationBeanLibrary.getStopAsBean(tree.getStop()));
      return;
    }

    Set<SelectionName> names = tree.getNames();

    // If we've only got one name, short circuit
    if (names.size() == 1) {

      SelectionName next = names.iterator().next();
      bean.addSelected(ApplicationBeanLibrary.getNameAsBean(next));

      StopSelectionTree subtree = tree.getSubTree(next);
      visitTree(subtree, bean, selection, index);

      return;
    }

    if (index >= selection.size()) {

      for (SelectionName name : names) {
        NameBean n = ApplicationBeanLibrary.getNameAsBean(name);
        Stop stop = getStop(tree.getSubTree(name));
        if (stop != null) {
          bean.addNameWithStop(n, ApplicationBeanLibrary.getStopAsBean(stop));
        } else {
          bean.addName(n);
        }
      }

      List<Stop> stops = tree.getAllStops();

      for (Stop stop : stops)
        bean.addStop(ApplicationBeanLibrary.getStopAsBean(stop));

      return;

    } else {

      int i = 0;
      int selectionIndex = selection.get(index);

      for (SelectionName name : names) {
        if (selectionIndex == i) {
          bean.addSelected(ApplicationBeanLibrary.getNameAsBean(name));
          tree = tree.getSubTree(name);
          visitTree(tree, bean, selection, index + 1);
          return;
        }
        i++;
      }
    }

    // If we made it here...
    throw new InvalidSelectionServiceException();
  }

  private Stop getStop(StopSelectionTree tree) {

    if (tree.hasStop())
      return tree.getStop();

    if (tree.getNames().size() == 1) {
      SelectionName next = tree.getNames().iterator().next();
      return getStop(tree.getSubTree(next));
    }

    return null;
  }

}
