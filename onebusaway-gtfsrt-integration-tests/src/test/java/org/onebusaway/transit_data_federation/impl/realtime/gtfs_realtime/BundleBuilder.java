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

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleConventionMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Support for building bundles in integration tests.
 */
public class BundleBuilder {

  private BundleContext _bundleContext = new BundleContext();

  protected static Logger _log = LoggerFactory.getLogger(BundleBuilder.class);

  public void setup(String integrationTestPath) throws Exception {
    String bundleGzipURI = _bundleContext.loadBundleGzipURIFromEnv();

    if (!exists(bundleGzipURI)) {
      _log.error("building bundle...to shortcut this step set \"bundle.keep=true\" and \"bundle.index.json\" to be the index file");
      // integration test support:  build bundle
      buildBundle(integrationTestPath);
    } else {
      // re-use the bundle if It's still present
      _log.error("loading existing bundle defined by env var \"bundle.index.json\" set to {}", bundleGzipURI);
      _bundleContext.prepForLoad(bundleGzipURI);
    }

  }

  public void buildBundle(String bundlePath) throws Exception {
    ClassPathResource bundleResource = new ClassPathResource(bundlePath);
    String bundleInputDir = bundleResource.getURL().getFile();
    String bundleRootDir =
            addSlash(System.getProperty("java.io.tmpdir"))
                    + "bundle" + System.currentTimeMillis();
    makeTempDirectory(bundleRootDir, System.getProperty("bundle.keep"));

    String bundleName = "v" + System.currentTimeMillis();
    String bundleGzipURI = buildBundle(bundleInputDir, bundleRootDir, bundleName);
    String bundleIndexURI = bundleRootDir + File.separator + "index.json";
    _bundleContext.setup(bundleRootDir, bundleGzipURI, bundleIndexURI);
    createIndexJson(bundleGzipURI,  bundleRootDir);
  }

  private String addSlash(String property) {
    if (property.endsWith("/"))
      return property;
    return property + "/";
  }

  protected String buildBundle(String bundleInputDir, String bundleOutputDir, String bundleName) throws IOException {
    dirCheck(bundleInputDir);
    dirCheck(bundleOutputDir);
    _log.info("buliding {} from {} to {}", bundleName, bundleInputDir, bundleOutputDir);
    FederatedTransitDataBundleConventionMain main = new FederatedTransitDataBundleConventionMain();
    String[] args = {bundleInputDir, bundleOutputDir, bundleName};
    return main.run(args);
  }

  public BundleContext getBundleContext() {
    return _bundleContext;
  }

  protected String createIndexJson(String bundleGzipURI, String baseLocation) throws Exception {
    String indexJson = "{\"latest\":\"" + bundleGzipURI + "\"}";
    String location = baseLocation + File.separator + "index.json";
    FileWriter fw = new FileWriter(location);
    fw.write(indexJson);
    fw.close();
    _log.info("wrote {} to {}", bundleGzipURI, baseLocation);
    return location;
  }

  protected File makeTempDirectory(String tmpDirStr, String keepIfSet) {
    File tmpDir = new File(tmpDirStr);
    tmpDir.mkdirs();
    if (keepIfSet == null) {
      DeleteTempDirectoryOnExitRunnable r = new DeleteTempDirectoryOnExitRunnable(
              tmpDir);
      Runtime.getRuntime().addShutdownHook(new Thread(r));
    }
    return tmpDir;
  }

  protected void dirCheck(String checkFileName) throws FileNotFoundException {
    if (checkFileName == null || checkFileName.length() == 0) {
      throw new IllegalStateException("null/empty filename provided");
    }
    File check = new File(checkFileName);
    if (!check.exists()) {
      throw new FileNotFoundException("expecting file/dir at " + checkFileName);
    }
  }

  protected boolean exists(String checkFileName) {
    if (checkFileName == null) return false;
    File checkFile = new File(checkFileName);
    return checkFile.exists() && checkFile.isFile();
  }


}
