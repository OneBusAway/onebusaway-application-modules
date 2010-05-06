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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

public class LoadRegionsFromKML {

    private RegionsDAO _dao;

    private File _dataDirectory;

    public static void main(String[] args) throws IOException, SAXException {
        ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
        LoadRegionsFromKML loader = (LoadRegionsFromKML) context.getBean("loadRegionsFromKML");
        loader.run();
    }

    public void setRegionsDAO(RegionsDAO dao) {
        _dao = dao;
    }

    public void setLocationLabelsDirectory(File file) {
        _dataDirectory = file;
    }

    public void run() throws IOException, SAXException {

        Map<String, Layer> dirToLayer = new HashMap<String, Layer>();
        dirToLayer.put("Cities", new Layer("Cities", 0));
        dirToLayer.put("Neighborhoods", new Layer("Neighborhoods", 1));
        dirToLayer.put("SmallNeighborhoods", new Layer("SmallNeighborhoods", 2));
        dirToLayer.put("Places", new Layer("Places", 3));

        // Add the layers
        for (Layer layer : dirToLayer.values())
            _dao.addLayer(layer);

        if (!(_dataDirectory.exists() && _dataDirectory.isDirectory()))
            return;

        for (String dirName : dirToLayer.keySet()) {

            Layer layer = dirToLayer.get(dirName);
            File dir = new File(_dataDirectory, dirName);

            if (dir.exists() && dir.isDirectory()) {
                File[] k = dir.listFiles(new KMLFilenameFilter());
                if (k != null) {
                    for (File f : k) {
                      System.out.println(f);
                        List<Region> regions = new ArrayList<Region>();
                        _dao.readRegionsFromKML(f, regions);
                        _dao.addRegions(layer, regions);
                    }
                }
            }
        }
    }

    private static class KMLFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".kml");
        }
    }
}
