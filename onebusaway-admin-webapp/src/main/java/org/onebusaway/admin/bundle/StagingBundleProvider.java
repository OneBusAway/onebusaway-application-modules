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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.bundle.model.BundleMetadata;

public class StagingBundleProvider {
  private static final String META_DATA_LOCATION = "metadata.json";
  private static final String ENV = "prod";
  
  private StagingBundleSource bundleSource;
  
  public StagingBundleProvider(StagingBundleSource bundleSource) {
    this.bundleSource = bundleSource;
  }
  
  public File getBundleFile(String bundleDirectory, String relativeFilePath) throws FileNotFoundException {
    return bundleSource.getBundleFile(ENV, relativeFilePath);
  }
  
  public boolean checkIsValidStagedBundleFile (String bundleId, String relativeFilePath) {
    return bundleSource.checkIsValidBundleFile(bundleId, relativeFilePath);
  }
  
  public BundleMetadata getMetadata(String stagingDirectory) throws Exception {
    File file = bundleSource.getBundleFile(stagingDirectory, 
        AbstractBundleSource.BUNDLE_DATA_DIRNAME + File.separator + META_DATA_LOCATION);
    ObjectMapper mapper = new ObjectMapper();
    BundleMetadata meta = mapper.readValue(file, BundleMetadata.class);
    return meta;
  }
}
