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

package org.gradle.api.internal.artifacts.dependencies;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.dsl.ConfigurationHandler;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.util.HelperUtil;
import org.gradle.util.WrapUtil;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Set;

/**
 * @author Hans Dockter
 */
@RunWith(JMock.class)
public class DefaultProjectDependencyTest extends AbstractModuleDependencyTest {
    private DefaultProjectDependency projectDependency;
    private Project dependencyProject;

    protected AbstractModuleDependency getDependency() {
        return projectDependency;
    }

    protected AbstractModuleDependency createDependency(String group, String name, String version) {
        return createDependency(group, name, version, null);    
    }

    protected AbstractModuleDependency createDependency(String group, String name, String version, String configuration) {
        DefaultProject dependencyProject = HelperUtil.createRootProject(new File(name));
        dependencyProject.setGroup(group);
        dependencyProject.setVersion(version);
        DefaultProjectDependency projectDependency;
        if (configuration != null) {
            projectDependency = new DefaultProjectDependency(dependencyProject, configuration);
        } else {
            projectDependency = new DefaultProjectDependency(dependencyProject); 
        }
        return projectDependency;
    }

    @Before public void setUp() {
        dependencyProject = HelperUtil.createRootProject(new File("dependency"));
        projectDependency = new DefaultProjectDependency(dependencyProject);
    }
    
    @Test
    public void init() {
        projectDependency = new DefaultProjectDependency(dependencyProject);
        assertTrue(projectDependency.isTransitive());
        assertEquals(dependencyProject.getName(), projectDependency.getName());
        assertEquals(dependencyProject.getGroup().toString(), projectDependency.getGroup());
        assertEquals(dependencyProject.getVersion().toString(), projectDependency.getVersion());
    }

    @Test
    public void getConfiguration() {
        final Configuration configurationDummy = context.mock(Configuration.class);
        final ConfigurationHandler configurationHandlerStub = context.mock(ConfigurationHandler.class);
        final Project projectStub = context.mock(Project.class);
        context.checking(new Expectations() {{
            allowing(projectStub).getConfigurations();
            will(returnValue(configurationHandlerStub));

            allowing(configurationHandlerStub).getByName("conf1");
            will(returnValue(configurationDummy));
        }});
        DefaultProjectDependency projectDependency = new DefaultProjectDependency(projectStub, "conf1");
        assertThat(projectDependency.getProjectConfiguration(), sameInstance(configurationDummy));
    }

    @Test
    public void resolve() {
        final Configuration configurationStub = context.mock(Configuration.class);
        final ConfigurationHandler configurationHandlerStub = context.mock(ConfigurationHandler.class);
        final Project projectStub = context.mock(Project.class);
        final SelfResolvingDependency selfResolvingDependency = context.mock(SelfResolvingDependency.class);
        final Set<File> selfResolvingFiles = WrapUtil.toSet(new File("somePath"));
        context.checking(new Expectations() {{

            allowing(projectStub).getConfigurations();
            will(returnValue(configurationHandlerStub));

            allowing(configurationHandlerStub).getByName("conf1");
            will(returnValue(configurationStub));

            allowing(configurationStub).getAllDependencies(SelfResolvingDependency.class);
            will(returnValue(WrapUtil.toSet(selfResolvingDependency)));

            allowing(selfResolvingDependency).resolve();
            will(returnValue(selfResolvingFiles));
        }});
        DefaultProjectDependency projectDependency = new DefaultProjectDependency(projectStub, "conf1");
        assertThat(projectDependency.resolve(), equalTo(selfResolvingFiles));
    }

    @Test
    public void contentEqualsWithEqualDependencies() {
        ProjectDependency dependency1 = createProjectDependency();
        ProjectDependency dependency2 = createProjectDependency();
        assertThat(dependency1.contentEquals(dependency2), equalTo(true));
    }

    @Test
    public void contentEqualsWithNonEqualDependencies() {
        ProjectDependency dependency1 = createProjectDependency();
        ProjectDependency dependency2 = createProjectDependency();
        dependency2.setTransitive(!dependency1.isTransitive());
        assertThat(dependency1.contentEquals(dependency2), equalTo(false));
    }

    @Test
    public void copy() {
        ProjectDependency dependency = createProjectDependency();
        ProjectDependency copiedDependency = (ProjectDependency) dependency.copy();
        assertDeepCopy(dependency, copiedDependency);
        assertThat(copiedDependency.getDependencyProject(), sameInstance(dependency.getDependencyProject()));
    }

    private ProjectDependency createProjectDependency() {
        ProjectDependency projectDependency = new DefaultProjectDependency(HelperUtil.createRootProject(), "conf");
        projectDependency.addArtifact(new DefaultDependencyArtifact("name", "type", "ext", "classifier", "url"));
        return projectDependency;
    }

    @Test
    @Override
    public void equality() {
        assertThat(new DefaultProjectDependency(dependencyProject), equalTo(new DefaultProjectDependency(dependencyProject)));
        assertThat(new DefaultProjectDependency(dependencyProject).hashCode(), equalTo(new DefaultProjectDependency(dependencyProject).hashCode()));
        assertThat(new DefaultProjectDependency(dependencyProject, "conf1"), equalTo(new DefaultProjectDependency(dependencyProject, "conf1")));
        assertThat(new DefaultProjectDependency(dependencyProject, "conf1").hashCode(), equalTo(new DefaultProjectDependency(dependencyProject, "conf1").hashCode()));
        assertThat(new DefaultProjectDependency(dependencyProject, "conf1"), not(equalTo(new DefaultProjectDependency(dependencyProject, "conf2"))));
        assertThat(new DefaultProjectDependency(dependencyProject), not(equalTo(new DefaultProjectDependency(context.mock(Project.class, "otherProject")))));
    }


}
