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

  public static Builder builder() {
    return new Builder();
  }

  private AgencyNarrative(Builder builder) {
    this.disclaimer = builder.disclaimer;
  }

  public String getDisclaimer() {
    return disclaimer;
  }

  public static class Builder {

    private String disclaimer;

    public AgencyNarrative create() {
      return new AgencyNarrative(this);
    }

    public void setDisclaimer(String disclaimer) {
      this.disclaimer = disclaimer;
    }
  }
}
