package org.onebusaway.geocoder.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

@Entity
@Table(name = "oba_geocoder_results")
@AccessType("field")
public class GeocoderResultsEntity {

  @Id
  private String location;

  @Lob
  private GeocoderResults results;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public GeocoderResults getResults() {
    return results;
  }

  public void setResults(GeocoderResults result) {
    this.results = result;
  }
}
