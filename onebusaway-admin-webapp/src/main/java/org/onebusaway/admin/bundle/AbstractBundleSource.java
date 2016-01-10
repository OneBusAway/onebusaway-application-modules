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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBundleSource {

  private static Logger _log = LoggerFactory.getLogger(AbstractBundleSource.class);
  protected static String BUNDLE_DATA_DIRNAME = "data";
  protected static String BUNDLE_INPUT_DIRNAME = "inputs";
  protected static String BUNDLE_OUTPUT_DIRNAME = "outputs";

  protected boolean arrayContainsItem(String[] array, String item) {
    boolean result = false;

    for (int i = 0; i < array.length; i++) {
      if (item.equals(array[i])) {
        result = true;
        break;
      }
    }

    return result;
  }

  public String getFilePath(String bundleId, String relativeFilePath) {
    if (bundleId == null && relativeFilePath == null)
      return "";
    if (bundleId == null)
      return relativeFilePath;

    String fileSep = System.getProperty("file.separator");

    String relPath = bundleId + fileSep + BUNDLE_DATA_DIRNAME + fileSep
        + relativeFilePath;

    return relPath;
  }

  public File getBundleFile(String bundleDirectory, String bundleId,
      String relativeFilePath) throws FileNotFoundException {

    File file = new File(bundleDirectory, getFilePath(bundleId,
        relativeFilePath));

    if (!file.exists()) {
      _log.info("A requested file in bundle " + bundleId
          + " does not exist at path: " + file.getPath());
      throw new FileNotFoundException("File " + file.getPath() + " not found.");
    } else {
      _log.debug("getBundleFile(" + file + ")");
    }
    return file;
  }

}
