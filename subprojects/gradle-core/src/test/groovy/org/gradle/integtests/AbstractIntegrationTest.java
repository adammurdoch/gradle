/*
 * Copyright 2008 the original author or authors.
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
package org.gradle.integtests;

import org.gradle.CacheUsage;
import org.gradle.StartParameter;
import org.gradle.util.HelperUtil;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AbstractIntegrationTest {
    private File testDir;

    @Before
    public void setupTestDir() throws IOException {
        testDir = HelperUtil.makeNewTestDir().getCanonicalFile();
    }

    public File getTestDir() {
        return testDir;
    }

    public TestFile testFile(String name) {
        return new TestFile(new File(getTestDir(), name));
    }

    public TestFile testFile(File dir, String name) {
        return new TestFile(new File(dir, name));
    }

    protected File getTestBuildFile(String name) {
        URL resource = getClass().getResource("testProjects/" + name);
        assertThat(String.format("Could not find resource '%s'", name), resource, notNullValue());
        assertThat(resource.getProtocol(), equalTo("file"));
        TestFile sourceFile = new TestFile(resource);
        TestFile destFile = testFile(sourceFile.getName());
        sourceFile.copyTo(destFile);
        return destFile;
    }

    private StartParameter startParameter() {
        StartParameter parameter = new StartParameter();
        parameter.setGradleHomeDir(testFile("gradle-home"));

        //todo - this should use the src/toplevel/gradle-imports file
        testFile("gradle-home/gradle-imports").writelns(
                "import static org.junit.Assert.*",
                "import static org.hamcrest.Matchers.*",
                "import org.gradle.api.*",
                "import org.gradle.api.file.*",
                "import org.gradle.api.logging.*",
                "import org.gradle.api.tasks.*");

        testFile("gradle-home/plugin.properties").writelns(
                "java=org.gradle.api.plugins.JavaPlugin",
                "groovy=org.gradle.api.plugins.GroovyPlugin",
                "war=org.gradle.api.plugins.WarPlugin",
                "maven=org.gradle.api.plugins.MavenPlugin",
                "code-quality=org.gradle.api.plugins.quality.CodeQualityPlugin"
        );

        parameter.setGradleUserHomeDir(testFile("user-home"));

        parameter.setSearchUpwards(false);
        parameter.setCacheUsage(CacheUsage.ON);
        parameter.setCurrentDir(getTestDir());

        return parameter;
    }

    protected GradleExecuter inTestDirectory() {
        return inDirectory(testDir);
    }

    protected GradleExecuter inDirectory(File directory) {
        return new InProcessGradleExecuter(startParameter()).inDirectory(directory);
    }

    protected GradleExecuter usingBuildFile(File file) {
        StartParameter parameter = startParameter();
        parameter.setBuildFile(file);
        return new InProcessGradleExecuter(parameter);
    }

    protected GradleExecuter usingBuildScript(String script) {
        return new InProcessGradleExecuter(startParameter()).usingBuildScript(script);
    }

    protected GradleExecuter usingProjectDir(File projectDir) {
        StartParameter parameter = startParameter();
        parameter.setProjectDir(projectDir);
        return new InProcessGradleExecuter(parameter);
    }

    protected ArtifactBuilder artifactBuilder() {
        return new GradleBackedArtifactBuilder(new InProcessGradleExecuter(startParameter()), new File(getTestDir(), "artifacts"));
    }
}
