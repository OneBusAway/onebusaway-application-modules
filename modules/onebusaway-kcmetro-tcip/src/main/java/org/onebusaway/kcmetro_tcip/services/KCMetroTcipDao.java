package org.onebusaway.kcmetro_tcip.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;

import java.util.List;

public interface KCMetroTcipDao {
  public List<TimepointToStopMapping> getTimepointToStopMappingsForTrackerTripId(AgencyAndId trackerTripId);
}
