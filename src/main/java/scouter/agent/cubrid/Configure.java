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

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import scouter.lang.conf.ConfigDesc;
import scouter.lang.conf.ConfigValueType;
import scouter.lang.conf.ConfigValueUtil;
import scouter.lang.conf.ValueType;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.net.NetConstants;
import scouter.util.FileUtil;
import scouter.util.HashUtil;
import scouter.util.StringEnumer;
import scouter.util.StringKeyLinkedMap;
import scouter.util.StringSet;
import scouter.util.StringUtil;
import scouter.util.ThreadUtil;

public class Configure extends Thread {

    private static Configure instance = null;
    public static final String CONF_DIR = "./conf/";

    public static final synchronized Configure getInstance() {
        if (instance == null) {
            instance = new Configure();
            instance.setDaemon(true);
            instance.setName(ThreadUtil.getName(instance));
            instance.start();
        }
        return instance;
    }
    // Cubrid Manager Server
    @ConfigDesc("Cubrid Manager Server IP")
    public String cubrid_cms_ip = "127.0.0.1";
    @ConfigDesc("Cubrid Manager Server Port")
    public int cubrid_cms_port = 8001;
    @ConfigDesc("Cubrid Manager Server Version")
    public String clinetVersion = "11.1.0";
    
    // Network
    @ConfigDesc("UDP local IP")
    public String net_local_udp_ip = "127.0.0.1";
    @ConfigDesc("UDP local Port")
    public int net_local_udp_port = 6200;
    @ConfigDesc("Collector IP")
    public String net_collector_ip = "127.0.0.1";
    @ConfigDesc("Collector UDP Port")
    public int net_collector_udp_port = NetConstants.SERVER_UDP_PORT;
    @ConfigDesc("Collector TCP Port")
    public int net_collector_tcp_port = NetConstants.SERVER_TCP_PORT;
    @ConfigDesc("Collector TCP Session Count")
    public int net_collector_tcp_session_count = 1;
    @ConfigDesc("Collector TCP Socket Timeout(ms)")
    public int net_collector_tcp_so_timeout_ms = 60000;
    @ConfigDesc("Collector TCP Connection Timeout(ms)")
    public int net_collector_tcp_connection_timeout_ms = 3000;
    @ConfigDesc("UDP Buffer Size")
    public int net_udp_packet_max_bytes = 60000;

    // Object
    @ConfigDesc(
            "Deprecated. It's just an alias of monitoring_group_type which overrides this value.")
    public String obj_type = "cubridagent";
    @ConfigDesc("Object Name")
    public String obj_name = "Cubrid";

    // Manager
    @ConfigDesc("")
    @ConfigValueType(ValueType.COMMA_SEPARATED_VALUE)
    public StringSet mgr_log_ignore_ids = new StringSet();

    // Log
    @ConfigDesc("Retaining log according to date")
    public boolean log_rotation_enalbed = true;

    @ConfigDesc("Log directory")
    public String log_dir = "./logs";

    @ConfigDesc("Keeping period of log")
    public int log_keep_days = 365;

    // internal variables
    private int objHash;
    private String objName;

    private Configure() {
        Properties p = new Properties();
        Map args = new HashMap();
        args.putAll(System.getenv());
        args.putAll(System.getProperties());
        p.putAll(args);
        this.property = p;
        reload();
    }

    private Configure(boolean b) {}

    private long last_load_time = -1;
    public Properties property = new Properties();

    private File propertyFile;

    public File getPropertyFile() {
        if (propertyFile != null) {
            return propertyFile;
        }
        String s = System.getProperty("scouter.config", CONF_DIR + "scouter_cubrid.conf");
        if (Main.jarPath.equals("")) {
        	propertyFile = new File(s.trim());
        } else {
        	propertyFile = new File(Main.jarPath, s.trim());
        }
        return propertyFile;
    }

    long last_check = 0;

    public synchronized void reload() {
        File file = getPropertyFile();
        Properties temp = new Properties();
        if (file.canRead()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                temp.load(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtil.close(in);
            }
        }
        property = ConfigValueUtil.replaceSysProp(temp);
        apply();
    }

    private void apply() {
        this.net_udp_packet_max_bytes =
                getInt("net_udp_packet_max_bytes", getInt("udp.packet.max", 60000));
        this.mgr_log_ignore_ids = getStringSet("mgr_log_ignore_ids", ",");

        this.net_local_udp_ip = getValue("net_local_udp_ip", "127.0.0.1");
        this.net_local_udp_port = getInt("net_local_udp_port", 6200);

        this.net_collector_ip = getValue("net_collector_ip", getValue("server.addr", "127.0.0.1"));
        this.net_collector_udp_port =
                getInt(
                        "net_collector_udp_port",
                        getInt("server.port", NetConstants.SERVER_UDP_PORT));
        this.net_collector_tcp_port =
                getInt(
                        "net_collector_tcp_port",
                        getInt("server.port", NetConstants.SERVER_TCP_PORT));
        this.net_collector_tcp_session_count = getInt("net_collector_tcp_session_count", 1, 1);
        this.net_collector_tcp_connection_timeout_ms =
                getInt("net_collector_tcp_connection_timeout_ms", 3000);
        this.net_collector_tcp_so_timeout_ms = getInt("net_collector_tcp_so_timeout_ms", 60000);

        this.cubrid_cms_ip = getValue("cubrid_cms_ip", "127.0.0.1");
        this.cubrid_cms_port = getInt("cubrid_cms_port", 8001);

        resetObjInfo();
    }

    private StringSet getStringSet(String key, String deli) {
        StringSet set = new StringSet();
        String v = getValue(key);
        if (v != null) {
            String[] vv = StringUtil.split(v, deli);
            for (String x : vv) {
                x = StringUtil.trimToEmpty(x);
                if (x.length() > 0) set.put(x);
            }
        }
        return set;
    }

    public synchronized void resetObjInfo() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "UnkownHost";
        }
        this.objName = "/" + hostName + "/" + this.obj_name;
        Logger.systemDebugPrintln(this.objName);
        this.objHash = HashUtil.hash(objName);
    }

    public String getValue(String key) {
        return StringUtil.trim(property.getProperty(key));
    }

    public String getValue(String key, String def) {
        return StringUtil.trim(property.getProperty(key, def));
    }

    public int getInt(String key, int def) {
        try {
            String v = getValue(key);
            if (v != null) return Integer.parseInt(v);
        } catch (Exception e) {
        }
        return def;
    }

    public int getInt(String key, int def, int min) {
        try {
            String v = getValue(key);
            if (v != null) {
                return Math.max(Integer.parseInt(v), min);
            }
        } catch (Exception e) {
        }
        return Math.max(def, min);
    }

    public long getLong(String key, long def) {
        try {
            String v = getValue(key);
            if (v != null) return Long.parseLong(v);
        } catch (Exception e) {
        }
        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            String v = getValue(key);
            if (v != null) return Boolean.parseBoolean(v);
        } catch (Exception e) {
        }
        return def;
    }

    public int getObjHash() {
        return this.objHash;
    }

    public String getObjName() {
        return this.objName;
    }

    public String getDbObjName(String dbName) {
        return this.objName + "&" + dbName;
    }

    public String loadText() {
        File file = getPropertyFile();
        InputStream fin = null;
        try {
            fin = new FileInputStream(file);
            byte[] buff = FileUtil.readAll(fin);
            return new String(buff);
        } catch (Exception e) {
        } finally {
            FileUtil.close(fin);
        }
        return null;
    }

    public boolean saveText(String text) {
        File file = getPropertyFile();
        OutputStream out = null;
        try {
            if (file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file);
            out.write(text.getBytes());
            return true;
        } catch (Exception e) {
        } finally {
            FileUtil.close(out);
        }
        return false;
    }

    public void printConfig() {
        Logger.systemDebugPrintln("Configure -Dscouter.config=" + propertyFile);
    }

    private static HashSet<String> ignoreSet = new HashSet<String>();

    static {
        ignoreSet.add("property");
    }

    public MapValue getKeyValueInfo() {
        StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(new Configure(true));
        StringKeyLinkedMap<Object> curMap = ConfigValueUtil.getConfigDefault(this);
        MapValue m = new MapValue();
        ListValue nameList = m.newList("key");
        ListValue valueList = m.newList("value");
        ListValue defList = m.newList("default");

        StringEnumer enu = defMap.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextString();
            if (ignoreSet.contains(key)) continue;
            nameList.add(key);
            valueList.add(ConfigValueUtil.toValue(curMap.get(key)));
            defList.add(ConfigValueUtil.toValue(defMap.get(key)));
        }

        return m;
    }

    public StringKeyLinkedMap<String> getConfigureDesc() {
        return ConfigValueUtil.getConfigDescMap(this);
    }

    public StringKeyLinkedMap<ValueType> getConfigureValueType() {
        return ConfigValueUtil.getConfigValueTypeMap(this);
    }

    public static void main(String[] args) {
        Configure o = new Configure(true);
        StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(o);
        StringKeyLinkedMap<String> descMap = ConfigValueUtil.getConfigDescMap(o);
        StringEnumer enu = defMap.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextString();
            if (ignoreSet.contains(key)) continue;
            Logger.systemDebugPrintln(
                    key
                            + " : "
                            + ConfigValueUtil.toValue(defMap.get(key))
                            + (descMap.containsKey(key) ? " (" + descMap.get(key) + ")" : ""));
        }
    }
}
