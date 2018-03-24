/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs_merge.GtfsMerger;
import org.onebusaway.gtfs_merge.strategies.AgencyMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateDetectionStrategy;
import org.onebusaway.gtfs_merge.strategies.EDuplicateRenamingStrategy;
import org.onebusaway.gtfs_merge.strategies.RouteMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.ServiceCalendarMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.StopMergeStrategy;
import org.onebusaway.gtfs_merge.strategies.TripMergeStrategy;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Combine all Modified GTFS data from multiple agencies into single zip file.
 */
public class GtfsMergeTask extends BaseModTask implements Runnable {
  private static final String CONSOLIDATED_DIR = "consolidated";
  private static Logger _log = LoggerFactory.getLogger(GtfsMergeTask.class);
	

	public void run() {
	  if (!requestResponse.getRequest().getConsolidateFlag()) {
	    _log.info("consolidate flag not set, extiting");
	    return;
	  }
		_log.info("GtfsMergeTask Starting with outputDirectory=" + getOutputDirectory());
		try {			
		  
			_log.info("Started merging modified GTFS feeds.");
			GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
			List<File> inputPaths = new ArrayList<File>();
		  // note this will be overridden if properly configured
			String outputLocation = System.getProperty("java.io.tmpdir") + File.separator + "gtfs_puget_sound_consolidated.zip"; 			
			if (getOutputDirectory() != null) {
			  String consolidatedPath = getOutputDirectory() + File.separator + CONSOLIDATED_DIR;
			  File consolidatedDir = new File(consolidatedPath);
			  consolidatedDir.mkdirs();
			  outputLocation = consolidatedPath + File.separator + "gtfs_puget_sound_consolidated.zip";
			}
			_log.info("Consolidated file output location: " + outputLocation);
			int i = 0;
			for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {	
				if(gtfsBundle.getPath() != null){
					_log.info("addiing agency data file path for agency[" + i + "]=" + gtfsBundle.getPath());				
					inputPaths.add(gtfsBundle.getPath());
				}else{
					_log.info("null file path for agency.");
				}
			}
						
			//Now call GTFS merger
			GtfsMerger feedMerger = new GtfsMerger();
			AgencyMergeStrategy agencyStrategy = new AgencyMergeStrategy();
			// agencies aren't duplicates, its by design
			agencyStrategy.setDuplicateDetectionStrategy(EDuplicateDetectionStrategy.FUZZY);
      feedMerger.setAgencyStrategy(agencyStrategy);
      
			StopMergeStrategy stopStrategy = new StopMergeStrategy();
			stopStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
			feedMerger.setStopStrategy(stopStrategy);
			
			RouteMergeStrategy routeStrategy = new RouteMergeStrategy();
			routeStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
      feedMerger.setRouteStrategy(routeStrategy);
			
      ServiceCalendarMergeStrategy serviceCalendarStrategy = new ServiceCalendarMergeStrategy();
      serviceCalendarStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
      feedMerger.setServiceCalendarStrategy(serviceCalendarStrategy);
      
      
      TripMergeStrategy tripStrategy = new TripMergeStrategy();
      tripStrategy.setDuplicateRenamingStrategy(EDuplicateRenamingStrategy.AGENCY);
      feedMerger.setTripStrategy(tripStrategy);
      
      File outputFile = new File(outputLocation);
      outputFile.createNewFile();
			feedMerger.run(inputPaths, new File(outputLocation));
			
		} catch (Throwable ex) {
			_log.error("Error merging gtfs:", ex);
		} finally {
			_log.info("GtfsMergeTask Exiting");
		}
	}
}
