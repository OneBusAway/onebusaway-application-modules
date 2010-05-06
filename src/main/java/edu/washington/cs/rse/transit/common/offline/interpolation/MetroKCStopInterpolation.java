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
package edu.washington.cs.rse.transit.common.offline.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.ServicePattern;

public class MetroKCStopInterpolation {

    public static void main(String[] args) {
        ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
        MetroKCStopInterpolation m = new MetroKCStopInterpolation();
        ctx.getAutowireCapableBeanFactory().autowireBean(m);
        m.run();
    }

    private MetroKCDAO _dao;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public void run() {

        // Pre-Cache
        _dao.getAllTimepoints();

        List<ServicePattern> servicePatterns = getServicePatterns();

        for (ServicePattern servicePattern : servicePatterns) {

            ServicePatternStopInterpolation interp = new ServicePatternStopInterpolation(_dao, servicePattern);
            interp.run();
        }
    }

    private List<ServicePattern> getServicePatterns() {
      
      if( true )
        return _dao.getAllServicePatterns();
      
      List<ServicePattern> servicePatterns = new ArrayList<ServicePattern>();
      servicePatterns.add(_dao.getActiveServicePatternById(21118324));
      return servicePatterns;
    }
}
