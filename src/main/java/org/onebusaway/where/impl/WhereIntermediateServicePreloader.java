package org.onebusaway.where.impl;

import org.onebusaway.common.spring.PostConstruct;
import org.onebusaway.common.spring.PreDestroy;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.services.WhereIntermediateService;
import org.onebusaway.where.web.common.client.model.StopCalendarDayBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class WhereIntermediateServicePreloader {

  @Autowired
  private GtdfDao _dao;

  @Autowired
  private WhereIntermediateService _whereIntermediate;

  private Runner _runner;

  @PostConstruct
  public void start() {
    _runner = new Runner();
    Thread thread = new Thread(_runner);
    thread.start();
  }

  @PreDestroy
  public void stop() {
    if (_runner != null)
      _runner.exit();
  }

  private class Runner implements Runnable {

    private boolean _exit = false;

    public synchronized void exit() {
      _exit = true;
    }

    public void run() {

      try {
        preCacheRoutes();

        if (wantsExit())
          return;

        preCacheStops();

        if (wantsExit())
          return;

        System.out.println(WhereIntermediateServicePreloader.class.getName()
            + " - complete");
      } catch (ServiceException e) {
        e.printStackTrace();
      }
    }

    private synchronized boolean wantsExit() {
      return _exit;
    }

    private void preCacheRoutes() throws ServiceException {
      List<Route> routes = _dao.getAllRoutes();

      int routeIndex = 0;

      for (Route route : routes) {

        System.out.println("pre-cache: route=" + (routeIndex++) + "/"
            + routes.size());

        try {
          _whereIntermediate.getServicePatternBlocksByRoute(route.getShortName());
        } catch (ServiceException e) {
          e.printStackTrace();
        }

        if (wantsExit())
          return;

        _whereIntermediate.getStopSelectionTreeForRoute(route.getShortName());

        if (wantsExit())
          return;
      }
    }

    public void preCacheStops() throws ServiceException {

      List<Stop> stops = _dao.getAllStops();

      for (Stop stop : stops) {

        _whereIntermediate.getStop(stop.getId());

        if (wantsExit())
          return;

        List<StopCalendarDayBean> calendars = _whereIntermediate.getCalendarForStop(stop.getId());

        if (wantsExit())
          return;
        for (StopCalendarDayBean bean : calendars) {
          _whereIntermediate.getScheduledArrivalsForStopAndDate(stop.getId(),
              bean.getDate());
          if (wantsExit())
            return;
        }
      }
    }
  }

}
