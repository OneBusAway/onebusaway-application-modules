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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Context information for building or loading a bundle for use in
 * integration tests.
 */
public class BundleContext {
  protected static Logger _log = LoggerFactory.getLogger(BundleContext.class);
  protected String _bundleGzipURI;
  protected String _bundleRootDir;
  protected String _bundleIndexURI;

  public BundleContext() {
  }

  public void setup(String bundleRootDir, String bundleGzipURI, String bundleIndexURI) {
    this._bundleRootDir = bundleRootDir;
    this._bundleGzipURI = bundleGzipURI;
    this._bundleIndexURI = bundleIndexURI;
  }

  public String loadBundleGzipURIFromEnv() {
    return System.getProperty("bundle.index.json");
  }

  public void prepForLoad(String bundleGzipURI) {
    _bundleGzipURI = bundleGzipURI;
    _bundleRootDir = parsePath(_bundleGzipURI);
    _bundleIndexURI = _bundleRootDir + File.separator + "index.json";
  }

  protected String parsePath(String fileName) {
    File file = new File(fileName);
    return file.getParent();
  }

}
