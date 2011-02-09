package org.onebusaway.webapp.services;

import java.io.File;
import java.io.IOException;

import org.onebusaway.users.model.UserIndex;

public interface TccParticipantRegistrationService {

  public void register(String data) throws IOException;

  public void register(File data) throws IOException;

  public UserIndex register(TccParticipantRegistrationBean record);
}
