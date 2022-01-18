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
package org.onebusaway.webapp.actions.admin.apikeymanagement;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@AllowedMethods({"firstPage", "nextPage", "previousPage"})
public class ListApiKeysAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListApiKeysAction.class);
    private List<UserBean> apiKeysUserBeansList = new ArrayList<UserBean>();
    private UserService userService;
    private int keysPerPage = 15;
    private int numberOfPages;
    private int thisPage;


    public String execute() {
        super.execute();

        firstPage();
        return SUCCESS;
    }

    public String firstPage(){
        setThisPage(1);
        int count = userService.getApiKeyCount();
        setNumberOfPages((int) Math.ceil((double)count/getKeysPerPage()));
        int firstKey = 0;
        List<User> users = userService.getApiKeys(firstKey, getKeysPerPage());
        generateUserBeans(users);
        return SUCCESS;
    }

    public String nextPage() {
        setThisPage(getThisPage() + 1);
        int firstUser = (getThisPage() * getKeysPerPage()) - getKeysPerPage();
        List<User> users = userService.getApiKeys(firstUser, getKeysPerPage());
        generateUserBeans(users);
        return SUCCESS;
    }

    public String previousPage() {
        int firstUser = 0;
        setThisPage(thisPage - 1);
        if (thisPage !=1) {
            firstUser = (getThisPage() * getKeysPerPage()) - getKeysPerPage();
        }
        List<User> users = userService.getApiKeys(firstUser, getKeysPerPage());
        generateUserBeans(users);
        return SUCCESS;
    }

    public void generateUserBeans(List<User> users) {

        if(!users.isEmpty()) {
            for (User user : users) {
                UserBean bean = userService.getUserAsBean(user);
                if (bean != null) {
                    getApiKeysUserBeansList().add(bean);
                }
            }
        }
    }

    /**
     * Injects {@link UserService}
     * @param userService the userService to set
     */
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public int getKeysPerPage() {
        return keysPerPage;
    }

    public void setKeysPerPage(int keysPerPage) {
        this.keysPerPage = keysPerPage;
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

    public List<UserBean> getApiKeysUserBeansList() {
        return apiKeysUserBeansList;
    }

    public void setApiKeysUserBeansList(List<UserBean> apiKeysUserBeansList) {
        this.apiKeysUserBeansList = apiKeysUserBeansList;
    }
}
