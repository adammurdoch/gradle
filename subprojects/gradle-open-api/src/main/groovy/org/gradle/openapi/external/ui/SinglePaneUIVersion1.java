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
package org.gradle.openapi.external.ui;

import javax.swing.JComponent;
import java.io.File;

/*
 This is a gradle UI that is entirely within a single panel (and only a panel;
 no dialog or frame). This is meant to simplify how a plugin can interact with
 gradle.

 To use this, you'll want to get an instance of this from Gradle. Then setup
 your UI and add this to it via getComponent. Then call aboutToShow before
 you display your UI. Call close before you hide your UI. You'll need to set
 the current directory (at any time) so gradle knows where your project is
 located.

 @author mhunsicker
  */
public interface SinglePaneUIVersion1
{
   /*
      @return the panel for this pane. This can be inserted directly into your UI.
      @author mhunsicker
   */
   public JComponent getComponent();

   /*
      Call this whenever you're about to show this panel. We'll do whatever
      initialization is necessary.
      @author mhunsicker
   */
   public void aboutToShow();

   //
            public interface CloseInteraction
            {
               /*
                  This is called if gradle tasks are being executed and you want to know if
                  we can close. Ask the user.
                  @return true if the user confirms cancelling the current tasks. False if not.
                  @author mhunsicker
               */
               public boolean promptUserToConfirmClosingWhileBusy();
            }

   /*
      Call this to deteremine if you can close this pane. if we're busy, we'll
      ask the user if they want to close.

      @param  closeInteraction allows us to interact with the user
      @return true if we can close, false if not.
      @author mhunsicker
   */
   public boolean canClose( CloseInteraction closeInteraction );

   /*
      Call this before you close the pane. This gives it an opportunity to do
      cleanup. You probably should call canClose before this. It gives the
      app a chance to cancel if its busy.
      @author mhunsicker
   */
   public void close();

   /*
      @return the root directory of your gradle project.
      @author mhunsicker
   */
   public File getCurrentDirectory();

   /*
      @param  currentDirectory the new root directory of your gradle project.
      @author mhunsicker
   */
   public void setCurrentDirectory( File currentDirectory );

   /*
      @return the gradle home directory. Where gradle is installed.
      @author mhunsicker
   */
   public File getGradleHomeDirectory();

   /*
      This is called to get a custom gradle executable file. If you don't run
      gradle.bat or gradle shell script to run gradle, use this to specify
      what you do run. Note: we're going to pass it the arguments that we would
      pass to gradle so if you don't like that, see alterCommandLineArguments.
      Normaly, this should return null.
      @return the Executable to run gradle command or null to use the default
      @author mhunsicker
   */
   public File getCustomGradleExecutable();

   /*
      Call this to add an additional tab to the gradle UI. You can call this
      at any time.

      @param  index             the index of where to add the tab.
      @param  gradleTabVersion1 the tab to add.
      @author mhunsicker
   */
   public void addTab( int index, GradleTabVersion1 gradleTabVersion1 );

   /*
      Call this to remove one of your own tabs from this.
      @param  gradleTabVersion1 the tab to remove
      @author mhunsicker
   */
   public void removeTab( GradleTabVersion1 gradleTabVersion1 );

   /*
      @return the total number of tabs.
      @author mhunsicker
   */
   public int getGradleTabCount();

   /*
      @param  index      the index of the tab
      @return the name of the tab at the specified index.
      @author mhunsicker
   */
   public String getGradleTabName( int index );

   /*
      This allows you to add a listener that can add additional command line
      arguments whenever gradle is executed. This is useful if you've customized
      your gradle build and need to specify, for example, an init script.

      @param  listener   the listener that modifies the command line arguments.
      @author mhunsicker
   */
   public void addCommandLineArgumentAlteringListener( CommandLineArgumentAlteringListenerVersion1 listener );

   public void removeCommandLineArgumentAlteringListener( CommandLineArgumentAlteringListenerVersion1 listener );
   
   /*
      Call this to execute the given gradle command.

      @param  commandLineArguments the command line arguments to pass to gradle.
      @param displayName           the name displayed in the UI for this command
      @author mhunsicker
   */
   public void executeCommand( String commandLineArguments, String displayName );

}
