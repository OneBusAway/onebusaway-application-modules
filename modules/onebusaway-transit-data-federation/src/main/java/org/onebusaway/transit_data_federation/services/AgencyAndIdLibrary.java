package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;

public class AgencyAndIdLibrary {

  public static final char ID_SEPARATOR = '_';

  public static AgencyAndId convertFromString(String value) {
    int index = value.indexOf(ID_SEPARATOR);
    if (index == -1) {
      throw new IllegalStateException("invalid agency-and-id: " + value);
    } else {
      return new AgencyAndId(value.substring(0, index), value.substring(index + 1));
    }
  }

  public static String convertToString(AgencyAndId aid) {
    return aid.getAgencyId() + ID_SEPARATOR + aid.getId();
  }
}
