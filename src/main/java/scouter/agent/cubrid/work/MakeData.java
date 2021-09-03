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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.DbData;
import scouter.agent.cubrid.data.list.PrefDataList;
import scouter.agent.cubrid.data.list.StatusServerInfoList;
import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.agent.cubrid.work.http.BrokerInfoWork;
import scouter.agent.cubrid.work.http.DbInfoWork;
import scouter.agent.cubrid.work.http.DbProcStatWork;
import scouter.agent.cubrid.work.http.DbSpaceInfo;
import scouter.agent.cubrid.work.http.HaStatus;
import scouter.agent.cubrid.work.http.HostStatWork;
import scouter.agent.cubrid.work.http.PlanDumpWork;
import scouter.agent.cubrid.work.http.StatDumpWork;
import scouter.agent.cubrid.work.http.TransactionWork;
import scouter.agent.cubrid.work.http.ViewLogWork;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;

public class MakeData {
	
	private static final String AVG_COUNT = "avg_count";
	
	private static List<String> avgList = Arrays.asList(
			PrefDataList.CPU_USED,
			PrefDataList.XASL_PLAN_HIT_RATE,
			PrefDataList.FILTER_PLAN_HIT_RATE);
	
    public static void make() {
    	
        if (!HttpConnect.isLoginSuccess()) {
            if (!HttpConnect.login()) {
                Logger.systemPrintln("CMS Login Error Check Server");
                return;
            }
        }

        getCMSData();
        makeServerInfo();
        makeToPrefData();
    }

    private static void getCMSData() {
        DbInfoWork.activeDbInfo();
        HostStatWork.start();
        TransactionWork.start();
        DbProcStatWork.start();
        StatDumpWork.get();
        BrokerInfoWork.getBrokerInfo();
        DbSpaceInfo.get();
        PlanDumpWork.get();
    }

    public static boolean makeToPrefData() {

        DbData deltaData;
        CMServerInfo cmServerInfo = CMServerInfo.getInstance();
        
        for (String dbName : cmServerInfo.getActiveDb().keySet()) {
            deltaData = cmServerInfo.activeDbData.get(dbName);
            MapValue dailyPerfData = cmServerInfo.prefDataDaily.get(dbName);
            if (dailyPerfData == null || dailyPerfData.isEmpty()) {
                dailyPerfData = new MapValue();
                dailyPerfData.put(AVG_COUNT, 0);
                cmServerInfo.prefDataDaily.put(dbName, dailyPerfData);
            }            
            MapValue perfData = new MapValue();

            try {
                // delta DML DATA
                perfData.put(
                        PrefDataList.QUERY_INSERT,
                        getStatdumpDelta(PrefDataList.QUERY_INSERT, deltaData));
                perfData.put(
                        PrefDataList.QUERY_UPDATE,
                        getStatdumpDelta(PrefDataList.QUERY_INSERT, deltaData));
                perfData.put(
                        PrefDataList.QUERY_DELETE,
                        getStatdumpDelta(PrefDataList.QUERY_INSERT, deltaData));
                perfData.put(
                        PrefDataList.QUERY_SELECT,
                        getStatdumpDelta(PrefDataList.QUERY_INSERT, deltaData));
                perfData.put(
                        PrefDataList.DATA_PAGE_IO_WRITES,
                        getStatdumpDelta(PrefDataList.DATA_PAGE_IO_WRITES, deltaData));
                perfData.put(
                        PrefDataList.DATA_PAGE_IO_READS,
                        getStatdumpDelta(PrefDataList.DATA_PAGE_IO_READS, deltaData));
                perfData.put(
                        PrefDataList.DATA_PAGE_FETCHES,
                        getStatdumpDelta(PrefDataList.DATA_PAGE_FETCHES, deltaData));
                perfData.put(
                        PrefDataList.DATA_PAGE_DIRTIES,
                        getStatdumpDelta(PrefDataList.DATA_PAGE_DIRTIES, deltaData));
                perfData.put(
                        PrefDataList.DATA_BUFFER_HIT_RATIO,
                        getStatdumpDelta(PrefDataList.DATA_BUFFER_HIT_RATIO, deltaData));
                perfData.put(
                        PrefDataList.QUERY_SSCANS,
                        getStatdumpDelta(PrefDataList.QUERY_SSCANS, deltaData));
                perfData.put(
                        PrefDataList.SORT_IO_PAGE,
                        getStatdumpDelta(PrefDataList.SORT_IO_PAGE, deltaData));
                perfData.put(
                        PrefDataList.SORT_DATA_PAGE,
                        getStatdumpDelta(PrefDataList.SORT_DATA_PAGE, deltaData));

                // Normal Data
                perfData.put(PrefDataList.ACTVIE_SESSION, getActiveSession(dbName));
                perfData.put(PrefDataList.CPU_USED, calcCpuUsed(dbName));
                perfData.put(PrefDataList.LOCK_WAIT_SESSIONS, getLockWaitSession(dbName));

                perfData.put(PrefDataList.TOTAL_QPS, BrokerInfoWork.getQps());
                perfData.put(PrefDataList.TOTAL_TPS, BrokerInfoWork.getTps());
                perfData.put(PrefDataList.TOTAL_ERROR_QUERY, BrokerInfoWork.getErrorQuery());

                perfData.put(PrefDataList.XASL_PLAN_HIT_RATE, deltaData.planXASLHitRate);
                perfData.put(PrefDataList.FILTER_PLAN_HIT_RATE, deltaData.planFilterHitRate);

                // For Percent Data
                dailyPerfData.put(AVG_COUNT, dailyPerfData.getInt(AVG_COUNT) + 1);
                
                for (String key : perfData.keySet()) {
                	// For Percent Data
            		if (key.equals(avgList.get(0)) || key.equals(avgList.get(1))
            				|| key.equals(avgList.get(2))) {
            			dailyPerfData.put(key+"_sum", (dailyPerfData.getInt(key+"_sum") + perfData.getInt(key)));
            			dailyPerfData.put(key, (dailyPerfData.getInt(key+"_sum")) / dailyPerfData.getInt(AVG_COUNT));
            		} else {
            			dailyPerfData.put(key, dailyPerfData.getInt(key) + perfData.getInt(key));
            		}
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }

            CMServerInfo.getInstance().prefData.put(dbName, perfData);
        }

        return true;
    }

    private static long getStatdumpDelta(String item, DbData deltaData) {

        long prvTemp = 0;
        long NowTemp = 0;

        if (deltaData.prvStatDumpData == null || deltaData.lastStatDumpData == null) {
            Logger.println("getDeltaValue data is null");
            return 0;
        }

        if (!deltaData.prvStatDumpData.isEmpty() && !deltaData.lastStatDumpData.isEmpty()) {
            try {
                prvTemp = Long.parseLong(deltaData.prvStatDumpData.get(item).toString());
                NowTemp = Long.parseLong(deltaData.lastStatDumpData.get(item).toString());
            } catch (NullPointerException e) {
                e.printStackTrace();
                return 0;
            }
        }

        return NowTemp - prvTemp;
    }

    private static void makeServerInfo() {
        MapValue mapValue;
        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            CMServerInfo.getInstance().serverInfoData.put(dbName, new MapValue());
            mapValue = CMServerInfo.getInstance().serverInfoData.get(dbName);
            mapValue.put(StatusServerInfoList.DB_NAME, dbName);
            mapValue.put(StatusServerInfoList.IP, Configure.getInstance().cubrid_cms_ip);
            mapValue.put(StatusServerInfoList.CPU_USED, Integer.toString(calcCpuUsed(dbName)));
            mapValue.put(StatusServerInfoList.ACTVIE_SESSION, Integer.toString(getActiveSession(dbName)));
            mapValue.put(StatusServerInfoList.LOCK_WAIT_SESSIONS, Integer.toString(getLockWaitSession(dbName)));
            
            MapValue deltaData = CMServerInfo.getInstance().activeDbData.get(dbName).dbSpaceData;
            if (deltaData != null) {
                mapValue.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_TOTAL, deltaData.get(StatusServerInfoList.DBSPACE_PERM_AND_PERM_TOTAL));
                mapValue.put(StatusServerInfoList.DBSPACE_PERM_AND_PERM_USED, deltaData.get(StatusServerInfoList.DBSPACE_PERM_AND_PERM_USED));
                mapValue.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_TOTAL, deltaData.get(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_TOTAL));
                mapValue.put(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_USED, deltaData.get(StatusServerInfoList.DBSPACE_PERM_AND_TEMP_USED));
                mapValue.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_TOTAL, deltaData.get(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_TOTAL));
                mapValue.put(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_USED, deltaData.get(StatusServerInfoList.DBSPACE_TEMP_AND_TEMP_USED));
            }
        }
    }

    private static int calcCpuUsed(String dbName) {
        double cpuUserDelta = DbProcStatWork.getCpuUserDelta(dbName);
        double cpuTotalDelta = HostStatWork.getCpuTotalDelta();
        return (int) ((cpuUserDelta / cpuTotalDelta * 100) + 0.5);
    }

    private static int getActiveSession(String dbName) {
    	int ret = 0;
        if (CMServerInfo.getInstance().transactionInfo != null
                && !CMServerInfo.getInstance().transactionInfo.isEmpty()) {
        	ret = CMServerInfo.getInstance().transactionInfo.get(dbName).size();
            return ret;
        }
        return 0;
    }

    private static int getLockWaitSession(String dbName) {
        int count = 0;
        if (CMServerInfo.getInstance().transactionInfo != null
                && !CMServerInfo.getInstance().transactionInfo.isEmpty()) {
            Hashtable<String, MapValue> transaction = CMServerInfo.getInstance().transactionInfo;
            for (String index : transaction.get(dbName).keySet()) {
            	JSONParser parser = new JSONParser();
            	try {
            		JSONObject jsonObj = (JSONObject) parser.parse(String.valueOf(transaction.get(dbName).getText(index)));
            		if (!jsonObj.get("wait_for_lock_holder").toString().equals("-1")) {
                			count++;
                		}
                	} catch (ParseException e) {
                        e.printStackTrace();
                    }
            
            }
        }
        return count;
    }
}
