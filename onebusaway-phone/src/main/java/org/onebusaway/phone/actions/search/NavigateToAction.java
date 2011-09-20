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
package org.onebusaway.phone.actions.search;

import org.onebusaway.phone.actions.AbstractAction;

public class NavigateToAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private NavigationBean _navigation;

  private int _index;

  public void setNavigation(NavigationBean navigation) {
    _navigation = navigation;
  }

  public NavigationBean getNavigation() {
    return _navigation;
  }

  public void setIndex(int index) {
    _index = index;
  }

  @Override
  public String execute() throws Exception {

    _navigation.setCurrentIndex(_index);

    return SUCCESS;
  }
}
