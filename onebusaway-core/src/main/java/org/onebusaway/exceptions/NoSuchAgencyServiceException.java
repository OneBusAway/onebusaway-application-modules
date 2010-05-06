package org.onebusaway.exceptions;

public class NoSuchAgencyServiceException extends ServiceAreaServiceException {

  private static final long serialVersionUID = 1L;

  private String _agencyId;
  
  public NoSuchAgencyServiceException(String agencyId) {
    super("No such agency: " + agencyId);
    _agencyId = agencyId;
  }
  
  public String getAgencyId() {
    return _agencyId;
  }
}
