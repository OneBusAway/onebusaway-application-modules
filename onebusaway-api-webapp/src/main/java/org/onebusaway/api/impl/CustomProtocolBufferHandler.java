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
package org.onebusaway.api.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.handler.ContentTypeHandler;
import org.onebusaway.api.model.ResponseBean;

import com.google.protobuf.Message;

public class CustomProtocolBufferHandler implements ContentTypeHandler {

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
    ResponseBean response = (ResponseBean) obj;
    if (response.getData() != null && response.getData() instanceof Message) {
      Message message = (Message) response.getData();
      /**
       * Instead of writing to the output Writer, we write directly to the
       * HttpServletResponse output stream. That way, we can avoid any weirdness
       * with encoding the serialized protobuf to a String.
       */
      HttpServletResponse res = ServletActionContext.getResponse();
      message.writeTo(res.getOutputStream());
    } else {
      stream.write(response.getText());
    }
    return null;
  }

  @Override
  public String fromObject(ActionInvocation actionInvocation, Object obj, String s, Writer stream) throws IOException {
    ResponseBean response = (ResponseBean) obj;
    if (response.getData() != null && response.getData() instanceof Message) {
      Message message = (Message) response.getData();
      /**
       * Instead of writing to the output Writer, we write directly to the
       * HttpServletResponse output stream. That way, we can avoid any weirdness
       * with encoding the serialized protobuf to a String.
       */
      HttpServletResponse res = ServletActionContext.getResponse();
      message.writeTo(res.getOutputStream());
    } else {
      stream.write(response.getText());
    }
    return null;
  }

  @Override
  public String getContentType() {
    return "application/x-google-protobuf";
  }

  @Override
  public String getExtension() {
    return "pb";
  }
}
