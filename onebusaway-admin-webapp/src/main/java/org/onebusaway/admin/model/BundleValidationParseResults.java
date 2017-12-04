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

import java.util.List;

/**
 * This is the result returned from parsing a file of bundle validation checks.
 * @author jpearson
 *
 */
public class BundleValidationParseResults {
  public List<ParsedBundleValidationCheck> parsedBundleChecks;
  public List<BundleValidationParseError> parseErrors;
  
  public List<ParsedBundleValidationCheck> getParsedBundleChecks() {
    return parsedBundleChecks;
  }
  public void setParsedBundleChecks(
      List<ParsedBundleValidationCheck> parsedBundleChecks) {
    this.parsedBundleChecks = parsedBundleChecks;
  }
  public List<BundleValidationParseError> getParseErrors() {
    return parseErrors;
  }
  public void setParseErrors(List<BundleValidationParseError> parseErrors) {
    this.parseErrors = parseErrors;
  }
}
