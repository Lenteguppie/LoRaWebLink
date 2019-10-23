package WebLink;

import Applications.Application;
import Applications.ApplicationManager;
import Extentions.ConfigManager;
import Extentions.DatabaseManager;
import Extentions.FileManager;
import Extentions.Logger.Log;
import Extentions.Webserver.PageHandler;
import Extentions.Webserver.Webserver;
import Modules.AccessKey;
import Modules.Gateway;
import Modules.LocationObject;
import Modules.Node;
import Users.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class WebServices {

    private static String TAG = WebServices.class.getSimpleName();
    private ApplicationManager applicationManager;
    private UserSessionManager userSessionManager;
    private DatabaseManager databaseManager;
    private Webserver webServer;

    public WebServices(ConfigManager configManager) {
        try {
            this.webServer = Webserver.initFromJSON(configManager.getConfigObject("Webserver"), true);
            this.databaseManager = DatabaseManager.initFromJSON(configManager.getConfigObject("Database"));
            this.databaseManager.connect();
            this.userSessionManager = new UserSessionManager(databaseManager);
            this.applicationManager = new ApplicationManager(userSessionManager, databaseManager);
        } catch (ConfigManager.ConfigManagerException | FileManager.FileManagerException.General | DatabaseManager.DatabaseException.IsOffline e) {
            e.printStackTrace();
        }
        addDefaultHandlers();
        webServer.start();
    }

    private void addDefaultHandlers() {

        webServer.addPageListener("/", new PageHandler.OnPageListener() {
            @Override
            public void onPage(PageHandler.Page page) {
                page.handle("online");
            }
        });

        webServer.addPageListener("/login", page -> {
            JSONObject response = new JSONObject();
            if (page.hasArgs(new String[]{"email", "password"})) {
                String email = page.getArguments().get("email");
                String passwd = page.getArguments().get("password");
                if (userSessionManager.checkUserCredentials(email, passwd)) {
                     boolean sessionCreated = userSessionManager.createSession(
                            userSessionManager.getUser(email).getUserData().getUUID()
                    );
                    if (sessionCreated) {
                        String sessionKey = userSessionManager.getSessionEmail(email).getSessionKey().toString();
                        response.put("sessionKey", sessionKey);
                    } else {
                        response.put("error", "There already is a user logged in with this account");
                    }
                } else {
                    response.put("error", "Incorrect username or password");
                }
            } else {
                response.put("error", "invalid login request");
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/register", page -> {
            JSONObject response = new JSONObject();
            if (page.hasArgs(new String[]{"username", "password", "firstname", "lastname", "email"})) {
                HashMap<String, String> args = page.getArguments();
                boolean userCreated = userSessionManager.createUser(args.get("username"), args.get("password"), args.get("firstname"), args.get("lastname"), args.get("email"));
                if (userCreated) {
                    response.put("registration", "User created successfully");
                } else {
                    response.put("error", "User already exists with this email and/or username");
                }
            } else {
                response.put("error", "invalid login request");
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/profile", page -> {
            JSONObject response = new JSONObject();
            if(page.hasArgs(new String[]{"sessionKey"}) && userSessionManager.isValidSession(page.getArguments().get("sessionKey"))){
                User user =userSessionManager.getSession(page.getArguments().get("sessionKey")).getUser();
                response = user.toJSON(true);
            }else{
                response.put("error", "invalid profile request");
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/node", page -> {
            JSONObject response = new JSONObject();
            if(page.hasArgs(new String[]{"sessionKey"}) && userSessionManager.isValidSession(page.getArg("sessionKey"))){
                //region remove node from app
                if(page.hasArgs(new String[]{"node_uid", "remove"})){
                    if(page.getArg("remove").equals("true")){
                        User user = userSessionManager.getSession(page.getArguments().get("sessionKey")).getUser();
                        try {
                            for (Node node:userSessionManager.getNodes(user)) {
                                if(node.getOwner().getUserData().getUUID().toString().equals(user.getUserData().getUUID().toString())){
                                    response.put("done", node.remove(databaseManager));
                                    break;
                                }
                            }
                            if(!response.has("done")){
                                response.put("done", false);
                                response.put("error", "you are not owner of this node");
                            }
                        }
                        catch (UserManager.UserManagerException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //endregion

                //region addnode to app
                else if(page.hasArgs(new String[]{"name", "brand", "frequency", "lat", "long", "alt", "uid"})){
                    try {
                        Application application = applicationManager.getApplicationComplete(new Application(page.getArg("uid")));
                        Node newNode = new Node();
                        newNode.setBrand(page.getArg("brand"));
                        newNode.setName(page.getArg("name"));
                        newNode.setFrequency(Integer.parseInt(page.getArg("frequency")));
                        newNode.setLocation(new LocationObject(page.getArg("alt"), page.getArg("long"), page.getArg("lat")));
                        newNode.setOwner(userSessionManager.getSession(page.getArg("sessionKey")).getUser());
                        newNode.setUid(Node.generateUID());
                        response.put("done", application.addNode(databaseManager,newNode));
                    } catch (ApplicationManager.ApplicationException e) {
                        response.put("done", false);
                        response.put("error", e.getMessage());
                    }
                }
                //endregion
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/removeApp", page -> {
            JSONObject response = new JSONObject();
            if (page.hasArgs(new String[]{"uid", "sessionKey"})) {
                if (userSessionManager.isValidSession(page.getArguments().get("sessionKey"))) {
                    Application oldApplication = new Application();
                    oldApplication.setAPPID(page.getArguments().get("uid"));
                    try {
                        response.put("removed", applicationManager.removeApplication(userSessionManager.getSession(page.getArguments().get("sessionKey")).getUser(), oldApplication));
                    } catch (ApplicationManager.ApplicationException e) {
                        response.put("error", e.getMessage());
                    }
                } else {
                    response.put("error", "Invalid session please logout and try again");
                }
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/createApp", page -> {
            JSONObject response = new JSONObject();
            if (page.hasArgs(new String[]{"name", "description", "sessionKey"})) {
                if (userSessionManager.isValidSession(page.getArguments().get("sessionKey"))) {
                    Application application = new Application();
                    application.setName(page.getArguments().get("name"));
                    application.setDescription(page.getArguments().get("description"));
                    application.setOwner(userSessionManager.getSession(page.getArguments().get("sessionKey")).getUser());
//                    application.addAccessKey();
                    try {
                        response.put("uid", applicationManager.createApplication(page.getArguments().get("sessionKey"), application).getAPPID());
                    } catch (ApplicationManager.ApplicationException e) {
                        response.put("error", e.getMessage());
                    }
                } else {
                    response.put("error", "Invalid session please logout and try again");
                }
            } else {
                response.put("error", "invalid app creating request");
            }
            page.handle(response.toString());


        });

        webServer.addPageListener("/apps", page -> {
            JSONObject response = new JSONObject();
            if (page.hasArgs(new String[]{"sessionKey", "uid"})) {
                if (userSessionManager.isValidSession(page.getArguments().get("sessionKey"))) {
                    try {
                       Application application = applicationManager.getApplicationComplete(new Application(page.getArguments().get("uid")));
                       response = application.toJSON(true);
                    } catch (ApplicationManager.ApplicationException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.put("error", "Invalid session please logout and try again");
                }
                //region json page handle
                page.handle(response.toString());
                //endregion
            } else if (page.hasArgs(new String[]{"sessionKey"})) {
                if (userSessionManager.isValidSession(page.getArguments().get("sessionKey"))) {
                    try {
                        ArrayList<Application> applications = applicationManager.getApplications(userSessionManager.getSession(page.getArguments().get("sessionKey")).getUser());
                        response = ApplicationManager.listToJSON(applications);
                        page.handle(response.toString());
                    } catch (ApplicationManager.ApplicationException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.put("error", "Invalid session please logout and try again");
                }
            }
            page.handle(response.toString());
        });

        webServer.addPageListener("/map", page -> {
            JSONObject response = new JSONObject();
            if(page.hasArgs(new String[]{"sessionKey"})){
                if(userSessionManager.isValidSession(page.getArguments().get("sessionKey"))){
                    UserSession session = userSessionManager.getSession(page.getArguments().get("sessionKey"));
                    try {
                        response.put("gateways", Gateway.toJSONArray(userSessionManager.getGateways(session.getUser())));
                        response.put("nodes", Node.toJSONArray(userSessionManager.getNodes(session.getUser())));
                    } catch (UserManager.UserManagerException e) {
                        e.printStackTrace();
                    }
                }
                page.handle(response.toString());
            }else{
                response.put("error", "invalid map request");
                page.handle(response.toString());
            }
        });


    }

}
