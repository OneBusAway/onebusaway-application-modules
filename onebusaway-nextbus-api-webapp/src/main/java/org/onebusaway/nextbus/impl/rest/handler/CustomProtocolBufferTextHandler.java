/**
 * Copyright (C) 2013 Google, Inc.
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
package org.onebusaway.nextbus.impl.rest.handler;

import com.google.protobuf.Message;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.handler.ContentTypeHandler;
import org.onebusaway.api.model.ResponseBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class CustomProtocolBufferTextHandler implements ContentTypeHandler {

  @Override
  public void toObject(Reader in, Object target) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toObject(ActionInvocation actionInvocation, Reader reader, Object o) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String fromObject(Object obj, String resultCode, Writer stream)
      throws IOException {
    if (obj != null && obj instanceof FeedMessage) {
      FeedMessage message = (FeedMessage) obj;
      stream.write(message.toString());
    }
    return null;
  }

  @Override
  public String fromObject(ActionInvocation actionInvocation, Object obj, String s, Writer stream) throws IOException {
    if (obj != null && obj instanceof FeedMessage) {
      FeedMessage message = (FeedMessage) obj;
      stream.write(message.toString());
    }
    return null;
  }

  @Override
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public String getExtension() {
    return "pbtext";
  }
}
