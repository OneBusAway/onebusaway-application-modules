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

import java.util.List;

import org.onebusaway.admin.bundle.model.Bundle;
import org.onebusaway.json.model.JsonMessage;

public class BundlesListMessage extends JsonMessage {
  List<Bundle> bundles;

  public List<Bundle> getBundles() {
    return bundles;
  }

  public void setBundles(List<Bundle> bundles) {
    this.bundles = bundles;
  }
}
