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

import java.io.File;
import java.net.URLDecoder;
import java.security.CodeSource;

public class Util {

    public static String subStringBetween(String orignalString, String start, String end) {
        String result = "";
        int startIndex = 0;
        int endIndex = 0;
        startIndex = orignalString.indexOf(start);
        endIndex = orignalString.indexOf(end);

        if (startIndex < 0 || endIndex < 0) {
            return "error";
        }

        result = orignalString.substring(startIndex, endIndex);
        return result;
    }

    public static String subStringBetweenIndex(
            String orignalString, String start, int startplus, String end, int endplus) {
        String result = "";
        int startIndex = 0;
        int endIndex = 0;
        startIndex = orignalString.indexOf(start) + startplus;
        endIndex = orignalString.indexOf(end) + endplus;

        if (startIndex < 0 || endIndex < 0) {
            return "error";
        }

        result = orignalString.substring(startIndex, endIndex);
        return result;
    }
    
    public static String getJarContainingFolder(Class<?> aclass) throws Exception {
    	CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

    	File jarFile;

    	if (codeSource.getLocation() != null) {
    		jarFile = new File(codeSource.getLocation().toURI());
    	}
    	else {
    		String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
    		String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
    		jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
    		jarFile = new File(jarFilePath);
    	}
    	return jarFile.getParentFile().getAbsolutePath();
    }
}
