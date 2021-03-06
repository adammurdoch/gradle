/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.api.plugins;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.tasks.ConventionValue;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.GradleManifest;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.*;

/**
 * @author Hans Dockter
 */
public class DefaultConventionsToPropertiesMapping {

    public final static Map<String, ConventionValue> JAVADOC = GUtil.map(
            "srcDirs", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    Set<File> srcDirs = convention.getPlugin(JavaPluginConvention.class).getSource().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava().getSrcDirs();
                    return new ArrayList<File>(srcDirs);
                }
            },
            "destinationDir", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    return convention.getPlugin(JavaPluginConvention.class).getJavadocDir();
                }
            }
    );

    public final static Map TEST = GUtil.map(
            "testClassesDir", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    return convention.getPlugin(JavaPluginConvention.class).getSource().getByName(SourceSet.TEST_SOURCE_SET_NAME).getClassesDir();
                }
            },
            "testResultsDir", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    return convention.getPlugin(JavaPluginConvention.class).getTestResultsDir();
                }
            },
            "testReportDir", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    return convention.getPlugin(JavaPluginConvention.class).getTestReportDir();
                }
            },
            "testSrcDirs", new ConventionValue() {
                public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                    Set<File> srcDirs = convention.getPlugin(JavaPluginConvention.class).getSource().getByName(
                            SourceSet.TEST_SOURCE_SET_NAME).getJava().getSrcDirs();
                    return new ArrayList<File>(srcDirs);
                }
            }
    );

    public final static Map ARCHIVE = GUtil.map(
            "version", new ConventionValue() {
        public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
            return "" + ((Task) conventionAwareObject).getProject().getVersion();
        }
        }, "baseName", new ConventionValue() {
        public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
            return convention.getPlugin(BasePluginConvention.class).getArchivesBaseName();
        }
    });

    public final static Map ZIP = new HashMap(ARCHIVE);

    static {
        ZIP.putAll(GUtil.map(
                "destinationDir", new ConventionValue() {
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        return convention.getPlugin(BasePluginConvention.class).getDistsDir();
                    }
                }
        ));
    }

    public final static Map TAR = new HashMap(ZIP);

    public final static Map JAR = new HashMap(ARCHIVE);

    static {
        JAR.putAll(GUtil.map(
                "destinationDir", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return convention.getPlugin(BasePluginConvention.class).getLibsDir();
            }
        },
                "baseDir", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return convention.getPlugin(JavaPluginConvention.class).getSource().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getClassesDir();
            }
        },
                "manifest", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return new GradleManifest(convention.getPlugin(JavaPluginConvention.class).getManifest().getManifest());
            }
        },
                "metaInfResourceCollections", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return convention.getPlugin(JavaPluginConvention.class).getMetaInf();
            }
        }));
    }

    public final static Map WAR = new HashMap(JAR);

    static {
        WAR.putAll(GUtil.map(
                "baseDir", new ConventionValue() {
                    public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                        return null;
                    }
                },
                "classesFileSets", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                JavaPluginConvention pluginConvention = convention.getPlugin(JavaPluginConvention.class);
                Project project = pluginConvention.getProject();
                File classesDir = pluginConvention.getSource().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                        .getClassesDir();
                return Arrays.asList(project.fileTree(classesDir));
            }
        },      "resourceCollections", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                WarPluginConvention warPluginConvention = convention.getPlugin(WarPluginConvention.class);
                Project project = warPluginConvention.getProject();
                return Arrays.asList(project.fileTree(warPluginConvention.getWebAppDir()));
            }
        },
                "libExcludeConfigurations", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return Arrays.asList(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
            }
        },
                "libConfigurations", new ConventionValue() {
            public Object getValue(Convention convention, IConventionAware conventionAwareObject) {
                return Arrays.asList(JavaPlugin.RUNTIME_CONFIGURATION_NAME);
            }
        }));
    }
}
