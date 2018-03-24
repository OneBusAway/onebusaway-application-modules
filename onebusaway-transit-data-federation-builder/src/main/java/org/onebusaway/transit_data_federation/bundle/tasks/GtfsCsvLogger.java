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

import com.conveyal.gtfs.model.Statistic;

public class GtfsCsvLogger {

	private Logger _log = LoggerFactory.getLogger(GtfsCsvLogger.class);
	private String _fileName = "gtfs_stats.csv";
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
	}
	
	public void close() {
		if (buff != null) {
			buff.stream.close();
			buff = null;
		}
	}

	public void header() {
		_log.info("creating file=" + _fileName);
		buff = new Buffer(_fileName);
		buff.stream.print("agency,route_count,trip_count,stop_count,stop_times_count,calendar_service_start,calendar_service_end,calendar_start_date,calendar_end_date\n");
	}

	public void addHeader(String headerText) {
		_log.info("creating file=" + _fileName);
		buff = new Buffer(_fileName);
		buff.stream.print(headerText);
	}
	
	private String sanitize(String s) {
		if (s == null) return null;
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

	public void logStat(String id, Statistic s) {
		buff.stream.print(s.getAgencyId());
		buff.stream.print(",");
		buff.stream.print(s.getRouteCount());
		buff.stream.print(",");
		buff.stream.print(s.getTripCount());
		buff.stream.print(",");
		buff.stream.print(s.getStopCount());
		buff.stream.print(",");
		buff.stream.print(s.getStopTimeCount());
		buff.stream.print(",");
		buff.stream.print(s.getCalendarServiceStart());
		buff.stream.print(",");
		buff.stream.print(s.getCalendarServiceEnd());
		buff.stream.print(",");
		buff.stream.print(s.getCalendarStartDate());
		buff.stream.print(",");
		buff.stream.print(s.getCalendarEndDate());
		buff.stream.print("\n");
	}

	public void setFilename(String fileName) {
		_fileName = fileName;
		
	}

	public void log(String[] stringArray) {
		int i = 0;
		for (String s : stringArray) {
			if (i != 0) buff.stream.print(",");
			buff.stream.print(s);
			i++;
		}
	}


}
