package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Collection;
import java.util.List;

public interface ShapeBeanService {

  public EncodedPolylineBean getPolylineForShapeId(AgencyAndId id);

  public List<EncodedPolylineBean> getMergedPolylinesForShapeIds(Collection<AgencyAndId> shapeIds);
}
