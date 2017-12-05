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
package org.onebusaway.realtime.api;

/**
 * Support for realtime vehicle types.
 * For convenience this maps to Route.types
 * See https://developers.google.com/transit/gtfs/reference/#routestxt
 */
public enum EVehicleType {
    UNSET (-1),
    LIGHT_RAIL(0),
    SUBWAY (1),
    RAIL (2),
    BUS (3),
    FERRY (4),
    CABLE_CAR (5),
    GONDOLA (6),
    FUNICULAR (7);


    private final int gtfsType;
    EVehicleType(int gtfsType) {
        this.gtfsType = gtfsType;
    }

    // TODO use a more idiomatic approach here
    public static EVehicleType toEnum(int i) {
        EVehicleType type = UNSET;
        switch (i) {
            case -1:
                type = UNSET;
                break;
            case 0:
                type = LIGHT_RAIL;
                break;
            case 1:
                type = SUBWAY;
                break;
            case 2:
                type = RAIL;
                break;
            case 3:
                type = BUS;
                break;
            case 4:
                type = FERRY;
                break;
            case 5:
                type = CABLE_CAR;
                break;
            case 6:
                type = GONDOLA;
                break;
            case 7:
                type = FUNICULAR;
                break;
            default:
                type = UNSET;
        }
         return type;
    }

    public int getGtfsType() {
        return gtfsType;
    }
    public String toLabel() { return toString().toLowerCase(); }
}
