package Users;

import Extentions.DatabaseManager;
import Extentions.Logger.Log;

import java.util.UUID;
import java.time.*;

public class UserSession {
    private String ipaddress;
    private User user;
    private LocalDateTime dateTime;
    private UUID sessionKey;
    private DatabaseManager databaseManager;

    //region Constructors
    public UserSession(String ipaddress, User user, LocalDateTime dateTime) {
        this.ipaddress = ipaddress;
        this.user = user;
        this.dateTime = dateTime;
        this.sessionKey = generateSessionKey();
    }

    public UserSession(User user, LocalDateTime dateTime) {
        this.user = user;
        this.dateTime = dateTime;
        this.sessionKey = generateSessionKey();
    }

    public UserSession() {
    }
    //endregion

    //region Getters and setters
    public String getIpAddress() {
        return ipaddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipaddress = ipaddress;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public UUID getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(UUID sessionKey) {
        this.sessionKey = sessionKey;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    //endregion

    //region generators
    private UUID generateSessionKey(){
        UUID sessionKey;
        try {
            sessionKey = UUID.randomUUID();
            return sessionKey;
        } catch (Exception e) {
            Log.d("UUID error", e);
            return null;
        }
    }
    //endregion
}
