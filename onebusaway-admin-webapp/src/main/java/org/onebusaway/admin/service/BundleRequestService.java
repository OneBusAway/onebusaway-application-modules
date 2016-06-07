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
package org.onebusaway.admin.service;

import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleRequest;
import org.onebusaway.admin.model.BundleResponse;

public interface BundleRequestService {
  BundleResponse validate(BundleRequest bundleRequest);

  BundleResponse lookupValidationRequest(String id);
  BundleBuildResponse lookupBuildRequest(String id);
  BundleBuildResponse build(BundleBuildRequest bundleRequest);
  
  /**
   * Builds and returns the URL where build bundle results can be viewed after the process completes
   * @param id the id of the bundle request
   * @return the response with the url set
   */
  BundleBuildResponse buildBundleResultURL(String Id);
  
  void cleanup();

  
}
