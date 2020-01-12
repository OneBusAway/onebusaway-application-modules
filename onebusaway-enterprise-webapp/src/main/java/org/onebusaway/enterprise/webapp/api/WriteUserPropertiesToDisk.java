/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.enterprise.webapp.api;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.properties.UserPropertiesV3;
import org.onebusaway.users.model.properties.UserPropertiesV4;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * As the current UserProperties impl uses java serialized objects,
 * flush a textual / CSV representation to disk for archival/backup/inspection.
 */
public class WriteUserPropertiesToDisk {

    private static Logger _log = LoggerFactory.getLogger(WriteUserPropertiesToDisk.class);

    private String filename = System.getProperty("java.io.tmpdir") + File.separator + "user_properties.csv";

    private UserService _userService;

    private UserPropertiesService _userPropertiesService;

    private long batchSize = 1000;

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Autowired
    public void setUserService(UserService userService) {
        _userService = userService;
    }

    @Autowired
    public void setUserPropertiesService(
            UserPropertiesService userPropertiesService) {
        _userPropertiesService = userPropertiesService;
    }

    public void init() {
        // no-op
    }

    @PostConstruct
    public void execute() {
        List<Integer> allUserIds = _userService.getAllUserIds();
        try {
            _log.info("writing users to " + filename);
            open();
            header();

            int count = 0;
            for (Integer userId : allUserIds) {
                count++;
                if ( count % 1000 == 0) {
                    _log.info("wrote " + count + " users");
                }
                User user = _userService.getUserForId(userId);
                UserProperties up = user.getProperties();
                if (up instanceof UserPropertiesV3) {
                    write(user, (UserPropertiesV3) up);
                } else if (up instanceof UserPropertiesV4) {
                    write(user, (UserPropertiesV4) up);
                } else {
                    _log.info("unsupported user " + up.getClass().getName()
                            + " with id" + userId);
                }

            }

            close();
            _log.info("finished writing users to " + filename);
        } catch (IOException ioe) {
            _log.error("file handling issue :" + ioe + " for " + filename);
        }
    }

    private void write(User user, UserPropertiesV3 v3) throws IOException {
        write(user, v3.getContactName(), v3.getContactCompany(), v3.getContactEmail(),
                v3.getContactDetails(), v3.getMinApiRequestInterval());

    }

    private void write (User user, UserPropertiesV4 v4) throws IOException {
        write(user, v4.getContactName(), v4.getContactCompany(), v4.getContactEmail(),
                v4.getContactDetails(), v4.getMinApiRequestInterval());

    }

    private void write(User user, String name, String company, String email, String details, Long limit)
            throws IOException {
        for (UserIndex index : user.getUserIndices()) {
            if (index.getId().getType().equals(UserIndexTypes.API_KEY)) {
                csvWriter.append(sanitize(index.getCredentials()))
                        .append(",")
                        .append(sanitize(name))
                        .append(",")
                        .append(sanitize(company))
                        .append(",")
                        .append(sanitize(email))
                        .append(",")
                        .append(sanitize(details))
                        .append(",")
                        .append(String.valueOf(limit))
                        .append("\n");
            }
        }
    }

    FileWriter csvWriter;
    private void open() throws IOException {
        csvWriter = new FileWriter(filename);
    }
    private void header() throws IOException {
        csvWriter.append("api_key,concatName,contactCompany,contactEmail,contactDetails,limit");

    }
    private void close() throws IOException {
        csvWriter.flush();
        csvWriter.close();
    }

    private String sanitize(String data) {
        if (StringUtils.isBlank(data)) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

}
