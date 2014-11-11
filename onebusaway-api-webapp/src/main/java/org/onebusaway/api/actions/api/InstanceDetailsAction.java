/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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

package org.onebusaway.api.actions.api;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.GitRepositoryState;
import org.springframework.beans.factory.annotation.Autowired;

public class InstanceDetailsAction extends ApiActionSupport {

	private static final long serialVersionUID = 1L;

	private static final int V2 = 2;

	@Autowired
	private TransitDataService _service;

	@Autowired
	private GitRepositoryState _repositoryState;

	public InstanceDetailsAction() {
		super(V2);
	}

	public DefaultHttpHeaders index() throws ServiceException {
		if (hasErrors())
			return setValidationErrorsResponse();

		BeanFactoryV2 factory = getBeanFactoryV2();
		return setOkResponse(factory.getInstances(_service.getInstanceDetails()));
	}
}