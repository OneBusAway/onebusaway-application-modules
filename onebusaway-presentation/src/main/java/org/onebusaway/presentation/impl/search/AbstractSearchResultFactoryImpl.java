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
      // look for both summary and descriptions; if both trivially append for display
      descriptionsAdded = setDescription(serviceAlertDescriptions,
              serviceAlertBean.getSummaries(),
              serviceAlertBean.getDescriptions(), serviceAlertBean.getUrls(), htmlizeNewlines)
          || setDescription(serviceAlertDescriptions,
              serviceAlertBean.getSummaries(), serviceAlertBean.getUrls(), htmlizeNewlines);
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

  private boolean setDescription(Set<String> serviceAlertDescriptions, List<NaturalLanguageStringBean> descriptions, List<NaturalLanguageStringBean> urls, boolean htmlizeNewlines) {
    boolean descriptionsAdded = false;
    if (descriptions != null) {
      for (int i = 0; i < descriptions.size(); i++) {
        NaturalLanguageStringBean description = descriptions.get(i);
        if (description.getValue() != null && !description.getValue().isEmpty()) {
          StringBuffer text = new StringBuffer();
          if (htmlizeNewlines) {
            if (urls != null && urls.size() > i) {
              text.append("<a href='");
              text.append(urls.get(i).getValue());
              text.append("' target='_blank'>");
            }
            text.append(description.getValue().replace("\n", "<br/>"));
            if (urls != null && urls.size() > i) {
              text.append("</a>");
            }
          }
          else
            text.append(description.getValue());
          serviceAlertDescriptions.add(text.toString());
          descriptionsAdded = true;
        }
      }
    }
    return descriptionsAdded;
  }

  private boolean setDescription(Set<String> serviceAlertDescriptions, List<NaturalLanguageStringBean> summaries, List<NaturalLanguageStringBean> descriptions, List<NaturalLanguageStringBean> urls, boolean htmlizeNewlines) {
    boolean descriptionsAdded = false;

    if (summaries != null) {
      if (summaries.size() > 0 && descriptions != null && descriptions.size() == summaries.size()) {
        // merge summary and description together
        for (int i = 0; i < summaries.size(); i++) {
          StringBuffer text = new StringBuffer();
          boolean foundSummary = false;
          NaturalLanguageStringBean summary = summaries.get(0);
          NaturalLanguageStringBean description = descriptions.get(0);

          if (urls != null && urls.size() > i) {
            text.append("<a href='");
            text.append(urls.get(i).getValue());
            text.append("' target='_blank'>");
          }

          if (summary.getValue() != null && !summary.getValue().isEmpty()) {
            foundSummary = true;
            text.append("<strong>");
            if (htmlizeNewlines) text.append(summary.getValue());
          }
          if (description.getValue() != null && !description.getValue().isEmpty()) {
            if (foundSummary && htmlizeNewlines) text.append("</strong><br/><br/>");
            text.append(description.getValue());
          } else {
            if (foundSummary && htmlizeNewlines) text.append("</strong>");
          }

          if (urls != null && urls.size() > i) {
            text.append("</a>");
          }

          serviceAlertDescriptions.add(htmlizeNewlines ? text.toString().replace("\n",
                  "<br/>") : text.toString());
          descriptionsAdded = true;

        }
      }
      if (descriptionsAdded) return descriptionsAdded;

      // if we are here our summaries and description don't match
      for (int i = 0; i < summaries.size(); i++) {
        NaturalLanguageStringBean summary = summaries.get(i);
        if (summary.getValue() != null && !summary.getValue().isEmpty()) {
          StringBuilder text = new StringBuilder();

          if (urls != null && urls.size() > i) {
            text.append("<a href='");
            text.append(urls.get(i).getValue());
            text.append("' target='_blank'>");
          }

          text.append(summary.getValue());

          if (urls != null && urls.size() > i) {
            text.append("</a>");
          }

          serviceAlertDescriptions.add(htmlizeNewlines ? "<strong>" + summary.getValue().replace("\n",
                  "<br/>") + "</strong>" : text.toString());
          descriptionsAdded = true;
        }
      }
    }
    if (descriptions != null) {
      for (int i = 0; i < descriptions.size(); i++) {
        NaturalLanguageStringBean description = descriptions.get(i);
        if (description.getValue() != null && !description.getValue().isEmpty()) {
          StringBuilder text = new StringBuilder();

          if (urls != null && urls.size() > i) {
            text.append("<a href='");
            text.append(urls.get(i).getValue());
            text.append("' target='_blank'>");
          }

          text.append(description.getValue());

          if (urls != null && urls.size() > i) {
            text.append("</a>");
          }

          serviceAlertDescriptions.add((htmlizeNewlines ? description.getValue().replace("\n",
                  "<br/>") : text.toString()));
          descriptionsAdded = true;
        }
      }
    }
    return descriptionsAdded;
  }

}