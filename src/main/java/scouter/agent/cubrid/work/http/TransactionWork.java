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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.lang.value.MapValue;

public class TransactionWork {

    private static JSONObject responseData;

    public static void start() {
        getTransaction();
    }

    public static void getTransaction() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            responseData =
                    HttpConnect.requestResponceData(
                            CubridTask.cubridApi(CubridTask.TESK_GET_TRANSACTION_INFO, dbName));

            if (!responseData.isEmpty()) {
            	parsingTransaction(responseData);
            }

        }
    }

    private static void parsingTransaction(JSONObject responseJson) {

        JSONArray jsonArray;
        JSONObject obj = null;
        MapValue newMap = new MapValue();

        if (responseJson.get("transactioninfo") == null
                || String.valueOf(responseJson.get("transactioninfo")).equals("{}")) {
            Logger.systemPrintln(
                    "transaction( "
                            + String.valueOf(responseJson.get("dbname"))
                            + ") is not exist");
            CMServerInfo.getInstance()
                    .transactionInfo
                    .put(String.valueOf(responseJson.get("dbname")), newMap);
            responseJson.clear();
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(responseJson.get("transactioninfo")));
                obj = (JSONObject) jsonArray.get(0);
                jsonArray = (JSONArray) parser.parse(String.valueOf(obj.get("transaction")));
                for (int i = 0; i < jsonArray.size(); i++) {
                    obj = (JSONObject) jsonArray.get(i);
                    obj.put("dbname", String.valueOf(responseJson.get("dbname")));
                    newMap.put(String.valueOf(i), String.valueOf(obj));
                }
                CMServerInfo.getInstance().transactionInfo.put(String.valueOf(responseJson.get("dbname")), newMap);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DbInfoWork.activeDbInfo();
        getTransaction();
    }
}
