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

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.conveyal.gtfs.model.Statistic;
import com.conveyal.gtfs.service.impl.GtfsStatisticsService;

public class GtfsStatisticsTask implements Runnable {
	private Logger _log = LoggerFactory.getLogger(GtfsStatisticsTask.class);
	private static final String ALL_AGENCIES = "TOTAL";
	//private GtfsMutableRelationalDao _dao;
	private GtfsRelationalDaoImpl _dao;
	private FederatedTransitDataBundle _bundle;
	
//	@Autowired
//	public void setGtfsDao(GtfsMutableRelationalDao dao) {
//		_dao = dao;
//	}

	@Autowired
	public void setGtfsDao(GtfsRelationalDaoImpl dao) {
		_dao = dao;
	}

	@Autowired
	public void setBundle(FederatedTransitDataBundle bundle) {
		_bundle = bundle;
	}
	
	public void run() {
		File basePath = _bundle.getPath();
		_log.info("Starting GTFS stats to basePath=" + basePath);
		GtfsStatisticsService service = new GtfsStatisticsService(_dao);
		// create logger file
		GtfsCsvLogger csvLogger = new GtfsCsvLogger();
		csvLogger.setBasePath(basePath);
		csvLogger.open();
		csvLogger.header();
		
		// per agency status
		Collection<Agency> agencies = service.getAllAgencies();
		for (Agency agency:agencies) {
			_log.info("processing stats for agency: " + agency.getId() + " (" + agency.getName() + ")");
			csvLogger.logStat(agency.getId(), service.getStatistic(agency.getId()));
		}

		// overall stats/totals
		Statistic all = new Statistic();
		Agency allAgency = new Agency();
		allAgency.setId(ALL_AGENCIES);
		all.setAgencyId(ALL_AGENCIES);
		all.setRouteCount(service.getRouteCount());
		all.setTripCount(service.getTripCount());
		all.setStopCount(service.getStopCount());
		all.setStopTimeCount(service.getStopTimesCount());
		all.setCalendarStartDate(service.getCalendarServiceRangeStart());
		all.setCalendarEndDate(service.getCalendarServiceRangeEnd());

		csvLogger.logStat(allAgency.getId(), all);
		
		_log.info("cleaning up");
		// cleanup
		csvLogger.close();
		_log.info("exiting");
	}
}
