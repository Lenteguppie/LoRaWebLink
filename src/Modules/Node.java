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
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class Node {
    private static String TAG = Node.class.getSimpleName();
    private String EUFI, name, uid, brand;
    private User owner;
    private int frequency;
    private LocationObject location;

    private ArrayList<Packet> packets = new ArrayList<>();

    public void addPacket(Packet packet){
        packets.add(packet);
    }

    public ArrayList<Packet> getPackets(){
        return packets;
    }

    public JSONObject getJSONPackets(){
        JSONObject nodeObject = new JSONObject();
        nodeObject.put("name", getName());
        nodeObject.put("uid", getUid());
        JSONArray packetArray = new JSONArray();
        for (Packet packet: getPackets()) {
            packetArray.put(packet.toJSON());
        }
        nodeObject.put("data", packetArray);
        return nodeObject;
    }

    //region getters and setters
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
            Log.d (TAG + " /node", String.format ("Node %s succesfully removed", this.getUid ()));
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

    public DataSet getPackets(DatabaseManager databaseManager, int nodeDataLimit){
        databaseManager.setQueryFormat("SELECT * FROM node_data WHERE node_id = '%s') LIMIT %s;");
        try {
            ResultSet resultSet = databaseManager.sendArgs(new String[]{this.getUid(), String.valueOf(nodeDataLimit)});
            return new DataSet(this, resultSet);
        } catch (SQLException e) {
            return null;
        }
    }

    public DataSet createDataSet(ResultSet resultSet){
        return new DataSet(this, resultSet);
    }

    public static JSONArray toJSONArray(ArrayList<Node> nodes){
        JSONArray nodesJSONArray = new JSONArray();
        for (Node node: nodes) {
            nodesJSONArray.put(node.toJSON());
        }
        return nodesJSONArray;
    }
    //endregion

    public static Packet processPacket(JSONObject packetObject) throws Packet.PacketException {
        return new Packet(packetObject);
    }

    public static class Packet{
        private String node_uid;
        private String modulation;
        private int status;
        private int RSSI;
        private DataPacket data;
        private int frequency;
        private int timeStamp;
        private int RFChain;
        private String CRIdentifier;
        private int size;
        private String DRIdentifier;
        private int SNR_ration;
        private String time;
        private int IFChannel;

        private JSONObject jsonPacket;

        public static Packet fromResultSet(ResultSet resultSet) throws SQLException, PacketException {
            return new Packet(new JSONObject(resultSet.getString("message")));
        }

        public Packet(JSONObject packetObject) throws PacketException {
            if(packetObject.has("rxpk")){
                throw new PacketException("More packets found in node packet can only be 1 node data packet");
            }
            this.jsonPacket = packetObject;
            try {
                setModulation(packetObject.getString("modu"));
                setStatus(packetObject.getInt("stat"));
                setRSSI(packetObject.getInt("rssi"));
                setData(new DataPacket(packetObject.getString("data")));
                //setNode_uid(data.getUID());//TODO LETOP VOOR DEBUG ROBIN
                setFrequency(packetObject.getInt("freq"));
                setTimeStamp(packetObject.getInt("tmst"));
                setRFChain(packetObject.getInt("rfch"));
                setCRIdentifier(packetObject.getString("codr"));
                setSize(packetObject.getInt("size"));
                setDRIdentifier(packetObject.getString("datr"));
                setSNR_ration(packetObject.getInt("lsnr"));
                setTime(packetObject.getString("time"));
                setIFChannel(packetObject.getInt("chan"));
            }catch (Exception e){
                throw new PacketException(e.getMessage());
            }
        }

        //region  modu | string | Modulation identifier "LORA" or "FSK"
        public String getModulation() {
            return modulation;
        }

        private void setModulation(String modulation) {
            this.modulation = modulation;
        }
        //endregion

        //region stat | number | CRC status: 1 = OK, -1 = fail, 0 = no CRC
        public int getStatus() {
            return status;
        }

        private void setStatus(int status) {
            this.status = status;
        }
        //endregion

        //region  rssi | number | RSSI in dBm (signed integer, 1 dB precision)
        public int getRSSI() {
            return RSSI;
        }

        private void setRSSI(int RSSI) {
            this.RSSI = RSSI;
        }
        //endregion

        //region data | string | Base64 encoded RF packet payload, padded
        public DataPacket getData() {
            return data;
        }

        private void setData(DataPacket data) {
            this.data = data;
        }
        //endregion

        //region freq | number | RX central frequency in MHz (unsigned float, Hz precision)
        public int getFrequency() {
            return frequency;
        }

        private void setFrequency(int frequency) {
            this.frequency = frequency;
        }
        //endregion

        //region tmst | number | Internal timestamp of "RX finished" event (32b unsigned)
        public int getTimeStamp() {
            return timeStamp;
        }

        private void setTimeStamp(int timeStamp) {
            this.timeStamp = timeStamp;
        }
        //endregion

        //region rfch | number | Concentrator "RF chain" used for RX (unsigned integer)
        public int getRFChain() {
            return RFChain;
        }

        private void setRFChain(int RFChain) {
            this.RFChain = RFChain;
        }
        //endregion

        //region codr | string | LoRa ECC coding rate identifier
        public String getCRIdentifier() {
            return CRIdentifier;
        }

        private void setCRIdentifier(String CRIdentifier) {
            this.CRIdentifier = CRIdentifier;
        }
        //endregion

        //region size | number | RF packet payload size in bytes (unsigned integer)
        public int getSize() {
            return size;
        }

        private void setSize(int size) {
            this.size = size;
        }
        //endregion

        //region datr | string | LoRa datarate identifier (eg. SF12BW500)
        public String getDRIdentifier() {
            return DRIdentifier;
        }

        private void setDRIdentifier(String DRIdentifier) {
            this.DRIdentifier = DRIdentifier;
        }
        //endregion

        //region lsnr | number | Lora SNR ratio in dB (signed float, 0.1 dB precision)
        public int getSNR_ration() {
            return SNR_ration;
        }

        private void setSNR_ration(int SNR_ration) {
            this.SNR_ration = SNR_ration;
        }
        //endregion

        //region chan | number | Concentrator "IF" channel used for RX (unsigned integer)
        public int getIFChannel() {
            return IFChannel;
        }

        private void setIFChannel(int channel) {
            this.IFChannel = channel;
        }
        //endregion

        //region time | string | UTC time of pkt RX, us precision, ISO 8601 'compact' format
        public String getTime() {
            return time;
        }

        private void setTime(String time) {
            this.time = time;
        }
        //endregion

        public String getNode_uid() {
            return node_uid;
        }

        private void setNode_uid(String node_uid) {
            this.node_uid = node_uid;
        }

        public JSONObject toJSON() {
            return getJsonPacket();
        }

        //region DataPacket class for transferring encode data en decoding data
        public static class DataPacket{
            private String dataEncoded;
            private String dataDecoded;
            private HashMap<String, String> dataDecodedMap;

            DataPacket(String dataEncoded){
                this.dataEncoded = dataEncoded;
            }

            HashMap<String, String> decode(){
                if(dataDecoded == null){
                    dataDecoded = decryptNodeMessage(dataEncoded);
                }
                dataDecodedMap = createDecodedMap(dataDecoded);
                return dataDecodedMap;
            }

            /*Decrypt the encrypted data which is encrypted with the Base64 encryption*/
            private String decryptNodeMessage(String encodedString) {
                byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
                return new String(decodedBytes);
            }

            String getUID() throws PacketException {
                if(dataDecodedMap == null || dataDecodedMap.isEmpty()){
                    decode();
                }
                if(!dataDecodedMap.isEmpty() && dataDecodedMap.containsKey("uid")){
                    return dataDecodedMap.get("uid");
                }else{
                    throw new Node.Packet.PacketException("Missing device identifier");
                }
            }

            private static HashMap<String, String> createDecodedMap(String URI) {
                HashMap<String, String> result = new HashMap<>();
                for (String param : URI.split("&")) {
                    String[] entry = param.split("=");
                    if (entry.length > 1) {
                        result.put(entry[0], entry[1]);
                    }
                }
                return result;
            }

            @Override
            public String toString() {
                if(dataDecoded == null){
                    decode();
                }
                return dataDecoded;
            }
        }
        //endregion

        public JSONObject getJsonPacket() {
            return jsonPacket;
        }

        public static class PacketException extends Exception{
            public PacketException(String message){
                super(message);
            }
        }

        @Override
        public String toString() {
            return getJsonPacket().toString();
        }
    }

    public static class DataSet{

        private Node node;
        private ResultSet resultSet;
        private volatile boolean processed = false;
        private ArrayList<Packet> packets = new ArrayList<>();

        public DataSet(Node node, ResultSet resultSet) {
            this.node = node;
            this.resultSet = resultSet;
        }

        public void process(){
            processed = false;
            new Thread(() -> {
                if(resultSet != null && DatabaseManager.getRowCount(resultSet) > 0){
                    for (int i = 1; i <= DatabaseManager.getRowCount(resultSet) ; i++) {
                        try {
                            resultSet.absolute(i);
                            node.addPacket(Packet.fromResultSet(resultSet));
                        }catch (SQLException | Packet.PacketException e){
                            e.printStackTrace();
                        }
                    }
                    packets = node.getPackets();
                }else{
                    Log.E(TAG, "Empty resultset");
                }
                processed = true;
            }).start();
        }

        public ArrayList<Packet> getPackets() {
            if(!processed){
                process();
            }
            while (!processed) {
                Thread.onSpinWait();
            }
            return packets;
        }

        public JSONObject getJSONPacket(){
            ArrayList<Packet> packets = getPackets();
            if (!packets.isEmpty()) {
                JSONObject packetObject = new JSONObject();
                packetObject.put("name", node.getName());
                packetObject.put("uid", node.getUid());
                JSONArray packetArray = new JSONArray();
                for (Packet packet : packets) {
                    packetArray.put(packet.toJSON());
                }
                packetObject.put("data", packetArray);
                return packetObject;
            }
            return null;
        }
    }
}
