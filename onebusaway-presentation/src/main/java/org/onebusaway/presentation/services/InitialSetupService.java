package org.onebusaway.presentation.services;

/**
 * Service that determines if the application is in a default startup state that
 * requires some initial setup. Right now, we mostly just check to see if an
 * admin account has been created yet.
 * 
 * @author bdferris
 */
public interface InitialSetupService {
  public boolean isInitialSetupRequired(boolean forceRefresh);
}
