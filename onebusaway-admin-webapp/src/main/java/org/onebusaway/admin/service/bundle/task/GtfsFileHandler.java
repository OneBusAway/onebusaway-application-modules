/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.bundle.tasks.GtfsReadingSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Base class to provide some raw GTFS/CSV file handling capabilities for some of
 * the validation tasks.
 */
public class GtfsFileHandler {

    private static Logger _log = LoggerFactory.getLogger(GtfsFileHandler.class);

    /**
     * retrieve a header row and a list of data rows for the given GTFS/CSV file
     * @param csvFilePath
     * @param entryName
     * @return
     */
    protected CSVData getCSVData(String csvFilePath, String entryName) {
        String header = null;
        ArrayList<String> rows = new ArrayList<String>();

        try (ZipFile zipFile = new ZipFile(csvFilePath)) {

            ZipEntry entry = zipFile.getEntry(entryName);
            if (entry != null) {
                InputStream stream = zipFile.getInputStream(entry);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = br.readLine()) != null) {
                    if (header == null) {
                        header = line;
                    } else {
                        rows.add(line);
                    }
                }
                br.close();
            }
        } catch (IOException e) {
            _log.error("Error reading CSV file: " + e.getMessage(), e);
        }
        _log.debug("return " + rows.size() + " rows from file " + entryName);
        return new CSVData(header, rows);
    }

    /**
     * return the 0-based index of the column "columnValue" in the CSV
     * headers.
     * @param headers
     * @param columnValue
     * @return
     */
    protected int getIndexForValue(String headers, String columnValue) {
        int i = 0;
        for (String t: headers.split(",")) {
            if (columnValue.equals(t)) {
                return i;
            }
            i++;
        }
        throw new IllegalArgumentException("unknown column " + columnValue + " in headers " + headers);
    }

    protected GtfsBundles getGtfsBundles(ApplicationContext context) {
        return GtfsReadingSupport.getGtfsBundles(context);
    }

    /**
     * Represents a CSV file as single line header and list of rows of data.
     */
    public class CSVData {
        private String header;
        private List<String> rows;
        public CSVData(String header, List<String> rows) {
            this.header = header;
            this.rows = rows;
        }

        public String getHeader() {
            return header;
        }
        public List<String> getRows() {
            return rows;
        }

    }

}
