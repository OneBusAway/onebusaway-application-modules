package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public abstract class AbstractSituationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long creationTime;

  private long situationId;

  private String miscellaneousReason;

  private String personnelReason;

  private String equipmentReason;

  private String environmentReason;

  private String undefinedReason;

  private DefaultedTextBean summary;

  private DefaultedTextBean description;

  private DefaultedTextBean detail;

  private DefaultedTextBean advice;

  private DefaultedTextBean internal;

  private SituationAffectsBean affects;

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public long getSituationId() {
    return situationId;
  }

  public void setSituationId(long situationId) {
    this.situationId = situationId;
  }

  public String getMiscellaneousReason() {
    return miscellaneousReason;
  }

  public void setMiscellaneousReason(String miscellaneousReason) {
    this.miscellaneousReason = miscellaneousReason;
  }

  public String getPersonnelReason() {
    return personnelReason;
  }

  public void setPersonnelReason(String personnelReason) {
    this.personnelReason = personnelReason;
  }

  public String getEquipmentReason() {
    return equipmentReason;
  }

  public void setEquipmentReason(String equipmentReason) {
    this.equipmentReason = equipmentReason;
  }

  public String getEnvironmentReason() {
    return environmentReason;
  }

  public void setEnvironmentReason(String environmentReason) {
    this.environmentReason = environmentReason;
  }

  public String getUndefinedReason() {
    return undefinedReason;
  }

  public void setUndefinedReason(String undefinedReason) {
    this.undefinedReason = undefinedReason;
  }

  public DefaultedTextBean getSummary() {
    return summary;
  }

  public void setSummary(DefaultedTextBean summary) {
    this.summary = summary;
  }

  public DefaultedTextBean getDescription() {
    return description;
  }

  public void setDescription(DefaultedTextBean description) {
    this.description = description;
  }

  public DefaultedTextBean getDetail() {
    return detail;
  }

  public void setDetail(DefaultedTextBean detail) {
    this.detail = detail;
  }

  public DefaultedTextBean getAdvice() {
    return advice;
  }

  public void setAdvice(DefaultedTextBean advice) {
    this.advice = advice;
  }

  public DefaultedTextBean getInternal() {
    return internal;
  }

  public void setInternal(DefaultedTextBean internal) {
    this.internal = internal;
  }

  public SituationAffectsBean getAffects() {
    return affects;
  }

  public void setAffects(SituationAffectsBean affects) {
    this.affects = affects;
  }

}
