package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.InvalidArgumentServiceException;

/**
 * Support parsing out the agencyId from an "agencyId_entityId" string
 * representation.
 * 
 * @author bdferris
 */
public class AgencyIdSupport {
  /**
   * @param entityId
   * @return the agencyId from an "agencyId_entityId" string representation
   * @throws InvalidArgumentServiceException on a parse error
   */
  public static String getAgencyIdFromEntityId(String entityId)
      throws InvalidArgumentServiceException {
    int index = entityId.indexOf('_');
    if (index == -1)
      throw new InvalidArgumentServiceException("entityId", "badEntityId");
    return entityId.substring(0, index);
  }
}
