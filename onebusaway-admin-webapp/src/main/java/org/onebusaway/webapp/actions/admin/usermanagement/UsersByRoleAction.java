/**
 * Copyright (C) 2017 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.usermanagement;

import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UsersByRoleAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(UsersByRoleAction.class);
    private UserManagementService userManagementService;
    private List<UserDetail> userDetailsList;

    public String execute() {
        super.execute();

        List<UserDetail> udl = userManagementService.getActiveUsersDetails();

        Collections.sort(udl, new Comparator<UserDetail>() {
            @Override
            public int compare(UserDetail userDetail, UserDetail userDetail2) {
                return userDetail.getRole().compareTo(userDetail2.getRole());
            }
        });

        setUserDetailsList(udl);

        return SUCCESS;
    }

    /**
     * @param userManagementService the userManagementService to set
     */
    @Autowired
    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public void setUserDetailsList(List<UserDetail> userDetailsList) {
        this.userDetailsList = userDetailsList;
    }

    public List<UserDetail> getUserDetailsList() {
        return userDetailsList;
    }
}

