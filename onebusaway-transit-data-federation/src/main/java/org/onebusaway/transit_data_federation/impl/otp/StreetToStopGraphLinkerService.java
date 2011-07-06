package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingEndsAtStopEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkToStopVertex;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.edgetype.loader.NetworkLinkerLibrary;
import org.opentripplanner.routing.services.GraphRefreshListener;
import org.opentripplanner.routing.services.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreetToStopGraphLinkerService implements GraphRefreshListener {

  private static Logger _log = LoggerFactory.getLogger(StreetToStopGraphLinkerService.class);

  private TransitGraphDao _transitGraphDao;

  private GraphService _graphService;

  private OTPConfigurationService _otpConfigurationService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setGraphService(GraphService graphService) {
    _graphService = graphService;
  }

  @Autowired
  public void setOtpConfigurationService(
      OTPConfigurationService otpConfigurationService) {
    _otpConfigurationService = otpConfigurationService;
  }

  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void handleTransitGraphRefresh() {
    /**
     * If the transit graph has been refreshed, we've potentially already run a
     * linker step, so it's easier just to reload the OTP graph and relink
     */
    _graphService.refreshGraph();
  }

  @Override
  public void handleGraphRefresh(GraphService graphService) {

    Graph graph = _graphService.getGraph();
    GraphContext context = _otpConfigurationService.createGraphContext();

    NetworkLinkerLibrary linker = new NetworkLinkerLibrary(graph);

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
        GraphVertex gv = graph.getGraphVertex(walkToStopVertex.getLabel());
        WaitingBeginsAtStopEdge edge = new WaitingBeginsAtStopEdge(context,
            stop, false);
        edge.setFromVertex(walkToStopVertex);
        gv.addOutgoing(edge);
      } else {
        _log.warn("error linking stop: " + stop.getId() + " to street network");
      }

      /****
       * Add stop-to-street edges
       ****/

      WalkFromStopVertex walkFromStopVertex = new WalkFromStopVertex(context,
          stop);

      if (linker.determineOutgoingEdgesForVertex(walkFromStopVertex, true)) {
        GraphVertex gv = graph.getGraphVertex(walkFromStopVertex.getLabel());
        WaitingEndsAtStopEdge edge = new WaitingEndsAtStopEdge(context, stop,
            true);
        edge.setToVertex(walkFromStopVertex);
        gv.addIncoming(edge);
      } else {
        _log.warn("error linking stop: " + stop.getId() + " to street network");
      }
    }

    linker.addAllReplacementEdgesToGraph();
  }

}
