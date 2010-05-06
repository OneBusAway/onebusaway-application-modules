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
package edu.washington.cs.rse.transit;

import com.opensymphony.xwork2.util.FileManager;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class MetroKCApplicationContext {

  public static ApplicationContext getApplicationContext() {
    return getApplicationContext(false);
  }

  public static ApplicationContext getApplicationContext(boolean shutdownHook) {

    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "applicationContext.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "applicationContext-offline.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("data-sources.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "data-sources-offline.xml"));
    ctx.refresh();
    ctx.registerShutdownHook();

    // When this was true, custom converters were continuously being
    // reloaded...
    FileManager.setReloadingConfigs(false);

    return ctx;
  }

  public static ApplicationContext getWebContext() {
    return getWebContext(false);
  }

  public static ApplicationContext getWebContext(boolean shutdownHook) {

    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "applicationContext.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "applicationContext-server.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource("dataSources.xml"));
    xmlReader.loadBeanDefinitions(new ClassPathResource(
        "dataSources-server.xml"));
    ctx.refresh();
    ctx.registerShutdownHook();

    // When this was true, custom converters were continuously being
    // reloaded...
    FileManager.setReloadingConfigs(false);

    return ctx;
  }
}
