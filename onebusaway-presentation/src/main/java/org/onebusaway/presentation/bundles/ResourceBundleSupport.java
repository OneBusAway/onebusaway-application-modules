/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.bundles;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.util.StrutsLocalizedTextProvider;

public class ResourceBundleSupport {
  public static Map<String, String> getLocaleMap(LocaleProvider localeProvider, Class<?> resourceType) {
    StrutsLocalizedTextProvider localizedTextProvider = new StrutsLocalizedTextProvider();
    ResourceBundle bundle = localizedTextProvider.findResourceBundle(resourceType.getName(), Locale.getDefault());
    Map<String, String> m = new LinkedHashMap<String, String>();
    for (Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();) {
      String key = en.nextElement();
      String value = bundle.getString(key);
      m.put(key, value);
    }
    return m;
  }
}
