package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.CPTSubscriptionHeader;
import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.PiStopPointETA;
import org.onebusaway.tcip.model.PiStopPointETASub;
import org.onebusaway.tcip.model.TcipMessage;
import org.onebusaway.tcip.services.TcipServlet;
import org.onebusaway.tcip.services.TcipServletRequest;
import org.onebusaway.tcip.services.TcipServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PiStopPointETAServlet extends TcipServlet implements
    PISchedAdherenceCountdownListener {

  /**
   * Todo add concurrency support
   */
  private List<Connection> _activeConnections = new ArrayList<Connection>();

  /****
   * {@link TcipSerlvet} Methods
   ****/

  @Override
  public void service(TcipServletRequest request, TcipServletResponse response) {
    TcipMessage message = request.getMessage();
    if (!(message instanceof PiStopPointETASub))
      throw new IllegalStateException();
    PiStopPointETASub sub = (PiStopPointETASub) message;
    System.out.println("adding new connection");
    _activeConnections.add(new Connection(response, sub));
  }

  /****
   * {@link PISchedAdherenceCountdownListener}
   ****/

  public void handle(List<PISchedAdherenceCountdown> events) {

    for (Connection connection : _activeConnections)
      connection.handle(events);
  }

  private static class Connection implements PISchedAdherenceCountdownListener {

    private TcipServletResponse _response;

    private PiStopPointETASub _sub;

    private Set<CPTStoppointIden> _activeStops;

    public Connection(TcipServletResponse response, PiStopPointETASub sub) {
      _response = response;
      _sub = sub;
      if (sub.getStoppoints() != null)
        _activeStops = new HashSet<CPTStoppointIden>(sub.getStoppoints());
    }

    public void handle(List<PISchedAdherenceCountdown> events) {

      List<PISchedAdherenceCountdown> activeArrivalEstimates = new ArrayList<PISchedAdherenceCountdown>();

      if (_activeStops == null) {
        activeArrivalEstimates.addAll(events);
      } else {
        for (PISchedAdherenceCountdown event : events) {
          if (_activeStops.contains(event.getStoppoint()))
            activeArrivalEstimates.add(event);
        }
      }

      if (activeArrivalEstimates.isEmpty())
        return;

      PiStopPointETA message = new PiStopPointETA();

      CPTSubscriptionHeader header = new CPTSubscriptionHeader();
      header.setRequestedType(_sub.getSubscriptionInfo().getRequestedType());
      header.setExpirationDate(new Date());
      header.setExpirationTime(new Date());
      message.setSubscriptionInfo(header);

      message.setArrivalEstimates(activeArrivalEstimates);

      _response.writeMessage(message);
    }
  }
}
