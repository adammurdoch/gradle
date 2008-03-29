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

package org.gradle.api.tasks.compile

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Task
import org.gradle.api.internal.project.DefaultProject

/**
 * @author Hans Dockter
 */
class GroovyCompile extends Compile {
    List groovySourceDirs = []

    AbstractAntCompile antGroovyCompile = new AntGroovyc()

    GroovyCompile self

    GroovyCompile(DefaultProject project, String name) {
        super(project, name)
        actions << this.&compile
        self = this
    }

    protected void compile(Task task) {
        if (!self.antCompile) throw new InvalidUserDataException("The ant compile command must be set!")
        if (!self.antGroovyCompile) throw new InvalidUserDataException("The ant groovy compile command must be set!")
        if (!self.targetDir) throw new InvalidUserDataException("The target dir is not set, compile can't be triggered!")

        List existingSourceDirs = existentDirsFilter.findExistingDirsAndLogexitMessages(self.sourceDirs)
        List classpath = null
        if (existingSourceDirs) {
            if (!self.sourceCompatibility || !self.targetCompatibility) {
                throw new InvalidUserDataException("The sourceCompatibility and targetCompatibility must be set!")
            }
            classpath = createClasspath()
            antCompile.execute(existingSourceDirs, self.targetDir, classpath, self.sourceCompatibility,
                    self.targetCompatibility, self.compileOptions, project.ant)
        }
        List existingGroovySourceDirs = existentDirsFilter.findExistingDirsAndLogexitMessages(self.groovySourceDirs)
        if (existingGroovySourceDirs) {
            if (!classpath) {classpath = createClasspath()}
            antGroovyCompile.execute(existingGroovySourceDirs, self.targetDir, classpath, self.sourceCompatibility,
                    self.targetCompatibility, self.compileOptions, project.ant)
        }
    }

    private List createClasspath() {
        classpathConverter.createFileClasspath(project.rootDir, self.unmanagedClasspath as Object[]) +
                self.dependencyManager.resolveClasspath(name)
    }

}