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

import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.data.list.CubridStatusConstants;
import scouter.agent.cubrid.net.UdpSendData;
import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.lang.value.MapValue;

public class DbInfoWork {

    public static void activeDbInfo() {
        MapValue resultMap = CMServerInfo.getInstance().getActiveDb();

        JSONObject resultJson =
                HttpConnect.requestResponceData(
                        CubridTask.cubridApi(CubridTask.TESK_GET_START_INFO, null));
        if (resultJson != null && !resultJson.isEmpty()) {
            resultMap = jsonToOutData(resultJson);

            CMServerInfo.getInstance().setActiveDbAndCheckChanged(resultMap);
            UdpSendData.sendStatus(CubridStatusConstants.CUBRID_ACTIVE_DB_LIST, resultMap);
            Logger.systemDebugPrintln("activeDbInfo resultMap = " + resultMap.toString());
        }
    }

    private static MapValue jsonToOutData(JSONObject resultJson) {

        JSONArray jsonArray = new JSONArray();
        MapValue resultMap = new MapValue();
        String dbName;

        if (String.valueOf(resultJson.get("activelist")).equals("{}")) {
            Logger.println("active DB is not exist");
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(resultJson.get("activelist")));
                JSONObject obj = null;
                obj = (JSONObject) jsonArray.get(0);
                jsonArray = (JSONArray) parser.parse(String.valueOf(obj.get("active")));

                for (int i = 0; i < jsonArray.size(); i++) {
                    obj = (JSONObject) jsonArray.get(i);
                    Iterator<?> iter = obj.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        dbName = String.valueOf(obj.get(key));
                        resultMap.put(dbName, Configure.getInstance().getDbObjName(dbName));
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return resultMap;
    }
}
