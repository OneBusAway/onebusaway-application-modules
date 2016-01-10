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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.onebusaway.admin.bundle.model.Bundle;
import org.onebusaway.admin.bundle.model.BundleFile;
import org.onebusaway.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a staging bundle source where each bundle is saved as a set of files
 * within a directory. The constructor takes a directory which contains multiple
 * subdirectories, one for each bundle.
 * 
 */
public class DirectoryStagingBundleSource extends AbstractBundleSource
    implements StagingBundleSource {
  private static Logger _log = LoggerFactory.getLogger(DirectoryStagingBundleSource.class);
  private File _masterBundleDirectory;
  private File _stagingDirectory;

  public DirectoryStagingBundleSource(String masterBundleDirectory,
      String stagingDirectory) {
    _masterBundleDirectory = new File(masterBundleDirectory);
    _stagingDirectory = new File(stagingDirectory);
  }

  public File getStagedBundleDirectory() {
    return _stagingDirectory;
  }

  public File getMasterBundleDirectory() {
    return _masterBundleDirectory;
  }

  @Override
  public void stage(String env, String bundleDir, String bundleName)
      throws Exception {
    File srcDir = new File(this.getMasterBundleDirectory().toString()
        + File.separator + bundleDir + File.separator + "builds"
        + File.separator + bundleName);
    File srcFile = new File(srcDir, bundleName + ".tar.gz");
    File destDir = this.getStagedBundleDirectory();
    _log.info("deleting " + destDir);
    // cleanup from past run
    try {
      FileUtils.deleteDirectory(destDir);
    } catch (Exception any) {
      _log.info("deleteDir failed with :", any);
    }

    FileUtility fu = new FileUtility();
    _log.info("making directory" + destDir);
    destDir.mkdir();

    _log.info("expanding " + srcFile + " to " + destDir);
    fu.unTargz(srcFile, destDir);
    File oldDir = new File(destDir + File.separator + bundleName);
    File newDir = new File(destDir + File.separator + env);
    _log.info("moving " + oldDir + " to " + newDir);
    FileUtils.moveDirectory(oldDir, newDir);
  }

  private Bundle loadStagedBundleDirectory(String dirName) throws IOException {
    Bundle resultBundle = null;
    File bundleFile = new File(getStagedBundleDirectory(), dirName);
    // File bundleFile = getStagedBundleDirectory();
    if (bundleFile.isDirectory()) {
      // List the contents of the directory.
      String[] dirList = bundleFile.list();
      if (arrayContainsItem(dirList, AbstractBundleSource.BUNDLE_DATA_DIRNAME)
          && arrayContainsItem(dirList, AbstractBundleSource.BUNDLE_INPUT_DIRNAME)
          && arrayContainsItem(dirList, AbstractBundleSource.BUNDLE_OUTPUT_DIRNAME)) {
        
        Bundle bundle = new Bundle();
        bundle.setName(dirName);
        bundle.setFiles(new ArrayList<BundleFile>());
        for (String s : dirList) {
          File fileCheck = new File(bundleFile, s);
          if (fileCheck.isDirectory()) {
            addSubdirectoryFiles(bundle.getFiles(), bundleFile, fileCheck);
          } else {
            BundleFile bf = new BundleFile();
            
            bf.setFilename(s);
            bundle.getFiles().add(bf);
          }
        }
        resultBundle = bundle;
      } else {
        _log.error("unexpected format of bundle dir=" + dirName);
      }
    }
    return resultBundle;
  }

  @Override
  public File getBundleFile(String bundleId, String relativeFilePath)
      throws FileNotFoundException {

    File file = new File(this.getStagedBundleDirectory(), getStagedFilePath(
        bundleId, relativeFilePath));

    if (!file.exists()) {
      _log.info("A requested file in bundle " + bundleId
          + " does not exist at path: " + file.getPath());
      throw new FileNotFoundException("File " + file.getPath() + " not found.");
    } else {
      _log.info("getBundleFile(" + file + ")");
    }
    return file;
  }

  private String getStagedFilePath(String bundleId, String relativeFilePath) {
    if (bundleId == null && relativeFilePath == null)
      return "";
    if (bundleId == null)
      return relativeFilePath;

    String fileSep = File.separator;

    String relPath = bundleId + fileSep + relativeFilePath;

    return relPath;
  }

  @Override
  public boolean checkIsValidBundleFile(String bundleId,
      String relativeFilePath) {
    boolean isValid = false;

    Bundle requestedBundle;
    try {

      requestedBundle = loadStagedBundleDirectory(bundleId);
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

  private void addSubdirectoryFiles(List<BundleFile> bundleFiles,
      File baseDirectory, File directory) {
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files == null)
        return;
      for (File file : files) {
        if (file.isDirectory()) {
          addSubdirectoryFiles(bundleFiles, baseDirectory, file);
        } else {
          BundleFile bf = new BundleFile();
          bf.setFilename(baseDirectory.toURI().relativize(file.toURI()).toString());
          bundleFiles.add(bf);
        }
      }
    }
  }
}
