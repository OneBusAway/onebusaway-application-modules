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
package edu.washington.cs.rse.transit.web.oba.common.server.ops;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import junit.framework.TestCase;

public class StopAreaOpTest extends TestCase {

    public void testSimple() throws InterruptedException {

        ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
        MetroKCDAO dao = (MetroKCDAO) context.getBean("metroKCDAO");
        StopAreaOp op = new StopAreaOp(dao);

        CacheManager manager = CacheManager.create(getClass().getResource("ehcache.xml"));
        manager.addCache("tmp");
        Cache cache = manager.getCache("tmp");

        op.startup(cache);

        Thread.sleep(10 * 1000);

        op.shutdown();
    }
}
