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

package org.gradle.api.internal.project;

import org.apache.commons.io.FileUtils;
import org.gradle.StartParameter;
import org.gradle.api.artifacts.dsl.RepositoryHandlerFactory;
import org.gradle.api.initialization.ProjectDescriptor;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.ClassGenerator;
import org.gradle.api.internal.plugins.PluginRegistry;
import org.gradle.api.internal.artifacts.ConfigurationContainerFactory;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.artifacts.configurations.ResolverProvider;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;
import org.gradle.api.internal.artifacts.dsl.PublishArtifactFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory;
import org.gradle.api.plugins.Convention;
import org.gradle.configuration.ProjectEvaluator;
import org.gradle.groovy.scripts.FileScriptSource;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.groovy.scripts.StringScriptSource;
import org.gradle.util.HelperUtil;
import org.gradle.util.Matchers;
import static org.hamcrest.Matchers.sameInstance;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class ProjectFactoryTest {
    private final JUnit4Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final ClassLoader buildScriptClassLoader = new URLClassLoader(new URL[0]);
    private final File rootDir = HelperUtil.makeNewTestDir();
    private final File projectDir = new File(rootDir, "project");
    private ConfigurationContainerFactory configurationContainerFactory = context.mock(ConfigurationContainerFactory.class);
    private DependencyFactory dependencyFactoryMock = context.mock(DependencyFactory.class);
    private RepositoryHandlerFactory repositoryHandlerFactory = context.mock(RepositoryHandlerFactory.class);
    private DefaultRepositoryHandler repositoryHandler = context.mock(DefaultRepositoryHandler.class);
    private PublishArtifactFactory publishArtifactFactoryMock = context.mock(PublishArtifactFactory.class);
    private ProjectEvaluator projectEvaluator = context.mock(ProjectEvaluator.class);
    private ClassGenerator classGenerator = context.mock(ClassGenerator.class);
    private ServiceRegistryFactory serviceRegistryFactory = new DefaultServiceRegistryFactory(
            repositoryHandlerFactory, configurationContainerFactory, publishArtifactFactoryMock, dependencyFactoryMock,
            projectEvaluator, classGenerator);
    private PluginRegistry pluginRegistry = context.mock(PluginRegistry.class);
    private IProjectRegistry projectRegistry = new DefaultProjectRegistry();
    private GradleInternal gradle = context.mock(GradleInternal.class);

    private ProjectFactory projectFactory;
    private StartParameter startParameterStub = new StartParameter();

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {{
            allowing(repositoryHandlerFactory).createRepositoryHandler(with(any(Convention.class)));
            will(returnValue(repositoryHandler));
        }});
        context.checking(new Expectations() {{
            allowing(gradle).getStartParameter();
            will(returnValue(startParameterStub));
            allowing(configurationContainerFactory).createConfigurationContainer(with(any(ResolverProvider.class)),
                    with(any(DependencyMetaDataProvider.class)));
            allowing(gradle).getProjectRegistry();
            will(returnValue(projectRegistry));
            allowing(gradle).getPluginRegistry();
            will(returnValue(pluginRegistry));
            allowing(gradle).getBuildScriptClassLoader();
            will(returnValue(buildScriptClassLoader));
            allowing(gradle).getGradleUserHomeDir();
            will(returnValue(new File("gradleUserHomeDir")));
        }});

        projectFactory = new ProjectFactory(serviceRegistryFactory, null);
    }

    @Test
    public void testConstructsRootProjectWithBuildFile() throws IOException {
        File buildFile = new File(rootDir, "build.gradle");
        FileUtils.writeStringToFile(buildFile, "build");
        ProjectDescriptor descriptor = descriptor("somename", rootDir, buildFile, "build.gradle");

        DefaultProject project = projectFactory.createProject(descriptor, null, gradle);

        assertEquals("somename", project.getName());
        assertEquals(buildFile, project.getBuildFile());
        assertNull(project.getParent());
        assertSame(rootDir, project.getRootDir());
        assertSame(rootDir, project.getProjectDir());
        assertSame(project, project.getRootProject());
        checkProjectResources(project);

        ScriptSource expectedScriptSource = new FileScriptSource("build file", buildFile);
        assertThat(project.getBuildScriptSource(), Matchers.reflectionEquals(expectedScriptSource));
    }

    @Test
    public void testConstructsChildProjectWithBuildFile() throws IOException {
        File buildFile = new File(projectDir, "build.gradle");
        FileUtils.writeStringToFile(buildFile, "build");

        ProjectDescriptor rootDescriptor = descriptor("root");
        ProjectDescriptor parentDescriptor = descriptor("parent");
        ProjectDescriptor projectDescriptor = descriptor("somename", projectDir, buildFile, "build.gradle");

        DefaultProject rootProject = projectFactory.createProject(rootDescriptor, null, gradle);
        DefaultProject parentProject = projectFactory.createProject(parentDescriptor, rootProject, gradle);

        DefaultProject project = projectFactory.createProject(projectDescriptor, parentProject, gradle);

        assertEquals("somename", project.getName());
        assertEquals(buildFile, project.getBuildFile());
        assertSame(parentProject, project.getParent());
        assertSame(rootDir, project.getRootDir());
        assertSame(projectDir, project.getProjectDir());
        assertSame(rootProject, project.getRootProject());
        assertSame(project, parentProject.getChildProjects().get("somename"));
        checkProjectResources(project);

        ScriptSource expectedScriptSource = new FileScriptSource("build file", buildFile);
        assertThat(project.getBuildScriptSource(), Matchers.reflectionEquals(expectedScriptSource));
    }
    
    @Test
    public void testAddsProjectToProjectRegistry() throws IOException {
        ProjectDescriptor rootDescriptor = descriptor("root");
        ProjectDescriptor parentDescriptor = descriptor("somename");

        DefaultProject rootProject = projectFactory.createProject(rootDescriptor, null, gradle);
        DefaultProject project = projectFactory.createProject(parentDescriptor, rootProject, gradle);

        assertThat(projectRegistry.getProject(":somename"), sameInstance((ProjectIdentifier) project));
    }

    @Test
    public void testUsesEmptyBuildFileWhenBuildFileIsMissing() {

        DefaultProject rootProject = projectFactory.createProject(descriptor("root"), null, gradle);
        DefaultProject project = projectFactory.createProject(descriptor("somename", projectDir), rootProject, gradle);

        ScriptSource expectedScriptSource = new StringScriptSource("empty build file", "");
        assertThat(project.getBuildScriptSource(), Matchers.reflectionEquals(expectedScriptSource));
    }

    @Test
    public void testConstructsRootProjectWithEmbeddedBuildScript() {
        ScriptSource expectedScriptSource = context.mock(ScriptSource.class);

        ProjectFactory projectFactory = new ProjectFactory(serviceRegistryFactory, expectedScriptSource);

        DefaultProject project = projectFactory.createProject(descriptor("somename"), null, gradle);

        assertEquals("somename", project.getName());
        assertEquals(new File(rootDir, "build.gradle"), project.getBuildFile());
        assertSame(rootDir, project.getRootDir());
        assertSame(rootDir, project.getProjectDir());
        assertNull(project.getParent());
        assertSame(project, project.getRootProject());
        assertNotNull(project.getConvention());
        checkProjectResources(project);
        assertSame(project.getBuildScriptSource(), expectedScriptSource);
    }

    private ProjectDescriptor descriptor(String name) {
        return descriptor(name, rootDir);
    }

    private ProjectDescriptor descriptor(String name, File projectDir) {
        return descriptor(name, projectDir, new File(projectDir, "build.gradle"), "build.gradle");
    }

    private ProjectDescriptor descriptor(final String name, final File projectDir, final File buildFile, final String buildFileName) {
        final ProjectDescriptor descriptor = context.mock(ProjectDescriptor.class, name);

        context.checking(new Expectations(){{
            allowing(descriptor).getName();
            will(returnValue(name));
            allowing(descriptor).getProjectDir();
            will(returnValue(projectDir));
            allowing(descriptor).getBuildFile();
            will(returnValue(buildFile));
            allowing(descriptor).getBuildFileName();
            will(returnValue(buildFileName));
        }});
        return descriptor;
    }

    private void checkProjectResources(DefaultProject project) {
        assertSame(projectRegistry, project.getProjectRegistry());
        assertSame(repositoryHandler, project.getRepositories());
        assertSame(gradle, project.getGradle());
    }
}
