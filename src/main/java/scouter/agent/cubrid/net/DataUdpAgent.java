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

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import scouter.agent.cubrid.Configure;
import scouter.agent.cubrid.Logger;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.net.NetCafe;
import scouter.util.KeyGen;

public class DataUdpAgent {

    private static DataUdpAgent instance;
    private DatagramSocket datagram = null;

    InetAddress server_host;
    int server_port;

    String local_udp_addr;
    int local_udp_port;

    private DataUdpAgent() {
        setTarget();
        openDatagramSocket();
    }

    private void setTarget() {
        Configure conf = Configure.getInstance();
        String host = conf.net_collector_ip;
        int port = conf.net_collector_udp_port;
        local_udp_addr = conf.net_local_udp_ip;
        local_udp_port = conf.net_local_udp_port;

        Logger.systemDebugPrintln("setTarget server ip : " + host);
        Logger.systemDebugPrintln("setTarget server port : " + port);
        Logger.systemDebugPrintln("setTarget local_udp_addr : " + local_udp_addr);
        Logger.systemDebugPrintln("setTarget local_udp_port : " + local_udp_port);

        try {
            server_host = InetAddress.getByName(host);
            server_port = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void close(DatagramSocket d) {
        if (d != null) {
            try {
                d.close();
            } catch (Exception e) {
            }
        }
    }

    private void openDatagramSocket() {
        try {
            if (datagram == null) {
                if (local_udp_addr != null) {
                    datagram =
                            new DatagramSocket(
                                    local_udp_port, InetAddress.getByName(local_udp_addr));
                    Logger.println(
                            "A118",
                            "Agent UDP local.addr="
                                    + local_udp_addr
                                    + " local.port="
                                    + local_udp_port);
                    Logger.systemDebugPrintln(
                            "Agent UDP local.addr="
                                    + local_udp_addr
                                    + " local.port="
                                    + local_udp_port);
                } else {
                    datagram = new DatagramSocket(local_udp_port);
                    Logger.println("A119", "Agent UDP local.port=" + local_udp_port);
                    Logger.systemDebugPrintln(" local.port=" + local_udp_port);
                }
            }
        } catch (BindException e) {
            e.printStackTrace();
            close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public static synchronized DataUdpAgent getInstance() {
        if (instance == null) {
            instance = new DataUdpAgent();
        }
        return instance;
    }

    private Configure conf = Configure.getInstance();

    public boolean write(byte[] p) {
        try {
            if (server_host == null) return false;

            if (datagram == null) {
                return false;
            }

            if (p.length > conf.net_udp_packet_max_bytes) {
                return writeMTU(p, conf.net_udp_packet_max_bytes);
            }

            DataOutputX out = new DataOutputX();
            out.write(NetCafe.CAFE);
            out.write(p);

            byte[] buff = out.toByteArray();
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            packet.setAddress(server_host);
            packet.setPort(server_port);
            datagram.send(packet);
            return true;
        } catch (IOException e) {
            Logger.println("A120", 10, "UDP", e);
            return false;
        }
    }

    private boolean writeMTU(byte[] data, int packetSize) {
        try {
            if (server_host == null) return false;

            long pkid = KeyGen.next();
            int total = data.length / packetSize;
            int remainder = data.length % packetSize;
            if (remainder > 0) total++;

            int num = 0;
            for (num = 0; num < data.length / packetSize; num++) {
                writeMTU(
                        pkid,
                        total,
                        num,
                        packetSize,
                        DataInputX.get(data, num * packetSize, packetSize));
            }
            if (remainder > 0) {
                writeMTU(
                        pkid,
                        total,
                        num,
                        remainder,
                        DataInputX.get(data, data.length - remainder, remainder));
            }
            return true;
        } catch (IOException e) {
            Logger.println("A121", 10, "UDP", e);
            return false;
        }
    }

    private void writeMTU(long pkid, int total, int num, int packetSize, byte[] data)
            throws IOException {
        DataOutputX out = new DataOutputX();
        out.write(NetCafe.CAFE_MTU);
        out.writeInt(conf.getObjHash());
        out.writeLong(pkid);
        out.writeShort(total);
        out.writeShort(num);
        out.writeBlob(data);
        byte[] buff = out.toByteArray();
        DatagramPacket packet = new DatagramPacket(buff, buff.length);
        packet.setAddress(server_host);
        packet.setPort(server_port);
        datagram.send(packet);
    }

    public void close() {
        if (datagram != null) datagram.close();
        datagram = null;
    }

    public boolean write(List<byte[]> p) {
        try {
            if (server_host == null) return false;

            DataOutputX out = new DataOutputX();
            out.write(NetCafe.CAFE_N);
            out.writeShort((short) p.size());
            for (int i = 0; i < p.size(); i++) {
                out.write(p.get(i));
            }

            byte[] buff = out.toByteArray();

            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            packet.setAddress(server_host);
            packet.setPort(server_port);
            datagram.send(packet);
            return true;
        } catch (IOException e) {
            Logger.println("A123", 10, "UDP", e);
            return false;
        }
    }

    public boolean debugWrite(String ip, int port, int length) {
        try {
            DataOutputX out = new DataOutputX();
            out.write("TEST".getBytes());
            if (length > 4) {
                out.write(new byte[length - 4]);
            }
            byte[] buff = out.toByteArray();
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            packet.setAddress(InetAddress.getByName(ip));
            packet.setPort(port);
            datagram.send(packet);
            Logger.println("A124", "Sent " + length + " bytes to " + ip + ":" + port);
            return true;
        } catch (IOException e) {
            Logger.println("A125", "UDP " + e.toString());
            return false;
        }
    }
}
