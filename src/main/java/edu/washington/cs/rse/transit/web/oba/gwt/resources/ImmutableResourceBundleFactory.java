/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.web.oba.gwt.resources;

import edu.washington.cs.rse.transit.web.oba.gwt.resources.ImmutableResourceBundle.Resource;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.impl.CssResourceImpl;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.impl.DataResourceImpl;
import edu.washington.cs.rse.transit.web.oba.gwt.resources.impl.ImmutableResourceBundleImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImmutableResourceBundleFactory {

  private Map<String, ImmutableResourceBundle> _bundles = new HashMap<String, ImmutableResourceBundle>();

  private File _tempDir;

  private URLStrategy _urlStrategy;

  public ImmutableResourceBundleFactory(URLStrategy urlStrategy) {
    _urlStrategy = urlStrategy;
  }

  public void setTempDir(File tempDir) {
    _tempDir = new File(tempDir, "ImmutableResourceBundles");
    if (!_tempDir.exists())
      _tempDir.mkdirs();
  }

  public void addResource(String key, Class<?> bundleType) throws IOException {

    if (!ImmutableResourceBundle.class.isAssignableFrom(bundleType))
      throw new IllegalArgumentException("class is not assignable to "
          + ImmutableResourceBundle.class + ": " + bundleType);

    ImmutableResourceBundleImpl bundle = new ImmutableResourceBundleImpl(key);
    bundle.setURLStrategy(_urlStrategy);

    for (Method method : bundleType.getMethods()) {

      String methodName = method.getName();
      Resource resource = method.getAnnotation(ImmutableResourceBundle.Resource.class);

      if (resource == null) {
        if (!(methodName.equals("getResource") || methodName.equals("getResources")))
          System.err.println("no Resource annotation found: " + methodName);
        continue;
      }

      String[] values = resource.value();
      if (values == null || values.length != 1)
        throw new IllegalStateException("no value");
      String resourceName = values[0];

      Class<?> returnType = method.getReturnType();

      URL localURL = bundleType.getResource(resourceName);

      if (CssResource.class.isAssignableFrom(returnType)) {
        bundle.addResource(new CssResourceImpl(bundle, methodName, localURL,
            _tempDir));
      } else if (DataResource.class.isAssignableFrom(returnType)) {
        bundle.addResource(new DataResourceImpl(bundle, methodName, localURL));
      }
    }

    ImmutableResourceBundle bundleProxy = (ImmutableResourceBundle) Proxy.newProxyInstance(
        bundleType.getClassLoader(), new Class[] {bundleType}, bundle);
    _bundles.put(key, bundleProxy);
  }

  public Map<String, ImmutableResourceBundle> getBundles() {
    return _bundles;
  }
}
