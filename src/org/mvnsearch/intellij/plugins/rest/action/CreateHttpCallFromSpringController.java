package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * create http call for Spring Controller
 *
 * @author linux_china
 */
public class CreateHttpCallFromSpringController extends HttpRequestBaseIntentionAction {

    private List<String> mappingAnnotationClasses = Arrays.asList(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping");
    private List<String> controllerAnnotationClasses = Arrays.asList(
            "org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController");

    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Create Http REST Call";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create Http REST Call for Spring Controller";
    }


    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement parent = psiElement.getParent();
        if (parent != null && (parent instanceof PsiMethod || parent instanceof PsiClass)) {
            PsiClass psiClass = null;
            List<PsiMethod> actionMethods = new ArrayList<>();
            if (parent instanceof PsiMethod) {
                PsiMethod javaMethod = (PsiMethod) parent;
                psiClass = javaMethod.getContainingClass();
                if (findAnnotation(javaMethod, mappingAnnotationClasses) != null) {
                    actionMethods.add(javaMethod);
                }
            } else {
                psiClass = (PsiClass) parent;
                for (PsiMethod psiMethod : psiClass.getMethods()) {
                    if (findAnnotation(psiMethod, mappingAnnotationClasses) != null) {
                        actionMethods.add(psiMethod);
                    }
                }
            }
            if (psiClass != null && !actionMethods.isEmpty()) {
                List<HttpCall> calls = actionMethods.stream().map(this::createFromRequestMappingMethod).collect(Collectors.toList());
                PsiDirectory directory = psiClass.getContainingFile().getParent();
                String restFileName = psiClass.getName() + ".http";
                try {
                    HttpRequestPsiFile restFile = getOrCreateHttpRequestFile(directory, restFileName);
                    appendContent(restFile, calls);
                } catch (Exception ignore) {

                }
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement.getContainingFile() instanceof PsiJavaFile) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiMethod) {
                PsiMethod javaMethod = (PsiMethod) parent;
                return findAnnotation(javaMethod, mappingAnnotationClasses) != null;
            } else if (parent instanceof PsiClass) {
                PsiClass javaClass = (PsiClass) parent;
                return findAnnotation(javaClass, controllerAnnotationClasses) != null;
            }
        }
        return false;
    }

    private HttpCall createFromRequestMappingMethod(PsiMethod javaMethod) {
        HttpCall httpCall = new HttpCall();
        httpCall.setComment(generateSeeRefer(javaMethod));
        //url
        PsiAnnotation mappingAnnotationOnClass = findAnnotation(javaMethod.getContainingClass(), mappingAnnotationClasses);
        PsiAnnotation mappingAnnotationOnMethod = findAnnotation(javaMethod, mappingAnnotationClasses);
        String path = getValueInMappingAnnotation(mappingAnnotationOnClass);
        path = path + getValueInMappingAnnotation(mappingAnnotationOnMethod);
        httpCall.setUrl("http://{{host}}" + path);
        //action
        if (Objects.equals(mappingAnnotationOnMethod.getQualifiedName(), "org.springframework.web.bind.annotation.PostMapping")) {
            httpCall.setAction("POST");
        } else {
            httpCall.setAction("GET");
        }
        //parameter
        if (httpCall.getAction().equals("GET")) {
            for (PsiParameter psiParameter : javaMethod.getParameterList().getParameters()) {
                PsiAnnotation[] annotations = psiParameter.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (Objects.equals(annotation.getQualifiedName(), "org.springframework.web.bind.annotation.RequestParam")) {
                        httpCall.addParam(psiParameter.getName(), "");
                    }
                }
            }
        }
        if (httpCall.getAction().equals("POST")) {
            for (PsiParameter psiParameter : javaMethod.getParameterList().getParameters()) {
                PsiAnnotation[] annotations = psiParameter.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if (Objects.equals(annotation.getQualifiedName(), "org.springframework.web.bind.annotation.RequestBody")) {
                        String paramType = psiParameter.getType().getCanonicalText();
                        switch (paramType) {
                            case "java.lang.String":
                                httpCall.setContentType("text/plain");
                                break;
                            case "byte[]":
                                httpCall.setContentType("application/binary");
                                break;
                            default:
                                httpCall.setContentType("application/json");
                                httpCall.setPayload("{}");
                                break;
                        }
                    }
                }
            }
        }
        return httpCall;
    }

    private String getValueInMappingAnnotation(PsiAnnotation mappingAnnotation) {
        if (mappingAnnotation == null) return "";
        String path = "";
        PsiNameValuePair[] attributes = mappingAnnotation.getParameterList().getAttributes();
        if (attributes.length == 1) {
            path = attributes[0].getLiteralValue();
        } else if (attributes.length > 1) {
            for (PsiNameValuePair attribute : attributes) {
                if ("value".equals(attribute.getName())) {
                    path = attribute.getLiteralValue();
                }
            }
        }
        return path;
    }


}
