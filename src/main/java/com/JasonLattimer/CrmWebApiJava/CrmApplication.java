package com.JasonLattimer.CrmWebApiJava;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.naming.ServiceUnavailableException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class CrmApplication {

    //This was registered in Azure AD as a WEB APPLICATION AND/OR WEB API
    //Azure Application Client ID
    private final static String CLIENT_ID = "00000000-0000-0000-0000-000000000000";
    //CRM URL
    private final static String RESOURCE = "https://org.crm.dynamics.com";
    //O365 credentials for authentication w/o login prompt
    private final static String USERNAME = "administrator@org.onmicrosoft.com";
    private final static String PASSWORD = "password";
    //Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
    private final static String AUTHORITY = "https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000";

    public static void main(String args[]) throws Exception {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

            //No prompt for credentials
            AuthenticationResult result = getAccessTokenFromUserCredentials();
            System.out.println("Access Token - " + result.getAccessToken());
            System.out.println("Refresh Token - " + result.getRefreshToken());
            System.out.println("ID Token - " + result.getIdToken());

            String userId = WhoAmI(result.getAccessToken());
            System.out.println("UserId - " + userId);

            String fullname = FindFullname(result.getAccessToken(), userId);
            System.out.println("Fullname: " + fullname);

            String accountId = CreateAccount(result.getAccessToken(), "Java Test");
            System.out.println("Created: " + accountId);

            accountId = UpdateAccount(result.getAccessToken(), accountId);
            System.out.println("Updated: " + accountId);

            accountId = DeleteAccount(result.getAccessToken(), accountId);
            System.out.println("Deleted: " + accountId);
        }
    }

    private static String DeleteAccount(String token, String accountId) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/accounts
        URL url = new URL(RESOURCE + "/api/data/accounts(" + accountId + ")");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        int responseCode = connection.getResponseCode();

        return accountId;
    }

    private static String UpdateAccount(String token, String accountId) throws MalformedURLException, IOException, URISyntaxException {
        JSONObject account = new JSONObject();
        account.put("websiteurl", "http://www.microsoft.com");

        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/accounts
        URL url = new URL(RESOURCE + "/api/data/accounts(" + accountId + ")");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.connect();

        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write(account.toJSONString());
        out.flush();
        out.close();

        int responseCode = connection.getResponseCode();

        return accountId;
    }

    private static String CreateAccount(String token, String name) throws MalformedURLException, IOException {
        JSONObject account = new JSONObject();
        account.put("name", name);

        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/accounts
        URL url = new URL(RESOURCE + "/api/data/accounts");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        connection.connect();

        BufferedWriter out
                = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        out.write(account.toJSONString());
        out.close();

        int responseCode = connection.getResponseCode();

        String headerId = connection.getHeaderField("OData-EntityId");

        String accountId = headerId.split("[\\(\\)]")[1];
        return accountId;
    }

    private static String FindFullname(String token, String userId) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/systemusers
        URL url = new URL(RESOURCE + "/api/data/systemusers(" + userId + ")?$select=fullname");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Object jResponse;
        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = (JSONObject) jResponse;
        String fullname = jObject.get("fullname").toString();
        return fullname;
    }

    private static String WhoAmI(String token) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/WhoAmI
        URL url = new URL(RESOURCE + "/api/data/WhoAmI");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("OData-MaxVersion", "4.0");
        connection.setRequestProperty("OData-Version", "4.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Object jResponse;
        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = (JSONObject) jResponse;
        String userId = jObject.get("UserId").toString();
        return userId;
    }

    private static AuthenticationResult getAccessTokenFromUserCredentials()
            throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(AUTHORITY, false, service);
            Future<AuthenticationResult> future = context.acquireToken(RESOURCE,
                    CLIENT_ID,
                    USERNAME,
                    PASSWORD, null);
            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        return result;
    }
}
