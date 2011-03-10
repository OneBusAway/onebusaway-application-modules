package org.onebusaway.transit_data.model.service_alerts;

public class DefaultedTextBean extends NaturalLanguageStringBean {

  private static final long serialVersionUID = 1L;

  private Boolean overridden;

  public Boolean getOverridden() {
    return overridden;
  }

  public void setOverridden(Boolean overridden) {
    this.overridden = overridden;
  }
}
