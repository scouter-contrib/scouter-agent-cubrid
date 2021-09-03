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
package scouter.agent.cubrid.work.http;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.net.http.HttpConnect;

public class BrokerInfoWork {

    private static JSONObject resultJson;
    public static ArrayList<String> brokerList = new ArrayList<>();
    private static Long prvTotalTps = 0L;
    private static Long lastTotalTps = 0L;
    private static Long prvTotalQps = 0L;
    private static Long lastTotalQps = 0L;
    private static Long prvTotalErrorQuery = 0L;
    private static Long lastTotalErrorQuery = 0L;

    public static void getBrokerInfo() {
        JSONObject requtestMsg;

        resultJson =
                HttpConnect.requestResponceData(
                        CubridTask.cubridApi(CubridTask.TESK_GET_BROKER_INFO, null));

        if (resultJson == null || resultJson.isEmpty()) {
            Logger.println("TESK_GET_BROKER_INFO error return");
            return;
        }

        jsonToBrokerList(resultJson);

        if (!brokerList.isEmpty()) {

            prvTotalTps = lastTotalTps;
            prvTotalQps = lastTotalQps;
            prvTotalErrorQuery = lastTotalErrorQuery;
            lastTotalTps = 0L;
            lastTotalQps = 0L;
            lastTotalErrorQuery = 0L;

            for (int i = 0; i < brokerList.size(); i++) {
                requtestMsg = CubridTask.cubridApi(CubridTask.TESK_GET_BROKER_STATUS, null);
                requtestMsg.put("bname", String.valueOf(brokerList.get(i)));
                resultJson = HttpConnect.requestResponceData(requtestMsg);
                if (resultJson.isEmpty()) {
                    Logger.println("TESK_GET_BROKER_STATUS error return");
                    return;
                }
                jsonToQpsAndTps(resultJson);
            }
        }
    }

    private static void jsonToBrokerList(JSONObject resultJson) {

        JSONArray jsonArray = new JSONArray();
        // MapValue resultMap = new MapValue();
        String brokerName;
        brokerList.clear();

        if (String.valueOf(resultJson.get("brokersinfo")).equals("{}")) {
            Logger.println("active DB is not exist");
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(resultJson.get("brokersinfo")));
                JSONObject obj = null;
                obj = (JSONObject) jsonArray.get(0);
                jsonArray = (JSONArray) parser.parse(String.valueOf(obj.get("broker")));

                for (int i = 0; i < jsonArray.size(); i++) {
                    obj = (JSONObject) jsonArray.get(i);
                    Iterator<?> iter = obj.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        if (key.equals("name")) {
                            brokerName = String.valueOf(obj.get(key));
                            brokerList.add(brokerName);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void jsonToQpsAndTps(JSONObject resultJson) {

        JSONArray jsonArray = new JSONArray();

        if (String.valueOf(resultJson.get("asinfo")).equals("{}")) {
            Logger.println("active DB is not exist");
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(resultJson.get("asinfo")));
                JSONObject obj = null;
                if (jsonArray == null) {
                    if (resultJson != null) {
                        Logger.println(
                                "jsonToQpsAndTps jsonArray is null return, resultJson : "
                                        + resultJson);
                    }
                    Logger.println("jsonToQpsAndTps jsonArray is null return");
                    return;
                } 
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    obj = (JSONObject) jsonArray.get(i);
                    Iterator<?> iter = obj.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        if (key.equals("as_num_query")) {
                            lastTotalQps += Long.valueOf(String.valueOf(obj.get(key)));
                        }

                        if (key.equals("as_num_tran")) {
                            lastTotalTps += Long.valueOf(String.valueOf(obj.get(key)));
                        }
                        
                        if (key.equals("as_error_query")) {
                        	lastTotalErrorQuery += Long.valueOf(String.valueOf(obj.get(key)));
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static Long getQps() {
        return lastTotalQps - prvTotalQps;
    }

    public static Long getTps() {
        return lastTotalTps - prvTotalTps;
    }
    
    public static Long getErrorQuery() {
        return lastTotalErrorQuery - prvTotalErrorQuery;
    }
}
