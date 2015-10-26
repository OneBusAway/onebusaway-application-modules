/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.server.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.admin.service.server.impl.BundleServerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleServerServiceImplTest {
	private static Logger _log = LoggerFactory.getLogger(BundleServerServiceImplTest.class);

  private BundleServerServiceImpl serverService = null;
  @Before
  public void setUp() throws Exception {
    serverService = new BundleServerServiceImpl() {
      @Override
      public String start(String instanceId) {
        return instanceId;
      }
      @Override
      public String pollPublicDns(String instanceId, int maxWaitSeconds) {
        return "localhost";
      }
      @Override
      public String findPublicDns(String instanceId) {
        return "localhost";
      }
      @Override
      public String findPublicIp(String instanceId) {
        return "127.0.0.1";
      }
      @Override
      public String stop(String instanceId) {
        return instanceId;
      }
      @Override
      public boolean ping(String instanceId) {
        return true;
      }
    };
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testStart() throws Exception {
    String instanceId = serverService.start("i-instance");
    long start = System.currentTimeMillis();
    String dns = serverService.pollPublicDns(instanceId, 60);
    long end = System.currentTimeMillis();
    _log.debug("found dns=" + dns + " after " + (end-start)/1000 + " seconds");
    String ip = serverService.findPublicIp(instanceId);
    _log.debug("found ip=" + ip);
    assertNotNull(ip);
    
    start = System.currentTimeMillis();

    int count = 0;
    while (!serverService.ping(instanceId) && count < 60) {
      Thread.sleep(1000);
      count++;
    }
    end = System.currentTimeMillis();
    _log.debug("ping=" + serverService.ping(instanceId) + " after " + (end-start)/1000 + " seconds");
    serverService.stop(instanceId);
  }

}
