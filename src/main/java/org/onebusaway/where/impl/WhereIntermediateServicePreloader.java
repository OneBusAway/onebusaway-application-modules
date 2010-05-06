package org.onebusaway.where.impl;

import org.onebusaway.common.impl.multi.MultiContext;
import org.onebusaway.common.impl.multi.MultiOperation;
import org.onebusaway.common.impl.multi.MultiRunner;
import org.onebusaway.common.spring.PostConstruct;
import org.onebusaway.common.spring.PreDestroy;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.services.WhereIntermediateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhereIntermediateServicePreloader {

  @Autowired
  private GtfsDao _dao;

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

  private class RouteOperation implements MultiOperation<Route> {

    public void evaluate(MultiContext<Route> context, Route route) {

      try {
        System.out.println("route=" + route);
        _whereIntermediate.getStopSequenceBlocksByRoute(route.getShortName());
        if (context.wantsExit())
          return;
        _whereIntermediate.getStopSelectionTreeForRoute(route.getShortName());
      } catch (ServiceException e) {
        e.printStackTrace();
      }
    }
  }

  private class Runner implements Runnable {

    private MultiRunner<Route> _routeRunner;

    public void exit() {
      if (_routeRunner != null) {
        _routeRunner.doExit();
        _routeRunner.waitForExit();
      }
    }

    public void run() {
      List<Route> routes = _dao.getAllRoutes();
      _routeRunner = new MultiRunner<Route>(1, new RouteOperation(), routes);
      _routeRunner.start();
    }
  }

}
