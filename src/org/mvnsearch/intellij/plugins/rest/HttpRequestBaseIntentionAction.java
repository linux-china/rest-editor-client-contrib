package org.mvnsearch.intellij.plugins.rest;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * http request base intention action
 *
 * @author linux_china
 */
public abstract class HttpRequestBaseIntentionAction extends PsiElementBaseIntentionAction {

    @Nullable
    protected PsiAnnotation findAnnotation(PsiModifierListOwner modifierListOwner, List<String> mappingAnnotationClasses) {
        PsiAnnotation[] annotations = modifierListOwner.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (mappingAnnotationClasses.contains(annotation.getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }

    protected HttpRequestPsiFile getOrCreateHttpRequestFile(PsiDirectory directory, String restFileName) {
        HttpRequestPsiFile restFile = (HttpRequestPsiFile) directory.findFile(restFileName);
        if (restFile == null) {
            restFile = (HttpRequestPsiFile) directory.createFile(restFileName);
        }
        return restFile;
    }

    protected void appendContent(HttpRequestPsiFile restFile, HttpCall httpCall) throws IOException {
        VirtualFile virtualFile = restFile.getVirtualFile();
        String content = new String(virtualFile.contentsToByteArray()) + "\n\n" + httpCall.toString();
        virtualFile.setBinaryContent(content.getBytes());
    }

    protected String generateSeeRefer(PsiMethod psiMethod) {
        //todo add method signature
        return "@see #" + psiMethod.getName();
    }
}
