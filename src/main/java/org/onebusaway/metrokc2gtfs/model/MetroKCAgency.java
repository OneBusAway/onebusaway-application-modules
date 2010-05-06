package org.onebusaway.metrokc2gtfs.model;

import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "agency.csv")
public class MetroKCAgency {

  private String name;
  private String url;
  private String timezone;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

}
