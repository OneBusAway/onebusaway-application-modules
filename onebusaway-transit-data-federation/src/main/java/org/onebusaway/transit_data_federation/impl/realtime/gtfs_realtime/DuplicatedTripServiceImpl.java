/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicatedTripServiceImpl implements DuplicatedTripService{

    private static final Logger _log = LoggerFactory.getLogger(DuplicatedTripServiceImpl.class);

    private GtfsRealtimeEntitySource _entitySource;
    public void setGtfsRealtimeEntitySource(GtfsRealtimeEntitySource source) {
        _entitySource = source;
    }

    private DuplicatedTripServiceParser duplicatedTripServiceParser = null;

    @Override
    public AddedTripInfo handleDuplicatedDescriptor(GtfsRealtime.TripUpdate tu) {
        try {
            return getParser().parse(tu);
        } catch (Throwable t) {
            _log.error("source-exception {}", t, t);
            return null;
        }
    }

    private DuplicatedTripServiceParser getParser() {
        // we need to lazy load due to the entitySource dependency
        if (duplicatedTripServiceParser == null) {
            duplicatedTripServiceParser = new DuplicatedTripServiceParserImpl();
            duplicatedTripServiceParser.setGtfsRealtimeEntitySource(_entitySource);
        }
        return duplicatedTripServiceParser;
    }
}
