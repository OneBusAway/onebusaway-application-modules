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
package org.onebusaway.webapp.gwt.where_library.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mockito.Mockito;

import com.google.gwt.core.client.GWT;
//import com.google.gwt.core.client.GWTBridge;
//import com.google.gwt.dev.About;

/**
 * See http://www.assertinteresting.com/2009/05/unit-testing-gwt/ for
 * inspiration
 * 
 * @author bdferris
 */
public class GWTMockLibrary {

  public static void enable() {
    //GWTBridge bridge = new GWTWidgetBridge();
    /** our change **/
    //setGwtBridge(bridge);
  }

  public static void disable() {
    //setGwtBridge(null);
  }

  /*
  private static void setGwtBridge(GWTBridge bridge) {
    Class<?> gwtClass = GWT.class;
    Class<?>[] paramTypes = new Class[] {GWTBridge.class};
    Method setBridgeMethod = null;
    try {
      setBridgeMethod = gwtClass.getDeclaredMethod("setBridge", paramTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    setBridgeMethod.setAccessible(true);
    try {
      setBridgeMethod.invoke(gwtClass, new Object[] {bridge});
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
  *

  /*
  private static class GWTWidgetBridge extends GWTBridge {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<?> classLiteral) {
      classLiteral = checkForAsyncService(classLiteral);
      return (T) Mockito.mock(classLiteral);
    }

    private Class<?> checkForAsyncService(Class<?> classLiteral) {

      try {
        String asyncClassName = classLiteral.getName() + "Async";
        return Class.forName(asyncClassName);
      } catch (ClassNotFoundException e) {

      }

      return classLiteral;
    }

    @Override
    public String getVersion() {
      return About.getGwtVersionNum();
    }

    @Override
    public boolean isClient() {
      return false;
    }

    @Override
    public void log(String s, Throwable throwable) {
      System.out.println(s);
    }
  }
  */

}
