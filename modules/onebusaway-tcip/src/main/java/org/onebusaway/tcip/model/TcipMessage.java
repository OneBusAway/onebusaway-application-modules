package org.onebusaway.tcip.model;

import org.onebusaway.tcip.impl.DateTimeConverter;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import java.util.Date;

public class TcipMessage {

  @XStreamAsAttribute
  @XStreamConverter(value = DateTimeConverter.class)
  private Date created;

  @XStreamAsAttribute
  private String schVersion;

  @XStreamAsAttribute
  private String sourceapp;

  @XStreamAsAttribute
  private String sourceip;

  @XStreamAsAttribute
  private int sourceport;

  @XStreamAsAttribute
  private String noNameSpaceSchemaLocation;

  @XStreamAsAttribute
  @XStreamConverter(value = DateTimeConverter.class)
  private Date activation;

  @XStreamAsAttribute
  @XStreamConverter(value = DateTimeConverter.class)
  private Date deactivation;

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getSchVersion() {
    return schVersion;
  }

  public void setSchVersion(String schVersion) {
    this.schVersion = schVersion;
  }

  public String getSourceapp() {
    return sourceapp;
  }

  public void setSourceapp(String sourceapp) {
    this.sourceapp = sourceapp;
  }

  public String getSourceip() {
    return sourceip;
  }

  public void setSourceip(String sourceip) {
    this.sourceip = sourceip;
  }

  public int getSourceport() {
    return sourceport;
  }

  public void setSourceport(int sourceport) {
    this.sourceport = sourceport;
  }

  public String getNoNameSpaceSchemaLocation() {
    return noNameSpaceSchemaLocation;
  }

  public void setNoNameSpaceSchemaLocation(String noNameSpaceSchemaLocation) {
    this.noNameSpaceSchemaLocation = noNameSpaceSchemaLocation;
  }

  public Date getActivation() {
    return activation;
  }

  public void setActivation(Date activation) {
    this.activation = activation;
  }

  public Date getDeactivation() {
    return deactivation;
  }

  public void setDeactivation(Date deactivation) {
    this.deactivation = deactivation;
  }

}
