package org.mvnsearch.intellij.plugins.rest.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

/**
 * http call kotlin annotator
 *
 * @author linux_china
 */
public class HttpCallKotlinAnnotator extends HttpCallBaseAnnotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (psiElement instanceof KtNamedFunction) {
            KtNamedFunction ktFun = (KtNamedFunction) psiElement;
            KtClass psiClass = (KtClass) ktFun.getParent().getParent();
            PsiDirectory directory = psiClass.getContainingFile().getParent();
            String rstFileName = psiClass.getName() + ".http";
            HttpRequestPsiFile rstFile = (HttpRequestPsiFile) directory.findFile(rstFileName);
            bindHttpCall(annotationHolder, rstFile, ktFun, ktFun.getName());
        }

    }

}
