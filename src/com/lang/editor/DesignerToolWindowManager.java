/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lang.editor;

import com.intellij.designer.DesignerEditorPanelFacade;
import com.intellij.designer.LightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.lang.util.UMLIconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexander Lobas
 */
public class DesignerToolWindowManager extends AbstractToolWindowManager implements Disposable {
  private final DesignerToolWindow myToolWindowPanel;

  public DesignerToolWindowManager(Project project, FileEditorManager fileEditorManager) {
    super(project, fileEditorManager);
    myToolWindowPanel = ApplicationManager.getApplication().isHeadlessEnvironment() ? null : new DesignerToolWindow(project, false);
    if (myToolWindowPanel != null) {
      Disposer.register(this, () -> myToolWindowPanel.dispose());
    }
  }

  public static DesignerToolWindow getInstance(UMLEditor designer) {
    DesignerToolWindowManager manager = getInstance(designer.getProject());
    if (manager.isEditorMode()) {
      return (DesignerToolWindow)manager.getContent(designer);
    }
    return manager.myToolWindowPanel;
  }

  public static DesignerToolWindowManager getInstance(Project project) {
    return project.getComponent(DesignerToolWindowManager.class);
  }

  @Nullable
  public UMLEditor getActiveFormEditor() {
    return (UMLEditor)getActiveDesigner();
  }

  @Override
  protected void initToolWindow() {
    myToolWindow = ToolWindowManager.getInstance(myProject).registerToolWindow(UMLBundle.message("toolwindow.ui.designer.name"),
                                                                               false, getAnchor(), myProject, true);
    myToolWindow.setIcon(UMLIconLoader.UML_ICON);

    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
      myToolWindow.getComponent().putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true");
    }

    initGearActions();

    ContentManager contentManager = myToolWindow.getContentManager();
    Content content =
      contentManager.getFactory()
        .createContent(myToolWindowPanel.getToolWindowPanel(), UMLBundle.message("toolwindow.ui.designer.title"), false);
    content.setCloseable(false);
    content.setPreferredFocusableComponent(myToolWindowPanel.getComponentTree());
    contentManager.addContent(content);
    contentManager.setSelectedContent(content, true);
    myToolWindow.setAvailable(false, null);
  }

  @Override
  protected void updateToolWindow(@Nullable DesignerEditorPanelFacade designer) {
//    myToolWindowPanel.update((UMLEditor)designer);

    if (designer == null) {
      myToolWindow.setAvailable(false, null);
    }
    else {
      myToolWindow.setAvailable(true, null);
      myToolWindow.show(null);
    }
  }

  @Override
  protected ToolWindowAnchor getAnchor() {
    return ToolWindowAnchor.LEFT;
  }

  @Override
  protected LightToolWindow createContent(@NotNull DesignerEditorPanelFacade designer) {
    DesignerToolWindow toolWindowContent = new DesignerToolWindow(myProject, false);
//    toolWindowContent.update((UMLEditor)designer);

    return createContent(designer,
                         toolWindowContent,
                         UMLBundle.message("toolwindow.ui.designer.title"),
                         UMLIconLoader.UML_ICON,
                         toolWindowContent.getToolWindowPanel(),
                         toolWindowContent.getComponentTree(),
                         320,
                         null);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "UIDesignerToolWindowManager";
  }
}