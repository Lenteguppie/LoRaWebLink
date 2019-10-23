package Extentions;

import Extentions.Logger.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static Extentions.FileManager.FileManagerException.*;

public class FileManager {

    private static String TAG = FileManager.class.getSimpleName();
    private final boolean log;
    private String fileContent;

    public FileManager(boolean log) {
        this.log = log;
        if(log) {
            Log.d(TAG, "Init");
        }
    }

    public void readFile(String path) throws PermissionDenied, Existence, General {
        File f = new File(path);
        if(!f.exists()){
            throw new Existence("File doesn't exist in given path: " + path);
        }else{
            if(!f.canRead()){
                throw new PermissionDenied("Can't read file permission denied");
            }else{
                fileContent = FileToString(path);
            }
        }
    }

    public JSONObject parseContent() throws Parse {
        try {
            return new JSONObject(fileContent);
        } catch (JSONException e) {
            throw new Parse(e.getMessage());
        }
    }

    private String FileToString(String filePath) throws General {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            throw new General(e.getMessage());
        }
        if(log) {
            Log.d(TAG, "\n File: " + contentBuilder.toString());
        }
        return contentBuilder.toString();
    }

    public static class FileManagerException {
        public static class General extends Exception{
            General(String message){
                super(message);
            }
        }

        public static class PermissionDenied extends Exception{
            PermissionDenied(String message){
                super(message);
            }
        }
        public static class Existence extends Exception{
            Existence(String message){
                super(message);
            }
        }
        public static class Parse extends Exception{
            Parse(String message){
                super(message);
            }
        }
    }

}
