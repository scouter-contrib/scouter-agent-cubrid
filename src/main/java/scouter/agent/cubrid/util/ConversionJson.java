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
package scouter.agent.cubrid.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import scouter.agent.cubrid.Logger;

public class ConversionJson {

    public static JSONObject searchAndToJson(String searchKey, JSONObject data) {

        JSONArray jsonArray = new JSONArray();
        JSONObject resultObject = new JSONObject();

        if (String.valueOf(data.get(searchKey)).equals("{}")) {
            Logger.println("searchAndToJson searchKey is not exist");
        } else {
            JSONParser parser = new JSONParser();
            try {
                jsonArray = (JSONArray) parser.parse(String.valueOf(data.get(searchKey)));
                resultObject = (JSONObject) jsonArray.get(0);

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        Logger.systemDebugPrintln(
                "searchAndChangeJson searchKey : "
                        + searchKey
                        + " resultObject : "
                        + String.valueOf(resultObject));

        return resultObject;
    }
}
