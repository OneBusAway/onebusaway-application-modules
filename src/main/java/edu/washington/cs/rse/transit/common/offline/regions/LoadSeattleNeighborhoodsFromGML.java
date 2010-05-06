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
package edu.washington.cs.rse.transit.common.offline.regions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Neighborhood;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

public class LoadSeattleNeighborhoodsFromGML {

    private static final Map<String, String> NEIGHBORHOOD_PARAMS = CollectionsLibrary.getValuesAsMap("ogr:S_HOOD",
            "setSpecificName", "ogr:L_HOOD", "setGeneralName");

    public static void main(String[] args) throws Exception {

        ApplicationContext context = MetroKCApplicationContext.getApplicationContext();

        LoadSeattleNeighborhoodsFromGML loader = new LoadSeattleNeighborhoodsFromGML();
        context.getAutowireCapableBeanFactory().autowireBean(loader);

        File root = new File("/Users/bdferris/l10n/edu.washington.cs.rse.transit/data");
        File f = new File(root, "OriginalDataSources/SeattleNeighborhoodsFromSeattleCityClerksOffice.gml");
        File largeNeighborhoods = new File(root,
                "LocationLabels/Neighborhoods/SeattleNeighborhoodsFromSeattleCityClerksOffice.kml");
        File smallNeighborhoods = new File(root,
                "LocationLabels/SmallNeighborhoods/SeattleNeighborhoodsFromSeattleCityClerksOffice.kml");

        loader.loadNeighborhoods(f, largeNeighborhoods, smallNeighborhoods);
    }

    /***************************************************************************
     * Private Members
     **************************************************************************/

    private RegionsDAO _dao;

    private Set<String> _expand = new HashSet<String>();

    private Map<String, String> _rename = new HashMap<String, String>();

    public void setRegionsDAO(RegionsDAO dao) {
        _dao = dao;
    }

    private void loadNeighborhoods(File f, File largeNeighborhoods, File smallNeighborhoods) throws Exception {

        addExpandOperation("UNIVERSITY DISTRICT");
        addExpandOperation("CASCADE");
        addExpandOperation("INTERBAY");
        addExpandOperation("DOWNTOWN");
        addExpandOperation("SEWARD PARK");

        addRename("CENTRAL AREA", "Central District");
        addRename("Industrial District", "South of Downtown");
        addRename("Pike-Market", "Downtown");
        addRename("Central Business District", "Downtown");
        addRename("BALLARD", "Ballard");
        addRename("QUEEN ANNE", "Queen Anne");
        addRename("MAGNOLIA", "Magnolia");
        addRename("WEST SEATTLE", "West Seattle");
        addRename("BEACON HILL", "Beacon Hill");
        addRename("RAINIER VALLEY", "Rainier Valley");
        addRename("DELRIDGE", "Delridge");
        addRename("CAPITOL HILL", "Capitol Hill");
        addRename("LAKE CITY", "Lake City");
        addRename("NORTHGATE", "Northgate");

        Map<String, Region> largeRegions = new HashMap<String, Region>();
        Map<String, Region> subRegions = new HashMap<String, Region>();

        List<Neighborhood> ns = new ArrayList<Neighborhood>();

        _dao.readRegionsFromGML(f, ns, Neighborhood.class, "ogr:clrk_nhd", NEIGHBORHOOD_PARAMS);

        for (Neighborhood n : ns) {
            if (n.getGeneralName() != null && n.getSpecificName() != null && !n.getSpecificName().equals("OOO")) {
                applyTransforms(n);
                if (n.hasGeneralName()) {

                    Region region = getRegionByName(largeRegions, n.getGeneralName());
                    Region region2 = getRegionByName(subRegions, n.getSpecificName());

                    for (List<IGeoPoint> boundary : n.getBoundaries()) {
                        region.addBoundary(boundary);
                        region2.addBoundary(boundary);
                    }

                } else {
                    Region region = getRegionByName(largeRegions, n.getSpecificName());
                    for (List<IGeoPoint> boundary : n.getBoundaries())
                        region.addBoundary(boundary);
                }
            }
        }

        _dao.writeRegionsAsKML(largeRegions.values(), largeNeighborhoods);
        _dao.writeRegionsAsKML(subRegions.values(), smallNeighborhoods);

    }

    /***************************************************************************
     * Private Members
     **************************************************************************/

    private void addExpandOperation(String generalName) {
        _expand.add(generalName);
    }

    private void addRename(String fromName, String toName) {
        _rename.put(fromName, toName);
    }

    private void applyTransforms(Neighborhood hood) {

        if (_expand.contains(hood.getGeneralName()))
            hood.setGeneralName(Neighborhood.NO_BROADER_TERM);

        if (_rename.containsKey(hood.getGeneralName()))
            hood.setGeneralName(_rename.get(hood.getGeneralName()));

        if (_rename.containsKey(hood.getSpecificName()))
            hood.setSpecificName(_rename.get(hood.getSpecificName()));
    }

    private Region getRegionByName(Map<String, Region> regions, String name) {
        Region region = regions.get(name);
        if (region == null) {
            region = new Region();
            region.setName(name);
            regions.put(name, region);
        }

        return region;
    }
}
