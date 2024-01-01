/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.bundle;

import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Allow different styles/intervals of scheduling bundle refreshes.
 */
public interface BundleScheduler {

  void setup(BundleManagementService service, ThreadPoolTaskScheduler taskScheduler);
}
