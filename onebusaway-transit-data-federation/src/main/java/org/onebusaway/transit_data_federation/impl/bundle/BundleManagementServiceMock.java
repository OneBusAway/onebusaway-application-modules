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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;

public class BundleManagementServiceMock implements BundleManagementService {

	@Override
	public String getBundleStoreRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBundleStoreRoot(String path) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTime(Date time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setServiceDate(ServiceDate serviceDate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServiceDate getServiceDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStandaloneMode(boolean standalone) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getStandaloneMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void discoverBundles() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshApplicableBundles() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reevaluateBundleAssignment() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getActiveBundleId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BundleMetadata getBundleMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void changeBundle(String bundleId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BundleItem getCurrentBundleMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BundleItem> getAllKnownBundles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean bundleWithIdExists(String bundleId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean bundleIsReady() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void registerInferenceProcessingThread(Future thread) {
		// TODO Auto-generated method stub
		
	}
}
