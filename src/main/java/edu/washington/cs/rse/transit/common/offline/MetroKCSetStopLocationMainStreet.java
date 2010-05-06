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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StreetName;

public class MetroKCSetStopLocationMainStreet {

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
        MetroKCSetStopLocationMainStreet m = new MetroKCSetStopLocationMainStreet();
        ctx.getAutowireCapableBeanFactory().autowireBean(m);
        m.run();
    }

    private MetroKCDAO _dao;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public void run() throws Exception {

        List<StopLocation> stops = _dao.getAllStopLocations();

        int count = 1000;
        List<StopLocation> updates = new ArrayList<StopLocation>(count);

        for (StopLocation stop : stops) {
            StreetName street = _dao.getStreetNameByStopId(stop.getId());
            stop.setMainStreetName(street);
            updates.add(stop);
            if (updates.size() >= count)
                flush(updates);
        }

        if (!updates.isEmpty())
            flush(updates);
    }

    private void flush(List<StopLocation> updates) {
        System.out.println("flush...");
        _dao.saveOrUpdateAllEntities(updates);
        updates.clear();
    }
}
