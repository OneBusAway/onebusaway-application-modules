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

import org.onebusaway.admin.model.BundleRequest;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.bundle.BundleValidationService;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.king_county_metro_gtfs.model.PatternPair;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class GtfsFullValidationTask implements  Runnable {
  private static Logger _log = LoggerFactory.getLogger(GtfsFullValidationTask.class);
  protected ApplicationContext _applicationContext;

  private BundleRequestResponse requestResponse;
  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }

  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }

  @Autowired
  private BundleValidationService _validateService;

  public void setValidateService(BundleValidationService validateService) {
    _validateService = validateService;
  }

  @Override
  public void run() {
    _log.info("GtfsFullValidationTask Starting");
    // Only run  this on a Final build
    if (!requestResponse.getRequest().getArchiveFlag()) {
      _log.info("archive flag not set, GtfsFullValidationTask exiting");
      return;
    }

    GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);
    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
      File gtfsFile = gtfsBundle.getPath();
      String gtfsFileName = gtfsFile.getName();
      String gtfsFilePath = gtfsBundle.getPath().toString();
      String outputFile = requestResponse.getResponse().getBundleOutputDirectory() 
          + "/final/" + gtfsFileName + ".html"; 
      _log.info(gtfsBundle.getPath().toString());
      try {
        _validateService.installAndValidateGtfs(gtfsFilePath, outputFile);
      } catch (Exception any) {
        _log.error("GtfsFullValidationTask failed:", any);
      } finally {
        _log.info("GtfsFullValidationTask Exiting");
      }
    }
    return;
  }

  protected GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }
}
