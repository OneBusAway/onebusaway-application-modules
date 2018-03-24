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
package org.onebusaway.twilio.actions;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
  @Result(name="handle-registration", location="registration-handle", type="chain"),
})

public class RegistrationAction extends TwilioSupport {
  private CurrentUserService _currentUserService;
  private String _code;
  

  public void setCode(String code) {
    _code = code;
  }

  public String getCode() {
    return _code;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }
  
  @Override
  public String execute() {
    logUserInteraction();
    if( ! _currentUserService.hasPhoneNumberRegistration() )
      return "complete";
    
    // TODO this precludes stops/routes from under 10
    if ( Integer.parseInt(getInput()) > 9 ) {
      // we have a code, 
      setCode(getInput());
      return "handle-registration";
    }
    addMessage(Messages.REGISTRATION_INSTRUCTIONS);
    addMessage(Messages.TO_REPEAT);


    return SUCCESS;
  }
}
