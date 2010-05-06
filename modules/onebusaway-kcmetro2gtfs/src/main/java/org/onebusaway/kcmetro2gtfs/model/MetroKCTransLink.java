package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

@CsvFields(filename = "trans_link.csv")
public class MetroKCTransLink extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private Integer id;
  private double linkLen;
  private int transNodeFrom;
  private int transNodeTo;
  private int streetNameId;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public double getLinkLen() {
    return linkLen;
  }

  public void setLinkLen(double linkLen) {
    this.linkLen = linkLen;
  }

  public int getTransNodeFrom() {
    return transNodeFrom;
  }

  public void setTransNodeFrom(int transNodeFrom) {
    this.transNodeFrom = transNodeFrom;
  }

  public int getTransNodeTo() {
    return transNodeTo;
  }

  public void setTransNodeTo(int transNodeTo) {
    this.transNodeTo = transNodeTo;
  }

  public int getStreetNameId() {
    return streetNameId;
  }

  public void setStreetNameId(int streetNameId) {
    this.streetNameId = streetNameId;
  }
}
