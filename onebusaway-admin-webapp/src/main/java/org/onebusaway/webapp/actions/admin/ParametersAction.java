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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.model.ParametersResponse;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.util.impl.configuration.ConfigParameter;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action class for parameters UI
 * @author abelsare
 *
 */
@Results({
	@Result(name="parameters", type="json", params= {"root","parametersResponse"}),
	@Result(name="agencyMap", type="json", params= {"root","parametersResponse"})
})
@AllowedMethods({"getParameters","getAgencyIdMap","saveParameters"})
public class ParametersAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;
	
	private ParametersResponse parametersResponse;
	private ParametersService parametersService;

	private ConfigurationService configurationService;
	private String[] params;

	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public String getParameters() {
		Map<String, String> configParameters = parametersService.getParameters();
		parametersResponse = new ParametersResponse();
		parametersResponse.setConfigParameters(configParameters);
		return "parameters";
	}


	public String getAgencyIdMap() {
		String agencyMapString = this.configurationService.getConfigurationValueAsString("agencyMap", "1:ACTA");
		Map<String, String> map = new HashMap<>();
		for (String kv :agencyMapString.split(",")) {
			String[] agencyAndName = kv.split(":");
			map.put(agencyAndName[0], agencyAndName[1]);
		}
		parametersResponse = new ParametersResponse();
		parametersResponse.setConfigParameters(map);
		return "agencyMap";
	}
	public List<String> getExcludingAgencies() {
		String agencyIdString = this.configurationService.getConfigurationValueAsString("agencyIds", "1");
		String[] list = agencyIdString.split(",");
		return Arrays.asList(list);
	}

	public String saveParameters() {
		parametersResponse = new ParametersResponse();
		Map<String, List<ConfigParameter>> parameters = buildParametersList();
		if(parametersService.saveParameters(parameters)) {
			parametersResponse.setSaveSuccess(true);
		} else {
			parametersResponse.setSaveSuccess(false);
		}
		return "parameters";
	}

	private Map<String, List<ConfigParameter>> buildParametersList() {
		Map<String, List<ConfigParameter>> parameters = new HashMap<>();
		if (params == null) return parameters;
		for(String param : params) {
			String[] configPairs = param.split(":");
			if (configPairs.length < 2) {
				throw new RuntimeException("Expecting config data in key value pairs");
			}
			ConfigParameter configParameter = new ConfigParameter();
			String [] agencyAndKey = configPairs[0].split("_");
			if (agencyAndKey.length < 2) {
				throw new RuntimeException("Expecting agency and key data in key value pairs");
			}

			configParameter.setKey(agencyAndKey[1]);
			configParameter.setValue(configPairs[1]);
			parameters.put(agencyAndKey[0], Arrays.asList(configParameter));
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
