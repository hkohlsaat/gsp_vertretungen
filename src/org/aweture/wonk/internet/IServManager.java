package org.aweture.wonk.internet;

import java.io.IOException;

/**
 * This class defines the communication interface with the servers front end.
 * 
 * @author Hannes Kohlsaat
 */
public interface IServManager {
	
	/**
	 * Encapsulates three results of the login procedures.
	 */
	public enum LoginResult {
		/** The login was successful. isLoggedIn() returns true. */
		Success,
		/** The login failed due to network problems. */
		NetworkFail,
		/** The login failed due to wrong login data. */
		WrongData;
	}
	
	/**
	 * Encapsulates the user data needed to perform the login.
	 */
	class LoginData {
		String name;
		String password;
	}
	
	/**
	 * Perform the login to the IServ web front end.
	 * @return login result as the {@link LoginResult} enum
	 */
	public LoginResult logIn();
	
	/**
	 * Download the substitutions plan. The login must be performed
	 * beforehand. It wont be done by this method.
	 * @return queried html table as {@link String}
	 */
	public String downloadSubstitutionPlan() throws IOException;
	
	/**
	 * Check whether there are valid cookies. Then a plan can be
	 * downloaded without login.
	 * @return validity of the cookies
	 */
	public boolean isLoggedIn();
}