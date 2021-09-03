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

import scouter.agent.cubrid.data.CMServerInfo;
import scouter.agent.cubrid.data.CubridTask;
import scouter.agent.cubrid.data.HostData;
import scouter.agent.cubrid.net.http.HttpConnect;

public class HostStatWork {

    private static Long cpuUserDelta = 0L;
    private static Long cpuKernelDelta = 0L;
    private static Long cpuIdleDelta = 0L;
    private static Long cpuIowaitDelta = 0L;
    private static Long cpuTotalDelta = 0L;

    private static HostData hostData;

    public static void start() {

        hostData = CMServerInfo.getInstance().hostData;
        getHoststat();
    }

    private static void getHoststat() {

        hostData.prvHostStatData = hostData.lastHostStatData;
        hostData.lastHostStatData =
                HttpConnect.requestResponceData(
                        CubridTask.cubridApi(CubridTask.TESK_GET_HOST_STAT, null));

        if (!hostData.prvHostStatData.isEmpty() && !hostData.lastHostStatData.isEmpty())
            setDeltaVal();
    }

    private static void setDeltaVal() {
        cpuUserDelta =
                Long.parseLong(hostData.lastHostStatData.get("cpu_user").toString())
                        - Long.parseLong(hostData.prvHostStatData.get("cpu_user").toString());
        cpuKernelDelta =
                Long.parseLong(hostData.lastHostStatData.get("cpu_kernel").toString())
                        - Long.parseLong(hostData.prvHostStatData.get("cpu_kernel").toString());
        cpuIdleDelta =
                Long.parseLong(hostData.lastHostStatData.get("cpu_idle").toString())
                        - Long.parseLong(hostData.prvHostStatData.get("cpu_idle").toString());
        cpuIowaitDelta =
                Long.parseLong(hostData.lastHostStatData.get("cpu_iowait").toString())
                        - Long.parseLong(hostData.prvHostStatData.get("cpu_iowait").toString());
        cpuTotalDelta = cpuUserDelta + cpuKernelDelta + cpuIdleDelta + cpuIowaitDelta;
    }

    public static Long getCpuUserDelta() {
        return cpuUserDelta;
    }

    public static Long getCpuKernelDelta() {
        return cpuKernelDelta;
    }

    public static Long getCpuIdleDelta() {
        return cpuIdleDelta;
    }

    public static Long getCpuIowaitDelta() {
        return cpuIowaitDelta;
    }

    public static Long getCpuTotalDelta() {
        return cpuTotalDelta;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DbInfoWork.activeDbInfo();
        getHoststat();
    }
}
