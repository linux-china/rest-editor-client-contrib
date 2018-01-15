package org.mvnsearch.intellij.plugins.rest;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * http call annotator
 *
 * @author linux_china
 */
public class HttpCallAnnotator implements Annotator {
    private Icon requestIcon = IconLoader.getIcon("/com/intellij/ws/rest/client/icons/request.png");

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod javaMethod = (PsiMethod) psiElement;
            PsiClass psiClass = (PsiClass) javaMethod.getParent();
            PsiDirectory directory = psiClass.getContainingFile().getParent();
            String rstFileName = psiClass.getName() + ".http";
            HttpRequestPsiFile rstFile = (HttpRequestPsiFile) directory.findFile(rstFileName);
            if (rstFile != null) {
                LeafPsiElement[] leafPsiElements = rstFile.findChildrenByClass(LeafPsiElement.class);
                for (LeafPsiElement leafPsiElement : leafPsiElements) {
                    if (leafPsiElement.getText().startsWith("###@see #" + javaMethod.getName())) {
                        NavigationGutterIconBuilder<PsiElement> iconBuilder = NavigationGutterIconBuilder.create(requestIcon);
                        iconBuilder
                                .setTargets(leafPsiElement)
                                .setPopupTitle("Navigate to http client call")
                                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                                .install(annotationHolder, javaMethod);
                        break;
                    }
                }
            }
        }

    }

}
