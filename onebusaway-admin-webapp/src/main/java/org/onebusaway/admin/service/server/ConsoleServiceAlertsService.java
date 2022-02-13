/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.server;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;

import java.util.List;

public interface ConsoleServiceAlertsService {

    ListBean<ServiceAlertBean> getAllServiceAlertsForAgencyId(String agencyId);

    ListBean<ServiceAlertBean> getActiveServiceAlertsForAgencyId(String agencyId);

    void removeServiceAlert(AgencyAndId id);

    void updateServiceAlert(String agencyId, ServiceAlertBean bean);

    void updateServiceAlert(String agencyId, ServiceAlertBean bean, Boolean isCopy);

    ServiceAlertBean createServiceAlert(String agencyId, ServiceAlertBean serviceAlertBean);

    ServiceAlertBean getServiceAlertForId(String alertId);

    ServiceAlertRecord copyServiceAlert(String agencyId, ServiceAlertBean model);

    ListBean<ServiceAlertRecordBean> getAllServiceAlertRecordsForAgencyId(String agencyId);

    void removeAllServiceAlertsForAgencyId(String agencyId);

    void removeServiceAlerts(List<AgencyAndId> toRemove);

    void updateServiceAlerts(String agencyId, List<ServiceAlertBean> toUpdate);

    void createServiceAlerts(String agencyId, List<ServiceAlertBean> toAdd);

    GtfsRealtime.FeedMessage getAlerts(String agencyId);

    GtfsRealtime.FeedMessage getActiveAlerts(String agencyId);

    ServiceAlerts.ServiceAlertsCollection getAlertsCollection(String agencyId);

    ServiceAlerts.ServiceAlertsCollection getActiveAlertsCollection(String agencyId);

}
