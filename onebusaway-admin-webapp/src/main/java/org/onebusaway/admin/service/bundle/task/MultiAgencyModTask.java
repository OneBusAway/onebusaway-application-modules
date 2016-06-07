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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiAgencyModTask extends BaseModTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(GtfsModTask.class);
  

  @Override
  public void run() {
    try {
      _log.info("GtfsModTask Starting");
      
      GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
      for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
        String agencyId = parseAgencyDir(gtfsBundle.getPath().getPath());
        //_log.info("no modUrl found for agency " + agencyId + " and bundle " + gtfsBundle.getPath());
        String oldFilename = gtfsBundle.getPath().getPath();
        String newFilename = runModifications(gtfsBundle, agencyId, getEmptyModUrl(), null);
        logger.changelog("Transformed " + oldFilename + " to " + newFilename + " to add multi-agency support");
      }
    } catch (Throwable ex) {
      _log.error("error modifying gtfs:", ex);
    } finally {
      _log.info("GtfsModTask Exiting");
    }
  }

}
