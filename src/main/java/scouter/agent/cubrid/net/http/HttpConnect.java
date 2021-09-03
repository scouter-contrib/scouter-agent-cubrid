/*
 *  Copyright 2021 the original author or authors.
 *  @https://github.com/scouter-contrib/scouter-agent-cubrid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package scouter.agent.cubrid.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CubridTask;

public class HttpConnect {
    private static final String METHOD = "/cm_api";
    private static HttpsURLConnection httpConn = null;
    private static HttpConnect instance;
    private static String loginToken = "";
    private static boolean isHttpConnecting = false;
    private static boolean isLoginSuccess = false;
    private static HashMap<String, Boolean> isStratDump = new HashMap<>();

    protected static boolean debugLog = false;

    public static synchronized HttpConnect getInstance() {
        if (instance == null) {
            synchronized (HttpConnect.class) {
                instance = new HttpConnect();
            }
        }
        return instance;
    }
    
    public static String cubrid_cms_user_name = "admin";
    public static String cubrid_cms_user_passwd = "admin";
    public static String cubrid_db_user_name = "dba";
    public static String cubrid_db_user_passwd = "";

    public static boolean connect() {

        if (httpConn != null) {
            httpConn.disconnect();
            httpConn = null;
        }
        
        Configure conf = Configure.getInstance();
        String requestUrl = "https://" + conf.cubrid_cms_ip + ":" + conf.cubrid_cms_port + METHOD;
        URL url;

        try {
            X509TrustManager trustManager =
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] xcs, String string)
                                throws CertificateException {}

                        public void checkServerTrusted(X509Certificate[] xcs, String string)
                                throws CertificateException {}

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {trustManager}, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            url = new URL(requestUrl);
            httpConn = (HttpsURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.addRequestProperty("Connection", "keep-alive");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(5000); // Added Read Timeout(issue)
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestProperty("Content-Type", "application/json");

            synchronized (HttpConnect.class) {
                try {
                    httpConn.connect();
                } catch (ConnectException e) {
                    Logger.println("ConnectException exception");
                    httpConn.disconnect();
                    httpConn = null;
                    clearStatus();
                }
            }

        } catch (Exception e) {
            httpConn.disconnect();
            httpConn = null;
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean login() {

        Logger.systemDebugPrintln("login");

        connect();

        try {
            Configure conf = Configure.getInstance();

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("task", "login");
            jsonObject.put("id", cubrid_cms_user_name);
            jsonObject.put("password", cubrid_cms_user_passwd);
            jsonObject.put("clientver", conf.clinetVersion);

            String jsonInputString = String.valueOf(jsonObject);
            Logger.systemDebugPrintln("jsonInputString : " + jsonInputString);

            if (httpConn == null) {
                Logger.println("login httpConn is null");
                return false;
            }
            OutputStream outStrem = httpConn.getOutputStream();
            outStrem.write(jsonInputString.getBytes("utf-8"));
            outStrem.flush();
            outStrem.close();

            BufferedReader bufferReader =
                    new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
            JSONObject object = (JSONObject) JSONValue.parse(bufferReader);
            Logger.systemDebugPrintln("jsonInputString3 object : " + object);
            loginToken = String.valueOf(object.get("token"));
            bufferReader.close();

            if (String.valueOf(object.get("status")).equals("success")) {
                Logger.println("login is Success");
                isLoginSuccess = true;
            } else {
                Logger.systemPrintln(
                        "connect failure, reason : " + String.valueOf(object.get("note")));
                    Logger.systemPrintln("Exit.");
                    System.exit(1);
            }

            int responseCode = httpConn.getResponseCode();
            Logger.systemDebugPrintln("https output responseCode : " + responseCode);

            if (200 == responseCode) {
                isHttpConnecting = true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean requestData(JSONObject requestJson) {

        boolean result = false;

        Logger.systemDebugPrintln("requestData task : " + String.valueOf(requestJson.get("task")));
        connect();

        try {

            if (httpConn == null) {
                Logger.println("requestData httpConn is null");
                return false;
            }

            String jsonInputString = String.valueOf(requestJson);
            OutputStream outStrem = httpConn.getOutputStream();
            outStrem.write(jsonInputString.getBytes("utf-8"));
            outStrem.flush();
            outStrem.close();

            BufferedReader bufferReader =
                    new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
            JSONObject object = (JSONObject) JSONValue.parse(bufferReader);
            bufferReader.close();

//            if (!String.valueOf(object.get("status")).equals("success")) {
//                // check Data
//                String responceCheck = String.valueOf(object);
//            }

            int responseCode = httpConn.getResponseCode();

            if (200 == responseCode) {
                isHttpConnecting = true;
            } else {
                return result;
            }

            if (String.valueOf(requestJson.get("task"))
                    .equals(CubridTask.TESK_GET_START_STATDUMP)) {
                if (String.valueOf(object.get("status")).equals("success")) {
                    isStratDump.put((String) requestJson.get("dbname"), true);
                    result = true;
                } else if (String.valueOf(object.get("note")).equals("already running")) {
                    isStratDump.put((String) requestJson.get("dbname"), true);
                    result = true;
                } 
            } else {
                result = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject requestResponceData(JSONObject requestJson) {

        JSONObject object = new JSONObject();

        Logger.systemDebugPrintln(
                "requestResponceData task : " + String.valueOf(requestJson.get("task")));

        connect();

        try {

            if (httpConn == null) {
                Logger.println("requestResponceData httpConn is null");
                object.clear();
                return object;
            }

            String jsonInputString = String.valueOf(requestJson);
            OutputStream outStrem = httpConn.getOutputStream();
            outStrem.write(jsonInputString.getBytes("utf-8"));
            outStrem.flush();
            outStrem.close();

            try {
                BufferedReader bufferReader =
                        new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
                object = (JSONObject) JSONValue.parse(bufferReader);
                bufferReader.close();
            } catch (java.net.SocketTimeoutException e) {
            	e.printStackTrace();
				object.clear();
				return object;
            }

            if (object == null || object.isEmpty()) {
            	return object;
            }
            
            if (!String.valueOf(object.get("status")).equals("success")) {
                object.clear();
                return object;
            }

            if (String.valueOf(object.get("note"))
                    .equals("Request is rejected due to invalid token. Please reconnect.")) {
                Logger.println("Request is rejected due to invalid token. Please reconnect.");
                object.clear();
                return object;
            }

            int responseCode = httpConn.getResponseCode();

            if (200 == responseCode) {
                // check again code
                isHttpConnecting = true;
            } else {
                object.clear();
                return object;
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (object != null) {
                object.clear();
            }
        }

        return object;
    }

    public static boolean isHttpConnect() {
        return isHttpConnecting;
    }

    private static void clearStatus() {
        isHttpConnecting = false;
        isStratDump.clear();
        isLoginSuccess = false;
    }

    public static boolean isStratDump(String dbName) {

        if (isStratDump != null) {
            if (isStratDump.get(dbName) == null) {
                return false;
            } else {
                return isStratDump.get(dbName);
            }
        } else {
            return false;
        }
    }

    public static boolean isLoginSuccess() {
        return isLoginSuccess;
    }

    public static String getLoginToken() {
        if (loginToken != null && !loginToken.isEmpty()) return loginToken;
        else return "notLogin";
    }

    public static void main(String[] args) throws Exception {

        boolean result;

        login();

        result = HttpConnect.requestData(CubridTask.cubridApi(CubridTask.TESK_GET_START_STATDUMP, "demodb"));

        if (result) {
        	new Thread(new Runnable() {

        		@Override
        		public void run() {
        			JSONObject jsonObject = new JSONObject();

        			while (true) {
        				jsonObject = requestResponceData(CubridTask.cubridApi(CubridTask.TESK_GET_STATDUMP_INFO, "demodb"));
        				Logger.systemPrintln(String.valueOf(jsonObject));

        			}

        		}
        	}).start();
        }
        
        result = HttpConnect.requestData(CubridTask.cubridApi(CubridTask.TESK_GET_STOP_STATDUMP, null));
    }
}
