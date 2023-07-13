/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build and load a bundle to match to gtfs-rt.  Then run comparisons against
 * the reults of the gtfs-rt.
 */
public abstract class AbstractGtfsRealtimeIntegrationTest {

  protected static Logger _log = LoggerFactory.getLogger(AbstractGtfsRealtimeIntegrationTest.class);

  private BundleContext _bundleContext;
  protected BundleContext getBundleContext() {
    return _bundleContext;
  }
  private BundleLoader _bundleLoader;
  public BundleLoader getBundleLoader() {
    return _bundleLoader;
  }

  private BundleBuilder _bundleBuilder;
  public BundleBuilder getBundleBuilder() {
    return _bundleBuilder;
  }

  protected abstract String getIntegrationTestPath();

  @Before
  public void setup() throws Exception {
    _bundleBuilder = new BundleBuilder();
    _bundleBuilder.setup(getIntegrationTestPath());
    _bundleContext = _bundleBuilder.getBundleContext();

    _bundleLoader = new BundleLoader(_bundleContext);
    _bundleLoader.create(getPaths());
    _bundleLoader.load();

  }

  protected abstract String[] getPaths();

  @After
  public void cleanup() {
    _bundleLoader.close();
  }

}
