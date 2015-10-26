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

import java.io.File;

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.config.BundleConfigDao;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationRecordDao;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.util.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Future;


public class BundleManagementServiceImpl implements BundleManagementService {

  // how long to wait for inference threads to exit before forcefully stopping
  // them
  // when the command to change the bundle has been received.
  protected static final int INFERENCE_PROCESSING_THREAD_WAIT_TIMEOUT_IN_SECONDS = 60;

  protected static final int MAX_EXPECTED_THREADS = 3000;

  private static Logger _log = LoggerFactory
      .getLogger(BundleManagementServiceImpl.class);

  protected BundleConfigDao _bundleConfigDao;

  private List<BundleItem> _allBundles = new ArrayList<BundleItem>();

  protected HashMap<String, BundleItem> _applicableBundles = new HashMap<String, BundleItem>();

  protected volatile List<Future> _inferenceProcessingThreads = new ArrayList<Future>();

  protected String _bundleRootPath = null;

  private BundleStoreService _bundleStore = null;

  protected boolean _standaloneMode = true;
  
  protected boolean _builderMode = false;

  protected boolean _bundleIsReady = false;

  protected String _currentBundleId = null;

  protected ServiceDate _currentServiceDate = null;

  protected HttpServiceClient _restApiLibrary;

  @Autowired
  protected TransitDataService _transitDataService;

  @Autowired
  protected TransitGraphDao _transitGraphDao;

  @Autowired
  protected FederatedTransitDataBundle _bundle;

  @Autowired
  protected ThreadPoolTaskScheduler _taskScheduler;

  @Autowired
  protected RefreshService _refreshService;

  @Autowired
  public void set_restApiLibrary(HttpServiceClient _restApiLibrary) {
    this._restApiLibrary = _restApiLibrary;
  }

  @Autowired
  public void setBundleConfigDao(BundleConfigDao bundleConfigDao) {
    _bundleConfigDao = bundleConfigDao;
  }

  @PostConstruct
  protected void setup() throws Exception {  
    if (_builderMode) {
      String bundleRoot = System.getProperty("bundle.root");
      _log.info("builder mode:  using bundle.root of " + bundleRoot);
      _bundleStore = new LocalBundleStoreImpl(bundleRoot);
      return;
    }
    if(!_standaloneMode) {
      _bundleStore = new HttpBundleStoreImpl(_bundleRootPath, _restApiLibrary);        
    }
    else{
      _bundleStore = new LocalBundleStoreImpl(_bundleRootPath); 	
    }
    
    try{
      discoverBundles();
    }catch(Exception e){
      _log.error("Unable to retreive Bundle List.");
      if(!(_bundleStore instanceof LocalBundleStoreImpl)){
        _log.info("Attempting to load local Bundle...");
        _bundleStore = new LocalBundleStoreImpl(_bundleRootPath);
        discoverBundles();
      }
      else{
        throw e; 
      }
    }
    
    refreshApplicableBundles();
    reevaluateBundleAssignment();

    if (_taskScheduler != null) {
      _log.info("Starting bundle discovery and switch threads...");

      BundleDiscoveryUpdateThread discoveryThread = new BundleDiscoveryUpdateThread();
      _taskScheduler.schedule(discoveryThread, discoveryThread);

      BundleSwitchUpdateThread switchThread = new BundleSwitchUpdateThread();
      _taskScheduler.schedule(switchThread, switchThread);
    }

  }

  /******************
   * Setup Methods
   ***************** 
   */
  public void discoverBundles() throws Exception {
    _allBundles = _bundleStore.getBundles();
  }

  /**
   * This method calculates which of the bundles available to us are valid for
   * today, and updates the internal list appropriately. It does not switch any
   * bundles.
   */
  public synchronized void refreshApplicableBundles() {
    _applicableBundles.clear();

    for (BundleItem bundle : _allBundles) {
      if (bundle.isApplicableToDate(getServiceDate())) {
        _log.info("Bundle " + bundle.getName() + "(" + bundle.getId() + ")"
            + " is active for today; adding to list of active bundles.");

        _applicableBundles.put(bundle.getId(), bundle);
      }
    }
  }

  /**
   * Recalculate which of the bundles that are available and active for today we
   * should be using. Switch to that bundle if not already active.
   */
  public void reevaluateBundleAssignment() throws Exception {
    if (_applicableBundles.size() == 0) {
      _log.error("No valid and active bundles found!");
      return;
    }

    // sort bundles by preference
    ArrayList<BundleItem> bestBundleCandidates = new ArrayList<BundleItem>(
        _applicableBundles.values());
    Collections.sort(bestBundleCandidates);

    BundleItem bestBundle = bestBundleCandidates.get(bestBundleCandidates
        .size() - 1);
    _log.info("Best bundle is " + bestBundle.getName() + " (" + bestBundle.getId() + ")");

    changeBundle(bestBundle.getId(), bestBundle.getName());
  }

  /********************
   * Getters / Setters
   ********************/

  public String getBundleStoreRoot() {
    return _bundleRootPath;
  }

  public void setBundleStoreRoot(String path) throws Exception {
    File bundleRootPath = new File(path);

    if (!bundleRootPath.exists() || !bundleRootPath.canWrite()) {
      throw new Exception("Bundle store path " + bundleRootPath
          + " does not exist or is not writable.");
    }

    this._bundleRootPath = path;
  }

  public void setTime(Date time) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(time);
    _currentServiceDate = new ServiceDate(cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));

    refreshApplicableBundles();
  }

  public void setServiceDate(ServiceDate serviceDate) {
    _currentServiceDate = serviceDate;

    refreshApplicableBundles();
  }

  public ServiceDate getServiceDate() {
    if (_currentServiceDate != null)
      return _currentServiceDate;
    else
      return new ServiceDate();
  }

  public void setStandaloneMode(boolean standalone) {
    _standaloneMode = standalone;
  }

  public boolean getStandaloneMode() {
    return _standaloneMode;
  }

  public void setBuilderMode(boolean builderMode) {
    _builderMode = builderMode;
  }
  
  /********************
   * Service Methods
   ********************/

  @Override
  public String getActiveBundleId() {
	
    if (_bundleConfigDao == null) {
      if(_currentBundleId == null){
        _log.error("config error:  bundleConfigDao is null");
        return null; 
      }
      else{
        _log.warn("config error:  bundleConfigDao is null, returning currentBundleId value instead.");
        _log.debug("Legacy Bundle most likely not detected");
        return _currentBundleId;
      } 
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
    return _allBundles;
  }

  @Override
  public boolean bundleWithIdExists(String bundleId) {
    return _applicableBundles.containsKey(bundleId);
  }

  @Override
  public BundleItem getCurrentBundleMetadata() {
    return _applicableBundles.get(_currentBundleId);
  }

  // Can messages be processed using this bundle and current state?
  @Override
  public Boolean bundleIsReady() {
    if (!_builderMode)
      return _bundleIsReady;
    return true;
  }

  // register inference processing thread with the bundle manager--
  // bundles cannot be changed as long as threads are actively using it.
  @Override
  public void registerInferenceProcessingThread(Future thread) {
    _inferenceProcessingThreads.add(thread);

    // keep our thread list from getting /too/ big unnecessarily
    if (_inferenceProcessingThreads.size() > MAX_EXPECTED_THREADS) {
      removeDeadInferenceThreads();
    }
  }

  @Override
  public void changeBundle(String bundleId) throws Exception {
    changeBundle(bundleId, bundleId);
  }
  
  public void changeBundle(String bundleId, String bundleName) throws Exception {
    if (bundleId == null || !_applicableBundles.containsKey(bundleId)) {
      throw new Exception("Bundle " + bundleName
          + " is not valid or does not exist.");
    }

    if (bundleId.equals(_currentBundleId)) {
      _log.info("Received command to change to " + bundleName
          + " (" + bundleId + ")"
          + "; bundle is already active.");
      return;
    }

    _log.info("Switching to bundle " + bundleName + " (" + bundleId + ")...");
    _bundleIsReady = false;

    // wait until all inference processing threads have exited...
    int t = INFERENCE_PROCESSING_THREAD_WAIT_TIMEOUT_IN_SECONDS / 5;
    while (t-- >= 0) {
      removeDeadInferenceThreads();
      _log.info("Waiting for all inference processing threads to exit... "
          + _inferenceProcessingThreads.size() + " thread(s) left.");

      // have all inference threads finished yet?
      if (allInferenceThreadsHaveExited()) {
        break;

        // forcefully cancel threads when we timeout
      } else if (t == 0) {
        for (Future thread : _inferenceProcessingThreads) {
          if (!thread.isDone() && !thread.isCancelled()) {
            thread.cancel(true);
          }
        }

        _inferenceProcessingThreads.clear();

        break;
      }

      Thread.yield();
      Thread.sleep(5 * 1000);
    }

    _log.info("All inference processing threads have now exited--changing bundle...");

    // switch bundle files
    File path;
    if(_bundleStore.isLegacyBundle()){
      path = new File(_bundleRootPath);
    }else{
      path = new File(_bundleRootPath, bundleName);
    }
    _bundle.setPath(path);

    try {
      _refreshService.refresh(RefreshableResources.TRANSIT_GRAPH);

      // give child classes a chance to do work here
      timingHook();

      _refreshService.refresh(RefreshableResources.CALENDAR_DATA);
      _refreshService.refresh(RefreshableResources.ROUTE_COLLECTIONS_DATA);
      _refreshService
          .refresh(RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA);
      _refreshService.refresh(RefreshableResources.STOP_SEARCH_DATA);
      _refreshService.refresh(RefreshableResources.WALK_PLANNER_GRAPH);
      _refreshService.refresh(RefreshableResources.BLOCK_INDEX_DATA);
      _refreshService.refresh(RefreshableResources.BLOCK_INDEX_SERVICE);
      _refreshService.refresh(RefreshableResources.STOP_TRANSFER_DATA);
      _refreshService.refresh(RefreshableResources.SHAPE_GEOSPATIAL_INDEX);
      _refreshService.refresh(RefreshableResources.STOP_GEOSPATIAL_INDEX);
      _refreshService.refresh(RefreshableResources.TRANSFER_PATTERNS);
      _refreshService.refresh(RefreshableResources.NARRATIVE_DATA);

    } catch (Exception e) {
      _log.error("Bundle " + bundleName + "(" + bundleId + ")"
          + " failed to load. Disabling for this session...");
      _applicableBundles.remove(bundleId);
      reevaluateBundleAssignment();

      throw new Exception("Bundle " + bundleName + "(" +  bundleId + ")"
          + " loading exception. Root exception follows.", e);
    }

    _log.info("Refresh/reload of bundle data complete.");

    // attempt to cleanup any dereferenced data--I know this is a debate in the
    // Java space--
    // do you let the magic GC do it's thing or force its hand? With a profiler,
    // I found this helps
    // keep memory use more consistently under 2x initial heap size. FWIW.
    System.gc();
    System.gc();
    _log.info("Garbage collection after bundle switch complete.");

    _currentBundleId = bundleId;
    _bundleIsReady = true;
    _log.info("New bundle is now ready.");

    // need to do after bundle is ready so TDS can not block
    removeAndRebuildCache();
    _log.info("Cache rebuild complete.");

    return;
  }

  // some kind of event notification system camsys setup?
  protected void timingHook() {
  }

  /*************************
   * Private Helper Methods
   *************************/

  private void removeAndRebuildCache() {
    // give subclasses a chance to do work
    timingHook();

    _log.info("Clearing all caches...");
    for (CacheManager cacheManager : CacheManager.ALL_CACHE_MANAGERS) {
      _log.info("Found " + cacheManager.getName());

      for (String cacheName : cacheManager.getCacheNames()) {
        _log.info(" > Cache: " + cacheName);
        cacheManager.getCache(cacheName).flush();
        cacheManager.clearAllStartingWith(cacheName);
      }

      cacheManager.clearAll(); // why not?
    }

    // Rebuild cache
    try {
      List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService
          .getAgenciesWithCoverage();

      for (AgencyWithCoverageBean agencyWithCoverage : agenciesWithCoverage) {
        AgencyBean agency = agencyWithCoverage.getAgency();

        ListBean<String> stopIds = _transitDataService
            .getStopIdsForAgencyId(agency.getId());
        for (String stopId : stopIds.getList()) {
          _transitDataService.getStop(stopId);
        }

        ListBean<String> routeIds = _transitDataService
            .getRouteIdsForAgencyId(agency.getId());
        for (String routeId : routeIds.getList()) {
          _transitDataService.getStopsForRoute(routeId);
        }
      }

      Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();
      for (TripEntry trip : _transitGraphDao.getAllTrips()) {
        AgencyAndId shapeId = trip.getShapeId();
        if (shapeId != null && shapeId.hasValues())
          shapeIds.add(shapeId);
      }

      for (AgencyAndId shapeId : shapeIds) {
        _transitDataService.getShapeForId(AgencyAndIdLibrary
            .convertToString(shapeId));
      }
      _log.info("cache clearing complete!");
    } catch (Exception e) {
      _log.error("Exception during cache rebuild: ", e.getMessage());
    }
  }

  protected void removeDeadInferenceThreads() {
    List<Future> finishedThreads = new ArrayList<Future>();

    // find all threads that are not running...
    for (Future thread : _inferenceProcessingThreads) {
      if (thread.isDone() || thread.isCancelled()) {
        finishedThreads.add(thread);
      }
    }

    // ...and then remove them from our list of processing threads
    for (Future deadThread : finishedThreads) {
      _inferenceProcessingThreads.remove(deadThread);
    }
  }

  protected boolean allInferenceThreadsHaveExited() {
    removeDeadInferenceThreads();

    return (_inferenceProcessingThreads.size() == 0);
  }

  protected class BundleSwitchUpdateThread extends TimerTask implements Trigger {

    // required for subclass
    public BundleSwitchUpdateThread() {
    }

    @Override
    public void run() {
      try {
        refreshApplicableBundles();
        reevaluateBundleAssignment();
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
      if (_applicableBundles.size() > 0 && _currentBundleId == null) {
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

  protected class BundleDiscoveryUpdateThread extends TimerTask implements
      Trigger {

    // required for subclass
    public BundleDiscoveryUpdateThread() {
    }

    @Override
    public void run() {
      try {
        discoverBundles();
      } catch (Exception e) {
        _log.error("Error updating bundle list: " + e.getMessage());
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

}
