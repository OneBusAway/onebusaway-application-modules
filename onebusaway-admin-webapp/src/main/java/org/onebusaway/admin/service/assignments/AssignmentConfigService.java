/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.assignments;

import java.util.Date;

public interface AssignmentConfigService {
    public String getConfigValueAsString(String key);
    public Date getConfigValueAsDate(String key);

    Date getConfigValueAsDateTime(String key);

    public void setConfigValue(String key, String value);

    void setConfigValueAsDateTime(String key, Date date);

    void deleteConfigValue(String key);

    void deleteAll();
}
