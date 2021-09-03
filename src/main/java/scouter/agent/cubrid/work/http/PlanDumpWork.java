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
import scouter.agent.cubrid.data.DbData;
import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.agent.cubrid.util.Util;

public class PlanDumpWork {

    public static boolean get() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            if (String.valueOf(getPlanDumpLoad(dbName).get("status")).equals("success")) {
                jsontoPlanData(dbName, getPlanDumpLoad(dbName));
            }
        }

        return true;
    }

    public static JSONObject getPlanDumpLoad(String dbName) {

        return HttpConnect.requestResponceData(
                CubridTask.cubridApi(CubridTask.TESK_GET_PLANDUMP_INFO, dbName));
    }

    private static void jsontoPlanData(String dbName, JSONObject resultJson) {

        if (resultJson == null) {
            Logger.println("PlanDumpWork jsonToPlanData resultJson is null");
        }

        JSONArray jsonArray = new JSONArray();
        // MapValue resultMap = new MapValue();
        String dumpLog = null;
        String xaslCache = null;
        String filterCache = null;

        if (!resultJson.get("status").equals("success")) {
            Logger.println("PlanDumpWork jsonToPlanData resultJson failed");
            return;
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(resultJson.get("log")));
                JSONObject obj = null;
                obj = (JSONObject) jsonArray.get(0);
                jsonArray = (JSONArray) parser.parse(String.valueOf(obj.get("line")));
                dumpLog = jsonArray.toString();
                dumpLog = dumpLog.replaceAll(" ", "");
                xaslCache =
                        Util.subStringBetween(
                                dumpLog, "\"XASLcache\"", "\"Filterpredicatecache\"");
                xaslCache = xaslCache.replaceAll("\"", "");
                filterCache =
                        Util.subStringBetween(
                                dumpLog, "\"Filterpredicatecache\"", "\"Entries:\"]");
                filterCache = filterCache.replaceAll("\"", "");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int xaslCacheHits = 0;
        int xaslCacheLookups = 0;
        int filterCacheHits = 0;
        int filterCacheLookups = 0;

        String[] xaslCacheTemp = xaslCache.split(",");
        String[] filterCacheTemp = filterCache.split(",");
        for (int i = 0; i < xaslCacheTemp.length; i++) {
            if (xaslCacheTemp[i].startsWith("Lookups")) {
                xaslCacheLookups = Integer.parseInt(xaslCacheTemp[i].replaceAll("Lookups:", ""));
            } else if (xaslCacheTemp[i].startsWith("Hits")) {
                xaslCacheHits = Integer.parseInt(xaslCacheTemp[i].replaceAll("Hits:", ""));
                break;
            }
        }

        for (int i = 0; i < filterCacheTemp.length; i++) {
            if (filterCacheTemp[i].startsWith("Lookups")) {
                filterCacheLookups =
                        Integer.parseInt(filterCacheTemp[i].replaceAll("Lookups:", ""));
            } else if (filterCacheTemp[i].startsWith("EntryHits")) {
                filterCacheHits = Integer.parseInt(filterCacheTemp[i].replaceAll("EntryHits:", ""));
                break;
            }
        }

        // Put Data
        DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);

        if (xaslCacheHits == 0 || xaslCacheLookups == 0) {
            dbData.planXASLHitRate = 0;
        } else {
            dbData.planXASLHitRate = (int) ((float) xaslCacheHits / xaslCacheLookups * 100);
        }

        if (filterCacheHits == 0 || filterCacheLookups == 0) {
            dbData.planFilterHitRate = 0;
        } else {
            dbData.planFilterHitRate = (int) ((float) filterCacheHits / filterCacheLookups * 100);
        }
    }
}
