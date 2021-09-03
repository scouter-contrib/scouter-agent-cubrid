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

import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.data.DbData;
import scouter.agent.cubrid.net.http.HttpConnect;

public class StatDumpWork {

    public static boolean get() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);
            dbData.prvStatDumpData = dbData.lastStatDumpData;
            dbData.lastStatDumpData = getStatDumpLoad(dbName);
            Logger.systemDebugPrintln(
                    "TESK_GET_STATDUMP_INFO resultJson : " + dbData.lastStatDumpData);
            if (dbData.lastStatDumpData == null || dbData.lastStatDumpData.isEmpty()) {
                dbData.lastStatDumpData = dbData.prvStatDumpData;
                Logger.println("dbData.lastStatDumpData is null get data fail");
                return false;
            }
        }

        return true;
    }

    public static boolean stopDump() {
        boolean result = false;

        if (!HttpConnect.isLoginSuccess()) {
            result = HttpConnect.login();
        }

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
        	result = HttpConnect.requestData(CubridTask.cubridApi(CubridTask.TESK_GET_STOP_STATDUMP, dbName));
        }

        return result;
    }

    public static boolean startDump(String dbName) {
        
        Configure conf = Configure.getInstance();
        boolean result = false;
        
        if (!HttpConnect.isLoginSuccess()) {
            result = HttpConnect.login();
        }

        if (!HttpConnect.isStratDump(dbName)) {
            result =
                    HttpConnect.requestData(
                            CubridTask.cubridApi(CubridTask.TESK_GET_START_STATDUMP, dbName + "@" + conf.cubrid_cms_ip));
        }

        return result;
    }

    public static JSONObject getStatDumpLoad(String dbName) {

        if (!HttpConnect.isLoginSuccess()) {
            if (!HttpConnect.login()) {
                Logger.println("getStatDumpLoad CMS Login Error Check Server");
            }
        }

        if (HttpConnect.isStratDump(dbName)) {
            return HttpConnect.requestResponceData(
                    CubridTask.cubridApi(CubridTask.TESK_GET_STATDUMP_INFO, dbName));
        } else {
            if (startDump(dbName)) {
                return HttpConnect.requestResponceData(
                        CubridTask.cubridApi(CubridTask.TESK_GET_STATDUMP_INFO, dbName));
            }
        }

        return null;
    }
}
