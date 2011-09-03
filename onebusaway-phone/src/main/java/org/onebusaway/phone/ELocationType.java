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
package org.onebusaway.phone;

public enum ELocationType {

    STOP(0), TIMEPOINT(1);

    private int _id;

    private ELocationType(int id) {
        _id = id;
    }

    public int getId() {
        return _id;
    }

    public static ELocationType getById(int id) {
        for (ELocationType locationType : ELocationType.values()) {
            if (locationType.getId() == id)
                return locationType;
        }
        throw new IllegalArgumentException("no location type with the specified id");
    }
}
