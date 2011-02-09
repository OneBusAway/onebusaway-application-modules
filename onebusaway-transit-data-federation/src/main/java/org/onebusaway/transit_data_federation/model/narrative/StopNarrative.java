package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;

/**
 * Stop narrative information. Really only includes the direction of travel /
 * orientation a stop relative to the street.
 * 
 * @author bdferris
 * @see Stop
 * @see NarrativeService
 */
public final class StopNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String direction;

  public static Builder builder() {
    return new Builder();
  }

  private StopNarrative(Builder builder) {
    this.direction = builder.direction;
  }

  public String getDireciton() {
    return direction;
  }

  public static class Builder {
    private String direction;

    public StopNarrative create() {
      return new StopNarrative(this);
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }
  }

}
