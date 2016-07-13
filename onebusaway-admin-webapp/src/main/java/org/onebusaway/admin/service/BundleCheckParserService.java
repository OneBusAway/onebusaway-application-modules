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
package org.onebusaway.admin.service;

import java.io.Reader;
import org.onebusaway.admin.model.BundleValidationParseResults;

/**
 * Provides a service to parse a file containing bundle validation checks used
 * to establish the validity of a transit data bundle.  Specific implementations
 * could parse data from different file formats, such as .csv or .xlsx.
 * @author jpearson
 *
 */
public interface BundleCheckParserService {
  public BundleValidationParseResults parseBundleChecksFile(Reader csvDataFile);
}
