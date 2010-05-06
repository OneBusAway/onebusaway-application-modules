package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.InvalidArgumentServiceException;


public class AgencyIdSupport {
  public static String getAgencyIdFromEntityId(String entityId) throws InvalidArgumentServiceException {
    int index = entityId.indexOf('_');
    if( index == -1)
      throw new InvalidArgumentServiceException("entityId","badEntityId");
    return entityId.substring(0,index);
  }
}
