package org.gradle.api.internal.tasks

import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.PathResolvingFileCollection
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.SourceSet
import org.junit.Test
import static org.gradle.util.Matchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

class DefaultSourceSetTest {
    private final FileResolver resolver = [resolve: {it as File}] as FileResolver

    @Test
    public void hasUsefulDisplayName() {
        SourceSet sourceSet = new DefaultSourceSet('set-name', resolver)
        assertThat(sourceSet.toString(), equalTo('set-name source'));
    }

    @Test public void defaultValues() {
        SourceSet sourceSet = new DefaultSourceSet('set-name', resolver)

        assertThat(sourceSet.classesDir, nullValue())

        assertThat(sourceSet.compileClasspath, instanceOf(PathResolvingFileCollection))
        assertThat(sourceSet.compileClasspath, isEmpty())

        assertThat(sourceSet.runtimeClasspath, instanceOf(PathResolvingFileCollection))
        assertThat(sourceSet.runtimeClasspath, isEmpty())

        assertThat(sourceSet.resources, instanceOf(DefaultSourceDirectorySet))
        assertThat(sourceSet.resources, isEmpty())
        assertThat(sourceSet.resources.displayName, equalTo('set-name resources'))

        assertThat(sourceSet.java, instanceOf(DefaultSourceDirectorySet))
        assertThat(sourceSet.java, isEmpty())
        assertThat(sourceSet.java.displayName, equalTo('set-name Java source'))

        assertThat(sourceSet.javaSourcePatterns.includes, equalTo(['**/*.java'] as Set))
        assertThat(sourceSet.javaSourcePatterns.excludes, isEmpty())

        assertThat(sourceSet.allJava, instanceOf(UnionFileTree))
        assertThat(sourceSet.allJava, isEmpty())
        assertThat(sourceSet.allJava.displayName, equalTo('set-name Java source'))
        assertThat(sourceSet.allJava.sourceCollections, not(isEmpty()))

        assertThat(sourceSet.compileTaskName, equalTo('compileSetName'))
        assertThat(sourceSet.processResourcesTaskName, equalTo('processSetNameResources'))
    }
    
    @Test public void mainSourceSetUsesSpecialCaseTaskNames() {
        SourceSet sourceSet = new DefaultSourceSet('main', resolver)

        assertThat(sourceSet.compileTaskName, equalTo('compile'))
        assertThat(sourceSet.processResourcesTaskName, equalTo('processResources'))
    }

    @Test public void canConfigureResources() {
        SourceSet sourceSet = new DefaultSourceSet('main', resolver)
        sourceSet.resources { srcDir 'src/resources' }
        assertThat(sourceSet.resources.srcDirs, equalTo([new File('src/resources')] as Set))
    }
    
    @Test public void canConfigureJavaSource() {
        SourceSet sourceSet = new DefaultSourceSet('main', resolver)
        sourceSet.java { srcDir 'src/java' }
        assertThat(sourceSet.java.srcDirs, equalTo([new File('src/java')] as Set))
    }
}
