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

import scouter.util.DateUtil;

public class WorkConstants {
    public static final int ALIVE_INTERVAL_SECOND = 5;
    public static final int DATA_INTERVAL_MILLI_SECOND = 5000;
    public static final int MAX_DAILY_COUNT =
            ((int) DateUtil.MILLIS_PER_FIVE_MINUTE / WorkConstants.DATA_INTERVAL_MILLI_SECOND) - 1;
    // public static final int MAX_DAILY_COUNT = (30 / 5) -1;
}
