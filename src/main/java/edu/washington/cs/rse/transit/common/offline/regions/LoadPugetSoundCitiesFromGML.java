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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

public class LoadPugetSoundCitiesFromGML {

    private static final Map<String, String> CITY_PARAMS = CollectionsLibrary.getValuesAsMap("ogr:CITYNAME", "setName");

    public static void main(String[] args) throws Exception {

        ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
        LoadPugetSoundCitiesFromGML loader = new LoadPugetSoundCitiesFromGML();
        context.getAutowireCapableBeanFactory().autowireBean(loader);

        File root = new File("/Users/bdferris/l10n/edu.washington.cs.rse.transit/data");
        File inputDir = new File(root, "OriginalDataSources/Cities");
        File[] inputFiles = inputDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".gml");
            }
        });

        File outputFile = new File(root, "LocationLabels/Cities/PugetSoundCities.kml");

        loader.loadNeighborhoods(inputFiles, outputFile);
    }

    /***************************************************************************
     * Private Members
     **************************************************************************/

    private RegionsDAO _dao;

    @Autowired
    public void setRegionsDAO(RegionsDAO dao) {
        _dao = dao;
    }

    public void loadNeighborhoods(File[] inputFiles, File outputFile) throws Exception {

        List<Region> ns = new ArrayList<Region>();

        for (File f : inputFiles)
            _dao.readRegionsFromGML(f, ns, Region.class, "ogr:city", CITY_PARAMS);

        Collection<Region> consolidated = _dao.consolidateRegionsByName(ns);
        _dao.writeRegionsAsKML(consolidated, outputFile);
    }
}
