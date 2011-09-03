/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.users.impl.PhoneNumberLibrary;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.services.TccParticipantRegistrationBean;
import org.onebusaway.webapp.services.TccParticipantRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TccParticipantRegistrationServiceImpl implements
    TccParticipantRegistrationService {

  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Override
  public void register(String data) throws IOException {
    register(new ByteArrayInputStream(data.getBytes("UTF-8")));
  }

  @Override
  public void register(File data) throws IOException {
    register(new FileInputStream(data));
  }

  @Override
  public UserIndex register(TccParticipantRegistrationBean record) {

    String phoneNumber = record.getPhoneNumber();
    if (phoneNumber == null || phoneNumber.trim().length() == 0)
      return null;
    phoneNumber = PhoneNumberLibrary.normalizePhoneNumber(phoneNumber);

    UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER,
        phoneNumber);
    UserIndex existingUserIndex = _userService.getOrCreateUserForIndexKey(key,
        "", false);
    User existingUser = existingUserIndex.getUser();

    String id = key.getType() + "|" + key.getValue();
    UserIndexKey studyKey = new UserIndexKey("tccStudyId", id);

    return _userService.addUserIndexToUser(existingUser, studyKey,
        record.getSecret());
  }

  /****
   * Private Methods
   ****/

  private void register(InputStream in) throws IOException {
    CsvEntityReader reader = getReader();
    reader.addEntityHandler(new EntityHandlerImpl());
    reader.readEntities(TccParticipantRegistrationBean.class, in);
  }

  private CsvEntityReader getReader() {
    CsvEntityReader reader = new CsvEntityReader();
    AnnotationDrivenEntitySchemaFactory factory = new AnnotationDrivenEntitySchemaFactory();
    factory.addEntityClass(TccParticipantRegistrationBean.class);
    reader.setEntitySchemaFactory(factory);
    return reader;
  }

  private class EntityHandlerImpl implements EntityHandler {

    @Override
    public void handleEntity(Object bean) {
      TccParticipantRegistrationBean record = (TccParticipantRegistrationBean) bean;
      register(record);
    }
  }
}
