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
package scouter.agent.cubrid.data.list;

public class PrefDataList {
    public static final String QUERY_SELECT = "num_query_selects";
    public static final String QUERY_INSERT = "num_query_inserts";
    public static final String QUERY_DELETE = "num_query_deletes";
    public static final String QUERY_UPDATE = "num_query_updates";

    public static final String ACTVIE_SESSION = "active_session";
    public static final String CPU_USED = "cpu_used";
    public static final String LOCK_WAIT_SESSIONS = "lock_wait_sessions";

	//statdump
    public static final String DATA_PAGE_IO_WRITES = "num_data_page_iowrites";
    public static final String DATA_PAGE_IO_READS = "num_data_page_ioreads";
    public static final String DATA_PAGE_FETCHES = "num_data_page_fetches";
	public static final String DATA_PAGE_DIRTIES = "num_data_page_dirties";
	
    public static final String DATA_BUFFER_HIT_RATIO = "data_page_buffer_hit_ratio";
    public static final String QUERY_SSCANS = "num_query_sscans";
    public static final String SORT_IO_PAGE = "num_sort_io_pages";
    public static final String SORT_DATA_PAGE = "num_sort_data_pages";

	//broker info
    public static final String TOTAL_TPS = "as_num_tran";
    public static final String TOTAL_QPS = "as_num_query";
    public static final String TOTAL_ERROR_QUERY = "as_error_query";

    // plandump data
    public static final String XASL_PLAN_HIT_RATE = "xasl_plan_hit_rate";
    public static final String FILTER_PLAN_HIT_RATE = "filter_plan_hit_rate";
	
    public static final String DATA_DB_NAME = "db_name"; // temp
}
