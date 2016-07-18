/*
 * Copyright (C) 2016 Tencent WeChat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.build.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * The configuration properties.
 *
 * @author shwenzhang
 */

public class TinkerDexExtension {
    /**
     * raw or jar, if you want to support below 4.0, you should use jar
     * default: raw, keep the orginal file type
     */
    String dexMode;
    /**
     * the dex file patterns, which dex or jar files will be deal to gen patch
     * such as [classes.dex, classes-*.dex, assets/multiDex/*.jar]
     */
    Iterable<String> pattern;
    /**
     * the loader files, they will be removed during gen patch main dex
     * and they should be at the primary dex
     * such as [com.tencent.tinker.loader.*, com.tinker.sample.MyApplication]
     */
    Iterable<String> loader;
    private Project project;

    public TinkerDexExtension(Project project) {
        dexMode = "jar"
        pattern = []
        loader = []
        this.project = project
    }

    void checkDexMode() {
        if (!dexMode.equals("raw") && !dexMode.equals("jar")) {
            throw new GradleException("dexMode can be only one of 'jar' or 'raw'!")
        }
    }

    @Override
    public String toString() {
        """| dexMode = ${dexMode}
           | pattern = ${pattern}
           | loader = ${loader}
        """.stripMargin()
    }
}