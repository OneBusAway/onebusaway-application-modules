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
package edu.washington.cs.rse.transit.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

import junit.framework.TestCase;

public class RegionsDAOTest extends TestCase {
    
    public void testGo() throws IOException, SAXException {
        RegionsDAO dao = new RegionsDAO();
        File file = new File("/Users/bdferris/l10n/edu.washington.cs.rse.transit/data/LocationLabels/Places/Docks.kml");
        dao.readRegionsFromKML(file, new ArrayList<Region>());
    }

    public void testReadRegionsFromKML02() throws IOException, SAXException {
        RegionsDAO dao = new RegionsDAO();
        File file = new File(
                "/Users/bdferris/l10n/edu.washington.cs.rse.transit/data/LocationLabels/Neighborhoods/SeattleNeighborhoodsFromSeattleCityClerksOffice.kml");
        dao.readRegionsFromKML(file, new ArrayList<Region>());
    }

}
