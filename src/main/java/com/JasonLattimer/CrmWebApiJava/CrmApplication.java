package com.JasonLattimer.CrmWebApiJava;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import org.json.JSONObject;

//import net.minidev.json.JSONValue;
//import net.minidev.json.JSONObject;

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


public class CrmApplication {
    /*
        <add name="CRMConfig" connectionString="Url=https://astontech.crm.dynamics.com; Username=dan.simmer@astontech.com; Password=Ast0np@ss" />
        <!--<add name="CRMConfig" connectionString="Url=https://astontechsandbox.crm.dynamics.com; Username=reportsadmin@astontech.com; Password=@@&3p0&t$@@" />-->
    */
    //This was registered in Azure AD as a WEB APPLICATION AND/OR WEB API
    //Azure Application Client ID
    private final static String CLIENT_ID = "30c0ffab-09b0-4bcf-8bf6-7edf6881648a";

    //O365 credentials for authentication w/o login prompt
    private final static String USERNAME = "dan.simmer@astontech.com";
    private final static String PASSWORD = "Ast0np@ss";

    //Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
    private final static String AUTHORITY = "https://login.windows.net/d5588966-6236-4e1e-a41b-2cef3ffbab62/oauth2/authorize";

    //CRM URL
    private final static String RESOURCE = "https://astontech.crm.dynamics.com";
    private final static String CRM_API_PREFIX = "/api/data/v8.0";
    private final static String CRM_ENTITY = "/aston_applicant";
    private final static String CRM_QUERY = "?$select=aston_name,aston_astonmarketingemail,aston_assigneddid,aston_dob&$filter=contains(aston_name,%20%27Pruden%27)";
    private final static String CRM_URL = RESOURCE + CRM_API_PREFIX + CRM_ENTITY;
    private final static String CRM_URL_QUERY = RESOURCE + CRM_API_PREFIX + CRM_ENTITY + CRM_QUERY;



    public static void main(String args[]) throws Exception {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

            //No prompt for credentials
            AuthenticationResult result = getAccessTokenFromUserCredentials();
            System.out.println("Access Token - " + result.getAccessToken());
            System.out.println("Refresh Token - " + result.getRefreshToken());
            System.out.println("ID Token - " + result.getIdToken());

            String aston_name = getEngineer(result.getAccessToken());
            System.out.println("aston_name - " + aston_name);

//            String productId = CreateProduct(result.getAccessToken(), "TEST");
//            System.out.println(productId);

//            String userId = WhoAmI(result.getAccessToken());
//            System.out.println("UserId - " + userId);
//
//            String fullname = FindFullname(result.getAccessToken(), userId);
//            System.out.println("Fullname: " + fullname);
//
//            String accountId = CreateAccount(result.getAccessToken(), "Java Test");
//            System.out.println("Created: " + accountId);
//
//            accountId = UpdateAccount(result.getAccessToken(), accountId);
//            System.out.println("Updated: " + accountId);
//
//            accountId = DeleteAccount(result.getAccessToken(), accountId);
//            System.out.println("Deleted: " + accountId);
        }
    }

    private static String getEngineer(String token) throws MalformedURLException, IOException {
        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/WhoAmI
        URL url = new URL(CRM_URL);
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

        JSONObject jObject = new JSONObject(response.toString());
//        String aston_name = jObject.getJSONArray("value").getJSONObject(0).get("aston_name").toString();
        String aston_name = jObject.getJSONArray("value").getJSONObject(0).toString();
        return aston_name;
    }

    private static String CreateProduct(String token, String name) throws MalformedURLException, IOException {
        JSONObject applicant = new JSONObject();
        applicant.put("aston_name", name);
        applicant.put("aston_location", 642190001);
        applicant.put("aston_practicetechnologygroup", 1);

        HttpURLConnection connection = null;
        //The URL will change in 2016 to include the API version - /api/data/v8.0/accounts
        URL url = new URL(RESOURCE + "/api/data/v8.0/aston_applicants");
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
        out.write(applicant.toString());
        out.close();

        int responseCode = connection.getResponseCode();

        String headerId = connection.getHeaderField("OData-EntityId");

        String accountId = headerId.split("[\\(\\)]")[1];
        return accountId;
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
        out.write(account.toString());
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
        out.write(account.toString());
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

//        Object jResponse;
//        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = new JSONObject(response.toString());
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

//        Object jResponse;
//        jResponse = JSONValue.parse(response.toString());
        JSONObject jObject = new JSONObject(response.toString());
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
