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
package edu.washington.cs.rse.transit.web.tags;

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.struts2.components.Component;

import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import com.opensymphony.xwork2.util.ValueStack;

public class GWTSerializationComponent extends Component {

    private static Logger LOG = Logger.getLogger(GWTSerializationComponent.class.getName());

    public GWTSerializationComponent(ValueStack stack) {
        super(stack);
    }

    private String _value;

    public void setValue(String value) {
        _value = value;
    }

    public boolean start(Writer writer) {
        boolean result = super.start(writer);

        if (_value == null) {
            _value = "top";
        } else if (altSyntax()) {
            // the same logic as with findValue(String)
            // if value start with %{ and end with }, just cut it off!
            if (_value.startsWith("%{") && _value.endsWith("}")) {
                _value = _value.substring(2, _value.length() - 1);
            }
        }

        Object actualValue = getStack().findValue(_value);

        if (actualValue != null) {
            try {
                SerializationPolicy policy = RPC.getDefaultSerializationPolicy();
                ServerSerializationStreamWriter gwtWriter = new ServerSerializationStreamWriter(policy);
                gwtWriter.serializeValue(actualValue, actualValue.getClass());
                String r = gwtWriter.toString();
                writer.write('\'' + r + '\'');

            } catch (Exception e) {
                LOG.log(Level.INFO, "Could not serialize out value '" + _value + "'", e);
            }
        }

        return result;
    }
}
