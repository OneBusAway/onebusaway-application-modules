package org.onebusaway.integration.api_webapp;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.caucho.hessian.client.HessianProxyFactory;

public class GoMain {

  private static DateFormat _format = new SimpleDateFormat(
      "yyyy_MM_dd-HH_mm_ss");

  public static void main(String[] args) throws Exception {

    // String serverName = "soak-api.onebusaway.org";
    String serverName = "http://localhost:9090/onebusaway-transit-data-federation-webapp";
    // String serverName = "aarhus:8080/onebusaway-api-webapp";

    if (args.length == 1)
      serverName = args[0];

    GoMain m = new GoMain();
    m.setServerName(serverName);

    m.run();

  }

  private String _serverName;

  public void setServerName(String serverName) {
    _serverName = serverName;
  }

  public void run() throws Exception {

    HessianProxyFactory factory = new HessianProxyFactory();
    TransitDataService service = (TransitDataService) factory.create(
        TransitDataService.class, _serverName
            + "/remoting/transit-data-service");

    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    query.setBounds(new CoordinateBounds(47.596737878383564,
        -122.38232162369592, 47.66418689295867, -122.24887344466346));

    while (true) {

      Date t = new Date();
      query.setTime(t.getTime());

      ListBean<TripDetailsBean> result = service.getTripsForBounds(query);

      String path = "/tmp/logs/" + _format.format(t) + ".csv";

      PrintWriter out = new PrintWriter(new FileWriter(path));

      for (TripDetailsBean bean : result.getList()) {
        TripStatusBean status = bean.getStatus();
        CoordinatePoint position = status.getPosition();
        out.println(bean.getTripId() + "," + position.getLat() + ","
            + position.getLon() + "," + status.isPredicted() + ","
            + status.getScheduleDeviation());
      }

      out.close();

      System.out.println(path + " " + result.getList().size());

      Thread.sleep(30 * 1000);

    }
  }
}
