package Extentions;


import org.json.JSONObject;

public class ConfigManager extends FileManager {

    private String path;
    private JSONObject configObject;

    public ConfigManager(boolean log, String path) {
        super(log);
        this.path = path;
        getConfigFile();
    }

    public ConfigManager(String path) {
        super(false);
        this.path = path;
        getConfigFile();
    }

    public JSONObject getConfigObject(String objectTitle) throws FileManagerException.General {
        if(configObject != null){
            return (JSONObject) configObject.get(objectTitle);
        }
        throw new FileManagerException.General("Can't find objectTitle in json config");
    }

    private void getConfigFile(){
        try {
            this.readFile(path);
            this.configObject = parseContent();
        } catch (FileManagerException.PermissionDenied | FileManagerException.Existence | FileManagerException.General | FileManagerException.Parse permissionDenied) {
            permissionDenied.printStackTrace();
        }
    }

    public static class ConfigManagerException extends Exception{
        public ConfigManagerException(String ex){
            super(ex);
        }
    }
}
