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

import org.apache.commons.lang.StringUtils;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by sbrown on 11/28/16.
 */
public class MissingBlockForTrips extends GtfsFileHandler implements Runnable {
    private static final String FILENAME_TRIP = "trips_not_in_block.csv";
    private static final String FILENAME_BLOCK = "blocks_not_in_block.csv";
    private static final String GTFS_BLOCK = "block.txt";
    private static final String GTFS_TRIPS = "trips.txt";
    private static final String LINK_AGENCY = "KCM";
    // TODO this should come from configuration
    private static final String LINK_ROUTE = "599";
    private static final String LINK_ROUTE_ID = "100479";
    private static Logger _log = LoggerFactory.getLogger(GtfsFullValidationTask.class);
    protected ApplicationContext _applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
    }

    @Autowired
    protected MultiCSVLogger _logger;

    public void setLogger(MultiCSVLogger logger) {
        _logger = logger;
    }

    @Override
    public void run() {
        _log.info("MissingBlockForTrips Task Starting (v3)");

        Set<String> linkTrips = new HashSet<String>();
        Set<String> linkBlocks = new HashSet<String>();
        Set<String> missingBlocks = new HashSet<String>();
        List<String> unlinkedTrips = new ArrayList<String>();

        Map<String, String> linkTripEntries = new HashMap<String, String>();
        GtfsBundles gtfsBundles = getGtfsBundles(_applicationContext);


        for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
            String gtfsFilePath = gtfsBundle.getPath().toString();
            if (gtfsBundle.getAgencyIdMappings().containsKey(LINK_AGENCY)) {

                CSVData blockCsvData = getCSVData(gtfsFilePath, GTFS_BLOCK);
                if (blockCsvData == null) {
                    continue;
                }
                String blockHeaders = blockCsvData.getHeader();
                List<String> blockRows = blockCsvData.getRows();
                if (StringUtils.isBlank(blockHeaders)) {
                    _log.info("missing blocks.txt for " + gtfsBundle);
                    continue;
                }
                int routeIndex = getIndexForValue(blockHeaders, "block_route_num");
                int blockSeqIndex = getIndexForValue(blockHeaders, "block_seq_num");

                for (String row : blockRows) {
                    String[] cols = row.split(",");
                    if (LINK_ROUTE.equals(cols[routeIndex])) {
                        String linkBlock = cols[blockSeqIndex].trim();
                        linkBlocks.add(linkBlock);
                    }
                }


                CSVData tripCsvData = getCSVData(gtfsFilePath, GTFS_TRIPS);
                String tripHeaders = tripCsvData.getHeader();
                List<String> tripRows = tripCsvData.getRows();
                int blockIndex = getIndexForValue(tripHeaders, "block_id");
                int routeIdIndex = getIndexForValue(tripHeaders, "route_id");
                int tripIndex = getIndexForValue(tripHeaders, "trip_id");
                for (String row : tripRows) {
                    String[] cols = row.split(",");
                    String tripBlock = cols[blockIndex].trim();
                    String routeId = cols[routeIdIndex].trim();
                    String tripId = cols[tripIndex];
                    if (LINK_ROUTE_ID.equals(routeId)) {
                        linkTrips.add(tripId);
                        _log.info("adding LINK block=" + tripBlock + " for trip= " + tripId);
                        linkTripEntries.put(tripBlock, row);
                        if (linkBlocks.contains(tripBlock)) {
                            _log.info("found block " + tripBlock + " for trip= " + tripId);
                        } else {
                            _log.info("missing block " + tripBlock + " for trip= " + tripId);
                            missingBlocks.add(tripBlock);
                            unlinkedTrips.add(row);
                        }
                    }
                }

                _log.info(linkTrips.size() + " trips found in trips.txt for LINK data with " + unlinkedTrips.size()
                        + " unmatched.  " + missingBlocks.size() + " blocks not matched.");


            }
        }
        // Write out results to .csv file
        _logger.header(FILENAME_TRIP, "route_id,service_id,trip_id,trip_headsign,trip_short_name,direction_id,block_id,shape_id,peak_flag,fare_id");
        for (String trip : unlinkedTrips) {
            _logger.logCSV(FILENAME_TRIP, trip);
        }

        _logger.header(FILENAME_BLOCK, "block_id");
        for (String block : missingBlocks) {
            _logger.logCSV(FILENAME_BLOCK, block);
        }

        _log.info("MissingBlockForTrips Task Exiting");
    }
}
