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
    private List<UserDetail> userDetailsList;
    private int usersPerPage = 5;
    private int numberOfPages;
    private int thisPage = 1;

    public String execute() {
        super.execute();

        firstPage();
        return SUCCESS;
    }

    //why are these all strings??

    public String firstPage(){
        setThisPage(1);
        int count = userManagementService.getUserDetailsCount();

        log.error("User details count is: " + count);
        log.error("Users per page is: " + getUsersPerPage());

        setNumberOfPages((int) Math.ceil((double)count/getUsersPerPage()));

        log.error("Number of pages is: " + getNumberOfPages());

        int first = 0;
        log.error("First: " + first);
        setUserDetailsList(userManagementService.getUserDetails(first, getUsersPerPage()));

        return SUCCESS;
    }

    public String nextPage() {
        setThisPage(thisPage + 1);
        int count = userManagementService.getUserDetailsCount();
        setNumberOfPages((int) Math.ceil((double)count/getUsersPerPage()));
        int first = (getThisPage() * getUsersPerPage()) - getUsersPerPage();
        log.error("First: " + first);
        setUserDetailsList(userManagementService.getUserDetails(first, getUsersPerPage()));

        addActionMessage("here we are at the Next page");

        return SUCCESS;
    }

    public String previousPage() {
        setThisPage(thisPage - 1);
        if (thisPage ==1) {
            firstPage();
            addActionMessage("here we are at the Next page");
            return SUCCESS;
        }
        int first = (getThisPage() * getUsersPerPage()) - getUsersPerPage();
        log.error("First: " + first);
        setUserDetailsList(userManagementService.getUserDetails(first, getUsersPerPage()));

        addActionMessage("here we are at the Previous page");

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
