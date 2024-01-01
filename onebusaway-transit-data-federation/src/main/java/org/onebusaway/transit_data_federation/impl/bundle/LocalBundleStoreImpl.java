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

import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;

/*import org.joda.time.DateTime;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A bundle source backed by an on-disk directory.
 * 
 * @author jmaki
 *
 */
public class LocalBundleStoreImpl extends AbstractBundleStoreImpl implements BundleStoreService {

	private static Logger _log = LoggerFactory.getLogger(LocalBundleStoreImpl.class);

	private boolean legacyBundle = false;

	public LocalBundleStoreImpl(String bundleRootPath) {
		super(bundleRootPath);
	}

	@Override
	public List<BundleItem> getBundles() {
		ArrayList<BundleItem> output = new ArrayList<BundleItem>();
		if (_bundleRootPath == null) return output;

		File bundleRoot = new File(_bundleRootPath);

		if(!bundleRoot.isDirectory()) {    
			return output;
		}

		for(String filename : bundleRoot.list()) {
			File possibleBundle = new File(bundleRoot, filename);

			if(possibleBundle.isDirectory()) {
				File calendarServiceObjectFile = new File(possibleBundle, CALENDAR_DATA);
				File metadataFile = new File(possibleBundle, METADATA);

				if(!calendarServiceObjectFile.exists()) {
					_log.warn("Could not find " + CALENDAR_DATA + " in local bundle '" + filename + "'; skipping. Not a bundle?");
					continue;
				}
				
				if(!metadataFile.exists()) {
					_log.warn("Could not find " + METADATA + " in local bundle '" + filename + "'; skipping. Not a bundle?");
					continue;
				}
				
				try{
	        BundleItem validLocalBundle = createBundleItem(calendarServiceObjectFile, metadataFile, filename);
	        output.add(validLocalBundle);
	      }catch(Exception e) {
	        continue;
	      }   
			}
			else if(possibleBundle.isFile() && possibleBundle.getName().equalsIgnoreCase(CALENDAR_DATA)){
			  try{
			    String parentFilename = bundleRoot.getName();
	        BundleItem validLocalBundle = createBundleItem(possibleBundle, parentFilename);
	        output.add(validLocalBundle);
	        setLegacyBundle(true);
	        break;
	      }catch(Exception e) {
	        continue;
	      }   
			}

		}
		
		return output;
	}

	public boolean isLegacyBundle() {
    return legacyBundle;
  }

  public void setLegacyBundle(boolean legacyBundle) {
    this.legacyBundle = legacyBundle;
  }
      
}
