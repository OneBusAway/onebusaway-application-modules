package org.onebusaway.transit_data_federation.services.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public interface ServiceAlertsBeanService {

  public SituationBean createServiceAlert(String agencyId,
      SituationBean situation);

  public void updateServiceAlert(SituationBean situation);

  public void removeServiceAlert(AgencyAndId situationId);

  public SituationBean getServiceAlertForId(AgencyAndId situationId);

  public List<SituationBean> getAllSituationsForAgencyId(String agencyId);

  public void removeAllSituationsForAgencyId(String agencyId);

  public List<SituationBean> getSituationsForStopId(long time,
      AgencyAndId stopId);

  public List<SituationBean> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId);

  public List<SituationBean> getSituationsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId);

}
