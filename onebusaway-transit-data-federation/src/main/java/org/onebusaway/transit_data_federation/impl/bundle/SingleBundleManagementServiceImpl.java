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
package org.onebusaway.transit_data_federation.impl.bundle;

import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data_federation.impl.config.BundleConfigDao;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.concurrent.Future;

public class SingleBundleManagementServiceImpl implements
    BundleManagementService {

  private static Logger _log = LoggerFactory
      .getLogger(BundleManagementServiceImpl.class);
  private BundleConfigDao _bundleConfigDao;

  @Autowired
  public void setBundleConfigDao(BundleConfigDao bundleConfigDao) {
    _bundleConfigDao = bundleConfigDao;
  }

  /********************
   * Service Methods
   ********************/

  @Override
  public String getActiveBundleId() {

    if (_bundleConfigDao == null) {
      _log.error("config error:  bundleConfigDao is null");
      return null;
    }

    if (_bundleConfigDao.getBundleMetadata() == null) {
      _log.error("data error:  getBundleMetadata is null");
      return null;
    }

    return _bundleConfigDao.getBundleMetadata().getId();
  }

  @Override
  public BundleMetadata getBundleMetadata() {
    if (_bundleConfigDao == null) {
      _log.error("config error:  bundleConfigDao is null");
      return null;
    }
    return _bundleConfigDao.getBundleMetadata();

  }

  @Override
  public List<BundleItem> getAllKnownBundles() {
    return null;
  }

  @Override
  public boolean bundleWithIdExists(String bundleId) {
    if(getActiveBundleId().equalsIgnoreCase(bundleId))
      return true;
    return false;
  }

  @Override
  public BundleItem getCurrentBundleMetadata() {
    return null;
  }

  // Can messages be processed using this bundle and current state?
  @Override
  public Boolean bundleIsReady() {
    return true;
  }

  // register inference processing thread with the bundle manager--
  // bundles cannot be changed as long as threads are actively using it.
  @Override
  public void registerInferenceProcessingThread(Future thread) {

  }

  @Override
  public void changeBundle(String bundleId) throws Exception {

  }

  // some kind of event notification system camsys setup?
  protected void timingHook() {
  }

}
