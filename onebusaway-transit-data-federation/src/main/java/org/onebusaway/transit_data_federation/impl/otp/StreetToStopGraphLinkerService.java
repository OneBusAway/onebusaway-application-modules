package org.onebusaway.transit_data_federation.impl.otp;

import javax.annotation.PostConstruct;

import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingEndsAtStopEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkToStopVertex;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.edgetype.loader.NetworkLinkerLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class StreetToStopGraphLinkerService {

  private static Logger _log = LoggerFactory.getLogger(StreetToStopGraphLinkerService.class);

  private TransitGraphDao _transitGraphDao;

  private Graph _otpGraph;

  private StopTransferService _stopTransferService;

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setOtpGraph(Graph otpGraph) {
    _otpGraph = otpGraph;
  }

  @Autowired
  public void setStopTranferService(StopTransferService stopTransferService) {
    _stopTransferService = stopTransferService;
  }

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService realTimeArrivalAndDepartureService) {
    _arrivalAndDepartureService = realTimeArrivalAndDepartureService;
  }

  @PostConstruct
  public void setup() {

    GraphContext context = createContext();
    NetworkLinkerLibrary linker = new NetworkLinkerLibrary(_otpGraph);

    int index = 0;

    for (StopEntry stop : _transitGraphDao.getAllStops()) {

      if (index % 100 == 0)
        _log.info("linked stops: " + index);
      index++;

      /***
       * Add street-to-stop edges
       ****/

      WalkToStopVertex walkToStopVertex = new WalkToStopVertex(context, stop);

      if (linker.determineIncomingEdgesForVertex(walkToStopVertex, true)) {
        GraphVertex gv = _otpGraph.getGraphVertex(walkToStopVertex.getLabel());
        gv.addOutgoing(new WaitingBeginsAtStopEdge(context, stop, false));
      } else {
        _log.warn("error linking stop: " + stop.getId() + " to street network");
      }

      /****
       * Add stop-to-street edges
       ****/

      WalkFromStopVertex walkFromStopVertex = new WalkFromStopVertex(context,
          stop);

      if (linker.determineOutgoingEdgesForVertex(walkFromStopVertex, true)) {
        GraphVertex gv = _otpGraph.getGraphVertex(walkFromStopVertex.getLabel());
        gv.addIncoming(new WaitingEndsAtStopEdge(context, stop, true));
      } else {
        _log.warn("error linking stop: " + stop.getId() + " to street network");
      }
    }

    linker.addAllReplacementEdgesToGraph();
  }

  /****
   * Private Methods
   ****/

  private GraphContext createContext() {
    GraphContext context = new GraphContext();
    context.setArrivalAndDepartureService(_arrivalAndDepartureService);
    context.setStopTransferService(_stopTransferService);
    return context;
  }

}
