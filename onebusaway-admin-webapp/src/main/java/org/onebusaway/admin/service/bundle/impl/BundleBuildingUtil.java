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
package org.onebusaway.admin.service.bundle.impl;

import org.onebusaway.transit_data_federation.bundle.model.Bundle;
import org.onebusaway.transit_data_federation.bundle.model.BundleFile;
import org.onebusaway.transit_data_federation.bundle.model.SourceFile;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.onebusaway.transit_data_federation.bundle.utilities.BundleUtilties;
import org.onebusaway.transit_data_federation.bundle.utilities.JodaDateTimeAdapter;
import org.onebusaway.transit_data_federation.bundle.utilities.JodaLocalDateAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleConventionMain.BUNDLE_METADATA_JSON;

public class BundleBuildingUtil {
  private static Logger _log = LoggerFactory.getLogger(BundleBuildingUtil.class);
  public BundleBuildingUtil() {
    
  }
  
  public String generateJsonMetadata(BundleBuildRequest request, BundleBuildResponse response) {
    return generateJsonMetadata(request, response, null);
  }

  public String generateJsonMetadata(BundleBuildRequest request, BundleBuildResponse response, String bundleId) {
    File bundleDir = new File(response.getBundleDataDirectory());
    List<BundleFile> files = getBundleFilesWithSumsForDirectory(bundleDir, bundleDir);
    
    Gson gson = new GsonBuilder().serializeNulls()
        .registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter())
        .registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter())
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().create();
    
    Bundle bundle = new Bundle();

    if (bundleId != null) {
      bundle.setId(bundleId);   // Use id from previous build
    } else {
      bundle.setId(getBundleId(response.getBundleRootDirectory() + File.separator
        + "data" + File.separator + "metadata.json"));
    }

    bundle.setDataset(request.getBundleDirectory());

    bundle.setName(request.getBundleName());
    
    bundle.setServiceDateFrom(request.getBundleStartDate());
    bundle.setServiceDateTo(request.getBundleEndDate());
    
    DateTime now = new DateTime();
    
    bundle.setCreated(now);
    bundle.setUpdated(now);
    
    List<String> applicableAgencyIds = new ArrayList<String>();
    // TODO this should come from somewhere
    //applicableAgencyIds.add("MTA NYCT");
    
    bundle.setApplicableAgencyIds(applicableAgencyIds);
    
    bundle.setFiles(files);
    
    String output = gson.toJson(bundle);
    
    String outputFilename = response.getBundleRootDirectory() + File.separator + BUNDLE_METADATA_JSON;
    File outputFile = new File(outputFilename);
    _log.info("creating metadata file=" + outputFilename);
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(outputFile);
      writer.print(output);
    } catch (Exception any){
      _log.error(any.toString(), any);
      response.setException(any);
    } finally {
      writer.close();
    }
    return outputFilename;
  }
  
  public List<BundleFile> getBundleFilesWithSumsForDirectory(File baseDir, File dir) throws IllegalArgumentException {
    return getBundleFilesWithSumsForDirectory(baseDir, dir, null);
  }
  
  public List<BundleFile> getBundleFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) throws IllegalArgumentException {
    return new BundleUtilties().getBundleFilesWithSumsForDirectory(baseDir, dir, rootDir);
  }

  public List<SourceFile> getSourceFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) throws IllegalArgumentException {
    return new BundleUtilties().getSourceFilesWithSumsForDirectory(baseDir, dir, rootDir);
  }
  
  // Changed to public static to allow call 
  // from BundleBuildingServiceImpl.checkBundleId(...)
  public static String getBundleId(String metadataFile) {
    try {
      String bundleId = new JsonParser().parse(new FileReader(metadataFile)).getAsJsonObject().get(
          "id").getAsString();
      return bundleId;
    } catch (Exception e) {
    }
    return null;
  }

  // Set the bundle id in the specified metadata json files to the given value.
  public static void setBundleId(String metadataFile, String bundleId) {
    if (bundleId == null) {
      return;
    }
    FileWriter metadataWriter = null;
    try {
      JsonObject metadataJson = new JsonParser().parse(new FileReader(metadataFile)).getAsJsonObject();
      metadataJson.remove("id");
      metadataJson.addProperty("id", bundleId);
      String metadataString = metadataJson.toString();
      metadataWriter = new FileWriter(metadataFile, false);
      metadataWriter.write(metadataString);
    } catch (IOException e) {
      _log.info("Error writing metadata.json: " + e.getMessage());
    } catch (Exception e) {
      _log.error(e.getMessage());
    } finally {
      try {
        if (metadataWriter != null) {
          metadataWriter.close();
        }
      } catch (Exception e) {
        _log.error(e.getMessage());
      }
    }
    return;
  }

  private String getMd5ForFile(File file) {
    return new BundleUtilties().getMd5ForFile(file);
  }
  public String getUri(final File baseDir, final String endsWithName) {
    List<File> matches = findFiles(baseDir, endsWithName);
    if (matches != null && !matches.isEmpty()) {
      return baseDir.toURI().relativize(matches.get(0).toURI()).getPath();
    }
    return null;
  }
  private ArrayList<File> findFiles(final File baseDir, final String endsWithName) {
    final ArrayList<File> matches = new ArrayList<File>();
    
    if (!baseDir.exists() || !baseDir.isDirectory()) {
      _log.error("getUri called with illegal baseDir=" + baseDir);
      return matches;
    }
    
    File[] matchingFiles = baseDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        // check files
        return name.endsWith(endsWithName);
      }
    });
    
    if (matchingFiles != null && matchingFiles.length > 0) {
      for (File f : matchingFiles) {
        matches.add(f);
      }
    }

    // not found, recurse on dirs
    File[] matchingSubDirs = baseDir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        File subDir = new File(dir, name);
        if (subDir.isDirectory() && !subDir.equals(baseDir)) {
          // recurse on directories, preserving rootDir
          return true;
        }
        return false;
      }
    });


    if (matchingSubDirs != null && matchingSubDirs.length > 0) {
      for (File f : matchingSubDirs) {
        matches.addAll(findFiles(f, endsWithName));
      }
    }
    
    _log.debug("getUri(" + baseDir + ", " + endsWithName + ") found " + matches);
    return matches;
  } 
}

