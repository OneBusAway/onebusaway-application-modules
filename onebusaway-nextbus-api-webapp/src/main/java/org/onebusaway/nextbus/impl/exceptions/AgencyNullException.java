/**
 * Copyright (C) 2019 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.exceptions;

import org.onebusaway.nextbus.validation.ErrorMsg;

public class AgencyNullException extends Exception{

    private static final long serialVersionUID = 1L;

    public AgencyNullException(){
        super(ErrorMsg.AGENCY_NULL.getDescription());
    }
}
