package org.onebusaway.webapp.actions.admin.apikeymanagement;

import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ListApiKeysAction extends OneBusAwayNYCAdminActionSupport {

    private List<String> apiKeysList;
    private UserService userService;
    private UserPropertiesService userPropertiesService;


    /**
     * Injects {@link UserService}
     * @param userService the userService to set
     */
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Injects {@link UserPropertiesService}
     * @param userPropertiesService the userPropertiesService to set
     */
    @Autowired
    public void setUserPropertiesService(UserPropertiesService userPropertiesService) {
        this.userPropertiesService = userPropertiesService;
    }

    public void setApiKeysList(List<String> apiKeysList) {
        this.apiKeysList = apiKeysList;
    }

    // public List<UserDetail> getUserDetailsList() { return userDetailsList; }
    public List<String> getApiKeysList() {
        return userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);
    }


}
