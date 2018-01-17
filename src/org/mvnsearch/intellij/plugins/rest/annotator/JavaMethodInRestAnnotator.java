package org.mvnsearch.intellij.plugins.rest.annotator;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
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
                String referName = text.substring(text.indexOf("@see") + 4).trim();
                //resolve class name & method name
                String className = null;
                String methodName = null;
                if (referName.contains(" ")) {
                    referName = referName.substring(0, referName.indexOf(" "));
                }
                //method refer  #methodName
                if (referName.startsWith("#")) {
                    methodName = referName.substring(1);
                    className = restFile.getName().replace(".http", "");
                }
                //class & method ref  com.xxx.Class1#method1
                if (referName.contains("#")) {
                    className = referName.substring(0, referName.indexOf("#"));
                    methodName = referName.substring(referName.indexOf("#") + 1);
                }
                if (className == null) {
                    className = referName;
                }
                //method reference  com.foobar.Class.method1
                if (className.contains(".") && Character.isLowerCase(className.charAt(className.lastIndexOf(".") + 1))) {
                    String fullClassName = className.substring(0, className.lastIndexOf("."));
                    methodName = className.substring(className.lastIndexOf(".") + 1);
                    className = fullClassName;
                }
                // class & navigation element
                Project project = psiElement.getProject();
                PsiClass referPsiClass = null;
                PsiElement navElement = null;
                if (className.contains(".")) {  //full name
                    referPsiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
                } else {  // search by short name
                    PsiClass[] classesByName = PsiShortNamesCache.getInstance(psiElement.getProject()).getClassesByName(className, restFile.getResolveScope());
                    if (classesByName.length > 0) {
                        referPsiClass = classesByName[0];
                    }
                }
                //create icon gutter
                if (referPsiClass != null) {
                    if (methodName != null) {
                        for (PsiMethod psiMethod : referPsiClass.getMethods()) {
                            if (psiMethod.getName().equalsIgnoreCase(methodName)) {
                                navElement = psiMethod;
                                break;
                            }
                        }
                    }
                    if (navElement == null) {
                        navElement = referPsiClass;
                    }
                    Icon icon = (psiElement instanceof PsiClass) ? AllIcons.FileTypes.Java : AllIcons.Nodes.MethodReference;
                    NavigationGutterIconBuilder<PsiElement> iconBuilder = NavigationGutterIconBuilder.create(icon);
                    iconBuilder
                            .setTargets(navElement)
                            .setPopupTitle("Navigate to handler")
                            .setAlignment(GutterIconRenderer.Alignment.LEFT)
                            .install(annotationHolder, leafPsiElement);
                }

            }
        }
    }
}
