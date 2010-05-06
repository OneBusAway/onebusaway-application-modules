package org.onebusaway.transit_data_federation.impl.federated;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransitDataServicePerformanceRunner {
  public static void main(String[] args) {
    ApplicationContext context = ContainerLibrary.createContext(args);
    TransitDataService service = ContainerLibrary.getBeanOfType(context,
        TransitDataService.class);
    StopsForRouteBean stopsForRoute = service.getStopsForRoute("1_44");
    List<StopBean> stops = new ArrayList<StopBean>(stopsForRoute.getStops());
    Collections.shuffle(stops);
    int index = 0;
    for (int i = 0; i < 100; i++) {
      for (StopBean stop : stops) {
        CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
            stop.getLat(), stop.getLon(), 1000);
        ListBean<TripStatusBean> trips = service.getTripsForBounds(bounds,
            System.currentTimeMillis());
        System.out.println("i=" + i + " index=" + index + "/" + stops.size() + " results="
            + trips.getList().size());
        index++;
      }
    }

    System.exit(0);
  }
}
