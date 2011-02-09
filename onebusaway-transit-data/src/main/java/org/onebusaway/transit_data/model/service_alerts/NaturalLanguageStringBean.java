package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class NaturalLanguageStringBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String value;

  private String lang;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

}
