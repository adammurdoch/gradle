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

package org.gradle.invocation;

import groovy.lang.Closure;
import org.gradle.BuildListener;
import org.gradle.StartParameter;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.artifacts.repositories.InternalRepository;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.initialization.ScriptClassLoaderProvider;
import org.gradle.api.internal.plugins.DefaultPluginRegistry;
import org.gradle.api.internal.project.*;
import org.gradle.execution.DefaultTaskExecuter;
import org.gradle.execution.TaskExecuter;
import org.gradle.util.ConfigureUtil;
import org.gradle.util.GradleVersion;
import org.gradle.util.ListenerBroadcast;

import java.io.File;

public class DefaultGradle implements GradleInternal {
    private final Logger logger = Logging.getLogger(Gradle.class);

    private ProjectInternal rootProject;
    private ProjectInternal defaultProject;
    private TaskExecuter taskGraph;
    private StartParameter startParameter;
    private ClassLoader buildScriptClassLoader;
    private InternalRepository internalRepository;
    private StandardOutputRedirector standardOutputRedirector;
    private DefaultProjectRegistry<ProjectInternal> projectRegistry;
    private DefaultPluginRegistry pluginRegistry;
    private ScriptHandler scriptHandler;
    private ScriptClassLoaderProvider scriptClassLoaderProvider;
    private final ListenerBroadcast<ProjectEvaluationListener> projectEvaluationListenerBroadcast
            = new ListenerBroadcast<ProjectEvaluationListener>(ProjectEvaluationListener.class);
    private final ListenerBroadcast<BuildListener> buildListeners = new ListenerBroadcast<BuildListener>(
            BuildListener.class);

    public DefaultGradle(StartParameter startParameter, InternalRepository internalRepository,
                         ServiceRegistryFactory serviceRegistryFactory,
                         StandardOutputRedirector standardOutputRedirector) {
        this.startParameter = startParameter;
        this.internalRepository = internalRepository;
        this.standardOutputRedirector = standardOutputRedirector;
        this.projectRegistry = new DefaultProjectRegistry<ProjectInternal>();
        this.pluginRegistry = new DefaultPluginRegistry(startParameter.getPluginPropertiesFile());
        this.taskGraph = new DefaultTaskExecuter();

        ServiceRegistry serviceRegistry = serviceRegistryFactory.createForBuild(this);
        scriptHandler = serviceRegistry.get(ScriptHandler.class);
        scriptClassLoaderProvider = serviceRegistry.get(ScriptClassLoaderProvider.class);
    }

    public String getGradleVersion() {
        return new GradleVersion().getVersion();
    }

    public File getGradleHomeDir() {
        return startParameter.getGradleHomeDir();
    }

    public File getGradleUserHomeDir() {
        return startParameter.getGradleUserHomeDir();
    }

    public StartParameter getStartParameter() {
        return startParameter;
    }

    public ProjectInternal getRootProject() {
        return rootProject;
    }

    public void setRootProject(ProjectInternal rootProject) {
        this.rootProject = rootProject;
    }

    public ProjectInternal getDefaultProject() {
        return defaultProject;
    }

    public void setDefaultProject(ProjectInternal defaultProject) {
        this.defaultProject = defaultProject;
    }

    public TaskExecuter getTaskGraph() {
        return taskGraph;
    }

    public void setTaskGraph(TaskExecuter taskGraph) {
        this.taskGraph = taskGraph;
    }

    public IProjectRegistry<ProjectInternal> getProjectRegistry() {
        return projectRegistry;
    }

    public ClassLoader getBuildScriptClassLoader() {
        return buildScriptClassLoader;
    }

    public void setBuildScriptClassLoader(ClassLoader buildScriptClassLoader) {
        this.buildScriptClassLoader = buildScriptClassLoader;
    }

    public InternalRepository getInternalRepository() {
        return internalRepository;
    }

    public void setInternalRepository(InternalRepository internalRepository) {
        this.internalRepository = internalRepository;
    }

    public DefaultPluginRegistry getPluginRegistry() {
        return pluginRegistry;
    }

    public ProjectEvaluationListener addProjectEvaluationListener(ProjectEvaluationListener listener) {
        projectEvaluationListenerBroadcast.add(listener);
        return listener;
    }

    public void removeProjectEvaluationListener(ProjectEvaluationListener listener) {
        projectEvaluationListenerBroadcast.remove(listener);
    }

    public void beforeProject(Closure closure) {
        projectEvaluationListenerBroadcast.add("beforeEvaluate", closure);
    }

    public void afterProject(Closure closure) {
        projectEvaluationListenerBroadcast.add("afterEvaluate", closure);
    }

    public ProjectEvaluationListener getProjectEvaluationBroadcaster() {
        return projectEvaluationListenerBroadcast.getSource();
    }

    public void addBuildListener(BuildListener buildListener) {
        buildListeners.add(buildListener);
    }

    public BuildListener getBuildListenerBroadcaster() {
        return buildListeners.getSource();
    }

    public ScriptHandler getInitscript() {
        return scriptHandler;
    }

    public void initscript(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getInitscript());
    }

    public ScriptClassLoaderProvider getClassLoaderProvider() {
        return scriptClassLoaderProvider;
    }

    public StandardOutputRedirector getStandardOutputRedirector() {
        return standardOutputRedirector;
    }

    public void captureStandardOutput(LogLevel level) {
        standardOutputRedirector.on(level);
    }

    public void disableStandardOutputCapture() {
    }

    public Gradle getGradle() {
        return this;
    }

    public Logger getLogger() {
        return logger;
    }
}
