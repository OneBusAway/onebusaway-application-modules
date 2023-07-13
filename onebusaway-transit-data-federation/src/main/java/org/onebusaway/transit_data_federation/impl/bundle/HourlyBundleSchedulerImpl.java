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
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimerTask;

/**
 * Change bundles on the hour.
 */
public class HourlyBundleSchedulerImpl implements BundleScheduler {

  private static Logger _log = LoggerFactory
          .getLogger(HourlyBundleSchedulerImpl.class);

  private BundleManagementService _service;
  private ThreadPoolTaskScheduler _taskScheduler;

  public void setup(BundleManagementService service, ThreadPoolTaskScheduler taskScheduler) {
    _service = service;
    _taskScheduler = taskScheduler;
    if (_taskScheduler != null) {
      _log.info("Starting bundle discovery and switch threads...");

      BundleDiscoveryUpdateThread discoveryThread = new BundleDiscoveryUpdateThread();
      _taskScheduler.schedule(discoveryThread, discoveryThread);

      BundleSwitchUpdateThread switchThread = new BundleSwitchUpdateThread();
      _taskScheduler.schedule(switchThread, switchThread);
    }

  }

  protected class BundleDiscoveryUpdateThread extends TimerTask implements
          Trigger {

    // required for subclass
    public BundleDiscoveryUpdateThread() {
    }

    @Override
    public void run() {
      try {
        _service.discoverBundles();
      } catch (Exception e) {
        _log.error("Error updating bundle list: " + e.getMessage(), e);
        e.printStackTrace();
      }
    }

    @Override
    public Date nextExecutionTime(TriggerContext arg0) {
      Date lastTime = arg0.lastScheduledExecutionTime();
      if (lastTime == null) {
        lastTime = new Date();
      }

      Calendar calendar = new GregorianCalendar();
      calendar.setTime(lastTime);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.SECOND, 0);

      int minute = calendar.get(Calendar.MINUTE);
      calendar.set(Calendar.MINUTE, minute + 15);

      return calendar.getTime();
    }
  }

  protected class BundleSwitchUpdateThread extends TimerTask implements Trigger {

    // required for subclass
    public BundleSwitchUpdateThread() {
    }

    @Override
    public void run() {
      try {
        _service.refreshApplicableBundles();
        _service.reevaluateBundleAssignment();
      } catch (Exception e) {
        _log.error("Error re-evaluating bundle assignment: " + e.getMessage());
        e.printStackTrace();
      }
    }

    @Override
    public Date nextExecutionTime(TriggerContext arg0) {
      Date lastTime = arg0.lastScheduledExecutionTime();
      if (lastTime == null) {
        lastTime = new Date();
      }

      Calendar calendar = new GregorianCalendar();
      calendar.setTime(lastTime);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.SECOND, 1); // go into the next hour/day

      // if we have no current bundle, keep retrying every minute
      // to see if we're just waiting for the clock to rollover to the next day
      if (_service.getApplicableBundlesSize() > 0 && _service.getCurrentBundleId() == null) {
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE, minutes + 1);

      } else {
        calendar.set(Calendar.MINUTE, 0);

        int hour = calendar.get(Calendar.HOUR);
        calendar.set(Calendar.HOUR, hour + 1);
      }

      return calendar.getTime();
    }
  }


}
