package org.onebusaway.gtfs_diff.impl.serialization;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.model.ServiceId;

public class GtsDifferencesXStreamSourceImpl extends XStreamSourceImpl {

  private GtfsDifferences _differences;

  public GtsDifferencesXStreamSourceImpl(GtfsDifferences differences) {
    _differences = differences;
  }

  @Override
  protected String value(Object entity) {

    if (entity == null)
      return super.value(entity);

    if (entity instanceof Agency) {
      Agency agency = (Agency) entity;
      return "agency " + id(agency.getId());
    } else if (entity instanceof Route) {
      Route route = (Route) entity;
      return "route " + id(route.getId()) + " " + route.getShortName();
    } else if (entity instanceof Trip) {
      Trip trip = (Trip) entity;
      return "trip " + id(trip.getId());
    } else if (entity instanceof Stop) {
      Stop stop = (Stop) entity;
      return "stop " + id(stop.getId());
    } else if (entity instanceof ServiceId) {
      ServiceId serviceId = (ServiceId) entity;
      return "serviceId " + id(serviceId.getServiceId());
    } else if (entity instanceof AgencyAndId) {
      AgencyAndId id = id((AgencyAndId) entity);
      return id.toString();
    }

    return super.value(entity);
  }

  protected AgencyAndId id(AgencyAndId id) {
    String aid = id(id.getAgencyId());
    return new AgencyAndId(aid, id.getId());
  }

  protected String id(String id) {
    if (id.startsWith(_differences.getModelIdA()))
      id = id.substring(_differences.getModelIdA().length());
    if (id.startsWith(_differences.getModelIdB()))
      id = id.substring(_differences.getModelIdB().length());
    return id;
  }
}
