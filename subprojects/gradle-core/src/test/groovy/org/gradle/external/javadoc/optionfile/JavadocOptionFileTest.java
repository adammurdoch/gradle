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

package org.gradle.external.javadoc.optionfile;

import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Tom Eyckmans
 */
public class JavadocOptionFileTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private JavadocOptionFileOption optionFileOptionMock;
    private final String optionName = "testOption";
    

    private JavadocOptionFile optionFile;

    @Before
    public void setUp() {
        context.setImposteriser(ClassImposteriser.INSTANCE);

        optionFileOptionMock = context.mock(JavadocOptionFileOption.class);

        optionFile = new JavadocOptionFile();
    }

    @Test
    public void testDefaults() {
        assertNotNull(optionFile.getOptions());
        assertTrue(optionFile.getOptions().isEmpty());

        assertNotNull(optionFile.getPackageNames());
        assertNotNull(optionFile.getPackageNames().getValue());
        assertTrue(optionFile.getPackageNames().getValue().isEmpty());

        assertNotNull(optionFile.getSourceNames());
        assertNotNull(optionFile.getSourceNames().getValue());
        assertTrue(optionFile.getSourceNames().getValue().isEmpty());
    }

    @Test
    public void testAddOption() {
        context.checking(new Expectations() {{
            one(optionFileOptionMock).getOption();
            returnValue(optionName);
        }});

        optionFile.addOption(optionFileOptionMock);
    }
}
