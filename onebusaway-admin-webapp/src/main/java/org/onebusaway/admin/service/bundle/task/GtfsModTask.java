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

import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.gtfs_transformer.GtfsTransformer;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GtfsModTask extends BaseModTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(GtfsModTask.class);

  private BundleRequestResponse _requestResponse;

  @Autowired
  public void setRequestResponse(BundleRequestResponse requestResponse) {
    _requestResponse = requestResponse;
  }

  @Override
  public void run() {
    try {
      _log.info("GtfsModTask Starting");
      GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
      for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
        String agencyId = parseAgencyDir(gtfsBundle.getPath().getPath());
        if (agencyId != null) {
          // lookup meta info for agency
          String modUrl = getModUrl(agencyId);
          _log.info("using modUrl=" + modUrl + " for agency " + agencyId + " and bundle " + gtfsBundle.getPath());
          if (modUrl != null) {
            // run the mod script on this gtfsBundle
            String oldFilename = gtfsBundle.getPath().getPath();
            String transform = getTransform(agencyId, gtfsBundle.getPath().toString());
            String newFilename = runModifications(gtfsBundle, agencyId, modUrl, transform);
            logger.changelog("Transformed " + oldFilename + " to " + newFilename + " according to url " + getModUrl(agencyId));
          } else {
            _log.info("no modUrl found for agency " + agencyId + " and bundle " + gtfsBundle.getPath());
          }
        }
      }
    } catch (Throwable ex) {
      _log.error("error modifying gtfs:", ex);
    } finally {
      _log.info("GtfsModTask Exiting");
    }
  }

  private String getTransform(String agencyId, String path) {
    try {
    return configurationServiceClient.getItem("admin", agencyId+"_transform").replaceAll(":path", path);
  } catch (Exception e) {}
    return null;
  }

  private String getModUrl(String agencyId) {
    try {
    return configurationServiceClient.getItem("admin", agencyId+"_modurl");
  } catch (Exception e) {}
    return null;
  }

  @Override
  public void addExtraMods(GtfsTransformer mod) {
    SimpleFeedVersionStrategy strategy = new SimpleFeedVersionStrategy();
    String name = requestResponse.getRequest().getBundleName();
    strategy.setVersion(name);
    mod.addTransform(strategy);
  }

}
