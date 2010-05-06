package org.onebusaway.where.web.common.client.pages;

import org.onebusaway.common.web.common.client.AbstractPageSource;
import org.onebusaway.where.web.common.client.WhereLibrary;
import org.onebusaway.where.web.common.client.WhereMessages;
import org.onebusaway.where.web.common.client.rpc.WhereServiceAsync;

public class WhereCommonPage extends AbstractPageSource {

  protected static WhereMessages _msgs = WhereLibrary.MESSAGES;

  protected static WhereServiceAsync _service = WhereServiceAsync.SERVICE;

}
