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
package edu.washington.cs.rse.transit.web.oba.common.client.model;

import java.util.ArrayList;
import java.util.List;

public class NameBean extends ApplicationBean {

    private static final long serialVersionUID = 1L;

    private String _type;

    private List<String> _names = new ArrayList<String>();

    public NameBean() {

    }

    public NameBean(String type, String name, String... names) {
        _type = type;
        _names.add(name);
        for (String n2 : names)
            _names.add(n2);
    }

    public NameBean(String type, List<String> names) {
        _type = type;
        _names.addAll(names);
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public String getName() {
        return _names.get(0);
    }

    public String getName(int index) {
        return _names.get(index);
    }

    public List<String> getNames() {
        return _names;
    }

    /***************************************************************************
     * {@link Object} Interface
     **************************************************************************/

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NameBean))
            return false;
        NameBean bean = (NameBean) obj;
        return _type.equals(bean._type) && _names.equals(bean._names);
    }

    @Override
    public int hashCode() {
        return _type.hashCode() + _names.hashCode();
    }

    @Override
    public String toString() {
        return "name(type=" + _type + " name=" + _names + ")";
    }

}
