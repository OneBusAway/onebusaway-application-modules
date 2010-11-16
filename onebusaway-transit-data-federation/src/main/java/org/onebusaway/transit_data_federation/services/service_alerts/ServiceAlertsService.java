package org.onebusaway.transit_data_federation.services.service_alerts;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public interface ServiceAlertsService {

  public Situation createServiceAlert(String agencyId, Situation situation);

  public void updateServiceAlert(Situation situation);

  public void removeServiceAlert(AgencyAndId situationId);

  public Situation getServiceAlertForId(AgencyAndId situationId);

  public List<Situation> getAllSituationsForAgencyId(String agencyId);

  public List<Situation> getSituationsForStopId(long time, AgencyAndId stopId);

  public List<Situation> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId);

  public List<Situation> getSituationsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId);
}
