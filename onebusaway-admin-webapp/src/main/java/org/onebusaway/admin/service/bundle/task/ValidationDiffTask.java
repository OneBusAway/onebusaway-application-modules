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
package org.onebusaway.admin.service.bundle.task;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.impl.DiffTransformer;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidationDiffTask extends DiffTask {
	BundleRequestResponse bundleRequestResponse;
	ConfigurationServiceClient configurationServiceClient;
	
	@Autowired
	public void setBundleRequestResponse(BundleRequestResponse bundleRequestResponse) {
		this.bundleRequestResponse = bundleRequestResponse;
	}

	@Autowired
	public void setConfigurationServiceClient(ConfigurationServiceClient configurationServiceClient) {
		this.configurationServiceClient = configurationServiceClient;
	}
	
	private final String FILENAME = "gtfs_stats.csv";
	
	public void run(){
		diffService.setDiffTransformer(new ValidationDiffTransformer());
    _output = "diff_log.csv";
    try {
      _filename1 = configurationServiceClient.getItem("admin",
          "bundleStagingDir")
          + File.separator
          + bundleRequestResponse.getRequest().getBundleName()
          + File.separator
          + "outputs" 
          + File.separator 
          + FILENAME;
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		_filename2 = bundleRequestResponse.getResponse().getBundleOutputDirectory() + File.separator + FILENAME;
		super.run();
	}

	class ValidationDiffTransformer implements DiffTransformer {
		@Override
		public List<String> transform(List<String> preTransform) {
			List<String> diffResult = new LinkedList<String>(); 
			int minusLineNum = 0;
			int plusLineNum = 0;
			for(String line: preTransform.subList(2, preTransform.size())){
				diffResult.add(line);
				if(line.startsWith("@")){
					try{
						int firstComma = line.indexOf(",");
						minusLineNum = Integer.parseInt(line.substring(line.indexOf("-")+1, firstComma));
						plusLineNum = Integer.parseInt(line.substring(line.indexOf("+")+1, line.indexOf(",", firstComma+1)));
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				else{
					if (line.startsWith("-")){
						log(minusLineNum, line);
						minusLineNum++;
					}
					else if (line.startsWith("+")){
						log(plusLineNum, line);
						plusLineNum++;
					}
				}
			}
			return diffResult;
		}
	}
}