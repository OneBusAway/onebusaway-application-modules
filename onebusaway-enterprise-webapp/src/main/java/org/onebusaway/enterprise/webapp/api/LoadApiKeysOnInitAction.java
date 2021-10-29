/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Load API keys from csv file created from WriteUserPropertiesToDisk
 */
public class LoadApiKeysOnInitAction {

  public static int KEY = 0;
  public static int NAME = 1;
  public static int COMPANY = 2;
  public static int EMAIL = 3;
  public static int DETAILS = 4;
  public static int LIMIT = 5;

  private static Logger _log = LoggerFactory.getLogger(LoadApiKeysOnInitAction.class);
  private String filename = System.getProperty("java.io.tmpdir") + File.separator + "user_properties.csv";

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

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

  private BufferedReader csvReader;

  @PostConstruct
  public void execute() {
    _log.info("LoadApiKeysOnInitAction start");
    String line = null;
    try {
      _log.info("reading users from " + filename);
      open();
      line = readLine(); // discard header
      int count = 0;
      while (line != null) {
        line = readLine();
        List<Record> records = parseLine(line);
        if (records != null) {
          for (Record record : records) {
            createUser(record);
          }
        }
        if (count % 100 == 0) {
          _log.info("wrote " + count + " users");
        }
        count++;
      }
    } catch (Exception any) {
      _log.error("load users failed on line + |" + line + "| with " + any, any);
    } finally {
      _log.info("LoadApiKeysOnInitAction complete");
      try {
        close();
      } catch (Exception any) {
        // bury
      }
    }
  }


  private void createUser(Record record) {
    CreateAPIKeyOnInitAction action = new CreateAPIKeyOnInitAction();
    action.setUserService(_userService);
    action.setUserPropertiesService(_userPropertiesService);
    copyToAction(action, record);
  }

  private void copyToAction(CreateAPIKeyOnInitAction action, Record record) {
    action.setKey(record.getKey());
    action.setContactName(record.getContactName());
    action.setContactCompany(record.getContactCompany());
    action.setContactEmail(record.getContactEmail());
    action.setContactDetails(record.getContactDetails());
    action.setLimit(record.getLimit());
    action.setCreateAPIKey(true);
    try {
      action.execute();
    } catch (Exception any) {
      _log.error("record " + record.getKey() + " insertion failed: " + any.toString());
    }
  }


  private void open() throws IOException {
    csvReader = new BufferedReader(new FileReader(filename));
  }

  private void close() throws IOException {
    if (csvReader != null) {
      csvReader.close();
    }
  }

  private String readLine() throws IOException {
    return csvReader.readLine();
  }

  private List<Record> parseLine(String line) {
    List<Record> records = new ArrayList<>();
    List<String> keys = parseKeys(line);
    for (String key : keys) {
      Record user = new Record();
      user.setKey(key);
      user.setContactName(getSafeColumn(line, NAME));
      user.setContactCompany(getSafeColumn(line, COMPANY));
      user.setContactEmail(getSafeColumn(line, EMAIL));
      user.setContactDetails(getSafeColumn(line, DETAILS));
      user.setLimit(getSafeColumn(line, LIMIT));
      records.add(user);
    }
    return records;
  }

  private List<String> parseKeys(String line) {
    return getColumn(line, KEY, true);
  }

  String getSafeColumn(String line, int columnIndex) {
    List<String> columns = getColumn(line, columnIndex, false);
    if (columns.isEmpty()) return null;
    return columns.get(0);
  }

  List<String> getColumn(String line, int requestedColumn, boolean splitOnComma) {
    List<String> elements = new ArrayList<>();
    if (line == null)
      return elements;

    int currentColumn = 0;
    int start = 0;
    int end = 0;
    while (currentColumn <= requestedColumn) {

      if (start >= line.length()) return elements;
      if (line.charAt(start) == '"') {
        start++;
        end = line.indexOf('"', start);
      } else {
        end = line.indexOf(',', start);
      }
      if (end < 0 || end > line.length()) {
        end = line.length();
      }

      if (currentColumn == requestedColumn) {
        String columnStr = line.substring(start, end);
        if (splitOnComma) {
          for (String s : columnStr.split(",")) {
            if (s != null && s.trim() != null && s.trim().length() > 0) {
              elements.add(s.trim());
            }
          }
        } else {
          if (columnStr != null && columnStr.trim() != null && columnStr.trim().length() > 0) {
            elements.add(columnStr.trim());
          }
        }
      }

      if (start -1 >= 0 && line.charAt(start-1) == '"') {
        start = end + 2; // consume comma as well
      } else {
        start = end + 1;
      }
      currentColumn++;
    }
    return elements;

  }

  private static class Record {
    private String key;
    private String contactName;
    private String contactCompany;
    private String contactEmail;
    private String contactDetails;
    private String limit;
    public String getKey() {
      return key;
    }
    public void setKey(String key) {
      this.key = key;
    }
    public String getContactName() {
      return contactName;
    }
    public void setContactName(String contactName) {
      this.contactName = contactName;
    }
    public String getContactCompany() {
      return contactCompany;
    }
    public void setContactCompany(String contactCompany) {
      this.contactCompany = contactCompany;
    }
    public String getContactEmail() {
      return contactEmail;
    }
    public void setContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
    }
    public String getContactDetails() {
      return contactDetails;
    }
    public void setContactDetails(String contactDetails) {
      this.contactDetails = contactDetails;
    }
    public String getLimit() { return limit; }
    public void setLimit(String limit) { this.limit = limit; }
  }
}
