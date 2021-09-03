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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import scouter.lang.conf.ConfigValueUtil;
import scouter.lang.conf.ValueType;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.util.FileUtil;
import scouter.util.StringEnumer;
import scouter.util.StringKeyLinkedMap;
import scouter.util.StringSet;
import scouter.util.StringUtil;
import scouter.util.ThreadUtil;

public class AlertConfigure extends Thread {

    private static AlertConfigure instance = null;
    public static final String CONF_DIR = "./conf/";
    public static final String[] defaultAlertData = {"cpu_used","active_session","lock_wait_sessions"
    		,"num_data_page_iowrites","num_data_page_ioreads","num_data_page_fetches","num_data_page_dirties"
    		,"data_page_buffer_hit_ratio","num_query_sscans","num_sort_io_pages","num_sort_data_pages"
    		,"xasl_plan_hit_rate","filter_plan_hit_rate","as_num_tran","as_num_query","as_error_query"};

    public static final synchronized AlertConfigure getInstance() {
        if (instance == null) {
            instance = new AlertConfigure();
            instance.setDaemon(true);
            instance.setName(ThreadUtil.getName(instance));
            instance.start();
        }
        return instance;
    }

    public boolean agent_debuging = false;
    
    public long num_data_page_iowrites = 0;
    public long num_data_page_fetches = 0;

    public long active_session= 0;
    public long cpu_used = 0;
    public long lock_wait_sessions = 0;

    public long as_num_tran = 0;
    public long as_num_query = 0;
    public long as_error_query = 0;

    public long num_data_page_dirties = 0;
    public long data_page_buffer_hit_ratio = 0;
    public long num_query_sscans = 0;
    public long num_sort_io_pages = 0;
    public long num_sort_data_pages = 0;

    public long xasl_plan_hit_rate = 0;
    public long filter_plan_hit_rate = 0;

    private AlertConfigure() {
        Properties p = new Properties();
        Map args = new HashMap();
        args.putAll(System.getenv());
        args.putAll(System.getProperties());
        p.putAll(args);
        this.property = p;
        reload();
    }

    private AlertConfigure(boolean b) {}

    public Properties property = new Properties();
    
    private File propertyFile;

    public File getPropertyFile() {
        if (propertyFile != null) {
            return propertyFile;
        }
        String s = System.getProperty("scouter.config", CONF_DIR + "alert_warning.conf");
        if (Main.jarPath.equals("")) {
        	propertyFile = new File(s.trim());
        } else {
        	propertyFile = new File(Main.jarPath, s.trim());
        }
        
        if (!propertyFile.exists()) {
        	try {
        		propertyFile.createNewFile();
        		writeDefaultData();
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
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
    	agent_debuging = getBoolean("agent_debuging", false);

    	cpu_used = getInt("cpu_used", 0);
        active_session= getInt("active_session", 0);
        lock_wait_sessions = getInt("lock_wait_sessions", 0);
    	
    	num_data_page_iowrites = getInt("num_data_page_iowrites", 0);
    	num_data_page_iowrites = getInt("num_data_page_ioreads", 0);
        num_data_page_fetches = getInt("num_data_page_fetches", 0);
        num_data_page_dirties = getInt("num_data_page_dirties", 0);
        data_page_buffer_hit_ratio = getInt("data_page_buffer_hit_ratio", 0);

        num_query_sscans = getInt("num_query_sscans", 0);
        num_sort_io_pages = getInt("num_sort_io_pages", 0);
        num_sort_data_pages = getInt("num_sort_data_pages", 0);
        
        as_num_tran = getInt("as_num_tran", 0);
        as_num_query = getInt("as_num_query", 0);
        as_error_query = getInt("as_error_query", 0);

        xasl_plan_hit_rate = getInt("xasl_plan_hit_rate", 0);
        filter_plan_hit_rate = getInt("filter_plan_hit_rate", 0);
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

    public String getValue(String key) {
    	String ret = StringUtil.trim(property.getProperty(key));
        return ret;
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
    
    private static HashSet<String> ignoreSet = new HashSet<String>();

    static {
        ignoreSet.add("property");
    }

    public MapValue getAlertInfo() {
        StringKeyLinkedMap<Object> Map = ConfigValueUtil.getConfigDefault(this);
        MapValue m = new MapValue();
        ListValue nameList = m.newList("key");
        ListValue valueList = m.newList("value");

        StringEnumer enu = Map.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextString();
            if (ignoreSet.contains(key)) continue;
            nameList.add(key);
            valueList.add(ConfigValueUtil.toValue(Map.get(key)));
        }
        return m;
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
    
    private String mapToString(MapValue map) {
    	String temp = "";
    	for (String key : map.keySet()) {
    		temp = temp + key + "=" + map.getText(key) + "\n";
    	}
    	return temp;
    }
    
    public boolean writeSetValue(String key, String value) {
    	MapValue LoadData = new MapValue(); 
    	String load = loadText();
    	if (load != null && !load.isEmpty()) {
        	load= load.replace(" ", "");
        	String[] lineTemp = load.split("\n");
        	for (int i = 0; i < lineTemp.length ; i++) {
        		String[] temp =  lineTemp[i].split("=");
        		if (temp.length == 2) {
        			LoadData.put(temp[0], temp[1]);
        		} 
        	}
    	}     	
    	LoadData.put(key, value);
    	boolean ret = saveText(mapToString(LoadData));
    	//reload();
    	return ret;
    }
    
    public boolean writeDefaultData() {
    	String defaultString = "";
    	for (int i=0; i < defaultAlertData.length ; i++) {
    		defaultString = defaultString + defaultAlertData[i] + "=0" + "\n";
    	}
    	
    	boolean ret = saveText(defaultString);

    	return ret;
    }
    

    public StringKeyLinkedMap<String> getConfigureDesc() {
        return ConfigValueUtil.getConfigDescMap(this);
    }

    public StringKeyLinkedMap<ValueType> getConfigureValueType() {
        return ConfigValueUtil.getConfigValueTypeMap(this);
    }
    
    public static void main(String[] args) {
        AlertConfigure o = new AlertConfigure(true);
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
