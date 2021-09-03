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
import scouter.agent.cubrid.data.list.StatusServerInfoList;
import scouter.agent.cubrid.net.http.HttpConnect;

public class DbSpaceInfo {
	
	public static final long MEGA_BYTE = 1048576;
	
    public static boolean get() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            JSONObject resultJson = getDbspaceInfo(dbName);

            if (resultJson == null || resultJson.isEmpty()) {
            	Logger.println("DbSpaceInfo get resultJson is empty");
            	return false;
            }
            
            jsontoDbSpaceData(dbName, resultJson);
            
        }

        return true;
    }

    public static JSONObject getDbspaceInfo(String dbName) {

        return HttpConnect.requestResponceData(
                CubridTask.cubridApi(CubridTask.TESK_GET_DB_SPACE_INFO, dbName));
    }
    
    private static void jsontoDbSpaceData(String dbName, JSONObject resultJson) {

        if (resultJson == null) {
            Logger.systemPrintln("jsontoDbSpaceData resultJson is null");
            return;
        } 

        DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);
        dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_TOTAL, "0");
		dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_USED, "0");
		dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_TOTAL, "0");
		dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_USED, "0");
		dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_TOTAL, "0");
		dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_TOTAL, "0");
        JSONArray jsonArray = new JSONArray();

        if (!resultJson.get("status").equals("success")) {
            Logger.println("jsonToPlanData resultJson failed");
            return;
        } else {
        	long pageSize = Long.parseLong(resultJson.get("pagesize").toString());
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(resultJson.get("dbinfo")));
                JSONObject obj = null;
                String tempVal = "";
                if (jsonArray != null ) {
                	for (int i=0 ; i < jsonArray.size() ; i++) {
                		obj = (JSONObject) jsonArray.get(i);
                		if (obj.get("type").equals("PERMANENT") && obj.get("purpose").equals("PERMANENT")) {
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("total_size").toString()) / MEGA_BYTE); 
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_TOTAL, tempVal);
                			
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("used_size").toString()) / MEGA_BYTE);
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_USED, tempVal);
                		} else if (obj.get("type").equals("PERMANENT") && obj.get("purpose").equals("TEMPORARY")) {
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("total_size").toString()) / MEGA_BYTE);
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_TOTAL, tempVal);
                			
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("used_size").toString()) / MEGA_BYTE);
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_USED, tempVal);
                		} else if (obj.get("type").equals("TEMPORARY") && obj.get("purpose").equals("TEMPORARY")) {
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("total_size").toString()) / MEGA_BYTE);
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_TOTAL,tempVal);
                			
                			tempVal = Long.toString(pageSize * Long.parseLong(obj.get("used_size").toString()) / MEGA_BYTE);
                			dbData.dbSpaceData.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_USED, tempVal);
                		}
                	}
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    
}

