/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.api.impl;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class BundleSwapBroadcaster {

    private static final Logger _log = LoggerFactory.getLogger(BundleSwapBroadcaster.class);

    private TransitDataService _transitDataService;

    private String _bundleId;

    private ThreadPoolTaskScheduler _executor;

    private RefreshService _refreshService;

    @Autowired
    public void setTransitDataService(TransitDataService transitDataService) {
        _transitDataService = transitDataService;
    }

    @Autowired
    public void setRefreshService(RefreshService refreshService) {
        _refreshService = refreshService;
    }

    @PostConstruct
    public void init() {
        BackgroundThread thread = new BackgroundThread();
        new Thread(thread).start();
    }

    @PreDestroy
    public void destroy() {
        _executor.shutdown();
    }

    private void testForNewBundle() {
        String bundleId = _transitDataService.getActiveBundleId();
        _log.info("testing bundle, new bundle={} old bundle={}", bundleId, _bundleId);
        if (!_bundleId.equals(bundleId)) {
            _bundleId = bundleId;
            _refreshService.refresh(RefreshableResources.BUNDLE_SWAP);
        }
    }


    public class BackgroundThread implements Runnable {

        @Override
        public void run() {
            _log.info("blocking on active bundle....");
            _bundleId = _transitDataService.getActiveBundleId();
            _log.info("active bundle = " + _bundleId + ", initializing cron trigger");
            _executor = new ThreadPoolTaskScheduler();
            _executor.initialize();
            CronTrigger hourly = new CronTrigger("5 0 * * * *"); // 5 sec past hour
            _executor.schedule(new Runnable() {
                @Override
                public void run() {
                    testForNewBundle();
                }
            }, hourly);
            _log.info("initialed with bundle ID {}", _bundleId);
        }
    }
}
