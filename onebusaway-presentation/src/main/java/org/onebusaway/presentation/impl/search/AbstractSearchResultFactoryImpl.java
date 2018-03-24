/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.presentation.impl.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

public abstract class AbstractSearchResultFactoryImpl implements SearchResultFactory {

  public AbstractSearchResultFactoryImpl() {
    super();
  }

  protected void populateServiceAlerts(Set<String> serviceAlertDescriptions, List<ServiceAlertBean> serviceAlertBeans) {
    populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans, true);
  }

  protected void populateServiceAlerts(Set<String> serviceAlertDescriptions,
      List<ServiceAlertBean> serviceAlertBeans, boolean htmlizeNewlines) {
    if (serviceAlertBeans == null)
      return;
    for (ServiceAlertBean serviceAlertBean : serviceAlertBeans) {
      boolean descriptionsAdded = false;
      descriptionsAdded = setDescription(serviceAlertDescriptions,
          serviceAlertBean.getDescriptions(), htmlizeNewlines)
          || setDescription(serviceAlertDescriptions,
              serviceAlertBean.getSummaries(), htmlizeNewlines);
      if (!descriptionsAdded) {
        serviceAlertDescriptions.add("(no description)");
      }
    }
  }

  protected void populateServiceAlerts(
      List<NaturalLanguageStringBean> serviceAlertDescriptions,
      List<ServiceAlertBean> serviceAlertBeans, boolean htmlizeNewlines) {
    Set<String> d = new HashSet<String>();
    populateServiceAlerts(d , serviceAlertBeans, htmlizeNewlines);
    for (String s: d) {
      serviceAlertDescriptions.add(new NaturalLanguageStringBean(s, "EN"));
    }
  }

  // TODO This a problem, assumes English
  protected void populateServiceAlerts(List<NaturalLanguageStringBean> serviceAlertDescriptions, List<ServiceAlertBean> serviceAlertBeans) {
    populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans, true);
  }

  private boolean setDescription(Set<String> serviceAlertDescriptions, List<NaturalLanguageStringBean> descriptions, boolean htmlizeNewlines) {
    boolean descriptionsAdded = false;
    if (descriptions != null) {
      for (NaturalLanguageStringBean description : descriptions) {
        if (description.getValue() != null) {
          serviceAlertDescriptions.add((htmlizeNewlines ? description.getValue().replace("\n",
              "<br/>") : description.getValue()));
          descriptionsAdded = true;
        }
      }
    }
    return descriptionsAdded;
  }

}