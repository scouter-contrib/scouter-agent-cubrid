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
import scouter.io.DataOutputX;
import scouter.lang.pack.ObjectPack;

public class UdpSendAlive implements Runnable {

    @Override
    public void run() {
        Configure conf = Configure.getInstance();
        ObjectPack alivePack = new ObjectPack();
        alivePack.alive = true;
        alivePack.objType = conf.obj_type;
        alivePack.objName = conf.getObjName();
        alivePack.objHash = conf.getObjHash();
        alivePack.version = "0.1";
        try {
            byte[] objectTemp = new DataOutputX().writePack(alivePack).toByteArray();
            DataUdpAgent.getInstance().write(objectTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            Logger.systemDebugPrintln("- " + threadName + " has been started");
            try{
                InetAddress ia = InetAddress.getByName("172.31.0.122");
                DatagramSocket ds = new DatagramSocket(6200);
                str = "JAVA";
                byte buffer[] = str.getBytes();
                DatagramPacket dp = new DatagramPacket(
                        buffer,buffer.length,ia,SERVERPORT);
                ds.send(dp);
    //            buffer = new byte[512];
    //            dp = new DatagramPacket(buffer,buffer.length);
    //            ds.receive(dp);
                Logger.systemDebugPrintln("server ip : "+dp.getAddress() + " , server port : "+dp.getPort());
                Logger.systemDebugPrintln("send message : "+ new String(dp.getData()).trim());
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    */
}
