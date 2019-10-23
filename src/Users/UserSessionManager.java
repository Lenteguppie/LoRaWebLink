package Users;

import Extentions.DatabaseManager;
import Extentions.Logger.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/*
 * Hoe gebruik je de UserSessionManager?
 * - Om een Sessie te creeren voor de gebruiker moet de methode createSession aangeroepen worden met het userID, en het IP adress van de gebruiker
 * -
 *
 *
 *
 *
 */


public class UserSessionManager extends UserManager {
    // Initializing the list of loaded users
    static private ArrayList<UserSession> UserSessions;

    public UserSessionManager(DatabaseManager databaseManager) {
        super(databaseManager);
        UserSessions = new ArrayList<UserSession>();
    }

    public UserSession createSession(UUID userID, String ipAddress) {
        User tempUser = super.getUser(userID);
        if (!sessionexist(tempUser)) {
            UserSession session = new UserSession(ipAddress, tempUser, LocalDateTime.now());
            UserSessions.add(session);
            Log.d(TAG, String.format("User session created for user: %s with sessionKey: %s", tempUser.getUserData().getUserName(), session.getSessionKey().toString()));
            return session;
        } else {
            Log.d(TAG, "User session already exists");
            return null;
        }
    }

    public UserSession getSession(String sessionKey) {
        for (UserSession session : UserSessions) {
            if (session.getSessionKey().toString().equals(sessionKey)) {
                return session;
            }
        }
        return null;
    }

    public boolean createSession(UUID userID) {
        User tempUser = super.getUser(userID);
        try {
            UserSession session = new UserSession(tempUser, LocalDateTime.now());
            UserSessions.add(session);
            Log.d(TAG, String.format("User session created for user: %s with sessionKey: %s", tempUser.getUserData().getUserName(), session.getSessionKey().toString()));
            return true;
        } catch (Exception e) {
            Log.d(TAG, e);
            return false;
        }
    }

    public UserSession getSessionEmail(String email) {
        for (UserSession session : UserSessions) {
            if (session.getUser().getUserData().getEmail().equals(email)) {
                return session;
            }
        }
        return null;
    }

    public boolean isValidSession(String sessionKey){
        return getSession(sessionKey) != null;
    }

    boolean sessionExist(UUID sessionKey) {
        for (UserSession session : UserSessions) {
            if (session.getSessionKey().equals(sessionKey)) {
                return true;
            }
        }
        return false;
    }

    boolean sessionexist(User user) {
        for (UserSession session : UserSessions) {
            if (session.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    void removeSession(UUID sessionKey) {
        UserSessions.remove(sessionKey);
    }

    void removeSession(User user) {
        int index = 0;
        for (UserSession session : UserSessions) {
            index++;
            if (session.getUser().equals(user)) {
                break;
            }
        }

        try {
            UserSessions.remove(index);
            Log.d(TAG, String.format("Successfully removed session for User: %s", user.getUserData().getUUID().toString()));
        } catch (Exception e) {
            Log.d(TAG, String.format("Failed to remove session for user: %s", user.getUserData().getUUID().toString()));
            Log.d(TAG, e);
        }
    }

    void clearSessions() {
        UserSessions.clear();
    }


}
