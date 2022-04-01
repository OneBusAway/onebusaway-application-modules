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
package org.onebusaway.api.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.handler.AbstractContentTypeHandler;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.serializers.json.CustomSerializerProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


public class CustomJsonLibHandler extends AbstractContentTypeHandler {

        private String defaultEncoding = "ISO-8859-1";
        private ObjectMapper mapper = new ObjectMapper();

        public void toObject(ActionInvocation invocation, Reader in, Object target) throws IOException {
                this.mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
                ObjectReader or = this.mapper.readerForUpdating(target);
                or.readValue(in);
        }

        public String fromObject(ActionInvocation invocation, Object obj, String resultCode, Writer stream) throws IOException {
                String callback = getCallback();
                return fromObject(invocation, obj, resultCode, stream, callback);
        }

        public String fromObject(ActionInvocation invocation, Object obj, String resultCode, Writer stream, String callback) throws IOException {
                boolean isText = false;
                String value = null;
                if (obj != null && obj instanceof ResponseBean) {
                        // check if serialization already occurred as with SIRI calls
                        ResponseBean bean = (ResponseBean) obj;
                        isText = bean.isString();
                        if (bean.getData() != null) {
                                value = bean.getData().toString();
                        }
                }
                if (obj != null && !isText) {
                        mapper.setSerializerProvider(new CustomSerializerProvider());
                        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
                        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
                        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
                        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                        value = mapper.writeValueAsString(obj);
                }
                if (value != null && callback != null) {
                        stream.write(callback + "(" + value + ")");
                }
                else {
                        stream.write(value);
                }

                return null;
        }

        public String getCallback(){
                String callback = null;
                HttpServletRequest req = ServletActionContext.getRequest();
                if (req != null) {
                        callback = req.getParameter("callback");
                }
                return callback;
        }

        public String getContentType() {
                String callback = getCallback();
                if(callback != null){
                        return "application/javascript";
                }
                // we used to set charset for callbacks
                // after Jackson upgrade to 2.12.0 this no longer works
                //return "application/json;charset=" + this.defaultEncoding;
                return "application/json";
        }

        public String getExtension() {
                return "json";
        }

        @Inject("struts.i18n.encoding")
        public void setDefaultEncoding(String val) {
                this.defaultEncoding = val;
        }
}
