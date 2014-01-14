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
package org.onebusaway.presentation.impl.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.regex.Pattern;

import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;

import com.google.gwt.resources.client.CssResource;
import com.steadystate.css.parser.SACParserCSS2;

public class CssResourceImpl extends ResourcePrototypeImpl implements
    CssResource, ResourceWithUrl, LocalResource, InvocationHandler {

  static Pattern URL_PATTERN = Pattern.compile("^@url (\\w+) (\\w+);$");

  private URL _cssUrl;

  private File _outputFile;

  private String _outputText;

  private String _outputUrl;

  public CssResourceImpl(ClientBundleContext context,
      ClientBundleImpl parentBundle, String name, URL cssUrl) {
    super(context, parentBundle, name);
    _cssUrl = cssUrl;
  }

  /****
   * {@link CssResource} Interface
   ****/

  @Override
  public boolean ensureInjected() {
    // This method has no effect, as we do not support dynamic injection
    return false;
  }

  @Override
  public String getText() {
    refresh();
    return _outputText;
  }

  /****
   * {@link ResourceWithUrl}
   ****/

  @Override
  public String getUrl() {
    refresh();
    return _outputUrl;
  }

  /****
   * {@link InvocationHandler} Interface
   ****/
  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    Class<?> declaringClass = method.getDeclaringClass();
    if( declaringClass.isAssignableFrom(CssResourceImpl.class))
      return method.invoke(this, args);

    String name = method.getName();
    int index = name.lastIndexOf('.');
    if( index != -1)
      name = name.substring(index+1);
    return name;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  protected synchronized void refresh() {

    if (isUpToDate())
      return;

    System.out.println("refreshing resource: " + _cssUrl);

    try {

      _outputText = parse();
      String key = ResourceSupport.getHash(_outputText);
      _outputUrl = constructURL(key, "css", this);
      _outputFile = new File(_context.getTempDir(), cleanUrlForPath(_outputUrl));
      _localUrl = _outputFile.toURI().toURL();

      File parent = _outputFile.getParentFile();
      if (parent != null && !parent.exists())
        parent.mkdirs();

      BufferedWriter writer = new BufferedWriter(new FileWriter(_outputFile));
      writer.write(_outputText);
      writer.close();

      setUpToDate();

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String parse() throws IOException {
    InputStreamReader reader2 = new InputStreamReader(_cssUrl.openStream());
    InputSource source2 = new InputSource(reader2);
    Parser p = new SACParserCSS2();
    CssDocumentHandlerImpl handler = new CssDocumentHandlerImpl(_context,
        _parentBundle);
    p.setDocumentHandler(handler);
    p.parseStyleSheet(source2);
    reader2.close();
    return handler.getResults();
  }

  private String cleanUrlForPath(String url) {
    url = url.replace('?', '_');
    return url;
  }

}
