/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service;

import org.onebusaway.admin.model.ParsedBundleValidationCheck;

/**
 * For building an api query that will be used to check the validity of a transit data bundle.
 * The query is generated from parsed data returned from BundleCheckParserService.
 * @author jpearson
 *
 */

public interface BundleValidationCheckService {
  public String buildQuery(String envURI, String apiKey, String apiQuery,
      String siriQuery, ParsedBundleValidationCheck check);
}