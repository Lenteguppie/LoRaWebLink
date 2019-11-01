package Applications;

import Extentions.DatabaseManager;
import Extentions.Logger.Log;
import Modules.AccessKey;
import Modules.LocationObject;
import Modules.Node;
import Users.User;
import Users.UserManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Application {
    //    private static ArrayList<AccessKey> accessKeys;
    private String APPID;
    private String name;
    private String description;
    private ArrayList<AccessKey> accessKeys = new ArrayList<>();
    private ArrayList<String> EUIS;
    private ArrayList<Node> node;
    private User applicationOwner;
    private ArrayList<User> collaborators = new ArrayList<>();
    private DatabaseManager databaseManager;
    private String creationDate;

    public Application() {
    }

    public Application(String APPID) {
        setAPPID(APPID);
    }

    //region getters and setters
    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    //region accessKeys methodes

    public ArrayList<AccessKey> getAccessKeys() {
        return accessKeys;
    }

    public void setAccessKeys(ArrayList<AccessKey> accessKeys) {
        this.accessKeys = accessKeys;
    }

    public void addAccessKey(AccessKey accessKey) {
        accessKeys.add(accessKey);
    }

    public boolean addAccessKeysToDB(DatabaseManager databaseManager) throws AccessKey.AccessException, SQLException {
        if (databaseManager != null) {
            for (AccessKey key : accessKeys) {
                databaseManager.setQueryFormat("INSERT INTO `loraserver`.`application_accesskey` (`application_uid`, `accesskey`, `right`) VALUES ('%s', '%s', '%s');");
                databaseManager.sendArgs(new String[]{key.getApplicationUID(), key.getKey(), String.valueOf(key.getRightINT())});
            }
            return true;
        }
        return false;
    }

    private void generateAccessKeys() {
        addAccessKey(AccessKey.generate(this, AccessKey.Rights.ADMIN));
        addAccessKey(AccessKey.generate(this, AccessKey.Rights.VIEW));
        addAccessKey(AccessKey.generate(this, AccessKey.Rights.MANAGER));
    }

    public boolean addOwnerAccessKey(DatabaseManager databaseManager) throws AccessKey.AccessException {
        User user = getOwner();
        user.getUserData().setAccessKey(accessKeys.get(0));
        return addCollaborator(databaseManager, user);
    }

    private boolean addCollaborator(DatabaseManager databaseManager, User user) throws AccessKey.AccessException {
        if (user.getUserData().getAccessKey() != null) {
            collaborators.add(user);
            return updateCollaborators(databaseManager);
        } else {
            throw new AccessKey.AccessException("No AccessKey set");
        }
    }

    public boolean updateCollaborators(DatabaseManager databaseManager) throws AccessKey.AccessException {
        for (User collaborator : getCollaborators()) {
            databaseManager.setQueryFormat("INSERT INTO `loraserver`.`application_user` (`application_uid`, `user_uid`, `accesskey_uid`) VALUES ('%s', '%s', '%s');");
            try {
                databaseManager.sendArgs(new String[]{getAPPID(), collaborator.getUserData().getUUID().toString(), collaborator.getUserData().getAccessKey().getKey()});
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<AccessKey> getAccessKeys(DatabaseManager databaseManager) throws AccessKey.AccessException {
        ArrayList<AccessKey> keys = new ArrayList<>();
        databaseManager.setQueryFormat("SELECT * FROM application_accesskey WHERE application_uid = '%s';");
        ResultSet resultSet;
        try {
            resultSet = databaseManager.sendArgs(new String[]{getAPPID()});
            for (int i = 1; i <= DatabaseManager.getRowCount(resultSet); i++) {
                resultSet.absolute(i);
                try {
                    keys.add(AccessKey.fromResultSet(resultSet));
                } catch (AccessKey.AccessException e) {
                    throw new AccessKey.AccessException(e.getMessage());
                }
            }
            return keys;
        } catch (SQLException e) {
            throw new AccessKey.AccessException("unknown error");
        }
    }
    //endregion

    public ArrayList<String> getEUIS() {
        return EUIS;
    }

    public void setEUIS(ArrayList<String> EUIS) {
        this.EUIS = EUIS;
    }

    //region node methodes
    public ArrayList<Node> getNodes() {
        return node;
    }

    public void setNodes(ArrayList<Node> node) {
        this.node = node;
    }

    public boolean addNode(DatabaseManager databaseManager, Node node){
        databaseManager.setQueryFormat("INSERT INTO `loraserver`.`nodes` (`name`, `brand`, `frequency`, `location`, `owner`, `uid`) VALUES " +
                "('%s', '%s', '%s', '{\\n  \"latitude\": %s,\\n  \"longitude\": %s,\\n  \"altitude\": %s\\n}', '%s', '%s');");
        try {
            databaseManager.sendArgs(new String[]{node.getName(), node.getBrand(), String.valueOf(node.getFrequency()), String.valueOf(node.getLocation().getLatitude()),
                    String.valueOf(node.getLocation().getLongitude()), String.valueOf(node.getLocation().getAltitude()), node.getOwner().getUserData().getUUID().toString(), node.getUid() });
            addNodeToApplication(databaseManager, node);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean addNodeToApplication(DatabaseManager databaseManager, Node node){
        databaseManager.setQueryFormat("INSERT INTO `loraserver`.`node_application` (`node_uid`, `application_uid`) VALUES ('%s', '%s');");
        try {
            databaseManager.sendArgs(new String[]{node.getUid(), getAPPID()});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //endregion

    public User getOwner() {
        return applicationOwner;
    }

    public String generateUUID() throws ApplicationManager.ApplicationException {
        try {
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            Log.E("UUID error", e);
        }
        throw new ApplicationManager.ApplicationException("can't generate uuid");
    }

    public void setOwner(User applicationOwner) {
        this.applicationOwner = applicationOwner;
    }

    public ArrayList<User> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(ArrayList<User> collaborators) {
        this.collaborators = collaborators;
    }

    public void getCollaborators(UserManager userManager) {
        ArrayList<User> collaboratorArray = new ArrayList<>();
        DatabaseManager databaseManager = userManager.getDatabaseManager();
        databaseManager.setQueryFormat("SELECT * FROM application_user WHERE application_uid = '%s;");
        try {
            ResultSet resultSet = databaseManager.sendArgs(new String[]{""});
            for (int i = 1; i <= DatabaseManager.getRowCount(resultSet); i++) {
                resultSet.absolute(i);
                int Role = resultSet.getInt("rights");
                String userUID = resultSet.getString("user_uid");
                User user = userManager.getUserByUID(userUID);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    //endregion

    public Application create(DatabaseManager databaseManager) throws ApplicationManager.ApplicationException {
        if (getOwner() != null && databaseManager != null) {
            databaseManager.setQueryFormat("INSERT INTO `loraserver`.`applications` (`name`, `owner`, `description`, `uid`) VALUES ('%s', '%s', '%s', '%s');");
            setAPPID(generateUUID());
            try {
                databaseManager.sendArgs(new String[]{getName(), getOwner().getUserData().getUUID().toString(), getDescription(), getAPPID()});
                generateAccessKeys();
                if (addAccessKeysToDB(databaseManager) && addOwnerAccessKey(databaseManager)) {
                    return this;
                }
            } catch (SQLException | AccessKey.AccessException e) {
                throw new ApplicationManager.ApplicationException(e);
            }
        }
        throw new ApplicationManager.ApplicationException("error creating application");
    }

    public boolean remove(DatabaseManager databaseManager) throws SQLException {
        if (databaseManager != null) {
            databaseManager.setQueryFormat("DELETE FROM `loraserver`.`applications` WHERE  `uid`='%s';");
            databaseManager.sendArgs(new String[]{getAPPID()});
            return true;
        }
        return false;
    }

    public static ArrayList<Application> fromResultSet(UserManager userManager, DatabaseManager databaseManager, ResultSet resultSet) throws SQLException {
        resultSet.absolute(1);
        ArrayList<Application> applicationList = new ArrayList<>();
        for (int i = 1; i <= DatabaseManager.getRowCount(resultSet); i++) {
            Application newApplication = null;
            try {
                newApplication = Application.fromResultSet(userManager, databaseManager, resultSet, i);
            } catch (AccessKey.AccessException | LocationObject.LocationObjectException e) {
                e.printStackTrace();
            }
            applicationList.add(newApplication);
        }
        return applicationList;
    }

    public static Application fromResultSet(UserManager userManager, DatabaseManager databaseManager, ResultSet resultSet, int index) throws SQLException, AccessKey.AccessException, LocationObject.LocationObjectException {
        resultSet.absolute(index);
        Application newApplication = new Application();
        newApplication.setCreationDate(resultSet.getString("date"));
        newApplication.setAPPID(resultSet.getString("uid"));
        newApplication.setName(resultSet.getString("name"));
        newApplication.setDescription(resultSet.getString("description"));
        //region accessKeys
        ArrayList<AccessKey> accessKeys = newApplication.getAccessKeys(userManager.getDatabaseManager());
        newApplication.setAccessKeys(accessKeys);
        //endregion
        //region addNodes
        newApplication.setNodes(newApplication.getNodes(databaseManager, userManager));
        //endregion
        //TODO add contributors

        newApplication.setOwner(userManager.getUserByUID(resultSet.getString("owner")));
        return newApplication;
    }

    private void setCreationDate(String date) {
        this.creationDate = date;
    }

    private ArrayList<Node> getNodes(DatabaseManager databaseManager, UserManager userManager) throws LocationObject.LocationObjectException {
        ArrayList<Node> nodeArrayList = new ArrayList<>();
        databaseManager.setQueryFormat("SELECT * FROM nodes WHERE uid IN (SELECT node_uid FROM node_application WHERE application_uid = '%s');");
        try {
            ResultSet resultSet = databaseManager.sendArgs(new String[]{getAPPID()});
            for (int i = 1; i <= DatabaseManager.getRowCount(resultSet); i++) {
                resultSet.absolute(i);
                nodeArrayList.add(Node.fromResultSet(userManager, resultSet));
            }
            return nodeArrayList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject toJSON(boolean allInfo) {
        JSONObject applicationObject = new JSONObject();
        applicationObject.put("date", getCreationDate());
        applicationObject.put("name", getName());
        applicationObject.put("description", getDescription());
        applicationObject.put("uid", getAPPID());
        applicationObject.put("owner", getOwner().toJSON(false));
        if (allInfo) {
            JSONArray collaboratorArray = new JSONArray();
            if (getCollaborators() != null) {
                for (User collaborator : getCollaborators()) {
                    collaboratorArray.put(collaborator.toJSON(false));
                }
            }
            applicationObject.put("collaborators", collaboratorArray);

            //addAccessKeys
            JSONArray accessKeyArray = new JSONArray();
            if (getAccessKeys() != null) {
                for (AccessKey key : getAccessKeys()) {
                    accessKeyArray.put(key.toJSON());
                }
            }
            applicationObject.put("accesskeys", accessKeyArray);

            JSONArray nodeArray = new JSONArray();
            if (getNodes() != null) {
                for (Node node : getNodes()) {
                    nodeArray.put(node.toJSON());
                }
            }
            applicationObject.put("nodes", nodeArray);
            return applicationObject;
        }
        return applicationObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void addAccessKeys(ArrayList<AccessKey> accessKeys) {
        this.setAccessKeys(accessKeys);
    }

    public String getCreationDate() {
        return creationDate;
    }

    public ApplicationDataSet createDataSet(ApplicationManager applicationManager, int limit){
        ApplicationDataSet dataSet = new ApplicationDataSet(this, limit);
        return dataSet.getProcessedDataSet(applicationManager);
    }

    public class ApplicationDataSet{

        private final Application application;
        private int limit;
        private volatile boolean processed = false;
        private ArrayList<Node> nodes = null;
        private ArrayList<Node.DataSet> nodeDataSets = new ArrayList<>();
        private JSONObject ApplicationDataSetJSON = new JSONObject();

        public ApplicationDataSet(Application application, int limit){
            this.application = application;
            this.limit = limit;
        }

        public void processDataSet(ApplicationManager applicationManager){
            new Thread(() -> {
                processed = false;
                ApplicationDataSetJSON = new JSONObject();
                try {
                    nodes = applicationManager.getApplicationComplete(application).getNodes();
                } catch (ApplicationManager.ApplicationException e) {
                    processed = true;
                }
                JSONArray nodeDataSetArray = new JSONArray();
                for (Node node: nodes) {
                    Node.DataSet dataSet = applicationManager.getNodeData(node, limit);
                    if(dataSet != null) {
                        JSONObject nodeDataSet = dataSet.getJSONPacket();
                        if(nodeDataSet != null) {
                            nodeDataSetArray.put(nodeDataSet);
                            nodeDataSets.add(dataSet);
                        }
                    }
                }
                ApplicationDataSetJSON.put("dataset", nodeDataSetArray);
                processed = true;
            }).start();
        }

        public ApplicationDataSet getProcessedDataSet(ApplicationManager applicationManager){
            processDataSet(applicationManager);
            while (!processed) {Thread.onSpinWait();}
            return this;
        }

        public JSONObject toJSON(){
            return ApplicationDataSetJSON;
        }
    }
}
