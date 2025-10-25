// Constants.java
package services;

import com.google.gson.Gson;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;

public class Constants {
    public final static String SERVER_URL = "http://localhost:8080/web_demo_Web/";
    public static final String CONNECTED_USERS = SERVER_URL + "api/connected-users";// להתאים בפועל
    public static final Gson GSON = new Gson();
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
}


