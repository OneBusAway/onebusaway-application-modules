/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.impl;

import java.util.Comparator;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.util.comparators.AlphanumComparator;

public class AgencyAndRouteComparator implements Comparator<RouteBean> {
	
	private Comparator<String> alphaNumComparator = new AlphanumComparator();

    @Override
    public int compare(RouteBean t, RouteBean t1) {
        if (t.getAgency().getName().compareTo(t1.getAgency().getName()) == 0) {
            if (t.getShortName() != null && t1.getShortName() != null) {
            	return alphaNumComparator.compare(t.getShortName(), t1.getShortName());
            } else {
                return t.getId().compareTo(t1.getId());
            }
        } else {
            return t.getAgency().getName().compareTo(t1.getAgency().getName());
        }
    }
}
