package Users;


import Extentions.DatabaseManager;
import Extentions.Logger.Log;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    //TODO communicate with database to check if user excists
    static final public String TAG = User.class.getSimpleName();
    private UserData userData;

    /* Constructor for new users */
    public User(String username, String password, String firstName, String lastName, String email, String role) {
        this.userData = new UserData();
        userData.setEmail(email);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);
        userData.setRole(UserData.Role.valueOf(role));
        userData.setUserName(username);
        userData.setPassword(password);
        userData.generateUUID();
    }

    /* Constructor for excisting users */
    public User(String username, String password, String firstName, String lastName, String email) {
        this.userData = new UserData();
        userData.setEmail(email);
        userData.setFirstName(firstName);
        userData.setPassword(password);
        userData.setLastName(lastName);
        userData.setRole(UserData.Role.USER);
        userData.setUserName(username);
        userData.generateUUID();
    }

    public User(String uid, String username, String password, String firstName, String lastName, String email, UserData.Role role) {
        this.userData = new UserData();
        userData.setUUID(uid);
        userData.setEmail(email);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);
        userData.setUserName(username);
        userData.setPassword(password);
        userData.setRole(role);
    }

    public User(ResultSet r) throws SQLException {
        r.absolute(1);
        this.userData = new UserData();
        userData.setEmail(r.getString("email"));
        userData.setFirstName(r.getString("firstname"));
        userData.setLastName(r.getString("lastname"));
        userData.setUserName(r.getString("username"));
        userData.setPassword(r.getString("password"));
        userData.setUUID(r.getString("uid"));
        userData.setRole(UserData.Role.valueOf(r.getString("role")));
    }

    public void updateUserData(String username, String password, String firstName, String lastName, String email, UserData.Role role) {
        //TODO create a script that updates users in the database and filter out the field that doesn't change!
        this.userData = new UserData();
        userData.setEmail(email);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);
        userData.setRole(role);
        userData.setUserName(username);
        userData.setPassword(password);
        userData.generateUUID();
    }

    public JSONObject getUserDataJSON() {

        try {
            return new JSONObject(getUserData());
        } catch (Exception e) {
            Log.d(TAG, e);
            return null;
        }
    }

    public UserData getUserData() {
        return this.userData;
    }

    public JSONObject toJSON(boolean allInfo) {
        JSONObject userObject = new JSONObject();
        userObject.put("email", getUserData().getEmail());
        userObject.put("username", getUserData().getUserName());
        userObject.put("role", getUserData().getRole().toString());
        if(allInfo){
            userObject.put("firstname", getUserData().getFirstName());
            userObject.put("lastname", getUserData().getLastName());
            userObject.put("password", getUserData().getPassword());
            userObject.put("uid", getUserData().getUUID().toString());
        }
        return userObject;
    }
}
