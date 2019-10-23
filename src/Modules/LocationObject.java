package Modules;

import org.json.JSONObject;

public class LocationObject {
    private Double altitude, longitude, latitude;

    public LocationObject(Double altitude, Double longitude, Double latitude) {
        this.altitude = altitude;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LocationObject(String altitude, String longitude, String latitude) {
        this.altitude = Double.valueOf(altitude);
        this.longitude = Double.valueOf(longitude);
        this.latitude = Double.valueOf(latitude);
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public JSONObject toJSON(){
        JSONObject locationObject = new JSONObject();
        locationObject.put("altitude", getAltitude());
        locationObject.put("longitude", getLongitude());
        locationObject.put("latitude", getLatitude());
        return locationObject;
    }

    public static LocationObject fromJSON(JSONObject jsonObject) throws LocationObjectException {
        if(jsonObject.has("altitude") && jsonObject.has("longitude") && jsonObject.has("latitude")){
            return new LocationObject(jsonObject.getDouble("altitude"), jsonObject.getDouble("longitude"), jsonObject.getDouble("latitude"));
        }
        throw new LocationObjectException("can't parse jsonObject to an locationObject");
    }

    public static class LocationObjectException extends Exception{
        public LocationObjectException(String message){
            super(message);
        }
    }
}
