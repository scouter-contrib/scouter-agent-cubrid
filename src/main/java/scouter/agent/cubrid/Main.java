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

import java.io.Console;
import java.io.File;
import java.util.Map;
import java.util.Scanner;

import scouter.agent.cubrid.net.http.HttpConnect;
import scouter.agent.cubrid.util.Util;
import scouter.util.SysJMX;
import scouter.util.ThreadUtil;

public class Main {
	
	static String jarPath = "";
	
    public static void main(String[] args) {
        try {
        	jarPath = Util.getJarContainingFolder(Main.class);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	 
    	AlertConfigure.getInstance();
        Configure conf = Configure.getInstance();

        HttpConnect.cubrid_cms_user_name = System.getenv("agent_cms_user");
        HttpConnect.cubrid_cms_user_passwd = System.getenv("agent_cms_passwd");
        HttpConnect.cubrid_db_user_name = System.getenv("agent_dba_user");
        HttpConnect.cubrid_db_user_passwd = System.getenv("agent_dba_passwd");
        
        Logger.systemPrintln("Scouter Cubrid Agent Start Ver. 1.0.0");
        Logger.systemPrintln("System JRE version : " + System.getProperty("java.version"));

        if (true) {
            Logger.systemPrintln("Agent ip : " + conf.net_local_udp_ip);
            Logger.systemPrintln("Agent udp : " + conf.net_local_udp_port);
            Logger.systemPrintln("Scouter Server ip : " + conf.net_collector_ip);
            Logger.systemPrintln("Scouter Server tcp port : " + conf.net_collector_tcp_port);
            Logger.systemPrintln("Scouter Server udp port : " + conf.net_collector_udp_port);
            Logger.systemPrintln("cubrid_cms_ip : " + conf.cubrid_cms_ip);
            Logger.systemPrintln("cubrid_cms_port : " + conf.cubrid_cms_port);
            Logger.systemPrintln("cubrid_cms_user_name : " + HttpConnect.cubrid_cms_user_name);
            //Logger.systemPrintln("cubrid_cms_user_passwd : " + HttpConnect.cubrid_cms_user_passwd);
            Logger.systemPrintln("cubrid_db_user_name : " + HttpConnect.cubrid_db_user_name);
            //Logger.systemPrintln("cubrid_db_user_passwd : " + HttpConnect.cubrid_db_user_passwd);
        }

        Configure.getInstance().printConfig();

        // Agent work start
        CubridWorker.load();

        File exit;
       
        if (jarPath.equals("")) {
        	exit = new File(SysJMX.getProcessPID() + ".Cubrid");
        } else {
        	exit = new File(jarPath, SysJMX.getProcessPID() + ".Cubrid");
        }
        
        try {
            exit.createNewFile();
        } catch (Exception e) {
            String tmp = System.getProperty("user.home", "/tmp");
            Logger.println("System tmp : " + tmp);
            exit = new File(tmp, SysJMX.getProcessPID() + ".Cubrid.run");
            try {
                exit.createNewFile();
            } catch (Exception k) {
                System.exit(1);
            }
        }
        exit.deleteOnExit();

        while (true) {
            if (exit.exists() == false) {
                Logger.println("Scouter Cubrid Agent End");
                System.exit(0);
            }
            ThreadUtil.sleep(1000);
        }
    }
}
