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

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.model.BundleStatus;

import javax.ws.rs.core.Response;


public interface BundleDeployer {
  void setup();
  void deploy(BundleStatus status, String path, String nameFilter);
  List<String> listStagedBundles(String path);
  Response delete(String name);
}
