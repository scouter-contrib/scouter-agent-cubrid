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
package scouter.agent.cubrid.net;

import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.io.DataOutputX;
import scouter.lang.TimeTypeEnum;
import scouter.lang.pack.AlertPack;
import scouter.lang.pack.PerfCounterPack;
import scouter.lang.pack.StatusPack;
import scouter.lang.pack.TextPack;
import scouter.lang.value.MapValue;

public class UdpSendData {

    public static void sendPerf(MapValue data) {
        Configure conf = Configure.getInstance();
        PerfCounterPack perfPack = new PerfCounterPack();
        perfPack.objName = conf.getObjName();
        perfPack.timetype = TimeTypeEnum.REALTIME;
        perfPack.time = System.currentTimeMillis();
        perfPack.data = data;

        try {
            byte[] objectTemp = new DataOutputX().writePack(perfPack).toByteArray();
            Logger.systemDebugPrintln("UdpSendData sendPerf instance");
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendDailyPerfForMoreDb(MapValue data, String dbName) {
        Configure conf = Configure.getInstance();
        PerfCounterPack perfPack = new PerfCounterPack();
        perfPack.objName = conf.getDbObjName(dbName);
        perfPack.timetype = TimeTypeEnum.FIVE_MIN;
        perfPack.time = System.currentTimeMillis();
        perfPack.data = data;

        try {
            byte[] objectTemp = new DataOutputX().writePack(perfPack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendPerfForMoreDb(MapValue data, String dbName) {
        Configure conf = Configure.getInstance();
        PerfCounterPack perfPack = new PerfCounterPack();
        perfPack.objName = conf.getDbObjName(dbName);
        perfPack.timetype = TimeTypeEnum.REALTIME;
        perfPack.time = System.currentTimeMillis();
        perfPack.data = data;

        try {
            byte[] objectTemp = new DataOutputX().writePack(perfPack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendAlert(byte level, String title, String message, MapValue tags) {
        Configure conf = Configure.getInstance();
        AlertPack alertPack = new AlertPack();
        alertPack.objType = conf.obj_type;
        alertPack.objHash = conf.getObjHash();
        alertPack.level = level;
        alertPack.title = title;
        alertPack.message = message;
        if (tags != null) {
            alertPack.tags = tags;
        }

        try {
            byte[] objectTemp = new DataOutputX().writePack(alertPack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendText(String xtype, int hash, String text) {
        TextPack textPack = new TextPack();
        textPack.xtype = xtype;
        textPack.hash = hash;
        textPack.text = text;

        try {
            byte[] objectTemp = new DataOutputX().writePack(textPack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendStatus(String key, MapValue val) {
        Configure conf = Configure.getInstance();
        StatusPack statusPack = new StatusPack();
        statusPack.time = System.currentTimeMillis();
        statusPack.objType = conf.obj_type;
        statusPack.objHash = conf.getObjHash();
        statusPack.key = key;
        statusPack.data = val;

        try {
            byte[] objectTemp = new DataOutputX().writePack(statusPack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
