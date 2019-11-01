package Extentions.Webserver;

import Extentions.Logger.Log;
import WebLink.WebServices;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static Extentions.ConfigManager.ConfigManagerException;

public class Webserver implements Runnable{

    private static String TAG = Webserver.class.getSimpleName();
    private PageHandler pageHandler;
    private static HttpServer server;
    private static InetSocketAddress boundaddres;
    private int port;

    public Webserver(int port){
        this.port = port;
        try {
//            this.server = HttpServer.create(new InetSocketAddress(8555), 0);
            this.server = HttpServer.create (new InetSocketAddress ("0.0.0.0", port),0);
//            this.server.bind (new InetSocketAddress ("0.0.0.0", 8555), 0);
            this.boundaddres = server.getAddress ();
            this.pageHandler = new PageHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Webserver(int port, Boolean debug){
        this.port = port;
        try {
            this.server = HttpServer.create((new InetSocketAddress ("0.0.0.0", port)), 0);
//            this.server.bind (new InetSocketAddress ("0.0.0.0", 8555), 0);
            this.boundaddres = this.server.getAddress ();
            Log.d (TAG, server.getAddress ());
            this.pageHandler = new PageHandler(debug);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        server.start();
    }

    public void stop(){
        server.stop(0);
    }

    public void addPageListener(String path, PageHandler.OnPageListener listener){
        pageHandler.addOnPageListener(path, listener);
        HttpContext context = server.createContext(path);
        context.setHandler(pageHandler);
    }

    public void start(){
        new Thread(this).start();
    }

    public static Webserver initFromJSON(JSONObject serverConfig, boolean debug) throws ConfigManagerException {
        if(serverConfig != null) {
            if(debug){
                Log.W(TAG, "Webserver initiated in debugger mode");
            }
//            Log.d (TAG, String.format ("Webserver running on: %s", Webserver.server.getAddress().getHostString ()));
            return new Webserver((Integer) serverConfig.get("port"), debug);
        }
        throw new ConfigManagerException("config file is null");
    }


    public static Webserver initFromJSON(JSONObject serverConfig) throws ConfigManagerException {
       return initFromJSON(serverConfig, false);
    }

    public int getPort() {
        return port;
    }
}
