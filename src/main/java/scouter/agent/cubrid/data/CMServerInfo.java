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
package scouter.agent.cubrid.data;

import java.util.Hashtable;

import scouter.lang.value.MapValue;

public class CMServerInfo {

    private static CMServerInfo instance = null;

    // inner Data
    // String : DBNAME
    public Hashtable<String, DbData> activeDbData = new Hashtable<String, DbData>();
    public HostData hostData = new HostData();

    // outer Data
    private MapValue activeDb = new MapValue();
    public Hashtable<String, MapValue> prefData = new Hashtable<>();
    public Hashtable<String, MapValue> prefDataDaily = new Hashtable<>();
    public Hashtable<String, MapValue> serverInfoData = new Hashtable<>();

    public Hashtable<String, MapValue> transactionInfo = new Hashtable<>();
    public Hashtable<String, MapValue> dbDmlInfo = new Hashtable<>();

    public static CMServerInfo getInstance() {
        if (instance == null) {
            synchronized (CMServerInfo.class) {
                if (instance == null) {
                    instance = new CMServerInfo();
                }
            }
        }
        return instance;
    }

    public boolean setActiveDbAndCheckChanged(MapValue val) {

        boolean isChanged = false;
        if (this.activeDb.isEmpty()) {
            isChanged = true;
            for (String key : val.keySet()) {
                this.activeDbData.put(key, new DbData());
            }
        } else {
            for (String key : this.activeDb.keySet()) {
                if (val.containsKey(key)) {
                    continue;
                } else {
                    isChanged = true;
                    this.activeDbData.remove(key);
                }
            }

            for (String key : val.keySet()) {
                if (this.activeDb.containsKey(key)) {
                    continue;
                } else {
                    isChanged = true;
                    this.activeDbData.put(key, new DbData());
                }
            }
        }

        this.activeDb = val;
        return isChanged;
    }

    public MapValue getActiveDb() {
        return this.activeDb;
    }
}
