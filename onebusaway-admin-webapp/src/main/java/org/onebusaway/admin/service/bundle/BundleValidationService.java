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
package org.onebusaway.admin.service.bundle;

import org.onebusaway.admin.model.BundleRequest;
import org.onebusaway.admin.model.BundleResponse;
import org.onebusaway.admin.model.ServiceDateRange;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface BundleValidationService {
  List<ServiceDateRange> getServiceDateRanges(InputStream gtfsZipFile);
  Map<String, List<ServiceDateRange>> getServiceDateRangesByAgencyId(List<ServiceDateRange> ranges);
  Map<String, List<ServiceDateRange>> getServiceDateRangesAcrossAllGtfs(List<InputStream> gtfsZipFiles);
  int validateGtfs(String gtfsZipFileName, String outputFile);
  int installAndValidateGtfs(String gtfsZipFileName, String outputFile);
  void upload(BundleRequest request, BundleResponse response);
  void downloadAndValidate(BundleRequest request, BundleResponse response);
}
