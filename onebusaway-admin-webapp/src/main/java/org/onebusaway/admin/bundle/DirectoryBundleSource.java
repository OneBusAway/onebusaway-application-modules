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
package org.onebusaway.admin.bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.admin.bundle.model.Bundle;
import org.onebusaway.admin.json.JsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a bundle source where each bundle is saved as a set of files
 * within a directory. The constructor takes a directory which contains multiple
 * subdirectories, one for each bundle.
 * 
 */
public class DirectoryBundleSource extends AbstractBundleSource implements BundleSource {

  private static Logger _log = LoggerFactory.getLogger(DirectoryBundleSource.class);

  private static String BUNDLE_METADATA_FILENAME = "BundleMetadata.json";
  

  private JsonTool jsonTool;
  private File masterBundleDirectory;
  private String masterBundleDirectoryPathname;
  private boolean isInitialized = false;

  public File getMasterBundleDirectory() {
    return masterBundleDirectory;
  }

  public void setMasterBundleDirectoryPathname(String pathname) {
    this.masterBundleDirectoryPathname = pathname;
  }

  public void setJsonTool(JsonTool tool) {
    jsonTool = tool;
  }

  /**
   * Construct this DirectoryBundleSource, verifying that the passed directory
   * is actually a directory.
   * 
   * @param masterBundleDirectoryPathname The path of a directory which can
   *          contain subdirectories for each bundle.
   * @param jsonTool The json utility to use, wrapped in a JsonTool class.
   * @throws Exception If bundleDirectoryPathname is not a directory.
   */
  public DirectoryBundleSource(String masterBundleDirectoryPathname,
      JsonTool jsonTool) throws Exception {
    super();
    this.masterBundleDirectoryPathname = masterBundleDirectoryPathname;
    this.jsonTool = jsonTool;
  }

  private void init() throws RuntimeException{
    if (isInitialized) return;

    masterBundleDirectory = new File(masterBundleDirectoryPathname);

    if (!getMasterBundleDirectory().isDirectory()) { // Check to make sure this
      // is a directory.
      throw new RuntimeException(
              "Can not construct DirectoryBundleSource with directory "
                      + masterBundleDirectoryPathname + ". It is not a directory.");
    }

    _log.info("Loaded DirectoryBundleSource with "
            + masterBundleDirectory.getPath() + " as master bundle directory.");
    isInitialized = true;
  }

  @Override
  public List<Bundle> getBundles() {
    init();
    List<Bundle> bundles = new ArrayList<Bundle>();

    /* Start with a list of the directories in our bundleDirectory */
    List<String> potentialBundles = getSubDirectoryNamesOfBundleDirectory();

    _log.info("Found "
        + potentialBundles.size()
        + " potential individual bundle directories in master bundle directory.");
    /*
     * Check each directory to verify that it contains a bundle, and if so, add
     * it to the list of bundles.
     */
    for (String potentialBundle : potentialBundles) {
      Bundle loadedBundle;
      try {
        loadedBundle = loadBundleDirectory(potentialBundle);
        if (loadedBundle != null) {
          bundles.add(loadedBundle);
        }
      } catch (IOException e) {
        _log.info("Invalid Individual Bundle Directory: Exception loading individual bundle directory " + potentialBundle);
      }
      
    }

    _log.info("Returning " + bundles.size()
        + " valid individual bundles in master bundle directory.");

    return bundles;
  }

  private List<String> getSubDirectoryNamesOfBundleDirectory() {
    List<String> subDirectories = new ArrayList<String>();

    String bundleDirPath = getMasterBundleDirectory().getPath();

    // We already checked that the master directory is a directory in the
    // constructor.
    for (String dirItemName : getMasterBundleDirectory().list()) {
      // dirItemName will also be the id of the bundle, if it is a bundle.

      File dirItem = new File(bundleDirPath, dirItemName);
      if (dirItem.isDirectory()) {
        subDirectories.add(dirItemName);
      }
    }

    return subDirectories;
  }

  /**
   * Load the directory at masterBundleDirectory/dirName, check if it is a
   * bundle directory, and if so, fill the bundle object and return a map item
   * relating the bundle name to the bundle.
   * 
   * @param dirName The name of the bundle directory, which corresponds to the
   *          id of the bundle.
   * @return A Map<String, Bundle> mapping the bundle id to its Bundle object,
   *         or null if dirName does not contain a legit bundle.
   * @throws IOException 
   */
  private Bundle loadBundleDirectory(String dirName) throws IOException {
    Bundle resultBundle = null;

    File bundleFile = new File(getMasterBundleDirectory(), dirName);

    if (bundleFile.isDirectory()) {
      // List the contents of the directory.
      String[] dirList = bundleFile.list();

      // Check for two entries, a file named 'BundleMetadata.json' and a
      // directory
      // named 'data'
      if (arrayContainsItem(dirList, BUNDLE_METADATA_FILENAME)
          && arrayContainsItem(dirList, AbstractBundleSource.BUNDLE_DATA_DIRNAME)) {
        File bundleMetadataFile = new File(bundleFile, BUNDLE_METADATA_FILENAME);
        File dataDir = new File(bundleFile, AbstractBundleSource.BUNDLE_DATA_DIRNAME);

        if (bundleMetadataFile.isFile() && dataDir.isDirectory()) {
          Bundle bundle;
          try {
            bundle = loadBundleMetadata(bundleMetadataFile);
          } catch (FileNotFoundException e) {
            throw new IOException("Could not load Bundle metadata", e);
          }

          if (bundle != null) {
            if (dirName.equals(bundle.getName())) {
              resultBundle = bundle;
            } else {
              _log.info("Invalid individual bundle directory " + dirName
                  + ": Direcorty name does not match name '" + bundle.getName()
                  + "' in metadata.");
            }
          } else {
            _log.info("Invalid individual bundle directory " + dirName
                + ": Could not parse metadata file as json.");
          }

        }
      } else {
        _log.info("Invalid individual bundle directory " + dirName
            + ": Individual bundle directory " + dirName + " should contain "
            + BUNDLE_METADATA_FILENAME + " json file and a directory named "
            + BUNDLE_DATA_DIRNAME + " to be a valid bundle.");
      }
    }

    return resultBundle;
  }
  
  private Bundle loadBundleMetadata(File metadataFile) throws FileNotFoundException {
    FileReader metadataReader = null;

    Bundle resultBundle = null;

    try {
      metadataReader = new FileReader(metadataFile);
      if (jsonTool == null) {
        // we have an issue with the spring configuration
        throw new RuntimeException("Configuration Error:  jsonTool not configured!");
      }
      resultBundle = jsonTool.readJson(metadataReader, Bundle.class);
    } catch (RuntimeException re) {
      _log.error("fatal configuration error!", re);
    } catch (Exception e) {
      // our bundle is corrupt or we have permissions/configuration issue
      // give up on this bundle and hope that others exist!
      _log.error("Configuration Exception:  Unable to load file " + metadataFile, e);
      return null;
    } finally {
      if (metadataReader != null)
        try {
          metadataReader.close();
        } catch (IOException e) {}
    }

    return resultBundle;
  }


  @Override
  public boolean checkIsValidBundleFile(String bundleId, String relativeFilePath) {
    init();
    boolean isValid = false;

    Bundle requestedBundle;
    try {
      requestedBundle = loadBundleDirectory(bundleId);
      if (requestedBundle != null) {
        if (requestedBundle.containsFile(relativeFilePath)) {
          isValid = true;
        }
      }
    } catch (IOException e) {
      isValid = false;
    }

    return isValid;
  }



  @Override
  public File getBundleFile(String bundleId, String relativeFilePath)
      throws FileNotFoundException {
    init();
    return getBundleFile(this.masterBundleDirectory.toString(), bundleId, relativeFilePath);
  }
  
 
}
