package Extentions.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class Log{

    //region infoLoggers
    public static void d(String TAG, String message){ d(TAG, message, false); }

    public static void d(String TAG, String message, int index){
        d(TAG, "\t".repeat(Math.max(0, index)) + message, false);
    }

    public static void d(String TAG, int message){
        d(TAG, String.valueOf(message), false);
    }

    public static void d(String TAG, Exception e){
        d(TAG, e.getMessage(), false);
    }

    public static void d(String TAG, String message, boolean process){
        if(process){
            System.out.print(TAG + ":\t" + message);
            LoggerHelper.logEntry(TAG, message,false);
        }else{
            LoggerHelper.logEntry(TAG, message,false);
            System.out.println(TAG + ":\t" + message);
        }
    }

    public static void d(String TAG, Object o) {
        LoggerHelper.logEntry(TAG, o, LoggerHelper.Types.INFO);
        System.out.println(TAG + ":\t" + o);
    }

    private static void d(String tag, Boolean o) {
        if(o){
            d(tag, "true");
        }else{
            d(tag, "false");
        }
    }
    //endregion

    public static void e(String message){
        System.out.println("\t" + message);
    }

    public static void i(String TAG, String message, int index, boolean process){
        String indexString = "";
        for (int i = 0; i <index ; i++) {
            indexString += "\t";
        }
        if(process){
            System.out.print(TAG + ":\t" + indexString + message);
        }else{
            System.out.println(TAG + ":\t" + indexString + message);
        }
    }


    //Log with function title
    public static void f(String TAG,String function, String message){
        System.out.println(TAG + ":\t"+ function + "\t" + message);
    }

    //Log with time stamp
    public static void T(String TAG,String message){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + "\t" + TAG + ":" + "\t" + message);
    }

    //region errorLoggers
    public static void E(String TAG,String message){
        T(TAG, "ERROR:\t" + message);
    }

    public static void E(String TAG,int message){
        T(TAG, "ERROR:\t" + message);
    }

    public static void E(String TAG,Exception e){
        T(TAG, "ERROR:\t" + e.getMessage());
    }
    //endregion

    public static void W(String TAG,String message){
        T(TAG, "WARNING:\t" + message);
    }


    //Log object for objectArrayList
    private static void o(String tag, Object o){
      if(o instanceof String){
          d(tag, (String) o);
      }else if(o instanceof Integer){
          d(tag, (Integer) o);
      }else if(o instanceof Boolean){
          d(tag, (Boolean) o);
      }else{
          d(tag, o);
      }
    }

    private static void o(String tag, String key, Object o){
      if(o instanceof String){
          d(tag, key + " : " + o);
      }else if(o instanceof Integer){
          d(tag, key + " : " + o);
      }else if(o instanceof Boolean){
          d(tag, key + " : " + o);
      }else if(o instanceof Double){
          d(tag, key + " : " + o);
      }else{
          d(tag, o);
      }
    }

    public static void list(String TAG, HashMap<String, Object> list){
        for (String key: list.keySet()) {
            o(TAG, key, list.get(key));
        }
    }



    public static void JSONArray(String TAG, JSONArray jArr) {
        try {

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject innerObj = jArr.getJSONObject( i );
                for (Iterator it = innerObj.keys(); it.hasNext(); ) {
                    String key = (String) it.next();
                    System.out.println( key + ":" + innerObj.get( key ) );
                }
            }
        } catch (JSONException e) {
            d( TAG, e );
        }
    }

    public static void arrLi(String TAG, ArrayList<String> arrLi){
        int i = 0;
        for(String item : arrLi)
        {
            System.out.println(item);
//            d(TAG + " ArrLi", item);
            i++;
        }
    }
}
