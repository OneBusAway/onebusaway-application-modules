package org.onebusaway.webapp.actions.bundles;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

public class ResourceBundleSupport {
  public static Map<String, String> getLocaleMap(LocaleProvider localeProvider, Class<?> resourceType) {
    TextProviderFactory factory = new TextProviderFactory();
    TextProvider provider = factory.createInstance(resourceType, localeProvider);
    ResourceBundle bundle = provider.getTexts();
    Map<String, String> m = new LinkedHashMap<String, String>();
    for (Enumeration<String> en = bundle.getKeys(); en.hasMoreElements();) {
      String key = en.nextElement();
      String value = bundle.getString(key);
      m.put(key, value);
    }
    return m;
  }
}
