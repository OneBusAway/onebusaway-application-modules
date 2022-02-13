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
package org.onebusaway.admin.service.impl;

import static org.junit.Assert.*;

import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.bundle.impl.BundleBuildingServiceImpl;
import org.onebusaway.admin.util.NYCFileUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.onebusaway.util.impl.configuration.ConfigurationServiceImpl;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BundleBuildingServiceImplTest {
  private static Logger _log = LoggerFactory.getLogger(BundleBuildingServiceImplTest.class);
  private static String CT_GIS_ZIP = "29_gis.zip";
  private static String CT_SCHEDULE_ZIP = "29_HastusRoutesAndSchedules.zip";
  private BundleBuildingServiceImpl _service;

  public void setup() {
    _service = new BundleBuildingServiceImpl() {
      @Override
      public String getDefaultAgencyId() {
        return null;
      }
    };
    
    _service.setDebug(true);
    FileService fileService;
    fileService = new S3FileServiceImpl() {
      @Override
      public void setup() {
      };
      @Override
      public boolean bundleDirectoryExists(String filename) {
        return !"noSuchDirectory".equals(filename);
      }

      @Override
      public boolean createBundleDirectory(String filename) {
        return true;
      };

      @Override
      public List<String[]> listBundleDirectories(int maxResults) {
        ArrayList<String[]> list = new ArrayList<String[]>();
        String[] columns0 = {"2012April", "", "" + System.currentTimeMillis()};
        list.add(columns0);
        String[] columns1 = {"2012Jan", "", "" + System.currentTimeMillis()};
        list.add(columns1);
        String[] columns2 = {"2011April", "", "" + System.currentTimeMillis()};
        list.add(columns2);
        String[] columns3 = {"2011Jan", "", "" + System.currentTimeMillis()};
        list.add(columns3);
        String[] columns4 = {"2010April", "", "" + System.currentTimeMillis()};
        list.add(columns4);
        String[] columns5 = {"2010Jan", "", "" + System.currentTimeMillis()};
        list.add(columns5);
        return list;
      }

      @Override
      public List<String> list(String directory, int maxResults) {
        _log.debug("list called with " + directory);
        ArrayList<String> list = new ArrayList<String>();
        if (directory.equals("test/gtfs_latest")) {
          list.add("gtfs-m34.zip");
        } else if (directory.equals("test/aux_latest")) {
          if ("true".equals(_service.getAuxConfig())) {
            list.add(CT_GIS_ZIP);
            list.add(CT_SCHEDULE_ZIP);
          } else {
            list.add("stif-m34.zip");
          }
        } else if (directory.equals("test/config")) {
          // do nothing
        } else {
          throw new RuntimeException("file not found for dir=" + directory);
        }
        return list;
      }

      @Override
      public String get(String key, String tmpDir) {
        _log.debug("get called with " + key);
        InputStream source = null;
        if (key.equals("gtfs-m34.zip")) {
          source = this.getClass().getResourceAsStream(
              "gtfs-m34.zip");
        } else if (key.equals("stif-m34.zip")) {
          source = this.getClass().getResourceAsStream("stif-m34.zip");
        } else if (key.equals(CT_GIS_ZIP)) {
          source = this.getClass().getResourceAsStream(CT_GIS_ZIP);
        } else if (key.equals(CT_SCHEDULE_ZIP)) {
          source = this.getClass().getResourceAsStream(CT_SCHEDULE_ZIP);
        } else {
          throw new RuntimeException("unmatched key=" + key + " for tmpDir=" + tmpDir);
        }
        String filename = tmpDir + File.separator + key;
        _log.info("copying " + source + " to " + filename);
        new NYCFileUtils().copy(source, filename);
        return filename;
      }

      @Override
      public String put(String key, String file) {
        // do nothing
        return null;
      }
    };
    fileService.setBucketName("obanyc-bundle-data");
    fileService.setGtfsPath("gtfs_latest");
    fileService.setAuxPath("aux_latest");
    fileService.setBuildPath("builds");
    fileService.setConfigPath("config");
    fileService.setup();

    // uncomment for s3
    // fileService = new FileServiceImpl();
    // fileService.setBucketName("obanyc-bundle-data");
    // fileService.setGtfsPath("gtfs_latest");
    // fileService.setStifPath("stif_latest");
    // fileService.setBuildPath("builds");
    // fileService.setConfigPath("config");
    // fileService.setup();
    _service.setFileService(fileService);

    ConfigurationService configService = new ConfigurationServiceImpl() {
      @Override
      public String getConfigurationValueAsString(String configurationItemKey,
                                                  String defaultValue) {
        return defaultValue;
      }

      public String getItem(String component, String key) throws Exception {
        return null;
      }
    };
    _service.setConfigurationService(configService);

    _service.setup();

  }

  @Test
  public void testMe() {
    BasicConfigurator.configure();
    setup();
    testBuildStif();
    setup();
    testBuildHastus();
  }
  
  
  private void testBuildStif() {
    _service.setAuxConfig("false");
    String bundleDir = "test";
    String tmpDir = new NYCFileUtils().createTmpDirectory();

    BundleBuildRequest request = new BundleBuildRequest();
    request.setBundleDirectory(bundleDir);
    request.setBundleName("testname");
    request.setTmpDirectory(tmpDir);
    request.setBundleStartDate("2012-04-08");
    request.setBundleEndDate("2012-07-07");
    request.setBundleComment("Test");
    assertNotNull(request.getTmpDirectory());
    assertNotNull(request.getBundleDirectory());
    BundleBuildResponse response = new BundleBuildResponse(""
        + System.currentTimeMillis());
    assertEquals(0, response.getStatusList().size());

    // step 1
    _service.download(request, response);
    assertNotNull(response.getGtfsList());
    assertEquals(1, response.getGtfsList().size());

    assertNotNull(response.getAuxZipList());
    assertEquals(1, response.getAuxZipList().size());
     
    assertNotNull(response.getStatusList());
    assertTrue(response.getStatusList().size() > 0);

    assertNotNull(response.getConfigList());
    assertEquals(0, response.getConfigList().size());
    
    // step 2
    _service.prepare(request, response);

    
    assertFalse(response.isComplete());
    
    // step 3
    int rc = _service.build(request, response);
    if (response.getException() != null) {
      _log.error("Failed with exception=" + response.getException(), response.getException());
    }
    assertNull(response.getException());
    assertFalse(response.isComplete());
    assertEquals(0, rc);
    
    // step 4
    // OBANYC-1451 -- fails on OSX TODO
    //_service.assemble(request, response);

    // step 5
    _service.upload(request, response);
    assertFalse(response.isComplete()); // set by BundleRequestService

  }

  private void testBuildHastus() {
    _service.setAuxConfig("true");
    String bundleDir = "test";
    String tmpDir = new NYCFileUtils().createTmpDirectory();

    BundleBuildRequest request = new BundleBuildRequest();
    request.setBundleDirectory(bundleDir);
    request.setBundleName("testnameHastus");
    request.setTmpDirectory(tmpDir);
    request.setBundleStartDate("2012-04-08");
    request.setBundleEndDate("2012-07-07");
    assertNotNull(request.getTmpDirectory());
    assertNotNull(request.getBundleDirectory());
    BundleBuildResponse response = new BundleBuildResponse(""
        + System.currentTimeMillis());
    assertEquals(0, response.getStatusList().size());

    // step 1
    _service.download(request, response);
    assertNotNull(response.getGtfsList());
    assertEquals(1, response.getGtfsList().size());

    assertNotNull(response.getAuxZipList());
    assertEquals(2, response.getAuxZipList().size());
     
    assertNotNull(response.getStatusList());
    assertTrue(response.getStatusList().size() > 0);

    assertNotNull(response.getConfigList());
    assertEquals(0, response.getConfigList().size());
    
    // step 2
    _service.prepare(request, response);

    
    assertFalse(response.isComplete());
    
    // step 3
    int rc = _service.build(request, response);
    if (response.getException() != null) {
      _log.error("Failed with exception=" + response.getException(), response.getException());
    }
    assertNull(response.getException());
    assertFalse(response.isComplete());
    assertEquals(0, rc);
    
    // step 4
    // OBANYC-1451 -- fails on OSX TODO
    //_service.assemble(request, response);

    // step 5
    _service.upload(request, response);
    assertFalse(response.isComplete()); // set by BundleRequestService

  }

  
}
