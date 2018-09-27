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
package org.onebusaway.enterprise.webapp.actions.api;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.presentation.model.SearchResultCollection;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.search.SearchService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})
public class SearchAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private SearchService _searchService;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private RealtimeService _realtimeService;

  @Autowired
  private ConfigurationService _configService;

  private SearchResultCollection _results = null;
  
  private String _q = null;

  public void setQ(String query) {
    if(query != null) {
      _q = query.trim();
    }
  }

  @Override
  public String execute() {    
    if(_q == null || _q.isEmpty())
      return SUCCESS;

    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    if (serviceDateFilterOn) {
      _results = _searchService.getSearchResultsForServiceDate(_q, new SearchResultFactoryImpl(_searchService, _transitDataService, _realtimeService, _configService), new ServiceDate(new Date(SystemTime.currentTimeMillis())));

    }
    else {
      _results = _searchService.getSearchResults(_q, new SearchResultFactoryImpl(_searchService, _transitDataService, _realtimeService, _configService));
    }
    return SUCCESS;
  }   
  
  /** 
   * VIEW METHODS
   */
  public SearchResultCollection getSearchResults() {
    return _results;
  }

}
