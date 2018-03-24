/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.List;

public interface ServiceAlertsPersistence {

  void delete(ServiceAlertRecord existingServiceAlertRecord);

  List<ServiceAlertRecord> getAlerts();

  void saveOrUpdate(ServiceAlertRecord record);

  ServiceAlertRecord getServiceAlertRecordByAlertId(String agencyId, String serviceAlertId);

  boolean needsSync();

  boolean cachedNeedsSync();

}
