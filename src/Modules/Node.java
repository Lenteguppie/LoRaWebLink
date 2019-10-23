package Modules;

import Extentions.DatabaseManager;
import Extentions.Logger.Log;
import Users.User;
import Users.UserManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class Node {
    private static String TAG = Node.class.getSimpleName();
    private String EUFI, name, uid, brand;
    private User owner;
    private int frequency;
    private LocationObject location;

    public Node(String uid){
        setUid(uid);
    }

    public Node(){}

    public static Node fromResultSet(UserManager userManager, ResultSet resultSet) throws SQLException, LocationObject.LocationObjectException {
        Node newNode = new Node(resultSet.getString("uid"));
        newNode.setName(resultSet.getString("name"));
        newNode.setBrand(resultSet.getString("brand"));
        newNode.setFrequency(resultSet.getInt("frequency"));
        newNode.setLocation(LocationObject.fromJSON(new JSONObject(resultSet.getString("location"))));
        newNode.setOwner(userManager.getUserByUID(resultSet.getString("owner")));
        return newNode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public LocationObject getLocation() {
        return location;
    }

    public void setLocation(LocationObject location) {
        this.location = location;
    }

    public boolean remove(DatabaseManager databaseManager){
        databaseManager.setQueryFormat("DELETE FROM `loraserver`.`nodes` WHERE  `uid`= '%s';");
        try {
            databaseManager.sendArgs(new String[]{getUid()});
            return true;
        } catch (SQLException e) {
            Log.d(TAG, "error removing node" + e.getMessage());
            return false;
        }
    }

    public static String generateUID(){
        return UUID.randomUUID().toString();
    }

    public JSONObject toJSON(){
        JSONObject nodeObject = new JSONObject();
        nodeObject.put("name", getName());
        nodeObject.put("frequency", getFrequency());
        nodeObject.put("brand", getBrand());
        nodeObject.put("location", getLocation().toJSON());
        nodeObject.put("uid", getUid());
        nodeObject.put("owner", getOwner().toJSON(false));
        return nodeObject;
    }

    public static JSONArray toJSONArray(ArrayList<Node> nodes){
        JSONArray nodesJSONArray = new JSONArray();
        for (Node node: nodes) {
            nodesJSONArray.put(node.toJSON());
        }
        return nodesJSONArray;
    }
}
