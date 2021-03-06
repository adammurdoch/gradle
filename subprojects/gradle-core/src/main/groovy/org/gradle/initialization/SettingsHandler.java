/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.initialization;

import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.StartParameter;
import org.gradle.groovy.scripts.StringScriptSource;

import java.io.File;
import java.net.URLClassLoader;

/**
 * Handles locating and processing setting.gradle files.  Also deals with the
 * buildSrc module, since that modules is found after settings is located, but
 * needs to be built before settings is processed.
 */
public class SettingsHandler
{
    private ISettingsFinder settingsFinder;
    private SettingsProcessor settingsProcessor;
    private BuildSourceBuilder buildSourceBuilder;

    public SettingsHandler(ISettingsFinder settingsFinder,
                           SettingsProcessor settingsProcessor,
                           BuildSourceBuilder buildSourceBuilder) {
        this.settingsFinder = settingsFinder;
        this.settingsProcessor = settingsProcessor;
        this.buildSourceBuilder = buildSourceBuilder;
    }

    public SettingsInternal findAndLoadSettings(GradleInternal gradle, IGradlePropertiesLoader gradlePropertiesLoader)
    {
        StartParameter startParameter = gradle.getStartParameter();
        SettingsInternal settings = findSettingsAndLoadIfAppropriate(startParameter, gradlePropertiesLoader);
        if (!startParameter.getDefaultProjectSelector().containsProject(settings.getProjectRegistry())) {
            // The settings we found did not include the desired default project. Try again with an empty settings file.

            StartParameter noSearchParameter = startParameter.newInstance();
            noSearchParameter.setSettingsScriptSource(new StringScriptSource("empty settings file", ""));
            settings = findSettingsAndLoadIfAppropriate(noSearchParameter, gradlePropertiesLoader);
            if (settings == null) // not using an assert to make sure it is not disabled
                throw new InternalError("Empty settings file does not contain expected project.");

            // Set explicit build file, if required
            if (noSearchParameter.getBuildFile() != null) {
                ProjectDescriptor rootProject = settings.getRootProject();
                assert noSearchParameter.getBuildFile().getParentFile().equals(rootProject.getProjectDir());
                rootProject.setBuildFileName(noSearchParameter.getBuildFile().getName());
            }
        }

        gradle.setBuildScriptClassLoader(settings.createClassLoader());
        return settings;
    }

    /**
     Finds the settings.gradle for the given startParameter, and loads it if contains the project selected by the
     startParameter, or if the startParameter explicity specifies a settings script.  If the settings file is not
     loaded (executed), then a null is returned.
     */
    private SettingsInternal findSettingsAndLoadIfAppropriate(StartParameter startParameter, IGradlePropertiesLoader gradlePropertiesLoader)
    {
        SettingsLocation settingsLocation = findSettings(startParameter);

        // We found the desired settings file, now build the associated buildSrc before loading settings.  This allows
        // the settings script to reference classes in the buildSrc.
        StartParameter buildSrcStartParameter = startParameter.newBuild();
        buildSrcStartParameter.setCurrentDir(new File(settingsLocation.getSettingsDir(), BaseSettings.DEFAULT_BUILD_SRC_DIR));
        URLClassLoader buildSourceClassLoader = buildSourceBuilder.buildAndCreateClassLoader(buildSrcStartParameter);

        return loadSettings(settingsLocation, buildSourceClassLoader, startParameter, gradlePropertiesLoader);
    }

    private SettingsLocation findSettings(StartParameter startParameter)
    {
        return settingsFinder.find(startParameter);
    }

    private SettingsInternal loadSettings(SettingsLocation settingsLocation, URLClassLoader buildSourceClassLoader, StartParameter startParameter, IGradlePropertiesLoader gradlePropertiesLoader) {
        return settingsProcessor.process(settingsLocation, buildSourceClassLoader, startParameter, gradlePropertiesLoader);
    }
}

