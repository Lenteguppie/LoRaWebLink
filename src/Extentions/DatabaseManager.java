package Extentions;

import Extentions.Logger.Log;
import org.json.JSONObject;

import java.sql.*;

import static Extentions.ConfigManager.ConfigManagerException;

public class DatabaseManager extends Thread{



    private enum Mode{
        UPDATE,
        SELECT
    }

    private static String TAG = DatabaseManager.class.getSimpleName();
    private String Username, Password;
    private String server_address, server_port = "3306", server_db_name;
    private String serverConnection = "jdbc:mysql://%s:%s/%s?connectTimeout=1000";
    private Connection conn = null;
    private String queryFormat;
    private Mode mode = Mode.SELECT;

    public DatabaseManager(String Address, String database_name) {
        this.server_address = Address;
        this.server_db_name = database_name;
    }

    //set login parameters for login database SQL
    public void setLogin(String Username, String Password) {
        this.Username = Username;
        this.Password = Password;
    }

    //set name of database
    public void setDatabase(String database_name) {
        this.server_db_name = database_name;
    }

    //Set database port (Default 3306)
    public void setPort(int port) {
        this.server_port = String.valueOf(port);
    }

    //Connect the databaseManager to a database
    public void connect() throws DatabaseException.IsOffline {
        this.start();
        if(!isConnected()){
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String ConnectionString = String.format(serverConnection, server_address, server_port, server_db_name);
                //Log.d(TAG, "connect: " + ConnectionString);
                conn = DriverManager.getConnection(ConnectionString, Username, Password);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new DatabaseException.IsOffline(TAG + "\t" + e.getMessage());
            }
        }else{
            try {
                if(conn.isClosed()) {
                    connect();
                }
            } catch (SQLException e) {
                throw new DatabaseException.IsOffline(TAG + "\t" + e.getMessage());
            }
        }
    }

    //Disconnect database manager from database
    public void disconnect() {
        try {
            if(conn != null) {
                if (!conn.isClosed()) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "disconnected");
    }

    //send update query as completed string
    public boolean update(String query) throws SQLException {
        if(isConnected()) {
            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.executeUpdate(query);
                return true;
            } else {
                Log.d(TAG, "update: Not connected with DB");
                return false;
            }
        }else{
            Log.d(TAG, "update: not connected to the internet");
            return false;
        }
    }

    //Check if database manager is connected (return true == CONNECTED)
    public boolean isConnected(){
            if (conn != null) {
                try {
                    if(!conn.isClosed()) {
                        return true;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        return false;
    }

    //Do a select query
    public ResultSet query(String query) throws SQLException {
        //Log.d(TAG, "query: " + query);
        if (conn != null) {
            Statement statement = conn.createStatement();
            return statement.executeQuery(query);
        } else {
            Log.d(TAG, "query: Not connected");
            return null;
        }
    }

    //get first result back as string
    public static String getSingleResult(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            if(resultSet.next()) {
                resultSet.absolute(1);
                return resultSet.getString(1);
            }
        }
        return null;
    }

    //EXCEPTIONS
    public static class DatabaseException extends Exception {

        public DatabaseException(String s) {
            super(s);
        }

        public static class IsOffline extends Exception{
            public IsOffline(String message){
                super(message);
            }
        }

        public static class IsNull extends Exception{
            public IsNull(String message){
                super(message);

            }
        }
        public static class EmptyResultSet extends Exception{
            public EmptyResultSet(String message){
                super(message);
            }
        }

    }

    //Get mode of the database manager UPDATE / SELECT
    public Mode getMode() {
        return mode;
    }

    //get amount of results in resultSet
    public static int getRowCount(ResultSet resultSet){
        int size;
        try {
            resultSet.last();
            size = resultSet.getRow();
            resultSet.beforeFirst();
        }
        catch(SQLException ex) {
            return 0;
        }
        return size;
    }

    //Set query non filled in with programmable parameters %s
    public void setQueryFormat(String format){
        this.queryFormat = format;
        if(queryFormat.toLowerCase().contains("update") || queryFormat.toLowerCase().contains("insert") || queryFormat.toLowerCase().contains("delete")  ){
            this.mode = Mode.UPDATE;
        }else{
            this.mode = Mode.SELECT;
        }
    }

    //Send query to database and wait for response; UPDATE (Return null)
    public ResultSet sendArgs(String[] args) throws SQLException {
        for (int i = 0; i <args.length ; i++) {
            queryFormat = queryFormat.replaceFirst("(%s*)", args[i]);
        }
//        Log.d(TAG, "Query: " + queryFormat);
        switch (getMode()){
            case SELECT:{
                return query(queryFormat);
            }
            case UPDATE:{
                update(queryFormat);
                return null;
            }
        }
        return query(queryFormat);
    }

    //Create database manager from json config file
    public static DatabaseManager initFromJSON(JSONObject databaseConfig) throws ConfigManagerException {
        Log.d(TAG,"LOADING...", true);
        if(databaseConfig != null) {
            DatabaseManager databaseManager;
            try {
                databaseManager = new DatabaseManager((String) databaseConfig.get("address"), (String) databaseConfig.get("databaseName"));
                databaseManager.setLogin((String) databaseConfig.get("username"), (String) databaseConfig.get("password"));
                databaseManager.setPort(Math.toIntExact(databaseConfig.getInt("port")));
                Log.e("DONE");
                return databaseManager;
            }catch (NullPointerException e){
                Log.e("FAILED");
            }

        }
        throw new ConfigManagerException("config file is null");
    }

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }
}




