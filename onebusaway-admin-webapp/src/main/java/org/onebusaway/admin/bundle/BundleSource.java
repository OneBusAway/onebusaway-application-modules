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
import java.util.List;

import org.onebusaway.admin.bundle.model.Bundle;

public interface BundleSource {
  /**
   * Get all the bundles that seem initially correct.
   * @return A list of the available bundle objects.
   */
  List<Bundle> getBundles();
  
  /**
   * Checks that the bundleId exists, and that the file is listed in the metadata.
   * @param bundleId The Id of the bundle to which the requested file belongs.
   * @param relativeFilePath the relative path of the file, as listed in the metadata.
   * @return true if the bundle exists and file is listed in the metadata. false otherwise. 
   */
  boolean checkIsValidBundleFile(String bundleId, String relativeFilePath);
  
  /**
   * Get the full path of a bundle file.
   * @param bundleId
   * @param relativeFilePath
   * @return
   */
  File getBundleFile(String bundleId, String relativeFilePath) throws FileNotFoundException;

  File getBundleFile(String bundleDirectory, String bundleId,
      String relativeFilePath) throws FileNotFoundException;

}
