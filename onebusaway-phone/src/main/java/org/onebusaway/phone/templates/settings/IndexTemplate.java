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
package org.onebusaway.phone.templates.settings;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;
import org.onebusaway.users.client.model.UserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/settings/index")
public class IndexTemplate extends AbstractAgiTemplate {

  private TextModification _locationPronunciation;

  @Autowired
  public void setLocationPronunciation(
      @Qualifier("locationPronunciation") TextModification locationPronunciation) {
    _locationPronunciation = locationPronunciation;
  }

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack stack = context.getValueStack();
    UserBean user = (UserBean) stack.findValue("currentUser");
    if (user.getDefaultLocationName() != null) {
      addMessage(Messages.SETTINGS_YOUR_DEFAULT_SEARCH_LOCATION_IS_CURRENTLY);
      addText(_locationPronunciation.modify(user.getDefaultLocationName()));
    }
    addMessage(Messages.INDEX_ACTION_SET_DEFAULT_SEARCH_LOCATION);
    addAction("1", "/settings/askForDefaultSearchLocation");

    if (user != null) {
      if (user.isRememberPreferencesEnabled())
        addMessage(Messages.PREFERENCES_DO_NOT_REMEMBER);
      else
        addMessage(Messages.PREFERENCES_DO_REMEMBER);
      addAction("2", "/settings/setRememberPreferences", "enabled",
          !user.isRememberPreferencesEnabled());
    }

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
    addAction("[#23456789*]", "/repeat");
  }
}
