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
package org.onebusaway.webapp.actions.p;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.cachecontrol.CacheControl;
import org.onebusaway.webapp.actions.AbstractAction;
import org.onebusaway.wiki.api.WikiAttachmentContent;
import org.onebusaway.wiki.api.WikiDocumentService;
import org.onebusaway.wiki.api.WikiException;
import org.onebusaway.wiki.api.WikiPage;
import org.onebusaway.wiki.api.WikiRenderingService;
import org.onebusaway.wiki.api.impl.WikiPageImpl;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
    @Result(location = "/WEB-INF/content/p/index.jspx"),
    @Result(name = "notFound", location = "/WEB-INF/content/p/index-notFound.jspx"),
    @Result(name = "raw", type = "stream", params = {
        "contentType", "contentType"}),
    @Result(name = "404", type = "httpheader", params = {
        "error", "404", "errorMessage", "resource not found"})})
@Namespace("/p/{pageName}")
public class IndexAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private WikiDocumentService _wikiDocumentService;

  private WikiRenderingService _wikiRenderingService;
  
  private String _namespace = "Main";

  private String _pageName;
  
  private boolean _forceRefresh = false;

  private WikiPage _page;

  private String _renderedContent;

  private String _editLink;

  /****
   * Members for Raw Result
   ****/

  private InputStream _inputStream;

  private String _contentType;

  @Autowired
  public void setWikiModelService(WikiDocumentService wikiModelService) {
    _wikiDocumentService = wikiModelService;
  }

  @Autowired
  public void setWikiRenderingService(WikiRenderingService wikiRenderingService) {
    _wikiRenderingService = wikiRenderingService;
  }
  
  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  public void setPageName(String pageName) {
    _pageName = pageName;
  }

  public void setForceRefresh(boolean forceRefresh) {
    _forceRefresh = forceRefresh;
  }

  public WikiPage getPage() {
    return _page;
  }

  public String getRenderedContent() {
    return _renderedContent;
  }

  public String getEditLink() {
    return _editLink;
  }

  public boolean isAdmin() {
    return _currentUserService.isCurrentUserAdmin();
  }

  /****
   * Methods for Raw Result
   ****/

  public InputStream getInputStream() {
    return _inputStream;
  }

  public String getContentType() {
    return _contentType;
  }

  public Date getLastModified() throws WikiException {

    ensureWikiPage();

    if (_page == null)
      return null;

    return _page.getLastModified();
  }

  public String attachment() throws Exception {

    String name = _pageName;
    int index = name.indexOf('@');
    if (index == -1)
      return INPUT;
    String pageName = name.substring(0, index);
    name = name.substring(index + 1);

    WikiAttachmentContent content = _wikiDocumentService.getWikiAttachmentContent(
        _namespace, pageName, name, getLocale(), _forceRefresh);

    if (content == null)
      return "404";

    _inputStream = content.getContent();
    _contentType = content.getContentType();
    return "raw";
  }

  @CacheControl(lastModifiedMethod = "getLastModified", maxAge = 60 * 60)
  public String raw() throws Exception {

    ensureWikiPage();

    String content = "";

    if (_page != null)
      content = _page.getContent();

    _inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
    _contentType = "text/css";
    return "raw";
  }

  @Override
  public String execute() throws Exception {

    ensureWikiPage();

    if (_page == null) {

      WikiPageImpl page = new WikiPageImpl();
      page.setNamespace(_namespace);
      page.setName(_pageName);
      _page = page;
      _editLink = _wikiRenderingService.getEditLink(_page);
      return "notFound";
    }

    _renderedContent = _wikiRenderingService.renderPage(_page);
    _editLink = _wikiRenderingService.getEditLink(_page);

    return SUCCESS;
  }

  private void ensureWikiPage() throws WikiException {

    if (_page != null)
      return;

    _page = _wikiDocumentService.getWikiPage(_namespace, _pageName, getLocale(),
        _forceRefresh);
  }

}
