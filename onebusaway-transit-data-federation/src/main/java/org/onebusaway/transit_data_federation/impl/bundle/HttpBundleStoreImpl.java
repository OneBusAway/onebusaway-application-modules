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

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.model.bundle.BundleFileItem;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.transit_data_federation.services.bundle.BundleStoreService;
import org.onebusaway.transit_data_federation.util.HttpServiceClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * A bundle source backed by an External Web Server.
 * 
 * @author jmaki
 *
 */
public class HttpBundleStoreImpl implements BundleStoreService {

	private static Logger _log = LoggerFactory.getLogger(HttpBundleStoreImpl.class);
	
	private static final DateTimeFormatter _updatedDateFormatter = ISODateTimeFormat.dateTimeNoMillis();

	// number of times to retry downloading a file if the request fails.
	private static final int _fileDownloadRetries = 2;


	private String _bundleRootPath = null;

	private HttpServiceClient _apiLibrary;

	public HttpBundleStoreImpl(String bundleRootPath, HttpServiceClient apiLibrary) throws Exception {
		_bundleRootPath = bundleRootPath;
		_apiLibrary = apiLibrary;
	}

	private List<BundleItem> getBundleListFromHttp() throws Exception {
		ArrayList<BundleItem> output = new ArrayList<BundleItem>();

		_log.info("Getting current bundle list from Server...");	     
		List<JsonObject> bundles = _apiLibrary.getItemsForRequest("bundle",/*"staged",*/"list");

		for(JsonObject itemToAdd : bundles) {
			BundleItem item = new BundleItem();

			item.setId(itemToAdd.get("id").getAsString());
			item.setName(itemToAdd.get("name").getAsString());

			/*ArrayList<String> applicableAgencyIds = new ArrayList<String>();
			for(JsonElement _subitemToAdd : itemToAdd.get("applicable-agency-ids").getAsJsonArray()) {
				String agencyIdToAdd = _subitemToAdd.getAsString();  
				applicableAgencyIds.add(agencyIdToAdd);
			}*/

			item.setServiceDateFrom(ServiceDate.parseString(
					itemToAdd.get("service-date-from").getAsString().replace("-", "")));
			item.setServiceDateTo(ServiceDate.parseString(
					itemToAdd.get("service-date-to").getAsString().replace("-", "")));
			
			if(itemToAdd.get("created") != null)
			  item.setCreated(_updatedDateFormatter.parseDateTime(itemToAdd.get("created").getAsString()));
      
			if(itemToAdd.get("updated") != null)
			  item.setUpdated(_updatedDateFormatter.parseDateTime(itemToAdd.get("updated").getAsString()));


			ArrayList<BundleFileItem> files = new ArrayList<BundleFileItem>();
			for(JsonElement _subitemToAdd : itemToAdd.get("files").getAsJsonArray()) {
				JsonObject subitemToAdd = _subitemToAdd.getAsJsonObject();

				BundleFileItem fileItemToAdd = new BundleFileItem();
				fileItemToAdd.setFilename(subitemToAdd.get("filename").getAsString());
				fileItemToAdd.setMd5(subitemToAdd.get("md5").getAsString());
				files.add(fileItemToAdd);
			}
			item.setFiles(files);

			output.add(item);
		}

		_log.info("Found " + output.size() + " bundle(s) available from the Server.");

		return output;
	}

	private void downloadUrlToLocalPath(URL url, File destFilename, String expectedMd5) throws Exception {	  

		try {
			_log.info("Downloading bundle item from " + url + "...");

			File containerPath = destFilename.getParentFile();
			if(!containerPath.exists() && !containerPath.mkdirs()) {
				throw new Exception("Could not create parent directories for path " + destFilename);
			}

			if(!destFilename.createNewFile()) {
				throw new Exception("Could not create empty file at path " + destFilename);	      
			}

			// download file 
			FileOutputStream out = new FileOutputStream(destFilename.getPath());
			BufferedOutputStream bufferedOut = new BufferedOutputStream(out, 1024);

			MessageDigest md5Hasher = MessageDigest.getInstance("MD5");
			BufferedInputStream in = new java.io.BufferedInputStream(url.openStream());    

			byte data[] = new byte[1024];
			while(true) {
				int readBytes = in.read(data, 0, data.length);
				if(readBytes < 0) {
					break;
				}

				md5Hasher.update(data, 0, readBytes);
				bufferedOut.write(data, 0, readBytes);
			}

			bufferedOut.close();
			out.close();
			in.close();         

			// check hash
			byte messageDigest[] = md5Hasher.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]); 

				if(hex.length() == 1) {
					hexString.append('0');
				}

				hexString.append(hex);
			}

			if(!hexString.toString().equals(expectedMd5)) {
				throw new Exception("MD5 hash doesn't match.");
			}	    
		} catch(Exception e) {
			if(!destFilename.delete()) {
				if(destFilename.exists()) {
					throw new Exception("Could not delete corrupted file " + destFilename);
				}
			}
			throw e;
		}
	}

	@Override
	public List<BundleItem> getBundles() throws Exception {

		List<BundleItem> output = new ArrayList<BundleItem>();

		List<BundleItem> bundlesFromHttp = getBundleListFromHttp();
		for(BundleItem bundle : bundlesFromHttp) {
			boolean bundleIsValid = true;

			// ensure bundle path exists locally
			File bundleRoot = new File(_bundleRootPath, bundle.getId());

			if(!bundleRoot.exists()) {
				if(!bundleRoot.mkdirs())  {
					throw new Exception("Creation of bundle root for " + bundle.getId() + " at " + bundleRoot + " failed.");
				}
			}

			for(BundleFileItem file : bundle.getFiles()) {
				File fileInBundlePath = new File(bundleRoot, file.getFilename());

				// see if file already exists locally and matches its MD5
				if(fileInBundlePath.exists()) {
					if(!file.verifyMd5(fileInBundlePath)) {
						_log.warn("File " + fileInBundlePath + " is corrupted; removing.");

						if(!fileInBundlePath.delete()) {
							_log.error("Could not remove corrupted file " + fileInBundlePath);
							bundleIsValid = false;
							break;
						}
					}
				}

				// if the file is not there, or was removed above, try to download it again from the TDM.
				if(!fileInBundlePath.exists()) {
					int tries = _fileDownloadRetries;

					while(tries > 0) {
						URL fileDownloadUrl = _apiLibrary.buildUrl("bundle","deploy", bundle.getId(), "file", file.getFilename(), "get");

						try {
							downloadUrlToLocalPath(fileDownloadUrl, fileInBundlePath, file.getMd5());
						} catch(Exception e) {
							tries--;
							if(tries == 0) {
								bundleIsValid = false;
							}

							_log.warn("Download of " + fileDownloadUrl + " failed (" + e.getMessage() + ");" + 
									" retrying (retries left=" + tries + ")");

							continue;
						}

						// file was downloaded successfully--break out of retry loop
						break;
					}
				}
			} // for each file

			if(bundleIsValid) {
				_log.info("Bundle " + bundle.getId() + " files pass checksums; added to list of local bundles.");
				output.add(bundle);

			} else {
				_log.warn("Bundle " + bundle.getId() + " files do NOT pass checksums; skipped.");
			}
		} // for each bundle

		return output;
	}

  @Override
  public boolean isLegacyBundle() {
    return false;
  }

}
