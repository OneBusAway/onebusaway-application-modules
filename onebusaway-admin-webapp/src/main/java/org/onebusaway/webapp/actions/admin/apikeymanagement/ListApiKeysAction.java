package org.onebusaway.webapp.actions.admin.apikeymanagement;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ListApiKeysAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListApiKeysAction.class);
    private List<String> apiKeysList;
    private List<UserBean> apiKeysUserBeansList = new ArrayList<UserBean>();
    private UserService userService;
    private UserPropertiesService userPropertiesService;

    public String execute() {
        super.execute();

        generateUserBeans();
        return SUCCESS;
    }

    public void generateUserBeans() {

        for(String key : getApiKeysList()) {
            UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
            UserIndex userIndexForId = userService.getUserIndexForId(userKey);
            if (userIndexForId != null) {
                User user = userIndexForId.getUser();
                UserBean bean = userService.getUserAsBean(user);
                if (bean != null) {
                    getApiKeysUserBeansList().add(bean);
                    log.error("** ! User bean id: " + bean.getUserId());
                    log.error("** ! User bean index: " + bean.getIndices().get(0).getValue());
                }
            }
        }

        /*
        // Check if key already exists
    UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndexForId = userService.getUserIndexForId(userKey);
    if (userIndexForId == null) {
      addActionMessage("Key '" + key + "' does not exist");
    } else {
      User user = userIndexForId.getUser();
      UserBean bean = userService.getUserAsBean(user);
      minApiReqInt = bean.getMinApiRequestInterval();
      contactName = bean.getContactName();
      contactCompany = bean.getContactCompany();
      contactEmail = bean.getContactEmail();
      contactDetails = bean.getContactDetails();
      addActionMessage("Key '" + key + "' found");
    }
    return SUCCESS;
  }
         */
    }


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

    public List<String> getApiKeysList() {
        return userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);
    }


    public List<UserBean> getApiKeysUserBeansList() {
        return apiKeysUserBeansList;
    }

    public void setApiKeysUserBeansList(List<UserBean> apiKeysUserBeansList) {
        this.apiKeysUserBeansList = apiKeysUserBeansList;
    }
}
