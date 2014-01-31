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
import java.util.List;

import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.conveyal.gtfs.model.DuplicateStops;
import com.conveyal.gtfs.service.GtfsValidationService;

public class GtfsValidationTask implements Runnable {
	private Logger _log = LoggerFactory.getLogger(GtfsStatisticsTask.class);
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
	
	@Override
	public void run() {
		File basePath = _bundle.getPath();
		_log.info("Starting GTFS valdiation to basePath=" + basePath);
		GtfsValidationService service = new GtfsValidationService(_dao);
		List<DuplicateStops> duplicateStops = service.duplicateStops();
		GtfsCsvLogger csvLogger = new GtfsCsvLogger();
		csvLogger.setBasePath(basePath);
		csvLogger.setFilename("gtfs_validation.csv");
		csvLogger.open();
		csvLogger.addHeader("agency,stop_1,_stop2\n");
		for (DuplicateStops ds : duplicateStops) {
			String[] s = {ds.stop1.getId().getAgencyId(), ds.stop1.getId().toString(), ds.stop2.getId().toString()+"\n",};
			csvLogger.log(s);
		}
		csvLogger.close();
		_log.info("Exiting");
	}
	
	
}
