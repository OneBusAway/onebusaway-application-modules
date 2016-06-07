/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.model.ParametersResponse;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action class for parameters UI
 * @author abelsare
 *
 */
@Results({
	@Result(name="parameters", type="json", params= {"root","parametersResponse"})
})
public class ParametersAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;
	
	private ParametersResponse parametersResponse;
	private ParametersService parametersService;
	
	private String[] params;
	
	
	public String getParameters() {
		Map<String, String> configParameters = parametersService.getParameters();
		
		parametersResponse = new ParametersResponse();
		parametersResponse.setConfigParameters(configParameters);
		
		return "parameters";
	}
	
	public String saveParameters() {
		parametersResponse = new ParametersResponse();
		Map<String, String> parameters = buildParameters();
		if(parametersService.saveParameters(parameters)) {
			parametersResponse.setSaveSuccess(true);
		} else {
			parametersResponse.setSaveSuccess(false);
		}
		return "parameters";
	}
	
	private Map<String, String> buildParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		
		for(String param : params) {
			String [] configPairs = param.split(":");
			if(configPairs.length < 2) {
				throw new RuntimeException("Expecting config data in key value pairs");
			} 
			parameters.put(configPairs[0], configPairs[1]);
		}
		
		return parameters;
	}

	/**
	 * @return the parametersResponse
	 */
	public ParametersResponse getParametersResponse() {
		return parametersResponse;
	}

	/**
	 * Injects parameters service
	 * @param parametersService the parametersService to set
	 */
	@Autowired
	public void setParametersService(ParametersService parametersService) {
		this.parametersService = parametersService;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(String[] params) {
		this.params = params;
	}
	
	public String[] getParams() {
		return params;
	}

}
