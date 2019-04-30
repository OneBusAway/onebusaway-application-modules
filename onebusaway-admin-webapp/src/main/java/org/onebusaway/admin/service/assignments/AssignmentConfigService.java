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
