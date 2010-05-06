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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.collections.coverage.CoverageMaps;
import edu.washington.cs.rse.collections.coverage.IMutableCoverageMap;
import edu.washington.cs.rse.collections.coverage.ITimeInterval;
import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.ChangeDate;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternTimeBlock;

public class MetroKCServicePatternTimeBlocks {

    // 2 hours
    private static final int ROUTE_SPLIT_TIME = 2 * 60;

    public static void main(String[] args) {
        ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
        MetroKCServicePatternTimeBlocks m = new MetroKCServicePatternTimeBlocks();
        ctx.getAutowireCapableBeanFactory().autowireBean(m);
        m.run();
    }

    private MetroKCDAO _dao;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public void run() {

        List<Route> routes = _dao.getAllRoutes();
        List<ChangeDate> dates = _dao.getChangeDates();

        for (ChangeDate date : dates) {
            System.out.println("service revision=" + date);
            for (Route route : routes) {

                System.out.println("  route=" + route.getNumber());

                List<ServicePatternTimeBlock> blocks = _dao.getServicePatternTimeBlocksByRoute(date, route);

                System.out.println("    blocks in=" + blocks.size());

                int out = 0;

                for (ServicePatternTimeBlock block : blocks) {
                    List<Integer> times = _dao.getPassingTimesByServicePatternTimeBlock(block);
                    IMutableCoverageMap m = CoverageMaps.create(ROUTE_SPLIT_TIME);
                    for (int time : times)
                        m.addValue(time);
                    for (ITimeInterval interval : m.getIntervals()) {
                        int from = (int) interval.getFrom();
                        int to = (int) interval.getTo();
                        ServicePatternTimeBlock reducedBlock = new ServicePatternTimeBlock(block.getServicePattern(),
                                block.getScheduleType(), from, to);
                        _dao.save(reducedBlock);
                        out++;
                    }
                }

                System.out.println("    blocks out=" + out);
            }
        }
    }
}
