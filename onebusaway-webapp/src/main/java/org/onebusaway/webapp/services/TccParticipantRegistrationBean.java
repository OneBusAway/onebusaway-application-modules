package org.onebusaway.webapp.services;

import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "participant_registration")
public class TccParticipantRegistrationBean {

  private String participantId;

  private String secret;

  private String phoneNumber;

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }
}
