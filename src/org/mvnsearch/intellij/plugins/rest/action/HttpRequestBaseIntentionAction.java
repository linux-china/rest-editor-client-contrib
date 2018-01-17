package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nullable;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * http request base intention action
 *
 * @author linux_china
 */
@SuppressWarnings("SameParameterValue")
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

    /**
     * get method signature
     *
     * @param psiMethod         psi method
     * @param includeParameters include parameters
     * @return signature
     */
    protected String getMethodSignature(PsiMethod psiMethod, boolean includeParameters) {
        //methodName(param_type, param_type)
        StringBuilder builder = new StringBuilder();
        builder.append(psiMethod.getName());
        if (includeParameters) {
            builder.append("(");
            //params type
            String paramsType = Stream.of(psiMethod.getParameterList().getParameters())
                    .map(param -> param.getType().getCanonicalText())
                    .collect(Collectors.joining(","));
            if (paramsType != null) {
                builder.append(paramsType);
            }
            builder.append(")");
        }
        return builder.toString();
    }

    protected String generateJsonExampleForPsiClass(PsiClass psiClass) {
        return "{" +
                Stream.of(psiClass.getFields())
                        .map(psiField -> {
                            return "\"" + psiField.getName() + "\": " + getDefaultJsonValue(psiField.getType().getCanonicalText());
                        })
                        .collect(Collectors.joining(","))
                + "}";

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
        if (attributes.length == 1) {
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

    protected String getDefaultJsonValue(String dataType) {
        if (dataType.endsWith("[]")) return "[]";
        if (dataType.contains("<")) {
            dataType = dataType.substring(0, dataType.indexOf("<"));
        }
        switch (dataType) {
            case "java.lang.String":
                return "\"\"";
            case "java.lang.Boolean":
            case "boolean":
                return "true";
            case "java.lang.Integer":
            case "java.lang.Long":
            case "int":
                return "1";
            case "java.lang.Double":
            case "double":
                return "1.0";
            case "java.lang.Float":
            case "float":
                return "1.0";
            case "java.util.Date":
                return "\"2017-10-10 10:10:10\"";
            case "java.util.Map":
                return "{}";
            case "java.util.List":
                return "[]";
            default:
                return "\"\"";
        }
    }
}
