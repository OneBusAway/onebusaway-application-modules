package org.onebusaway.webapp.actions.admin.usermanagement;

import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EditUsersAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListUsersAction.class);
    private UserManagementService userManagementService;
    private String username;

    public void init() {

        log.info("Getting user detail for user : {}", username);

        UserDetail userDetail = userManagementService.getUserDetail(username);

    }

    /**
     * @param userManagementService the userManagementService to set
     */
    @Autowired
    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public List<String> getPossibleRoles() {
        return userManagementService.getAllRoleNames();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
