package org.mvnsearch.intellij.plugins.rest;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * java method in rest annotator
 *
 * @author linux_china
 */
public class JavaMethodInRestAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        PsiFile restFile = psiElement.getContainingFile();
        if (psiElement instanceof LeafPsiElement && restFile instanceof HttpRequestPsiFile) {
            LeafPsiElement leafPsiElement = (LeafPsiElement) psiElement;
            String text = leafPsiElement.getText();
            if (text.contains("@see")) {
                String className = text.substring(text.indexOf("@see") + 4).trim();
                String methodName = null;
                if (className.contains(" ")) {
                    className = className.substring(0, className.indexOf(" "));
                }
                //method refer
                if (className.startsWith("#")) {
                    methodName = className.substring(1);
                    className = restFile.getName().replace(".http", "");
                }
                if (className.contains("#")) {
                    className = className.substring(0, className.indexOf("#"));
                    methodName = className.substring(className.indexOf("#") + 1);
                }
                PsiClass[] classesByName = PsiShortNamesCache.getInstance(psiElement.getProject()).getClassesByName(className, restFile.getResolveScope());
                for (PsiClass psiClass : classesByName) {
                    PsiElement navElement = psiClass;
                    if (methodName != null) {
                        for (PsiMethod psiMethod : psiClass.getMethods()) {
                            if (psiMethod.getName().equalsIgnoreCase(methodName)) {
                                navElement = psiMethod;
                                break;
                            }
                        }
                    }
                    Icon icon = (psiElement instanceof PsiClass) ? AllIcons.FileTypes.Java : AllIcons.Nodes.MethodReference;
                    NavigationGutterIconBuilder<PsiElement> iconBuilder = NavigationGutterIconBuilder.create(icon);
                    iconBuilder
                            .setTargets(navElement)
                            .setPopupTitle("Navigate to handler")
                            .setAlignment(GutterIconRenderer.Alignment.LEFT)
                            .install(annotationHolder, leafPsiElement);
                    break;
                }

            }
        }
    }
}
