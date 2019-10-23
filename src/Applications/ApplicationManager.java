package Applications;

import Extentions.DatabaseManager;
import Modules.AccessKey;
import Modules.LocationObject;
import Users.User;
import Users.UserManager;
import Users.UserSessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

public class ApplicationManager {
    private UserSessionManager sessionManager;
    private DatabaseManager databaseManager;


    public ApplicationManager(UserSessionManager sessionManager, DatabaseManager databaseManager){
        this.databaseManager = databaseManager;
        this.sessionManager = sessionManager;
    }

    public Application createApplication(String sessionKey, Application application) throws ApplicationException {
        Application application1 = getApplicationFromDB(application);
        if(application1 != null){
            throw new ApplicationException("application already exists");
        }else{
           return application.create(databaseManager);
        }
    }

    public ArrayList<Application> getApplications(User user) throws ApplicationException {
        ArrayList<Application> allApplications = new ArrayList<>();
        //allApplications.addAll(getOwnerApplications(user));
        allApplications.addAll(getContributionApplications(user));
        return allApplications;
    }

    private ArrayList<Application> getOwnerApplications(User user) throws ApplicationException {
        String uid = user.getUserData().getUUID().toString();
        databaseManager.setQueryFormat("SELECT * FROM applications WHERE owner = '%s';");
        try {
            return Application.fromResultSet(sessionManager, databaseManager ,databaseManager.sendArgs(new String[]{uid}));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private ArrayList<Application> getContributionApplications(User user) throws ApplicationException {
        String uid = user.getUserData().getUUID().toString();
        databaseManager.setQueryFormat("SELECT * FROM applications WHERE uid IN (SELECT uid FROM application_user where user_uid = '%s') AND owner = '%s';");
        try {
            return Application.fromResultSet(sessionManager, databaseManager ,databaseManager.sendArgs(new String[]{uid, uid}));
        } catch (SQLException e) {
            throw new ApplicationException(e);
        }
    }

    private Application getApplicationFromDB(Application application) throws ApplicationException {
        if(application.getAPPID() != null){
            databaseManager.setQueryFormat("SELECT * FROM applications WHERE uid = '%s';");
            try {
                return Application.fromResultSet(sessionManager, databaseManager, databaseManager.sendArgs(new String[]{application.getAPPID()}), 1);
            } catch (SQLException e) {
                throw new ApplicationException(e);
            } catch (LocationObject.LocationObjectException | AccessKey.AccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Application getApplicationComplete(Application application) throws ApplicationException {
        return getApplicationFromDB(application);
     }

    public ArrayList<AccessKey> getAccessKey(Application application) throws AccessKey.AccessException {
        return application.getAccessKeys(databaseManager);
    }

    public boolean removeApplication(User user, Application application) throws ApplicationException {
        if(application.getAPPID() != null){
            Application applicationDB = getApplicationFromDB(application);
            if (applicationDB != null) {
                if(applicationDB.getOwner().getUserData().getUUID().equals(user.getUserData().getUUID())){
                    try {
                        applicationDB.remove(databaseManager);
                        return true;
                    } catch (SQLException e) {
                        throw new ApplicationManager.ApplicationException(e.getMessage());
                    }
                }else{
                    throw new ApplicationManager.ApplicationException("You don't have the right to remove this application");
                }
            }
        }
        return false;
    }

    public static JSONObject listToJSON(ArrayList<Application> applications){
        JSONObject applicationsJSON = new JSONObject();
        JSONArray applicationArray = new JSONArray();
        for (Application application: applications) {
            applicationArray.put(application.toJSON(false));
        }
        applicationsJSON.put("applications", applicationArray);
        return applicationsJSON;
    }

    public static class ApplicationException extends Exception{
        public ApplicationException(String message){
            super(message);
        }
        public ApplicationException(Exception ex){
            super(ex);
        }
    }


}
