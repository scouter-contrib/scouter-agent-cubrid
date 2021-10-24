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
package scouter.agent.cubrid.work;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scouter.agent.cubrid.AlertConfigure;
import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.CubridWorker;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.list.CubridStatusConstants;
import scouter.agent.cubrid.data.list.WorkConstants;
import scouter.agent.cubrid.net.UdpSendData;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;

public class SendDataCollector {

	private static boolean prefDataSkip = true;
	public static Hashtable<String, MapValue> alertPrefDataSkipCount = new Hashtable<>();
	public static final int ALERT_SKIP_COUNT = 10;
	
    public static void sendServerInfo() {

        for (String key : CMServerInfo.getInstance().serverInfoData.keySet()) {
            UdpSendData.sendStatus(
                    CubridStatusConstants.CUBRID_DB_SERVER_INFO + key,
                    CMServerInfo.getInstance().serverInfoData.get(key));
        }
    }

    public static void sendTransactionInfo() {
    	Hashtable<String, MapValue> tranSactionInfo = CMServerInfo.getInstance().transactionInfo;
    	MapValue sendData = new MapValue();

    	float query_time, tran_time = 0;
    	String sql_id;
    	
    	for (String dbName : tranSactionInfo.keySet()) {
          ListValue pidLv = sendData.newList("pid");
        	ListValue sqlIdLv = sendData.newList("SQL_ID");
        	ListValue sqlTextLv = sendData.newList("SQL_Text");
        	ListValue userLv = sendData.newList("user");
        	ListValue hostLv = sendData.newList("host");
        	ListValue programLv = sendData.newList("program");
        	ListValue queryTimeLv = sendData.newList("query_time");
        	ListValue tranTimeLv = sendData.newList("tran_time");
        	
        	for (String index : tranSactionInfo.get(dbName).keySet()) {
              JSONParser parser = new JSONParser();
        		  try {
            	    JSONObject obj = (JSONObject) parser.parse(String.valueOf(tranSactionInfo.get(dbName).get(index)));
                  query_time = Float.parseFloat(String.valueOf(obj.get("query_time")));
                  tran_time = Float.parseFloat(String.valueOf(obj.get("tran_time")));
                  sql_id = String.valueOf(obj.get("SQL_ID"));
                  if ( (query_time > 3 || tran_time > 3) && !sql_id.equals("empty")) {
                      pidLv.add(String.valueOf(obj.get("pid")));
                    	if (obj.get("SQL_Text") != null) {
                    	    sqlTextLv.add(String.valueOf(obj.get("SQL_Text")));
                      } else {
                    	    sqlTextLv.add("not support on CMS version");
                      }
                      userLv.add(String.valueOf(obj.get("@user")));
                      hostLv.add(String.valueOf(obj.get("host")));
                      programLv.add(String.valueOf(obj.get("program")));
                    	sqlIdLv.add(sql_id);
                    	queryTimeLv.add(query_time);
                    	tranTimeLv.add(tran_time);
                      }
        		  }
        		  catch (ParseException e) {
                	e.printStackTrace();
        		  }
              }
        	  UdpSendData.sendStatus(CubridStatusConstants.CUBRID_DB_TRANSACTION_INFO + dbName, sendData);
        	  sendData.clear();
    	}
    }
    
    public static void sendDbPrefData() {

    	if (prefDataSkip) {
    		prefDataSkip = false;
    		return;
    	}
    		
        for (String dbName : CMServerInfo.getInstance().prefData.keySet()) {
            UdpSendData.sendPerfForMoreDb(CMServerInfo.getInstance().prefData.get(dbName), dbName);
            sendWarningAlert(CMServerInfo.getInstance().prefData.get(dbName), dbName);
            UdpSendData.sendDailyPerfForMoreDb(
                    CMServerInfo.getInstance().prefDataDaily.get(dbName), dbName);
        }

        if (CubridWorker.getDailyCount() == WorkConstants.MAX_DAILY_COUNT) {
            Logger.systemDebugPrintln("dailyCounter and data clear");
            CMServerInfo.getInstance().prefDataDaily.clear();
        }
    }
    
    private static void sendWarningAlert(MapValue map, String dbname) {
    	AlertConfigure conf = AlertConfigure.getInstance();
    	long currentVal;
    	long warningVal;
    	int delayVal;
    	
    	MapValue delayCheckMap = null;
    	if (alertPrefDataSkipCount.get(dbname) == null) {
    		alertPrefDataSkipCount.put(dbname, new MapValue());
    	}
    	
    	delayCheckMap = alertPrefDataSkipCount.get(dbname);
    	
    	for (String key : map.keySet()) {
    		currentVal = map.getLong(key);
    		warningVal = conf.getLong(key, 0);
    		if (warningVal > 0 && currentVal > warningVal) {
    			if (delayCheckMap.getInt(key) == 0) {
        			UdpSendData.sendAlert((byte)1, "CUBRID PreferenceData", 
        					"[" + dbname + "] " + key + "(" + currentVal + ") " + "is greater than warning value (" + warningVal + ")", null);
    			}
    			
    			if (delayCheckMap.getInt(key) == ALERT_SKIP_COUNT) {
    				delayCheckMap.put(key, 0);
    			} else {
    				delayVal = delayCheckMap.getInt(key) + 1;
    				delayCheckMap.put(key, delayVal);
    			}
    		}
    	}
    }
}

