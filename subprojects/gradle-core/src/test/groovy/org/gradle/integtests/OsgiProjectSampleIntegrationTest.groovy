/*
 * Copyright 2007-2008 the original author or authors.
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

package org.gradle.integtests

import java.util.jar.Manifest
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test

/**
 * @author Hans Dockter
 */
@RunWith(DistributionIntegrationTestRunner.class)
class OsgiProjectSampleIntegrationTest {
    // Injected by test runner
    private GradleDistribution dist;
    private GradleExecuter executer;

    @Test
    public void osgiProjectSamples() {
        long start = System.currentTimeMillis()
        File osgiProjectDir = new File(dist.samplesDir, 'osgi')
        executer.inDirectory(osgiProjectDir).withTasks('clean', 'libs').run()
        AntBuilder ant = new AntBuilder()
        ant.unjar(src: "$osgiProjectDir/build/libs/osgi-1.0.jar", dest: "$osgiProjectDir/build")
        Manifest manifest = new Manifest(new FileInputStream("$osgiProjectDir/build/META-INF/MANIFEST.MF"))
        checkManifest(manifest, start)
    }

    static void checkManifest(Manifest manifest, start) {
        assertEquals('Example Gradle Activator', manifest.mainAttributes.getValue('Bundle-Name'))
        assertEquals('2', manifest.mainAttributes.getValue('Bundle-ManifestVersion'))
        assertEquals('Bnd-0.0.255', manifest.mainAttributes.getValue('Tool'))
        assertTrue(start <= Long.parseLong(manifest.mainAttributes.getValue('Bnd-LastModified')))
        assertEquals('1.0', manifest.mainAttributes.getValue('Bundle-Version'))
        assertEquals('gradle_tooling.osgi', manifest.mainAttributes.getValue('Bundle-SymbolicName'))
    }
}
