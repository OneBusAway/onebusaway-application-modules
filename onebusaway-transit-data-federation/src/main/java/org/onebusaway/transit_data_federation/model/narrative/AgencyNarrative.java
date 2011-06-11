package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Agency narrative information. Mostly just adds disclaimer information.
 * 
 * @author bdferris
 * @see Agency
 * @see NarrativeService
 */
public final class AgencyNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String disclaimer;

  /**
   * If true, indicates the agency provides private service that is not
   * available to the general public.
   */
  private final boolean privateSerivce;

  public static Builder builder() {
    return new Builder();
  }

  private AgencyNarrative(Builder builder) {
    this.disclaimer = builder.disclaimer;
    this.privateSerivce = builder.privateService;
  }

  public String getDisclaimer() {
    return disclaimer;
  }

  /**
   * If true, indicates the agency provides private service that is not
   * available to the general public.
   */
  public boolean isPrivateService() {
    return privateSerivce;
  }

  public static class Builder {

    private String disclaimer;

    private boolean privateService;

    public AgencyNarrative create() {
      return new AgencyNarrative(this);
    }

    public void setDisclaimer(String disclaimer) {
      this.disclaimer = disclaimer;
    }

    public void setPrivateService(boolean privateService) {
      this.privateService = privateService;
    }
  }
}
