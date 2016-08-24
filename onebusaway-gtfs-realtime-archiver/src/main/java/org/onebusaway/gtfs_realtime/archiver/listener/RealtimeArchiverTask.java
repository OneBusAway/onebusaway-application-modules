/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.listener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs_realtime.archiver.listener.RealtimeArchiverTask.BackgroundInitTask;
import org.onebusaway.gtfs_realtime.archiver.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public abstract class RealtimeArchiverTask implements ApplicationListener {

  protected static final Logger _log = LoggerFactory.getLogger(
        GtfsRealtimeArchiverTask.class);
  protected ScheduledExecutorService _scheduledExecutorService;
  protected ScheduledFuture<?> _refreshTask;
  protected FeedService _feedService;
  protected int _refreshInterval = 30;
  protected boolean initialized = false;

  public RealtimeArchiverTask() {
    super();
  }

  @Autowired
  public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
    _log.info("executor=" + scheduledExecutorService);
    _scheduledExecutorService = scheduledExecutorService;
  }

  @Autowired
  public void setFeedService(FeedService feedService) {
    _feedService = feedService;
  }

  @Autowired
  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  public void setInitialized(boolean isInitialized) {
    this.initialized = isInitialized;
  }

  public boolean getInitialized() {
    return this.initialized;
  }

  @PostConstruct
  public void start() {
    BackgroundInitTask bit = new BackgroundInitTask();
    new Thread(bit).start();
    _log.error("PostConstruct Complete");
  }

  @PreDestroy
  public void stop() {
    _log.info("stopping");
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
  }

  protected class BackgroundInitTask implements Runnable {
    @Override
    public void run() {
      try {
        init();
      } catch (Throwable ex) {
        _log.warn("Error initializing", ex);
      }
    }
  }

  abstract protected void init();

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof ContextRefreshedEvent) {
      setInitialized(true);
    }
  }
}