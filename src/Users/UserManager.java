package Users;

import Extentions.DatabaseManager;
import Extentions.Logger.Log;
import Modules.Gateway;
import Modules.LocationObject;
import Modules.Node;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    static final public String TAG = UserManager.class.getSimpleName();
    private static HashMap<UUID, User> userBuffer = new HashMap<>();
    private DatabaseManager databaseManager;

    public UserManager(DatabaseManager databaseManager) {
       this.databaseManager = databaseManager;
    }

    public User getUser(UUID uuid) {
        return getUserFromBuffer(uuid);
    }

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    public User getUser(String email) {
        if (isInUserBuffer(email)) {
            return getUserFromBuffer(email);
        } else if (isInUserDatabase(email)) {
            return getUserFromDatabase(email);
        } else {
            return null;
        }
    }

    public User getUserByUID(String uid){
        User bufferUser = getUserInBufferByUID(uid);
        if(bufferUser != null){
            return bufferUser;
        }else {
            return getUserFromDatabaseUID(uid);
        }
    }

    public boolean createUser(String username, String password, String firstName, String lastName, String email) {
        //TODO Create Query to check if user with that username and/or email exists
        boolean exist = userExist(email);

        User user = new User(username, password, firstName, lastName, email);
        if (!exist) {
            user.getUserData().generateUUID();
            addUserToBuffer(user);
            addUserToDatabase(user);
            return true;
        } else {
            Log.d(TAG, "User already exist");
            addUserToBuffer(user);
          return false;
        }
    }

    public void createUser(String uuid, String username, String password, String firstName, String lastName, String email, String role) {
        //TODO Create Query to check if user with that username and/or email exists
        boolean exist = userExist(username);

        User user = new User(username, password, firstName, lastName, email, role);
        if (!exist) {
            user.getUserData().setUUID(uuid);
            addUserToBuffer(user);
            //            return true;
        } else {
            Log.d(TAG, "User already exist");
            addUserToBuffer(user);
        }
    }

    private boolean userExist(String email) {
        if (isInUserBuffer(email)) {
            Log.d(TAG, "User found in buffer with email: " + email);
            return true;
        } else if (isInUserDatabase(email)) {
            Log.d(TAG, "User found in database with email: " + email);
            return true;
        } else {
            Log.d(TAG, "No users found in the buffer or database with email: " + email);
            return false;
        }
    }

    public void addUserToDatabase(User user) {
        JSONObject userData = null;
        if (!(isInUserDatabase(user.getUserData().getUserName()))) {
            userData = new JSONObject(user.getUserData().toJson());
            Log.d(TAG + " JSON: ", userData);
            databaseManager.setQueryFormat("INSERT INTO `loraserver`.`users` (`email`,`firstname`, `lastname`, `role`, `username`, `password`, `uid`) VALUES ('%s','%s','%s','%s','%s','%s','%s');");
            try {
                databaseManager.sendArgs(new String[]{
                                (String) userData.get("email"),
                                (String) userData.get("firstname"),
                                (String) userData.get("lastname"),
                                (String) userData.get("role"),
                                (String) userData.get("username"),
                                (String) userData.get("password"),
                                (String) userData.get("uid")
                        }
                );
            } catch (SQLException e) {
                Log.d(TAG, e);
            }
        } else {
            Log.d(TAG, "User already exists so no push to dtb!");
        }


        //TODO create query to inset the user in the database

    }

    public void addUserToBuffer(User user) {
        //TODO Create Query to check if user with that username and/or email exists
        if (!isInUserBuffer(user.getUserData().getUserName())) {
            userBuffer.put(user.getUserData().getUUID(), user);
        }
    }

    void updateUser() {
        //TODO User needs to update
    }

//    public boolean checkUserCredentials(String username, String password) {
//        User tempUser = null;
//        if (!isInUserBuffer(username)) {
//            tempUser = getUserFromDatabase(username);
//            if(tempUser == null){
//                return false;
//            }else {
//                return (tempUser.getUserData().getPassword().equals( password ));
//            }
//        } else if (isInUserBuffer(username)) {
//            tempUser = getUserFromBuffer(username);
//            return (tempUser.getUserData().getPassword().equals(password));
//        } else {
//            return false;
//        }
//    }

    public boolean checkUserCredentials(String email, String password) {
        User tempUser;
        if (!isInUserBuffer(email)) {
            tempUser = getUserFromDatabase(email);
            if(tempUser == null){
                return false;
            }else {
                return (tempUser.getUserData().getPassword().equals( password ));
            }
        } else if (isInUserBuffer(email)) {
            tempUser = getUserFromBuffer(email);
            return (tempUser.getUserData().getPassword().equals(password));
        } else {
            return false;
        }
    }

    boolean isInUserBuffer(String email) {
        if(!userBuffer.isEmpty ()) {
            for (Map.Entry<UUID, User> entry : userBuffer.entrySet ( )) {
                if (entry.getValue ( ).getUserData ( ).getEmail ( ).equals (email)) {
                    Log.d (TAG, "User found with email " + email);
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }
    }

    User getUserInBufferByUID(String uid) {
        for (Map.Entry<UUID, User> entry : userBuffer.entrySet()) {
            if (entry.getValue().getUserData().getUUID().toString().equals(uid)) {
                return entry.getValue();
            }
        }
        return null;
    }

    boolean isInUserDatabase(String email) {
//        String query = String.format( "SELECT EXISTS(SELECT * from users WHERE %s);", username );
        ResultSet r;
        databaseManager.setQueryFormat("SELECT EXISTS(SELECT * from users WHERE email='%s') AS res;");
        try {
            r = databaseManager.sendArgs(new String[]{email});
            r.absolute(1);
            Log.d(TAG, "DTB User: " + r.getBoolean(1));
            return (r.getBoolean(1));
        } catch (SQLException e) {
            Log.d(TAG, e);
        }
        return true;
    }

    public User getUserFromDatabaseUID(String uuid) {
        //TODO Get the user from the database with UUID
        User tempUser;
        databaseManager.setQueryFormat("SELECT * FROM users WHERE uid='%s'");       // SELECT the first row from the result
        try {
            ResultSet r = databaseManager.sendArgs(new String[]{uuid});
            r.absolute(1);
            tempUser = new User(r);
            addUserToBuffer(tempUser);
        } catch (SQLException e) {
            Log.d(TAG, e);
            tempUser = null;
        }
        return tempUser;
    }

    public User getUserFromDatabase(String email) {
        User tempUser;
        databaseManager.setQueryFormat("SELECT * FROM users WHERE email='%s'");       // SELECT the first row from the result
        try {
            ResultSet r = databaseManager.sendArgs(new String[]{email});
            tempUser = new User(r);
            addUserToBuffer(tempUser);
        } catch (SQLException e) {
            Log.d(TAG + "UserFromDTB", e);
            tempUser = null;
        }
        return tempUser;
    }

    User getUserFromBuffer(UUID userId) {
        User u = userBuffer.get(userId);
        return u;
    }

    User getUserFromBuffer(String email) {
        for (Map.Entry<UUID, User> entry : userBuffer.entrySet()) {
            if (entry.getValue().getUserData().getEmail ().equals(email)) {
                Log.d(TAG, "User found with email in buffer " + email);
                return entry.getValue();
            }
        }
        return null;
    }

    public ArrayList<Node> getNodes(User user) throws UserManagerException {
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        databaseManager.setQueryFormat("SELECT * FROM nodes WHERE OWNER = '%s';");
        try {
            ResultSet resultSet = databaseManager.sendArgs(new String[]{user.getUserData().getUUID().toString()});
            for (int i = 1; i <=DatabaseManager.getRowCount(resultSet) ; i++) {
                resultSet.absolute(i);
                nodeArrayList.add(Node.fromResultSet(this, resultSet));
            }
            return nodeArrayList;
        } catch (SQLException | LocationObject.LocationObjectException e) {
            throw new UserManagerException(e.getMessage());
        }
    }

    public ArrayList<Gateway> getGateways(User user) throws UserManagerException {
        ArrayList<Gateway> gatewayArrayList = new ArrayList<>();
        databaseManager.setQueryFormat("SELECT * FROM gateways WHERE OWNER = '%s';");
        try {
            ResultSet resultSet = databaseManager.sendArgs(new String[]{user.getUserData().getUUID().toString()});
            for (int i = 1; i <=DatabaseManager.getRowCount(resultSet) ; i++) {
                resultSet.absolute(i);
                gatewayArrayList.add(Gateway.fromResultSet(this, resultSet));
            }
            return gatewayArrayList;
        } catch (SQLException | LocationObject.LocationObjectException e) {
            throw new UserManagerException(e.getMessage());
        }
    }

    public static class UserManagerException extends Exception {
        public UserManagerException(String message){
            super(message);
        }
    }
}
