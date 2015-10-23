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

import org.onebusaway.admin.service.DiffService;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DiffTask implements Runnable {
	Logger _log = LoggerFactory.getLogger(DiffTask.class);
	protected DiffService diffService;
	MultiCSVLogger logger;
	
	@Autowired
	public void setDiffService(DiffService diffService) {
		this.diffService = diffService;
	}
	
	@Autowired
	public void setLogger(MultiCSVLogger logger) {
		this.logger = logger;
	}
	
	protected String _filename1;
	protected String _filename2;
	protected String _output;

	public void run() {
		logger.difflogHeader(_output);
		try {
			diffService.diff(_filename1, _filename2);
		} catch (Exception e) {
			_log.error("diff failed:", e);
		}
		_log.info("exiting difftask");
	}
	
	void log(int lineNum, String line){
		if (_output != null)
			logger.difflog(lineNum, line);
	}
}