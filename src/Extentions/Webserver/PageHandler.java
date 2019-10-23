package Extentions.Webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class PageHandler implements HttpHandler {

    private Boolean debug = false;
    private HashMap<String, OnPageListener> onPageListeners = new HashMap<>();

    public PageHandler(Boolean debug) {
        this.debug = debug;
    }

    public PageHandler(){}

    @Override
    public void handle(HttpExchange httpExchange) {
        createOnPageEvent(new Page(httpExchange, debug));
    }

    void addOnPageListener(String path, OnPageListener listener) {
        if (onPageListeners.containsKey(path)) {
            throw new RuntimeException("ERROR RequestHandler:" + path + "Already registered");
        } else {
            onPageListeners.put(path, listener);
        }
    }

    private void createOnPageEvent(Page page) {
        OnPageListener listener = onPageListeners.get(page.getPath());
        if (listener != null) {
            listener.onPage(page);
        } else {
            page.handle(get404(page));
        }
    }

    public static class Page {

        private HashMap<String, String> arguments;
        private boolean hasArgs;
        private HttpExchange exchange;
        private String path;
        private boolean handled = false;

        Page(HttpExchange exchange, boolean debug) {
            this.exchange = exchange;
            this.path = exchange.getRequestURI().getPath();
            if (getExchange().getRequestURI().getQuery() != null) {
                this.arguments = PageHandler.getArguments(getExchange().getRequestURI().getQuery());
                this.hasArgs = !arguments.isEmpty();
            }
            if(!debug) {
                timeOutHandle(this);
            }
        }

        public HashMap<String, String> getArguments() {
            return arguments;
        }

        public boolean hasArgs() {
            return hasArgs;
        }

        public boolean hasArg(String key) {
            return hasArgs(new String[]{key});
        }

        public String getArg(String arg){
            if(hasArgs(new String[]{arg})){
                return getArguments().get(arg);
            }
            return null;
        }

        public boolean hasArgs(String[] args) {
            for (String arg : args) {
                if (arguments != null) {
                    if (!arguments.containsKey(arg)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }

        HttpExchange getExchange() {
            return exchange;
        }

        public void handle(String response) {
                handled = true;
                if (exchange != null) {
                    try {
                        response = response.replace("\\", "");
                        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (IOException e) {
                        if(e.getMessage().contains("stream is closed")){
                            System.out.println("Page already handled");
                        }else {
                            e.printStackTrace();
                        }
                    } finally {
                        exchange = null;
                    }
                }

        }

        String getPath() {
            return path;
        }

        private static void timeOutHandle(Page page) {
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!page.handled) {
                    page.handle(get204(page));
                }

            }).start();
        }
    }

    private static HashMap<String, String> getArguments(String URI) {
        HashMap<String, String> result = new HashMap<>();
        for (String param : URI.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }

    public interface OnPageListener {
        void onPage(Page page);
    }

    private static String get404(Page page) {
        return page.getPath() + " is unreachable please try again";
    }

    private static String get204(Page page) {
        return page.getPath() + "Takes to long to load please contact the developer";
    }


}
