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
package org.onebusaway.admin.service.bundle;

import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;

public interface BundleBuildingService {
  void setup();

  void download(BundleBuildRequest request, BundleBuildResponse response);

  void prepare(BundleBuildRequest request, BundleBuildResponse response);

  int build(BundleBuildRequest request, BundleBuildResponse response);

  void assemble(BundleBuildRequest request, BundleBuildResponse response);
  
  void upload(BundleBuildRequest request, BundleBuildResponse response);

  void doBuild(BundleBuildRequest request, BundleBuildResponse response);

  String getDefaultAgencyId();

  void createBundleBuildResponse(BundleBuildResponse bundleBuildResponse);

  void updateBundleBuildResponse(BundleBuildResponse bundleBuildResponse);

  BundleBuildResponse getBundleBuildResponseForId(String id);

  int getBundleBuildResponseMaxId();

}
