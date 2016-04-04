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
package org.onebusaway.phone.actions.stops;

import java.util.List;
import java.util.Set;

import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.phone.impl.PhoneArrivalsAndDeparturesModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class ArrivalsAndDeparturesForStopIdAction extends AbstractAction
    implements ModelDriven<PhoneArrivalsAndDeparturesModel> {

  private static final long serialVersionUID = 1L;

  private PhoneArrivalsAndDeparturesModel _model;

  @Autowired
  public void setModel(PhoneArrivalsAndDeparturesModel model) {
    _model = model;
  }

  public void setStopIds(List<String> stopIds) {
    _model.setStopIds(stopIds);
  }

  public void setRouteIds(Set<String> routeIds) {
    _model.setRouteFilter(routeIds);
  }

  public PhoneArrivalsAndDeparturesModel getModel() {
    return _model;
  }

  public String execute() throws Exception {

    if (_model.isMissingData())
      return INPUT;

    _model.process();

    return SUCCESS;
  }
}
