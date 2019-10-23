package Modules;

import Applications.Application;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccessKey {

    public int getRightINT() {
        return rightINT;
    }

    public void setRight(int rightINT) {
        this.rightINT = rightINT;
        switch (rightINT){
            case 0:{
                right = Rights.VIEW;
                break;
            }
            case 1:{
                right = Rights.ADMIN;
                break;
            }
            case 2:{
                right = Rights.MANAGER;
                break;
            }
            default:{
                right = Rights.VIEW;
                break;
            }
        }
    }

    public void setRight(Rights right){
        this.right = right;
        switch (right){
            case VIEW:{
                this.rightINT = 0;
                break;
            }
            case ADMIN:{
                this.rightINT = 1;
                break;
            }
            case MANAGER:{
                this.rightINT = 2;
                break;
            } default:{
                rightINT = 0;
                break;
            }
        }
    }

    public String getApplicationUID() {
        return applicationUID;
    }

    public enum Rights{
        ADMIN,
        VIEW,
        MANAGER
    }

    private String applicationUID;
    private String key;
    private Rights right;
    private int rightINT;

    public AccessKey(String applicationUID, Rights right){
        this.applicationUID = applicationUID;
        setRight(right);
        generateAccessKey();
    }


    public AccessKey(String key, String applicationUID, int right){
        this.key = key;
        this.applicationUID = applicationUID;
        setRight(right);
    }

    private void generateAccessKey(){
        this.key = UUID.randomUUID().toString();
    }

    public void setRights(Rights right){
        this.right = right;
    }

    public String getKey() throws AccessException {
        if(getRight() == null){
            throw new AccessException("no rights set");
        }else {
            return key;
        }
    }

    public Rights getRight() {
        return right;
    }

    public static AccessKey fromResultSet(ResultSet resultSet) throws AccessException {
        try {
            return  new AccessKey(resultSet.getString("accesskey"), resultSet.getString("application_uid"), resultSet.getInt("right"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new AccessException("invalid accesskey format");
    }

    public JSONObject toJSON(){
        JSONObject keyObject = new JSONObject();
//        keyObject.put("application_uid", getApplicationUID());
        try {
            keyObject.put("accesskey", getKey());
        } catch (AccessException e) {
            e.printStackTrace();
        }
        keyObject.put("right", getRightINT());
        return keyObject;
    }

    public static AccessKey generate(Application application, Rights right){
        return new AccessKey(application.getAPPID(), right);
    }

    public static class AccessException extends Exception{
        public AccessException(String message){
            super(message);
        }
    }



}
