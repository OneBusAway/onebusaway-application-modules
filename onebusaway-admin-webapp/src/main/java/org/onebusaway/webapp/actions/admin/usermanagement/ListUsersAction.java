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

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lists the users in the database.
 *
 */
@AllowedMethods({"firstPage", "nextPage", "previousPage"})
public class ListUsersAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListUsersAction.class);
    private UserManagementService userManagementService;
    private List<UserDetail> userDetailsList;
    private int usersPerPage = 15;
    private int numberOfPages;
    private int thisPage;

    public String execute() {
        super.execute();

        firstPage();
        return SUCCESS;
    }

    public String firstPage(){
        setThisPage(1);
        int count = userManagementService.getUserDetailsCount();
        setNumberOfPages((int) Math.ceil((double)count/getUsersPerPage()));
        int firstUser = 0;
        setUserDetailsList(userManagementService.getUserDetails(firstUser, getUsersPerPage()));

        return SUCCESS;
    }

    public String nextPage() {
        setThisPage(getThisPage() + 1);
        int firstUser = (getThisPage() * getUsersPerPage()) - getUsersPerPage();
        setUserDetailsList(userManagementService.getUserDetails(firstUser, getUsersPerPage()));

        return SUCCESS;
    }

    public String previousPage() {
        int firstUser = 0;
        setThisPage(thisPage - 1);
        if (thisPage !=1) {
            firstUser = (getThisPage() * getUsersPerPage()) - getUsersPerPage();
        }
        setUserDetailsList(userManagementService.getUserDetails(firstUser, getUsersPerPage()));

        return SUCCESS;
    }

    /**
     * @param userManagementService the userManagementService to set
     */
    @Autowired
    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public int getUsersPerPage() {
        return usersPerPage;
    }

    public void setUsersPerPage(int usersPerPage) {
        this.usersPerPage = usersPerPage;
    }

    public int getNumberOfPages() { return numberOfPages; }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public int getThisPage() {
        return thisPage;
    }

    public void setThisPage(int thisPage) {
        this.thisPage = thisPage;
    }

    public void setUserDetailsList(List<UserDetail> userDetailsList) {
        this.userDetailsList = userDetailsList;
    }

    public List<UserDetail> getUserDetailsList() {
        return userDetailsList;
    }
}
