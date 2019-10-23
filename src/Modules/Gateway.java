package Modules;

import Users.User;
import Users.UserManager;
import Users.UserSessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Gateway {
    private String EUFI, name, uid, brand;
    private User owner;
    private int frequency;
    private LocationObject location;

    public Gateway(String uid){
        setUid(uid);
    }

    //TODO Fields voor gateway met DTB maken

    public static Gateway fromResultSet(UserManager userManager, ResultSet resultSet) throws SQLException, LocationObject.LocationObjectException {
        Gateway newGateway = new Gateway(resultSet.getString("uid"));
        newGateway.setName(resultSet.getString("name"));
        newGateway.setBrand(resultSet.getString("brand"));
        newGateway.setFrequency(resultSet.getInt("frequency"));
        newGateway.setLocation(LocationObject.fromJSON(new JSONObject(resultSet.getString("location"))));
        newGateway.setOwner(userManager.getUserByUID(resultSet.getString("owner")));
        return newGateway;
    }

    //region getters and setters

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

    //endregion

    //region JSONparsers

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

    public static JSONArray toJSONArray(ArrayList<Gateway> gateways){
        JSONArray gatewaysJSONArray = new JSONArray();
        for (Gateway gateway: gateways) {
            gatewaysJSONArray.put(gateway.toJSON());
        }
        return gatewaysJSONArray;
    }
    //endregion
}
