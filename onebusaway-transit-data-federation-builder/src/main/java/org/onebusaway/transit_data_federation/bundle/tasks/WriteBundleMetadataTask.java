/**
 * Copyright (C) 2026 OneBusAway
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
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.onebusaway.transit_data_federation.bundle.model.BundleMetadata;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Last task of a CLI bundle build: writes {@code metadata.json} (with the
 * recorded {@code staticGtfsHash}) to the bundle path that
 * {@link FederatedTransitDataBundle#getBundleMetadataPath()} points at, so the
 * runtime {@code BundleConfigDao} can read it back. Single-feed: the first
 * non-null hash wins; a multi-feed list is out of scope for issue #487.
 */
public class WriteBundleMetadataTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(WriteBundleMetadataTask.class);

  private ApplicationContext _applicationContext;

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }

  @Override
  public void run() {
    try {
      GtfsBundles bundles = GtfsReadingSupport.getGtfsBundles(_applicationContext);

      // Do NOT use FederatedTransitDataBundle#getBundleMetadataPath() here:
      // in the creator context the bundle bean's path property is commented
      // out (application-context-common.xml), so its path is null and the
      // metadata file would end up parent-less. The creator sets the
      // bundlePath system property to the output directory before building
      // the context (FederatedTransitDataBundleCreator#setSystemProperties),
      // so that is the authoritative output path for this task.
      String bundlePathProperty = System.getProperty("bundlePath");
      if (bundlePathProperty == null) {
        throw new IllegalStateException("bundlePath system property is not set");
      }
      File bundlePath = new File(bundlePathProperty);

      BundleMetadata data = new BundleMetadata();
      data.setId(UUID.randomUUID().toString());
      data.setName(bundlePath.getName());
      for (GtfsBundle bundle : bundles.getBundles()) {
        if (bundle.getStaticGtfsHash() != null) {
          data.setStaticGtfsHash(bundle.getStaticGtfsHash());
          break;
        }
      }
      // Service dates must be present and parseable: store discovery does
      // Long.parseLong on them, and a bundle with null dates is skipped, so
      // a CLI-built bundle could never be loaded by a server. Mirror
      // ConventionMain's placeholder: active from now, out 30 days.
      data.setServiceDateFrom(new Date());
      data.setServiceDateTo(DateUtils.addDays(new Date(), 30));

      File outputFile = new File(bundlePath, "metadata.json");
      new ObjectMapper().writeValue(outputFile, data);
      _log.info("wrote bundle metadata to " + outputFile.getAbsolutePath());
    } catch (Exception e) {
      throw new IllegalStateException("error writing bundle metadata", e);
    }
  }
}
