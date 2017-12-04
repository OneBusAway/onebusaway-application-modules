/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.onebusaway.admin.service.BarcodeService;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BarcodeServiceImpl implements BarcodeService {

	private RemoteConnectionService remoteConnectionService;
	
	@Override
	public InputStream getQRCodesInBatch(File stopIdFile, int dimensions) throws IOException{
		String tdmHost = System.getProperty("tdm.host");
		String url = buildURL(tdmHost, "/barcode/batchGen?img-dimension=", dimensions);
		InputStream barCodeZip = remoteConnectionService.postBinaryData(url, stopIdFile, InputStream.class);
		return barCodeZip;
	}
	
	private String buildURL(String host, String api, int dimensionParam) {
		 return "http://" + host + "/api" + api + String.valueOf(dimensionParam);
	}

	/**
	 * @param remoteConnectionService the remoteConnectionService to set
	 */
	@Autowired
	public void setRemoteConnectionService(
			RemoteConnectionService remoteConnectionService) {
		this.remoteConnectionService = remoteConnectionService;
	}

}
