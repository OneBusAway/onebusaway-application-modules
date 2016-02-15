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
package org.onebusaway.phone.actions;

import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

public class RegistrationAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    
    private CurrentUserService _currentUserService;
    
    @Autowired
    public void setCurrentUserService(CurrentUserService currentUserService) {
      _currentUserService = currentUserService;
    }
    
    @Override
    public String execute() {
      if( ! _currentUserService.hasPhoneNumberRegistration() )
        return "complete";
      return SUCCESS;
    }
}
