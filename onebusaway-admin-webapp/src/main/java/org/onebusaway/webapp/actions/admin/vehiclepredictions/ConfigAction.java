/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.vehiclepredictions;

import org.apache.struts2.convention.annotation.Namespace;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.onebusaway.webapp.actions.admin.ReportsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Namespace(value="/admin/vehiclepredictions")
public class ConfigAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;

	private static Logger _log = LoggerFactory.getLogger(ReportsAction.class);
	
	public String execute() {
		return SUCCESS;
	}
	
}