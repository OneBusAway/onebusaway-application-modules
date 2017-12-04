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

import org.onebusaway.admin.bundle.model.Bundle;
import org.onebusaway.admin.bundle.model.BundleFile;
import org.onebusaway.admin.bundle.model.SourceFile;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BundleBuildingUtil {
  private static final String META_DATA_FILE = "BundleMetadata.json";
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
    
    String outputFilename = response.getBundleRootDirectory() + File.separator + META_DATA_FILE;
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
    List<BundleFile> files = new ArrayList<BundleFile>();
    
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getPath() + " is not a directory");
    } else {
      for (String filePath : dir.list()) {
        File listEntry = new File(dir, filePath);
        String listEntryFilename = null;
        try {
          listEntryFilename = listEntry.getCanonicalPath();
        } catch (Exception e) {
          // bury
        }
        // prevent lock files from insertion into json, they change
        if (listEntry.isFile()  
            && listEntryFilename != null 
            && !listEntryFilename.endsWith(".lck")) {
          BundleFile file = new BundleFile();
          
          String relPathToBase = null;
          if (rootDir == null) {
            _log.debug("baseDir=" + baseDir + "; listEntry=" + listEntry);
            relPathToBase = baseDir.toURI().relativize(listEntry.toURI()).getPath();
            file.setFilename(relPathToBase);
          } else {
            relPathToBase = rootDir.toURI().relativize(listEntry.toURI()).getPath();
            file.setFilename(relPathToBase);
          }
          
          String sum = getMd5ForFile(listEntry);
          file.setMd5(sum);
          
          files.add(file);
          _log.debug("file:" + listEntry + " has Md5=" + sum);
        } else if (listEntry.isDirectory()) {
          files.addAll(getBundleFilesWithSumsForDirectory(baseDir, listEntry, rootDir));
        }
      }
    }
    
    return files;
  }

  public List<SourceFile> getSourceFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) throws IllegalArgumentException {
    List<SourceFile> files = new ArrayList<SourceFile>();
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir.getPath() + " is not a directory");
    } else {
      for (String filePath : dir.list()) {
        File listEntry = new File(dir, filePath);
        String listEntryFilename = null;
        try {
          listEntryFilename = listEntry.getCanonicalPath();
        } catch (Exception e) {
          // bury
        }
        // prevent lock files from insertion into json, they change
        if (listEntry.isFile()  
            && listEntryFilename != null 
            && !listEntryFilename.endsWith(".lck")) {
          SourceFile file = new SourceFile();
          file.setCreatedDate(getCreatedDate(listEntry));
          
          String relPathToBase = rootDir.toURI().relativize(listEntry.toURI()).getPath();
          file.setUri(relPathToBase);
          file.setFilename(relPathToBase);
          
          String sum = getMd5ForFile(listEntry);
          file.setMd5(sum);
          
          files.add(file);
          _log.debug("file:" + listEntry + " has Md5=" + sum + " and createdDate=" + file.getCreatedDate());
        } else if (listEntry.isDirectory()) {
          files.addAll(getSourceFilesWithSumsForDirectory(baseDir, listEntry, rootDir));
        }
      }
    }
    
    return files;
  }
  
  private Date getCreatedDate(File file) {
    if (!file.exists()) {
      _log.error("file " + file + File.separator + file + " does not exist: cannot guess date!");
    }
    return new Date(file.lastModified());
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
    String sum;
  try {
    sum = Md5Checksum.getMD5Checksum(file.getPath());
  } catch (Exception e) {
    sum = "Error generating md5 for " + file.getPath();
  }
    
    return sum;
  } 
  public class JodaDateTimeAdapter implements JsonSerializer<DateTime> {

    public JsonElement serialize(DateTime src, Type typeOfSrc,
        JsonSerializationContext context) {
      DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
      return new JsonPrimitive(fmt.print(src));
    }
    
  } 
  
  public class JodaLocalDateAdapter implements JsonSerializer<LocalDate> {

    public JsonElement serialize(LocalDate src, Type typeOfSrc,
        JsonSerializationContext context) {
      DateTimeFormatter fmt = ISODateTimeFormat.date();
      return new JsonPrimitive(fmt.print(src));
    }
  }
  
  public static class Md5Checksum {

    public static byte[] createChecksum(String filename) throws Exception {
      InputStream fis = new FileInputStream(filename);

      byte[] buffer = new byte[1024];
      MessageDigest complete = MessageDigest.getInstance("MD5");
      int numRead;
      do {
        numRead = fis.read(buffer);
        if (numRead > 0) {
          complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);
      fis.close();
      return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
      byte[] b = createChecksum(filename);
      String result = "";
      for (int i = 0; i < b.length; i++) {
        result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
      }
      return result;
    }
  }

  public String getUri(final File baseDir, final String endsWithName) {
    List<File> matches = findFiles(baseDir, endsWithName);
    if (matches != null && !matches.isEmpty()) {
      return baseDir.toURI().relativize(matches.get(0).toURI()).getPath();
    }
    return null;
  }
  private  ArrayList<File> findFiles(final File baseDir, final String endsWithName) {
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

