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
package org.onebusaway.transit_data_federation.services.bundle;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;

public interface BundleManagementService {
  
  /*
   * Getters and Setters
   */
  
  public String getBundleStoreRoot();

  public void setBundleStoreRoot(String path) throws Exception;

  public void setTime(Date time);

  public void setServiceDate(ServiceDate serviceDate);

  public ServiceDate getServiceDate();

  public void setStandaloneMode(boolean standalone);

  public boolean getStandaloneMode();
  
  
  /*
   * Bundle Loading and Swapping
   */
  
  /**
   * This method retrieves the list of bundles that are available to us,
   * and updates the internal list appropriately. It does not switch any bundles.
   */
  public void discoverBundles() throws Exception;


/**
   * This method calculates which of the bundles available to us are valid for today,
   * and updates the internal list appropriately. It does not switch any bundles.
   */
  public void refreshApplicableBundles();


  /**
   * Recalculate which of the bundles that are available and active for today we should be 
   * using. Switch to that bundle if not already active. 
   */
  public void reevaluateBundleAssignment() throws Exception;
  
  
  /*
   * Service Methods
   */
  
  public String getActiveBundleId();
  
  BundleMetadata getBundleMetadata();
  
  public void changeBundle(String bundleId) throws Exception;

  public BundleItem getCurrentBundleMetadata();

  public List<BundleItem> getAllKnownBundles();

  public boolean bundleWithIdExists(String bundleId);

  // is bundle finished loading? 
  public Boolean bundleIsReady();

  // thread reference keepers
  public void registerInferenceProcessingThread(Future thread);

}
