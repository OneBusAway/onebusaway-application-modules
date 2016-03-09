/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.util.comparators.AlphanumComparator;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri.VehicleActivityStructure;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RouteListAction extends NextBusApiBase implements
		ModelDriven<Body<DisplayRoute>> {

	private String agencyId;

	public String getA() {
		return agencyId;
	}

	public void setA(String agencyId) {
		this.agencyId = getMappedAgency(agencyId);
	}

	public HttpHeaders index() {
		return new DefaultHttpHeaders("success");
	}

	@Override
	public Body<DisplayRoute> getModel() {
		Body<DisplayRoute> body = new Body<DisplayRoute>();
		if (isValid(body)) {
			List<RouteBean> routeBeans = _transitDataService
					.getRoutesForAgencyId(agencyId).getList();
			
			Collections.sort(routeBeans, new Comparator<RouteBean>() {
        AlphanumComparator alphaComparator = new AlphanumComparator();
        public int compare(RouteBean arg0, RouteBean arg1) {
          return alphaComparator.compare(arg0.getId(),arg1.getId());
        }
      });
			
			for (RouteBean routeBean : routeBeans) {
				DisplayRoute route = new DisplayRoute();
				route.setTag(getIdNoAgency(routeBean.getId()));
				route.setTitle(routeBean.getShortName() + " " + routeBean.getLongName());
				body.getResponse().add(route);
			}

		}
		return body;
	}

	private boolean isValid(Body body) {
		return isValidAgency(body, agencyId);
	}

}
