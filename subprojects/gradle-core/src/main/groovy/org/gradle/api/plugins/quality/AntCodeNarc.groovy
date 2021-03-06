package org.gradle.api.plugins.quality

import org.apache.tools.ant.BuildException
import org.codenarc.ant.CodeNarcTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.AntBuilderAware

class AntCodeNarc {
    def execute(def ant, AntBuilderAware source, File configFile, File reportFile) {
        ant.project.addTaskDefinition('codenarc', CodeNarcTask)
        try {
            ant.codenarc(ruleSetFiles: "file:$configFile", maxPriority1Violations: 0, maxPriority2Violations: 0, maxPriority3Violations: 0) {
                report(type: 'html', toFile: reportFile)
                source.addToAntBuilder(ant, null)
            }
        } catch (BuildException e) {
            if (e.message.matches('Exceeded maximum number of priority \\d* violations.*')) {
                throw new GradleException("CodeNarc check violations were found in $source. See the report at $reportFile.", e)
            }
            throw e
        }
    }
}
