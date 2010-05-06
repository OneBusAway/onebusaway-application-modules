package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.container.model.upgradeable.Upgradeable;

public final class AgencyNarrative implements Serializable, Upgradeable {

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
