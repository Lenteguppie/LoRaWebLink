import Extentions.ConfigManager;
import Extentions.FileManager;
import Extentions.Logger.LoggerHelper;
import Extentions.Webserver.PageHandler;
import Extentions.Webserver.Webserver;
import WebLink.WebServices;
import org.json.JSONObject;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        LoggerHelper loggerHelper = new LoggerHelper();
        try {
            loggerHelper.setup();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problems with creating the log files");
        }
        ConfigManager configManager = new ConfigManager("config.json");
        WebServices webServices = new WebServices(configManager);
    }
}
