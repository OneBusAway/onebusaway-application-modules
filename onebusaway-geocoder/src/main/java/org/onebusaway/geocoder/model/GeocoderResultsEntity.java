package org.onebusaway.geocoder.model;

import org.hibernate.annotations.AccessType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oba_geocoder_results")
@AccessType("field")
public class GeocoderResultsEntity {

  @Id
  private String location;

  @Column(columnDefinition="BLOB")
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
