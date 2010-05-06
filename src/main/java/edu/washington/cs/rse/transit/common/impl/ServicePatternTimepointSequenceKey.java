/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * 
 */
package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.transit.common.model.ServicePatternKey;

public class ServicePatternTimepointSequenceKey {

    private ServicePatternKey _spKey;

    private int _timepointId;

    private int _sequence;

    public ServicePatternTimepointSequenceKey(ServicePatternKey spKey, int timepointId, int sequence) {
        _spKey = spKey;
        _timepointId = timepointId;
        _sequence = sequence;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServicePatternTimepointSequenceKey))
            return false;
        ServicePatternTimepointSequenceKey key = (ServicePatternTimepointSequenceKey) obj;
        return _spKey.equals(key._spKey) && _timepointId == key._timepointId && _sequence == key._sequence;
    }

    @Override
    public int hashCode() {
        return _spKey.hashCode() + _timepointId * 3 + _sequence * 7;
    }

    @Override
    public String toString() {
        return "sp=" + _spKey + " timepoint=" + _timepointId + " sequence=" + _sequence;
    }
}