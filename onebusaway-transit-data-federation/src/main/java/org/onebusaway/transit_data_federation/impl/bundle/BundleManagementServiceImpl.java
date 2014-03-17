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
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationRecordDao;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BundleManagementServiceImpl implements BundleManagementService {

  private static Logger _log = LoggerFactory.getLogger(BundleManagementServiceImpl.class);
  private BundleConfigDao _bundleConfigDao;
  
  @Autowired
  public void setBundleConfigDao(BundleConfigDao bundleConfigDao) {
    _bundleConfigDao = bundleConfigDao;
  }
  
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
	
}
