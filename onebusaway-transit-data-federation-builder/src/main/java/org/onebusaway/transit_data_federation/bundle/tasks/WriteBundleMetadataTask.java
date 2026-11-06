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

  /**
   * Writes metadata.json for the bundle just built. When this run computed no
   * hash because the gtfs task was skipped (partial build), the hash from any
   * existing metadata on disk is preserved instead of being dropped. When a
   * directory feed was read, it correctly produces no hash, so no prior hash
   * is restored.
   */
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
      // A partial build can run bundle_metadata without gtfs: no bundle then
      // carries a hash, and overwriting metadata.json would silently drop the
      // hash the API reports. Preserve the hash from the metadata already on
      // disk only when GTFS was skipped. A bundle whose path is a regular
      // file would carry a hash if GTFS had run, so a file path with no
      // computed hash proves the gtfs task was skipped; a directory feed
      // correctly produces no hash, and restoring the old one would misreport
      // the previous zip's hash for directory data. Only a bundle hash
      // replaces a preserved one.
      File outputFile = new File(bundlePath, "metadata.json");
      boolean gtfsSkipped = false;
      for (GtfsBundle bundle : bundles.getBundles()) {
        File path = bundle.getPath();
        if (path != null && path.isFile()) {
          gtfsSkipped = true;
          break;
        }
      }
      if (data.getStaticGtfsHash() == null && gtfsSkipped && outputFile.exists()) {
        try {
          BundleMetadata existing = new ObjectMapper().readValue(outputFile, BundleMetadata.class);
          if (existing != null && existing.getStaticGtfsHash() != null) {
            data.setStaticGtfsHash(existing.getStaticGtfsHash());
          }
        } catch (Exception e) {
          _log.warn("could not read existing bundle metadata, hash will be omitted", e);
        }
      }
      // Service dates must be present and parseable: store discovery does
      // Long.parseLong on them, and a bundle with null dates is skipped, so
      // a CLI-built bundle could never be loaded by a server. Mirror
      // ConventionMain's placeholder: active from now, out 30 days.
      data.setServiceDateFrom(new Date());
      data.setServiceDateTo(DateUtils.addDays(new Date(), 30));

      new ObjectMapper().writeValue(outputFile, data);
      _log.info("wrote bundle metadata to " + outputFile.getAbsolutePath());
    } catch (Exception e) {
      throw new IllegalStateException("error writing bundle metadata", e);
    }
  }
}
