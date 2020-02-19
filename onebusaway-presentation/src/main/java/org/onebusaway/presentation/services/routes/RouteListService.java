/**
 * Copyright (C) 2013 Kurt Raschke
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
package org.onebusaway.presentation.services.routes;

import java.util.List;
import org.onebusaway.transit_data.model.RouteBean;

/**
 *
 */
public interface RouteListService {

    public boolean getShowAgencyNames();

    public boolean getUseAgencyId();

    public List<RouteBean> getRoutes();

    public List<RouteBean> getFilteredRoutes(String agencyFilter);
}
