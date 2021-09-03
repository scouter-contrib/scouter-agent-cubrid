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

package scouter.agent.cubrid;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import scouter.agent.cubrid.data.list.WorkConstants;
import scouter.agent.cubrid.net.ReqestHandlingProxy;
import scouter.agent.cubrid.net.TcpRequestMgr;
import scouter.agent.cubrid.net.UdpSendAlive;
import scouter.agent.cubrid.work.MakeData;
import scouter.agent.cubrid.work.SendDataCollector;
import scouter.util.ThreadUtil;

public class CubridWorker extends Thread {

    private static volatile CubridWorker instance;

    private static int dailyCount = 0;

    public static CubridWorker load() {
        if (instance == null) {
            synchronized (CubridWorker.class) {
                if (instance == null) {
                    instance = new CubridWorker();
                    instance.setName(ThreadUtil.getName(CubridWorker.class));
                    instance.setDaemon(true);
                    instance.start();
                }
            }
        }
        return instance;
    }

    @Override
    public void run() {

        TcpRequestMgr.getInstance();
        ReqestHandlingProxy.load(ReqestHandlingProxy.class);

        // udp object alive
        Runnable aliveRunnable = new UdpSendAlive();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(
                aliveRunnable, 0, WorkConstants.ALIVE_INTERVAL_SECOND, TimeUnit.SECONDS);

        while (true) {

            MakeData.make();
            SendDataCollector.sendServerInfo();
            SendDataCollector.sendDbPrefData();
            SendDataCollector.sendTransactionInfo();

            try {
                Thread.sleep(WorkConstants.DATA_INTERVAL_MILLI_SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (dailyCount == WorkConstants.MAX_DAILY_COUNT) {
                dailyCount = 0;
            } else {
                dailyCount++;
            }
        }
    }

    public static int getDailyCount() {
        return dailyCount;
    }
}
