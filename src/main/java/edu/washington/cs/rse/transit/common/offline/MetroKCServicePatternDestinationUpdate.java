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
package edu.washington.cs.rse.transit.common.offline;

import java.util.List;
import java.util.SortedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vividsolutions.jts.geom.Point;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;

public class MetroKCServicePatternDestinationUpdate {

    public static void main(String[] args) {
        ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
        MetroKCServicePatternDestinationUpdate m = new MetroKCServicePatternDestinationUpdate();
        ctx.getAutowireCapableBeanFactory().autowireBean(m);
        m.run();
    }

    private MetroKCDAO _dao;

    private RegionsDAO _regionsDAO;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    @Autowired
    public void setNeighborhoodsDAO(RegionsDAO neighborhoodsDAO) {
        _regionsDAO = neighborhoodsDAO;
    }

    public void run() {

        List<ServicePattern> servicePatterns = _dao.getAllServicePatterns();
        System.out.println("servicePatterns=" + servicePatterns.size());

        for (ServicePattern servicePattern : servicePatterns) {

            System.out.println("  servicePattern=" + servicePattern.getId());

            setDestination(servicePattern);

            _dao.update(servicePattern);
        }
    }

    private void setDestination(ServicePattern servicePattern) {

        List<Timepoint> timepoints = _dao.getTimepointsByServicePattern(servicePattern);

        Timepoint last = timepoints.get(timepoints.size() - 1);
        Point location = last.getTransNode().getLocation();

        SortedMap<Layer, Region> regions = _regionsDAO.getRegionsByLocation(location);

        if (regions.isEmpty()) {
            System.out.println("NO INFO!");
            servicePattern.setGeneralDestination(last.getName40());
            servicePattern.setSpecificDestination(last.getName40());
        } else {
            StringBuilder b = new StringBuilder();
            for (Region r : regions.values()) {
                if (b.length() > 0)
                    b.append(" - ");
                b.append(r.getName());
            }
            String label = b.toString();
            servicePattern.setGeneralDestination(label);
            servicePattern.setSpecificDestination(label + " - " + last.getName40());
            servicePattern.setTimepointDestination(last.getName40());
        }
    }
}
