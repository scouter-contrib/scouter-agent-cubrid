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
package scouter.agent.cubrid.data;

import org.json.simple.JSONObject;
import scouter.agent.cubrid.net.http.HttpConnect;

public class CubridTask {
    public static final String TESK_LOGIN = "login";
    public static final String TESK_GET_TRANSACTION_INFO = "gettransactioninfo";
    public static final String TESK_GET_STATDUMP_INFO = "statdump";
    public static final String TESK_GET_PLANDUMP_INFO = "plandump";
    public static final String TESK_GET_START_STATDUMP = "start_statdump";
    public static final String TESK_GET_STOP_STATDUMP = "stop_statdump";

    public static final String TESK_GET_BROKER_DIAG_DATA = "getbrokerdiagdata";
    public static final String TESK_GET_BROKER_INFO = "getbrokersinfo";
    public static final String TESK_GET_BROKER_STATUS = "getbrokerstatus";
    public static final String TESK_GET_ADD_BROKER_INFO = "getaddbrokerinfo";

    public static final String TESK_GET_DB_SPACE_INFO = "dbspaceinfo";
    public static final String TESK_GET_DB_SPACE = "dbspace";

    public static final String TESK_GET_START_INFO = "startinfo";

    public static final String TESK_GET_DB_PROC_STAT = "getdbprocstat";

    public static final String TESK_GET_HOST_STAT = "gethoststat";

    public static final String TESK_GET_HA_STATUS = "ha_status";
    public static final String TESK_GET_HA_APPLY_INFO = "gethaapplyinfo";

    public static final String TESK_GET_LOG_FILE_INFO = "getlogfileinfo";
    public static final String TESK_GET_LOG_INFO = "getloginfo";
    public static final String TESK_GET_VIEW_LOG = "viewlog";

    @SuppressWarnings("unchecked")
    public static JSONObject cubridApi(String task, String ActiveDBName) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("task", task);
        requestJson.put("token", HttpConnect.getLoginToken());

        switch (task) {
            case CubridTask.TESK_GET_TRANSACTION_INFO:
                requestJson.put("dbname", ActiveDBName);
                requestJson.put("dbuser", HttpConnect.cubrid_db_user_name);
                requestJson.put("dbpasswd", HttpConnect.cubrid_db_user_passwd);
                break;
            case CubridTask.TESK_GET_STATDUMP_INFO:
            case CubridTask.TESK_GET_DB_PROC_STAT:
            case CubridTask.TESK_GET_DB_SPACE_INFO:
            case CubridTask.TESK_GET_LOG_INFO:
                requestJson.put("dbname", ActiveDBName);
                break;
            case CubridTask.TESK_GET_START_STATDUMP:
                requestJson.put("dbname", ActiveDBName);
                requestJson.put("dbuser", HttpConnect.cubrid_db_user_name);
                requestJson.put("dbpasswd", HttpConnect.cubrid_db_user_passwd);
                requestJson.put("interval", 5);
                break;
            case CubridTask.TESK_GET_STOP_STATDUMP:
                requestJson.put("dbname", ActiveDBName);
                requestJson.put("dbuser", HttpConnect.cubrid_db_user_name);
                requestJson.put("dbpasswd", HttpConnect.cubrid_db_user_passwd);
                break;
            case CubridTask.TESK_GET_BROKER_DIAG_DATA:
            case CubridTask.TESK_GET_BROKER_INFO:
            case CubridTask.TESK_GET_DB_SPACE:
            case CubridTask.TESK_GET_START_INFO:
            case CubridTask.TESK_GET_HOST_STAT:
            case CubridTask.TESK_GET_HA_STATUS:
                break;
            case CubridTask.TESK_GET_BROKER_STATUS:
                requestJson.put("dbname", ActiveDBName);
                requestJson.put("dbuser", HttpConnect.cubrid_db_user_name);
                requestJson.put("dbpasswd", HttpConnect.cubrid_db_user_passwd);
                break;
            case CubridTask.TESK_GET_LOG_FILE_INFO:
                // requestJson.put("broker", dbBrokerName);
                break;
            case CubridTask.TESK_GET_VIEW_LOG:
                requestJson.put("dbname", "demodb");
                requestJson.put("path", "$CUBRID/log/manager/cub_manager.err");
                requestJson.put("start", "1");
                requestJson.put("end", "1000");
                break;
            case CubridTask.TESK_GET_PLANDUMP_INFO:
                requestJson.put("dbname", ActiveDBName);
                requestJson.put("plandrop", "n");
            default:
                break;
        }

        return requestJson;
    }

}
