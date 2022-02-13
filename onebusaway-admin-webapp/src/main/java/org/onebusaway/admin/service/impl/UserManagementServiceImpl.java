/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.impl.authentication.VersionedPasswordEncoder;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UserManagementService}
 * @author abelsare
 *
 */
@Component
public class UserManagementServiceImpl implements UserManagementService {

	private SessionFactory _sessionFactory;
	private StandardAuthoritiesService authoritiesService;
	private UserPropertiesService userPropertiesService;
	private UserDao userDao;
	private UserService userService;
	private VersionedPasswordEncoder passwordEncoder;
	
	private static final Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<String> getUserNames(final String searchString) {
		
		final String hql = "select ui.id.value from UserIndex ui where ui.id.value like :searchString and " + 
			 "ui.id.type = 'username'";
		
		Query query = getSession().createQuery(hql);
		query.setParameter("searchString", "%" +searchString + "%");
		return query.list();
	}

	@Override
	@Transactional(readOnly=true)
    public Integer getUserDetailsCount() {
		Criteria criteria = getSession().createCriteria(User.class)
				.createCriteria("userIndices")
				.add(Restrictions.eq("id.type", UserIndexTypes.USERNAME))
				.setProjection(Projections.rowCount());
		List<User> users = criteria.list();
		Long lcount = (Long)criteria.uniqueResult();
		Integer count = BigDecimal.valueOf(lcount).intValueExact(); // do some range checking on the cast
		return count;
    }

	@Override
	@Transactional(readOnly=true)
	public List<UserDetail> getUserDetails(final int start, final int maxResults) {

		List<UserDetail> userDetails = new ArrayList<UserDetail>();
		Criteria criteria = getSession().createCriteria(User.class)
				.createCriteria("userIndices")
				.add(Restrictions.eq("id.type", UserIndexTypes.USERNAME))
				.setFirstResult(start)
				.setMaxResults(maxResults);
		List<User> users = criteria.list();

		if(!users.isEmpty()) {
			for (User user : users) {
			    if (!user.getUserIndices().isEmpty()) {
					UserDetail userDetail = buildUserDetail(user);
					userDetails.add(userDetail);
				}
			}
		}
		log.debug("Returning user details");

		return userDetails;
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserDetail> getActiveUsersDetails() {

		List<UserDetail> userDetails = new ArrayList<UserDetail>();
		Criteria criteria = getSession().createCriteria(User.class)
				.createCriteria("userIndices")
				.add(Restrictions.eq("id.type", UserIndexTypes.USERNAME));
		List<User> users = criteria.list();

		if(!users.isEmpty()) {
			for (User user : users) {
                UserBean bean = userService.getUserAsBean(user);
                if (!bean.isDisabled()) {
                    if (!user.getUserIndices().isEmpty()) {
                        UserDetail userDetail = buildUserDetail(user);
                        userDetails.add(userDetail);
                    }
                }
			}
		}
		log.debug("Returning user details");

		return userDetails;
	}

    @Override
	@Transactional(readOnly=true)
    public List<UserDetail> getInactiveUsersDetails() {

        List<UserDetail> userDetails = new ArrayList<UserDetail>();
		Criteria criteria = getSession().createCriteria(User.class)
				.createCriteria("userIndices")
				.add(Restrictions.eq("id.type", UserIndexTypes.USERNAME));
		List<User> users = criteria.list();

        if(!users.isEmpty()) {
            for (User user : users) {
                UserBean bean = userService.getUserAsBean(user);
                if (bean.isDisabled()) {
                    if (!user.getUserIndices().isEmpty()) {
                        UserDetail userDetail = buildUserDetail(user);
                        userDetails.add(userDetail);
                    }
                }
            }
        }
        log.debug("Returning user details");

        return userDetails;
    }

	@Override
	@Transactional(readOnly=true)
	public UserDetail getUserDetail(final String userName) {
		Criteria criteria = getSession().createCriteria(User.class)
				.createCriteria("userIndices")
				.add(Restrictions.like("id.value", userName));
		List<User> users = criteria.list();

		UserDetail userDetail = null;
		
		if(!users.isEmpty()) {
			userDetail = buildUserDetail(users.get(0));
		}
		
		log.debug("Returning user details for user : {}", userName);
		
		return userDetail;
		
	}

	private UserDetail buildUserDetail(User user) {
		UserDetail userDetail = new UserDetail();
		
		userDetail.setId(user.getId());
		
		for(UserIndex userIndex : user.getUserIndices()) {
			userDetail.setUsername(userIndex.getId().getValue());
		}
		
		for(UserRole role : user.getRoles()) {
			//There should be only one role
			userDetail.setRole(role.getName());
		}

        UserBean bean = userService.getUserAsBean(user);
        userDetail.setDisabled(bean.isDisabled());
		
		return userDetail;
	}

	@Override
	@Transactional
	public void disableOperatorRole(User user) {
		UserRole operatorRole = authoritiesService.getUserRoleForName(StandardAuthoritiesService.USER);
		
		Set<UserRole> roles = user.getRoles();
		
		if(roles.remove(operatorRole)) {
			userDao.saveOrUpdateUser(user);
		}
	}
	
	@Override
	@Transactional
	public boolean createUser(String userName, String password, boolean admin) {
		UserIndex userIndex = userService.getOrCreateUserForUsernameAndPassword(userName, password);

		if(userIndex == null)
			return false;

		if(admin) {
			User user = userIndex.getUser();
			//Enable admin role
			userService.enableAdminRoleForUser(user, false);
			//Disable operator role. User can either be admin or operator but not both
			disableOperatorRole(user);
		}

		log.info("User '{}' created successfully", userName);
		
		return true;
	}


	public boolean createUser(String userName, String password, String role) {
		UserIndex userIndex = userService.getOrCreateUserForUsernameAndPassword(userName, password);

		if(userIndex == null)
			return false;

		User user = userIndex.getUser();
		updateRole(role, user);
		userDao.saveOrUpdateUser(user);

		log.info("User '{}' created successfully", userName);

		return true;

	}
	
	@Override
	@Transactional
	public boolean updateUser(UserDetail userDetail) {
		
		User user = userService.getUserForId(userDetail.getId());
		
		if(user == null) {
			log.info("User '{}' does not exist in the system", userDetail.getUsername());
			return false;
		}

		//Update user password
		if(StringUtils.isNotBlank(userDetail.getPassword())) {
			// this is done for backward compatibility with Spring 3
			String credentials = passwordEncoder.encodePassword(userDetail.getPassword(), userDetail.getUsername());
			for(UserIndex userIndex : user.getUserIndices()) {
				userIndex.setCredentials(credentials);
			}
		}
		
		//Update user role
		updateRole(userDetail.getRole(), user);
		
		userDao.saveOrUpdateUser(user);
		
		log.info("User '{}' updated successfully", userDetail.getUsername());
		
		return true;
	}

	//Marks the user as inactive, can activate again
	@Override
	@Transactional
    public boolean inactivateUser(UserDetail userDetail) {
        User user = userService.getUserForId(userDetail.getId());

        if(user == null) {
            log.info("User '{}' does not exist in the system", userDetail.getUsername());
            return false;
        }

        userPropertiesService.disableUser(user);

        log.info("User '{}' disabled successfully", userDetail.getUsername());

        return true;
    }

    //Marks the user as active
    @Override
	@Transactional
    public boolean activateUser(UserDetail userDetail) {
        User user = userService.getUserForId(userDetail.getId());

        if(user == null) {
            log.info("User '{}' does not exist in the system", userDetail.getUsername());
            return false;
        }

        userPropertiesService.activateUser(user);

        log.info("User '{}' activated successfully", userDetail.getUsername());

        return true;
    }

    //deletes the user
	@Override
	@Transactional
	public boolean deactivateUser(UserDetail userDetail) {
		User user = userService.getUserForId(userDetail.getId());
		
		if(user == null) {
			log.info("User '{}' does not exist in the system", userDetail.getUsername());
			return false;
		}
		
		//Delete user indices so that a user cannot authenticate even if 
		//user record itself is still present
		for(Iterator<UserIndex> it = user.getUserIndices().iterator(); it.hasNext();) {
			UserIndex userIndex = it.next();
			userDao.deleteUserIndex(userIndex);
			it.remove();
		}

		userDao.saveOrUpdateUser(user);
		
		log.info("User '{}' deleted successfully", userDetail.getUsername());
		
		return true;
	}

	private void updateRole(String role, User user) {
		boolean updateRole = false;
		UserRole currentRole = null;

		Set<UserRole> userRoles = user.getRoles();
		
		for(UserRole userRole : userRoles) {
			//There should be only one role
			if(!(userRole.getName().equals(role))) {
				updateRole = true;
				currentRole = userRole;
			}
		}

		if(updateRole) {
			//Remove current role and add the new role
			userRoles.remove(currentRole);
			UserRole newRole = authoritiesService.getUserRoleForName(role);
			userRoles.add(newRole);
	
		}
	}

	@Autowired
	public void setSesssionFactory(SessionFactory sessionFactory) {
		_sessionFactory = sessionFactory;
	}

	private Session getSession(){
		return _sessionFactory.getCurrentSession();
	}


	/**
	 * @param authoritiesService the authoritiesService to set
	 */
	@Autowired
	public void setAuthoritiesService(StandardAuthoritiesService authoritiesService) {
		this.authoritiesService = authoritiesService;
	}

	/**
	 * @param userDao the userDao to set
	 */
	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
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
	 * @param passwordEncoder the passwordEncoder to set
	 */
	@Autowired
	public void setPasswordEncoder(VersionedPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public List<String> getAllRoleNames() {
		return StandardAuthoritiesService.STANDARD_AUTHORITIES;
	}

	@Override
	public List<String> getManagedRoleNames() {
		return StandardAuthoritiesService.MANAGED_AUTHORITIES;
	}


    /**
     * Injects {@link UserPropertiesService}
     * @param userPropertiesService the userPropertiesService to set
     */
    @Autowired
    public void setUserPropertiesService(UserPropertiesService userPropertiesService) {
        this.userPropertiesService = userPropertiesService;
    }

}
