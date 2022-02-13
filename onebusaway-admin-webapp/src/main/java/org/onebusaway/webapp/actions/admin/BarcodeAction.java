/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.webapp.actions.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.service.BarcodeService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
	@Result(type = "redirectAction", name = "redirect", params = {
     "actionName", "barcode"}),
    @Result(name = "downloadZip", type = "stream", 
    params = {"contentType", "application/zip",
    		  "inputName", "qrBatchStream",
    		  "contentDisposition", "attachment;fileName=\"qrBatch.zip\"",
    		  "bufferSize", "1024"}) 
})
@AllowedMethods({"genBusStopCode", "generateCodesBatch"})
public class BarcodeAction extends OneBusAwayNYCAdminActionSupport {
	private static Logger _log = LoggerFactory.getLogger(BarcodeAction.class);
	private static final long serialVersionUID = 1L;

	private int busStopId;
	private int edgeDimension;
	private int batchEdgeDimension;
	private InputStream qrBatchStream;
	private File stopIdCsvFile;
	
	private String qrResourceUrl = "";
	
	private BarcodeService barCodeService;
	
	public String getQrResourceUrl() {
		return qrResourceUrl;
	}

	public int getBusStopId() {
		return busStopId;
	}

	public void setBusStopId(int busStopId) {
		this.busStopId = busStopId;
	}

	public int getEdgeDimension() {
		return edgeDimension;
	}

	public void setEdgeDimension(int edgeDimension) {
		this.edgeDimension = edgeDimension;
	}
	
	public int getBatchEdgeDimension() {
		return batchEdgeDimension;
	}

	public void setBatchEdgeDimension(int batchEdgeDimension) {
		this.batchEdgeDimension = batchEdgeDimension;
	}
	
	/**
	 * @return the qrBatchStream
	 */
	public InputStream getQrBatchStream() {
		return qrBatchStream;
	}
	
	/**
	 * @param qrBatchStream the qrBatchStream to set
	 */
	public void setQrBatchStream(InputStream qrBatchStream) {
		this.qrBatchStream = qrBatchStream;
	}

	/**
	 * @return the stopIdCsvFile
	 */
	public File getStopIdCsvFile() {
		return stopIdCsvFile;
	}

	/**
	 * @param stopIdCsvFile the stopIdCsvFile to set
	 */
	public void setStopIdCsvFile(File stopIdCsvFile) {
		this.stopIdCsvFile = stopIdCsvFile;
	}

	public String genBusStopCode() {
		
		
		// local resource proxies the TDM request
		qrResourceUrl = "../api/barcode/getByStopId/" + String.valueOf(getBusStopId());
		
		addDimensionParam(edgeDimension);
		
		// need to display the image on the page.
		_log.debug("qrResourceUrl=" + qrResourceUrl);
		
		return SUCCESS;
	}
	
	public String generateCodesBatch() {
		
		try {
			qrBatchStream = barCodeService.getQRCodesInBatch(stopIdCsvFile, batchEdgeDimension);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "downloadZip";
	}
	
	private void addDimensionParam(int dimension) {
		String dimensionParam = "img-dimension=";
		qrResourceUrl = qrResourceUrl + "?";
		qrResourceUrl = qrResourceUrl + dimensionParam + String.valueOf(dimension);
	}

	/**
	 * @param barCodeService the barCodeService to set
	 */
	@Autowired
	public void setBarCodeService(BarcodeService barCodeService) {
		this.barCodeService = barCodeService;
	}

}