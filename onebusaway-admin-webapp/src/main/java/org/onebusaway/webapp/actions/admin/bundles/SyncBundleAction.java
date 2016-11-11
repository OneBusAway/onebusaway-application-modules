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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.simple.JSONObject;
import org.onebusaway.admin.util.NYCFileUtils;
import org.onebusaway.util.FileUtility;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Action class that holds properties and methods required for syncing the
 * active bundle in the prod environment with that on the staging environment,
 * i.e. deploying the active bundle from staging to production.
 * @author jpearson
 *
 */

@Namespace(value="/admin/bundles")
@Results({
  @Result(name="syncStatus", type="json",
      params={"root", "syncStatus"})
})
public class SyncBundleAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(ManageBundlesAction.class);
  private static final long serialVersionUID = 1L;

  private String environment = "dev";
  
  public String getEnvironment() {
    return environment;
  }

  public String syncBundle() {
    
    String syncStatus = "Syncing in progress";
    String apiHost = "http://admin.staging.obast.org:9999/api/bundle/latest";
    JsonObject latestBundle = null;
    try {
      latestBundle = getJsonData(apiHost).getAsJsonObject();
    }  catch (Exception e) {
      _log.error("Failed to retrieve name of the latest deployed bundle");
    }
    String datasetName =  latestBundle.get("dataset").getAsString();
    String buildName = latestBundle.get("name").getAsString();
    String bundleId = latestBundle.get("id").getAsString();
    String bundleFileName = buildName + ".tar.gz";
    String tmpDir = new NYCFileUtils().createTmpDirectory();
    String bundleDir = "/var/lib/oba/bundles";
    String deployDir = "/var/lib/oba/bundles/active";
    String bundleBuildDir = "/var/lib/oba/bundles/builder";
    
    try {
      String bundleSourceString = "http://admin.staging.obast.org:9999/api/bundle/archive/get-by-name/" 
          + datasetName + "/" + buildName + "/" + bundleFileName;
      URL bundleSource = new URL(bundleSourceString);
      ReadableByteChannel rbc = Channels.newChannel(bundleSource.openStream());
      FileOutputStream fos = new FileOutputStream(tmpDir + File.separator + bundleFileName);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    try {
      unzipBundle(tmpDir, bundleFileName);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Copy to extracted files /var/lib/oba/bundles/active
    try {
      //FileUtils.copyDirectory(new File(tmpDir + File.separator + "untarredBundle" + File.separator +  buildName), new File(deployDir));
      FileUtils.copyDirectory(new File(tmpDir + File.separator + "untarredBundle"), new File(deployDir));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Copy downloaded .tar.gz bundle to bundle builds dir
    String buildDest = bundleBuildDir + File.separator + datasetName + File.separator + "builds"
        + File.separator + buildName + File.separator + bundleFileName;
    try {
      FileUtils.copyFile(new File(tmpDir + File.separator + bundleFileName),
          new File(buildDest));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Copy inputs and outputs dirs
    File srcInputDir = new File(tmpDir + File.separator + "untarredBundle" + File.separator + buildName + File.separator + "inputs");
    File srcOutputDir = new File(tmpDir + File.separator + "untarredBundle" + File.separator + buildName + File.separator + "outputs");
    String destBuildsDir = bundleBuildDir + File.separator + datasetName + File.separator + "builds"
        + File.separator + buildName;
    try {
      FileUtils.copyDirectory(srcInputDir, new File(destBuildsDir + File.separator + "inputs"));
      FileUtils.copyDirectory(srcOutputDir, new File(destBuildsDir + File.separator + "outputs"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
   
    // Copy gtfs and aux files to aux_latest and gtfs_latest
    String gtfsFileDest = bundleBuildDir + File.separator + datasetName + File.separator + "gtfs_latest";
    String auxFileDest = bundleBuildDir + File.separator + datasetName + File.separator + "aux_latest";
    File[] inputFiles = srcInputDir.listFiles();
    for (File inputFile : inputFiles) {
      try {
        String fileName = inputFile.getName();
        int idx = fileName.indexOf("_");
        if (idx > 0) {        // Skip over config dir
          String agencyNum = fileName.substring(0,idx);
          String zipFileName = fileName.substring(idx + 1);
          if (agencyNum.equals("29")) {   // For CT aux files
            String fileDest = auxFileDest + File.separator + agencyNum + File.separator + zipFileName;
            FileUtils.copyFile(inputFile, new File(fileDest));
          } else {
            String fileDest = gtfsFileDest + File.separator + agencyNum + File.separator + zipFileName;
            FileUtils.copyFile(inputFile, new File(fileDest));
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    syncStatus = "Complete";
    return "syncStatus";
  }

  private JsonElement getJsonData (String spec) throws IOException {
    URL url = new URL((!spec.toLowerCase().matches("^\\w+://.*")?"http://":"") + spec);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    return new JsonParser().parse(in.readLine());
  }

  private void unzipBundle(String tmpDir, String bundleFileName) throws IOException {
    byte[] buffer = new byte[1024];

    GZIPInputStream zipIn = new GZIPInputStream(new FileInputStream(tmpDir + File.separator + bundleFileName));
    FileOutputStream out = new FileOutputStream(tmpDir + File.separator + "unzippedBundle");
    
    int len;
    while ((len = zipIn.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }

    zipIn.close();
    out.close();
    
    // Now to untar the unzipped file
    File tarFile = new File(tmpDir + File.separator + "unzippedBundle");
    File untarredFile = new File(tmpDir + File.separator + "untarredBundle");
    //File untarredFile = new File(tmpDir);
    try {
      List<File> fileList = (new FileUtility()).unTar(tarFile, untarredFile);
    } catch (ArchiveException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return;
  }
}
