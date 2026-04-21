package service;

import java.util.prefs.Preferences;

public final class UserSession {

    private static UserSession instance;
    private static final Object LOCK = new Object();
    private static final Preferences prefs =
            Preferences.userNodeForPackage(UserSession.class);

    private final String userName;
    private final String password;
    private final String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
    }

    public static UserSession createSession(String userName, String password, String privileges) {
        synchronized (LOCK) {
            instance = new UserSession(userName, password, privileges);

            prefs.put("username", userName);
            prefs.put("password", password);
            prefs.put("privileges", privileges);

            return instance;
        }
    }

    public static UserSession getCurrentSession() {
        synchronized (LOCK) {
            return instance;
        }
    }

    public static void clearSession() {
        synchronized (LOCK) {
            instance = null;
            prefs.remove("username");
            prefs.remove("password");
            prefs.remove("privileges");
        }
    }

    public static String getSavedUsername() {
        return prefs.get("username", "");
    }

    public static String getSavedPassword() {
        return prefs.get("password", "");
    }

    public static String getSavedPrivileges() {
        return prefs.get("privileges", "");
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getPrivileges() {
        return privileges;
    }
}