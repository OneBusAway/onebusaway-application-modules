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

import java.util.List;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback", "root", "suggestions"})
public class AutocompleteAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;
  
  @Autowired
  private EnterpriseGeocoderService _geocoderService;

  private List<String> suggestions = null;
  
  private String _term = null;

  public void setTerm(String term) {
    if(term != null) {
      _term = term.trim();
    }
  }

  @Override
  public String execute() {    
    if(_term == null || _term.isEmpty())
      return SUCCESS;
    
    suggestions = _transitDataService.getSearchSuggestions(null, _term.toLowerCase());
    
    if (suggestions != null && suggestions.size() == 0 && _term.length() > 2) {
    	List<EnterpriseGeocoderResult> geocoderResults = _geocoderService.enterpriseGeocode(_term);
    	// guard against misconfiguration
    	if (geocoderResults == null) return SUCCESS;
    	if (geocoderResults.size() > 0) {
        	for (int i = 0; i < 10; i++) {
        		suggestions.add(geocoderResults.get(i).getFormattedAddress());
        		if (i+1 == geocoderResults.size())
        			break;
        	}
    	}
    }
    return SUCCESS;
  }   
  
  /** 
   * VIEW METHODS
   */
  public List<String> getSuggestions() {
    return suggestions;
  }

}
