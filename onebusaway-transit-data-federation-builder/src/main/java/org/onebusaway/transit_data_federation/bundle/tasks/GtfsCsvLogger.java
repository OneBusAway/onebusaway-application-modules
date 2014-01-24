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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsCsvLogger {

	private Logger _log = LoggerFactory.getLogger(GtfsCsvLogger.class);

	private File basePath;
	private Buffer buff;

	public void setBasePath(File path) {
		this.basePath = path;
	}

	public void open() {
		// integration tests may not have a path
		if (basePath == null) {
			basePath = new File(System.getProperty("java.io.tmpdir"));
			_log.warn("GtfsCsvLogger initialized without path:  using "
					+ basePath);
		}
		if (!basePath.exists()) {
			basePath.mkdirs();
		}
		
		header();
	}
	
	public void close() {
		if (buff != null) {
			buff.stream.close();
			buff = null;
		}
	}

	public void header() {
		String fileName = "gtfs_stats.csv";  // do not include basepath
		_log.info("creating file=" + fileName);
		buff = new Buffer(fileName);
		buff.stream.print("agency,stat,value\n");
	}
	
	public void logStat(String agency, String stat, Object value) {
		if (value == null) {
			buff.stream.print(sanitize(agency) + "," + sanitize(stat) + "," + null + "\n");
		} else {
			buff.stream.print(sanitize(agency) + "," + sanitize(stat) + "," + sanitize(value.toString()) + "\n");
		}
	}
	
	private String sanitize(String s) {
		if (s.contains(",") || s.contains("\"")) {
			s = "\"" + s.replace("\"", "\"\"") + "\"";
		}
		return s;
	}

	private class Buffer {
		PrintStream stream;

		Buffer(String file) {
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(new File(basePath/*parent directory*/, file/*filename*/),
						true);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			stream = new PrintStream(outputStream);
		}
	}
}
