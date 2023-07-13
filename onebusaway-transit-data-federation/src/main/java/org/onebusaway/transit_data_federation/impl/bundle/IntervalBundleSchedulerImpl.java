/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Change bundles up to every interval minutes.
 */
public class IntervalBundleSchedulerImpl implements BundleScheduler {
  private static Logger _log = LoggerFactory
          .getLogger(IntervalBundleSchedulerImpl.class);

  private BundleManagementService _service;
  private ThreadPoolTaskScheduler _taskScheduler;

  private long intervalInMillis;
  public IntervalBundleSchedulerImpl(int intervalInMinutes) {
    intervalInMillis = intervalInMinutes * 60 * 1000;
  }

  public void setup(BundleManagementService service, ThreadPoolTaskScheduler taskScheduler) {
    _service = service;
    _taskScheduler = taskScheduler;
    if (_taskScheduler != null) {
      _log.info("Starting bundle discovery and switch threads...");
      BundleDiscoveryUpdateThread thread = new BundleDiscoveryUpdateThread();
      _taskScheduler.scheduleWithFixedDelay(thread, intervalInMillis);
    }
  }

  protected class BundleDiscoveryUpdateThread implements Runnable {

    @Override
    public void run() {
      try {
        _service.discoverBundles();
        _service.refreshApplicableBundles();
        _service.reevaluateBundleAssignment();
      } catch (Exception e) {
        _log.error("Error updating bundle list: " + e.getMessage(), e);
        e.printStackTrace();
      }

    }
  }
}
