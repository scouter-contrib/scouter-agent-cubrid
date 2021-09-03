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

import org.json.simple.JSONObject;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.net.http.HttpConnect;

public class ViewLogWork {

    public static boolean get() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            //DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);
            JSONObject resultJson = getViewLog(dbName);
            Logger.systemDebugPrintln("ViewLogWork resultJson : " + resultJson);
        }

        return true;
    }

    public static JSONObject getViewLog(String dbName) {

        return HttpConnect.requestResponceData(
                CubridTask.cubridApi(CubridTask.TESK_GET_LOG_INFO, dbName));
    }

    public static JSONObject getLogInfo() {

        if (!BrokerInfoWork.brokerList.isEmpty()) {
            for (int i = 0; i < BrokerInfoWork.brokerList.size(); i++) {
                Logger.systemDebugPrintln(
                        "TESK_GET_DB_SPACE_INFO String.valueOf(BrokerInfoWork.brokerList.get(i)) : "
                                + String.valueOf(BrokerInfoWork.brokerList.get(i)));
                JSONObject resultJson =
                        HttpConnect.requestResponceData(
                                CubridTask.cubridApi(
                                        CubridTask.TESK_GET_VIEW_LOG,
                                        String.valueOf(BrokerInfoWork.brokerList.get(i))));
                Logger.systemDebugPrintln("TESK_GET_DB_SPACE_INFO resultJson : " + resultJson);
            }
        }

        return null;
    }
}
