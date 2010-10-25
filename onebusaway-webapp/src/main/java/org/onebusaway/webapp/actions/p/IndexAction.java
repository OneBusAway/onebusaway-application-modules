package org.onebusaway.webapp.actions.p;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.cachecontrol.CacheControl;
import org.onebusaway.webapp.actions.AbstractAction;
import org.onebusaway.wiki.api.WikiDocumentService;
import org.onebusaway.wiki.api.WikiException;
import org.onebusaway.wiki.api.WikiPage;
import org.onebusaway.wiki.api.WikiRenderingService;
import org.onebusaway.wiki.api.impl.WikiPageImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

@Results({
    @Result(location = "/WEB-INF/content/p/index.jspx"),
    @Result(name = "notFound", location = "/WEB-INF/content/p/index-notFound.jspx"),
    @Result(name = "raw", type = "stream", params = {
        "contentType", "contentType"})})
@Namespace("/p/*")

public class IndexAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private WikiDocumentService _wikiDocumentService;

  private WikiRenderingService _wikiRenderingService;

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

  @CacheControl(lastModifiedMethod = "getLastModified", maxAge=60*60)
  public String raw() throws Exception {

    ensureWikiPage();
    
    System.out.println("raw=" + (_page == null ? "" : _page.getName()));

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
    
    System.out.println("wiki=" + (_page == null ? "" : _page.getName()));

    if (_page == null) {

      ActionContext context = ActionContext.getContext();
      ActionInvocation invocation = context.getActionInvocation();
      ActionProxy proxy = invocation.getProxy();

      String namespace = "Main";
      String name = proxy.getActionName();

      WikiPageImpl page = new WikiPageImpl();
      page.setNamespace(namespace);
      page.setName(name);
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

    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    String namespace = "Main";
    String name = proxy.getActionName();

    _page = _wikiDocumentService.getWikiPage(namespace, name, _forceRefresh);
  }

}
