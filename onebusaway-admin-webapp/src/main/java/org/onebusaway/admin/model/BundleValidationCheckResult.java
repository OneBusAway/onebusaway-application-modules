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
package org.onebusaway.admin.model;

/**
 * This is the result created by running a BundleValidationCheck query and 
 * assessing the returned value to determine success or failure.
 * @author jpearson
 *
 */
public class BundleValidationCheckResult {
  private int linenum;
  private int csvLinenum;
  private String specificTest;
  private String testStatus;
  private String testResult;
  private String testQuery;
  
  public int getLinenum() {
    return linenum;
  }
  public void setLinenum(int linenum) {
    this.linenum = linenum;
  }
  public int getCsvLinenum() {
    return csvLinenum;
  }
  public void setCsvLinenum(int csvLinenum) {
    this.csvLinenum = csvLinenum;
  }
  public String getSpecificTest() {
    return specificTest;
  }
  public void setSpecificTest(String specificTest) {
    this.specificTest = specificTest;
  }
  public String getTestStatus() {
    return testStatus;
  }
  public void setTestStatus(String testStatus) {
    this.testStatus = testStatus;
  }
  public String getTestResult() {
    return testResult;
  }
  public void setTestResult(String testResult) {
    this.testResult = testResult;
  }
  public String getTestQuery() {
    return testQuery;
  }
  public void setTestQuery(String testQuery) {
    this.testQuery = testQuery;
  }
}
