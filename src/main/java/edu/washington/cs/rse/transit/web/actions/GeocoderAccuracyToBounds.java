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
package edu.washington.cs.rse.transit.web.actions;

import java.util.HashMap;
import java.util.Map;

public class GeocoderAccuracyToBounds {
    private static Map<Integer, Integer> _accuracyToBounds = new HashMap<Integer, Integer>();

    static {
        _accuracyToBounds.put(9, 900);
        _accuracyToBounds.put(8, 900);
        _accuracyToBounds.put(7, 900);
        _accuracyToBounds.put(6, 6000);
        _accuracyToBounds.put(5, 12000);
        _accuracyToBounds.put(4, 48000);
        _accuracyToBounds.put(3, 97000);
    }

    public static int getBoundsInFeetByAccuracy(int accuracy) {
        Integer r = _accuracyToBounds.get(accuracy);
        if( r == null )
            r = 900;
        return r;
    }
}
