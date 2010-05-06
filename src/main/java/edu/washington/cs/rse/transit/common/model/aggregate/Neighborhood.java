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
package edu.washington.cs.rse.transit.common.model.aggregate;

public class Neighborhood extends Region {

    private static final long serialVersionUID = 1L;

    public static final String NO_BROADER_TERM = "NO BROADER TERM";

    private String _generalName;

    private String _specificName;

    public void setGeneralName(String name) {
        _generalName = name;
    }

    public boolean hasGeneralName() {
        return _generalName != null && !_generalName.equals(NO_BROADER_TERM);
    }

    public String getGeneralName() {
        return _generalName;
    }

    public void setSpecificName(String name) {
        _specificName = name;
    }

    public String getSpecificName() {
        return _specificName;
    }

    /***************************************************************************
     * {@link Region} Interface
     **************************************************************************/

    public String getName() {
        if (_generalName.equals(NO_BROADER_TERM))
            return _specificName;
        return _generalName + " - " + _specificName;
    }
}
