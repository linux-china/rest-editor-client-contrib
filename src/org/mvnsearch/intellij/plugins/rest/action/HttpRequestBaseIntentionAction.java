package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

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

    protected void appendContent(HttpRequestPsiFile restFile, List<HttpCall> httpCalls) throws IOException {
        VirtualFile virtualFile = restFile.getVirtualFile();
        StringBuilder builder = new StringBuilder();
        for (HttpCall httpCall : httpCalls) {
            builder.append("\n");
            builder.append(httpCall.toString());
        }
        String content = new String(virtualFile.contentsToByteArray()) + builder.toString();
        virtualFile.setBinaryContent(content.getBytes());
    }

    protected String generateSeeRefer(PsiMethod psiMethod) {
        return "@see #" + psiMethod.getName();
    }

    /**
     * get attribute value
     *
     * @param psiAnnotation PSI annotation
     * @param attributeName attribute name
     * @param isDefault     is default value
     * @return attribute value
     */
    protected String getAttributeValue(PsiAnnotation psiAnnotation, String attributeName, boolean isDefault) {
        if (psiAnnotation == null) return "";
        String attributeValue = "";
        PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();
        if (attributes.length == 1 && isDefault) {
            PsiNameValuePair attribute = attributes[0];
            if (attributeName.equals(attribute.getName()) || (attribute.getName() == null && isDefault)) {
                attributeValue = attribute.getLiteralValue();
            }
        } else if (attributes.length > 1) {
            for (PsiNameValuePair attribute : attributes) {
                if (attributeName.equals(attribute.getName())) {
                    attributeValue = attribute.getLiteralValue();
                }
            }
        }
        return attributeValue;
    }
}
