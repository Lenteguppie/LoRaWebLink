package Extentions.Logger;


import java.io.IOException;
import java.util.logging.*;

public class LoggerHelper {

    public enum Types{
        WARNING,
        ERROR,
        INFO,
        CAUTION
    }

    static private Logger logger;

    /* This file is to log the entries that are logged with the Log.java to a log file and a HTML page!!!! */

    public void setup() throws IOException {

        // get the global logger to configure it
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        logger.setLevel(Level.INFO);
        FileHandler fileTxt = new FileHandler("./LOGS/Logging.txt");
        FileHandler fileHTML = new FileHandler("./LOGS/Logging.html");

        // create a TXT formatter
        SimpleFormatter formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

        // create an HTML formatter
        Formatter formatterHTML = new MyHtmlFormatter();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);
    }

    static void logEntry(String tag, String msg, boolean warning) {
        if (warning) {
            logger.log(Level.WARNING, tag + "\t" + msg);
        } else {
            logger.log(Level.INFO, tag + "\t" + msg);
        }
    }

    static void logEntry(String tag, Exception e) {
        logger.log(Level.WARNING, tag + "\t" + e.getMessage());
    }

    static void logEntry(String tag, Object o, Types type) {
        switch (type){
            case WARNING:{
                logger.log(Level.WARNING, tag + "\t" + o);
            }
            case ERROR:{
                logger.log(Level.INFO, tag + "\t" + o);
            }
            case INFO:{
                logger.log(Level.INFO, tag + "\t" + o);
            }
            case CAUTION:{
                logger.log(Level.INFO, tag + "\t" + o);
            }
        }
    }
}