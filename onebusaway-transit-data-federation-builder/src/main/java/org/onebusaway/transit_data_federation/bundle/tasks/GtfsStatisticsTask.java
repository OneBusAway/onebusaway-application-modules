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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.util.Collection;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.conveyal.gtfs.GtfsStatistics;

public class GtfsStatisticsTask implements Runnable {
	private Logger _log = LoggerFactory.getLogger(GtfsStatisticsTask.class);
	private static final String ALL = "TOTAL";
	private GtfsMutableRelationalDao _dao;
	private FederatedTransitDataBundle _bundle;
	
	@Autowired
	public void setGtfsDao(GtfsMutableRelationalDao dao) {
		_dao = dao;
	}

	@Autowired
	public void setBundle(FederatedTransitDataBundle bundle) {
		_bundle = bundle;
	}
	
	public void run() {
		File basePath = _bundle.getPath();
		_log.info("Starting GTFS stats to basePath=" + basePath);
		GtfsStatistics stats = new GtfsStatistics(_dao);
		// create logger file
		GtfsCsvLogger csvLogger = new GtfsCsvLogger();
		csvLogger.setBasePath(basePath);
		csvLogger.open();
		
		// per agency status
		Collection<Agency> agencies = stats.getAllAgencies();
		for (Agency agency:agencies) {
			_log.info("processing stats for agency: " + agency.getId() + " (" + agency.getName() + ")");
			csvLogger.logStat(agency.getId(), "route_count", stats.getRouteCount(agency.getId()));
			csvLogger.logStat(agency.getId(), "trip_count", stats.getTripCount(agency.getId()));
			csvLogger.logStat(agency.getId(),  "stop_count",  stats.getStopCount(agency.getId()));
			csvLogger.logStat(agency.getId(), "stop_time_count", stats.getStopTimesCount(agency.getId()));
			csvLogger.logStat(agency.getId(), "calendar_start_date", stats.getCalendarServiceRangeStart(agency.getId()));
			csvLogger.logStat(agency.getId(), "calendar_end_date", stats.getCalendarServiceRangeEnd(agency.getId()));
		}

		// overall stats/totals
		csvLogger.logStat(ALL, "route_count", stats.getRouteCount());
		csvLogger.logStat(ALL, "trip_count", stats.getTripCount());
		csvLogger.logStat(ALL,  "stop_count",  stats.getStopCount());
		csvLogger.logStat(ALL, "stop_time_count", stats.getStopTimesCount());
		csvLogger.logStat(ALL, "calendar_start_date", stats.getCalendarServiceRangeStart());
		csvLogger.logStat(ALL, "calendar_end_date", stats.getCalendarServiceRangeEnd());

		
		
		_log.info("cleaning up");
		// cleanup
		csvLogger.close();
		_log.info("exiting");
	}
}
