/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;


public class ServiceAlertRecordBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ServiceAlertBean _serviceAlertBean;
	private Boolean _isCopy;
	
	public ServiceAlertBean getServiceAlertBean() {
		return _serviceAlertBean;
	}
	
	public void setServiceAlertBean(ServiceAlertBean serviceAlertBean) {
		_serviceAlertBean = serviceAlertBean;
	}
	
	public Boolean isCopy() {
		return _isCopy;
	}
	
	public void setCopy(Boolean isCopy) {
		_isCopy = isCopy;
	}
}
