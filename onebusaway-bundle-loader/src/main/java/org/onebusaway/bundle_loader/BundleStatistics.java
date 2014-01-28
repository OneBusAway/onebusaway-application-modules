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
package org.onebusaway.bundle_loader;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import com.conveyal.gtfs.BaseStatistics;
import com.conveyal.gtfs.Statistic;

/**
 * Retrieves a base set of statistics from the bundle.
 */
public class BundleStatistics implements BaseStatistics {

	private TransitGraphDao _transitGraph = null;
	private ExtendedCalendarService _calendarService = null;
	
	public void setTrasitGraphDao(TransitGraphDao transitGraph) {
		_transitGraph = transitGraph;
	}

	public void setExtendedCalendarService(ExtendedCalendarService ecsi) {
		_calendarService = ecsi;
	}

	@Override
	public Integer getAgencyCount() {
		return _transitGraph.getAllAgencies().size();
	}

	@Override
	public Integer getRouteCount() {
		return _transitGraph.getAllRoutes().size();
	}

	@Override
	public Integer getTripCount() {
		return _transitGraph.getAllTrips().size();
	}

	@Override
	public Integer getStopCount() {
		return _transitGraph.getAllStops().size();
	}

	@Override
	public Integer getStopTimesCount() {
		Set<StopTimeEntry> stes = new HashSet<StopTimeEntry>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			stes.addAll(te.getStopTimes());
		}
		return stes.size();
	}

	@Override
	public Date getCalendarDateStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getCalendarDateEnd() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Date getCalendarServiceRangeStart() {
		Date start = null;
		Set<ServiceDate> dates = new HashSet<ServiceDate>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			dates.addAll(_calendarService.getServiceDatesForServiceIds(new ServiceIdActivation(te.getServiceId())));
		}
		for (ServiceDate sd : dates) {
			if (start == null || start.after(sd.getAsDate())) {
				start = sd.getAsDate();
			}
		}
		return start;
	}

	@Override
	public Date getCalendarServiceRangeEnd() {
		Date end = null;
		Set<ServiceDate> dates = new HashSet<ServiceDate>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			dates.addAll(_calendarService.getServiceDatesForServiceIds(new ServiceIdActivation(te.getServiceId())));
		}
		for (ServiceDate sd : dates) {
			if (end == null || end.before(sd.getAsDate())) {
				end = sd.getAsDate();
			}
		}
		return end;
	}

	@Override
	public Integer getRouteCount(String agencyId) {
		int routeCount = 0;
		List<RouteEntry> routes = _transitGraph.getAllRoutes();
		for (RouteEntry re : routes) {
			if (re.getId().getAgencyId().equals(agencyId)) {
				routeCount++;
			}
		}
		return routeCount;
	}

	@Override
	public Integer getTripCount(String agencyId) {
		int tripCount = 0;
		List<TripEntry> trips = _transitGraph.getAllTrips();
		for (TripEntry te : trips) {
			if (te.getId().getAgencyId().equals(agencyId)) {
				tripCount++;
			}
		}
		return tripCount;
	}

	@Override
	public Integer getStopCount(String agencyId) {
		int stopCount = 0;
		List<StopEntry> stops = _transitGraph.getAllStops();
		for (StopEntry se : stops) {
			if (se.getId().getAgencyId().equals(agencyId)) {
				stopCount++;
			}
		}
		return stopCount;
	}

	@Override
	public Integer getStopTimesCount(String agencyId) {
		Set<StopTimeEntry> stes = new HashSet<StopTimeEntry>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			if (te.getId().getAgencyId().equals(agencyId)) {
				stes.addAll(te.getStopTimes());
			}
		}
		return stes.size();
	}

	@Override
	public Date getCalendarServiceRangeStart(String agencyId) {
		Date start = null;
		Set<ServiceDate> dates = new HashSet<ServiceDate>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			if (te.getId().getAgencyId().equals(agencyId)) {
				dates.addAll(_calendarService.getServiceDatesForServiceIds(new ServiceIdActivation(te.getServiceId())));
			}
		}
		for (ServiceDate sd : dates) {
			if (start == null || start.after(sd.getAsDate())) {
				start = sd.getAsDate();
			}
		}
		return start;
	}

	@Override
	public Date getCalendarServiceRangeEnd(String agencyId) {
		Date end = null;
		Set<ServiceDate> dates = new HashSet<ServiceDate>();
		for (TripEntry te : _transitGraph.getAllTrips()) {
			if (te.getId().getAgencyId().equals(agencyId)) {
				dates.addAll(_calendarService.getServiceDatesForServiceIds(new ServiceIdActivation(te.getServiceId())));
			}
		}
		for (ServiceDate sd : dates) {
			if (end == null || end.before(sd.getAsDate())) {
				end = sd.getAsDate();
			}
		}
		return end;
	}

	@Override
	public Date getCalendarDateStart(String agencyId) {
		// TODO provide real impl
		return getCalendarServiceRangeStart(agencyId);
	}

	@Override
	public Date getCalendarDateEnd(String agencyId) {
		// TODO provide real impl
		return getCalendarServiceRangeEnd(agencyId);
	}

	@Override
	public Statistic getStatistic(String agencyId) {
		Statistic bs = new Statistic();
		bs.setAgencyId(agencyId);
		bs.setRouteCount(getRouteCount(agencyId));
		bs.setTripCount(getTripCount(agencyId));
		bs.setStopCount(getStopCount(agencyId));
		bs.setStopTimeCount(getStopTimesCount(agencyId));
		bs.setCalendarStartDate(getCalendarServiceRangeStart(agencyId));
		bs.setCalendarEndDate(getCalendarServiceRangeEnd(agencyId));
		return bs;
	}
	
}
