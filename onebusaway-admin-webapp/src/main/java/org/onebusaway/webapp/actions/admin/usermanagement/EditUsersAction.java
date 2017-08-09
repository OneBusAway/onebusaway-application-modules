package org.onebusaway.webapp.actions.admin.usermanagement;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.StringReader;
import java.util.List;

public class EditUsersAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListUsersAction.class);
    private UserManagementService userManagementService;
    private String username;
    private String role;
    private String password;
    private UserDetail userToEdit;
    private String updateUserMessage;

    public String init() {

        setUserToEdit(userManagementService.getUserDetail(username));
        boolean success = userManagementService.updateUser(userToEdit);

        return SUCCESS;
    }

    public String userUpdate() {

        log.error("EditUsersAction .... *********** !!!!!!!!!!!!!");

        setUserToEdit(userManagementService.getUserDetail(username));
        log.error("User to Edit: username: " + getUserToEdit().getUsername());
        log.error("User to Edit: role: " + getUserToEdit().getRole());
        log.error("User to Edit: id: " + getUserToEdit().getId());
        //userToEdit.setRole(getRole());

        boolean success = userManagementService.updateUser(userToEdit);

        if(success) {
            updateUserMessage =  "User '" +userToEdit.getUsername() + "' edited successfully";
        } else {
            updateUserMessage = "Error editing user : '" +userToEdit.getUsername() +"'";
        }

        return "updateUser";

    }

    @Autowired
    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public String getUpdateUserMessage() {
        return updateUserMessage;
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

    public UserDetail getUserToEdit() {
        return userToEdit;
    }

    public void setUserToEdit(UserDetail userToEdit) {
        this.userToEdit = userToEdit;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
