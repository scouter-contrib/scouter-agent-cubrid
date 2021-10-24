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

import java.util.Hashtable;
import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.data.DbData;
import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.agent.cubrid.util.ConversionJson;

public class DbProcStatWork {

    private static Hashtable<String, DBProcStat> dbProcStatDelta = new Hashtable<>();

    public static void start() {
        // DbInfoWork.activeDbInfo();
        getDbProcStat();
    }

    private static void getDbProcStat() {

        for (String dbName : CMServerInfo.getInstance().getActiveDb().keySet()) {
            DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);
            dbData.prvDbProcStatData = dbData.lastDbProcStatData;

            dbData.lastDbProcStatData =
                    HttpConnect.requestResponceData(
                            CubridTask.cubridApi(CubridTask.TESK_GET_DB_PROC_STAT, dbName));

            if (!dbData.lastDbProcStatData.isEmpty()) {

                dbData.lastDbProcStatData =
                        ConversionJson.searchAndToJson("dbstat", dbData.lastDbProcStatData);

                if (!dbData.prvDbProcStatData.isEmpty()) {
                    setDeltaVal(dbName);
                }
            }
        }
    }

    private static void setDeltaVal(String dbName) {
        DbData dbData = CMServerInfo.getInstance().activeDbData.get(dbName);
        DBProcStat dbProcStat = new DBProcStat();
        dbProcStat.setCpuUserDelta(
                Long.parseLong(String.valueOf(dbData.lastDbProcStatData.get("cpu_user")))
                        - Long.parseLong(String.valueOf(dbData.prvDbProcStatData.get("cpu_user"))));
        dbProcStat.setCpuKernelDelta(
                Long.parseLong(String.valueOf(dbData.lastDbProcStatData.get("cpu_kernel")))
                        - Long.parseLong(String.valueOf(dbData.prvDbProcStatData.get("cpu_kernel"))));
        dbProcStatDelta.put(dbName, dbProcStat);
    }

    public static Long getCpuUserDelta(String dbName) {
        if (dbProcStatDelta != null && !dbProcStatDelta.isEmpty()) {
            if (dbProcStatDelta.get(dbName) != null)
                return dbProcStatDelta.get(dbName).getCpuUserDelta();
        }
        return 0L;
    }

    public static Long getCpuKernelDelta(String dbName) {
        if (dbProcStatDelta != null && !dbProcStatDelta.isEmpty()) {
            if (dbProcStatDelta.get(dbName) != null) {
                return dbProcStatDelta.get(dbName).getCpuKernelDelta();
            }
        }
        return 0L;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DbInfoWork.activeDbInfo();
        getDbProcStat();
    }
}

class DBProcStat {
    private Long cpuUserDelta = 0L;
    private Long cpuKernelDelta = 0L;

    public Long getCpuUserDelta() {
        return cpuUserDelta;
    }

    public void setCpuUserDelta(Long val) {
        cpuUserDelta = val;
    }

    public Long getCpuKernelDelta() {
        return cpuKernelDelta;
    }

    public void setCpuKernelDelta(Long val) {
        cpuKernelDelta = val;
    }
}
