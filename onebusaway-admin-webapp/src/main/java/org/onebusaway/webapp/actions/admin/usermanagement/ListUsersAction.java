package org.onebusaway.webapp.actions.admin.usermanagement;

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
public class ListUsersAction extends OneBusAwayNYCAdminActionSupport {

    private static Logger log = LoggerFactory.getLogger(ListUsersAction.class);
    private UserManagementService userManagementService;
    private int usersPerPage = 15;
    private int numberOfPages;
    private int thisPage;

    public void initializePages() {
        setThisPage(1);
        int count = userManagementService.getUserDetailsCount();
        log.error("User details count is: " + count);
        log.error("Users per page is: " + getUsersPerPage());
        setNumberOfPages((int) Math.ceil((double)count/getUsersPerPage()));
        log.error("Number of pages is: " + getNumberOfPages());
    }

    public void getPage() {

    }

    /**
     * @param userManagementService the userManagementService to set
     */
    @Autowired
    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public List<UserDetail> getAllUserDetailsList() {
        int first = (getThisPage() * getUsersPerPage()) - getUsersPerPage();
        log.error("First: " + first);
        //return userManagementService.getUserDetails(1, 500);
        return userManagementService.getUserDetails(first, getUsersPerPage());
    }

    public int getUsersPerPage() {
        return usersPerPage;
    }

    public void setUsersPerPage(int usersPerPage) {
        this.usersPerPage = usersPerPage;
    }

    public int getNumberOfPages() {
        int count = userManagementService.getUserDetailsCount();
        return ((int) Math.ceil((double)count/getUsersPerPage()));
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public int getThisPage() {
        return thisPage;
    }

    public void setThisPage(int thisPage) {
        this.thisPage = thisPage;
    }
}
