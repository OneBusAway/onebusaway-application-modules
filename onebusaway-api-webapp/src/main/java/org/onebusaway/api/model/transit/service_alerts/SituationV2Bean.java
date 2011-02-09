package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.api.model.transit.HasId;

public class SituationV2Bean implements HasId, Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long creationTime;

  private TimeRangeV2Bean publicationWindow;

  private String miscellaneousReason;

  private String personnelReason;

  private String equipmentReason;

  private String environmentReason;

  private String undefinedReason;

  private NaturalLanguageStringV2Bean summary;

  private NaturalLanguageStringV2Bean description;

  private NaturalLanguageStringV2Bean detail;

  private NaturalLanguageStringV2Bean advice;

  private NaturalLanguageStringV2Bean internal;

  private SituationAffectsV2Bean affects;

  private List<SituationConsequenceV2Bean> consequences;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public TimeRangeV2Bean getPublicationWindow() {
    return publicationWindow;
  }

  public void setPublicationWindow(TimeRangeV2Bean publicationWindow) {
    this.publicationWindow = publicationWindow;
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

  public NaturalLanguageStringV2Bean getSummary() {
    return summary;
  }

  public void setSummary(NaturalLanguageStringV2Bean summary) {
    this.summary = summary;
  }

  public NaturalLanguageStringV2Bean getDescription() {
    return description;
  }

  public void setDescription(NaturalLanguageStringV2Bean description) {
    this.description = description;
  }

  public NaturalLanguageStringV2Bean getDetail() {
    return detail;
  }

  public void setDetail(NaturalLanguageStringV2Bean detail) {
    this.detail = detail;
  }

  public NaturalLanguageStringV2Bean getAdvice() {
    return advice;
  }

  public void setAdvice(NaturalLanguageStringV2Bean advice) {
    this.advice = advice;
  }

  public NaturalLanguageStringV2Bean getInternal() {
    return internal;
  }

  public void setInternal(NaturalLanguageStringV2Bean internal) {
    this.internal = internal;
  }

  public SituationAffectsV2Bean getAffects() {
    return affects;
  }

  public void setAffects(SituationAffectsV2Bean affects) {
    this.affects = affects;
  }

  public List<SituationConsequenceV2Bean> getConsequences() {
    return consequences;
  }

  public void setConsequences(List<SituationConsequenceV2Bean> consequences) {
    this.consequences = consequences;
  }
}
