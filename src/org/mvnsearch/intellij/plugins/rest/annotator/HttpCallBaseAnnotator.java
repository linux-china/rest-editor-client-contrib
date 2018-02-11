package org.mvnsearch.intellij.plugins.rest.annotator;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * http call base annotator
 *
 * @author linux_china
 */
public abstract class HttpCallBaseAnnotator implements Annotator {
    private Icon requestIcon = IconLoader.getIcon("/com/intellij/ws/rest/client/icons/request.png");

    public void bindHttpCall(@NotNull AnnotationHolder annotationHolder, HttpRequestPsiFile rstFile, PsiElement targetElement, String referName) {
        if (rstFile != null) {
            LeafPsiElement[] leafPsiElements = rstFile.findChildrenByClass(LeafPsiElement.class);
            for (LeafPsiElement leafPsiElement : leafPsiElements) {
                if (leafPsiElement.getText().startsWith("###@see #" + referName)) {
                    NavigationGutterIconBuilder<PsiElement> iconBuilder = NavigationGutterIconBuilder.create(requestIcon);
                    iconBuilder
                            .setTargets(leafPsiElement)
                            .setPopupTitle("Navigate to http client call")
                            .setAlignment(GutterIconRenderer.Alignment.LEFT)
                            .install(annotationHolder, targetElement);
                    break;
                }
            }
        }
    }
}
