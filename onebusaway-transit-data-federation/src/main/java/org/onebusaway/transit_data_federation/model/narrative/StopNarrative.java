package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

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
