package org.mvnsearch.intellij.plugins.rest.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * http call annotator
 *
 * @author linux_china
 */
public class HttpCallJavaAnnotator extends HttpCallBaseAnnotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod javaMethod = (PsiMethod) psiElement;
            PsiClass psiClass = (PsiClass) javaMethod.getParent();
            PsiDirectory directory = psiClass.getContainingFile().getParent();
            String rstFileName = psiClass.getName() + ".http";
            HttpRequestPsiFile rstFile = (HttpRequestPsiFile) directory.findFile(rstFileName);
            bindHttpCall(annotationHolder, rstFile, javaMethod, javaMethod.getName());
        }

    }

}
