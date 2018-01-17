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
import java.util.stream.Collectors;

/**
 * create http call from swagger
 *
 * @author linux_china
 */
@SuppressWarnings("Duplicates")
public class CreateHttpCallFromSwagger extends HttpRequestBaseIntentionAction {
    private List<String> apiOperationAnnotationClasses = Arrays.asList(
            "io.swagger.annotations.ApiOperation");
    private List<String> apiAnnotationClasses = Arrays.asList(
            "io.swagger.annotations.Api");

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
        return "Create Http REST Call from @ApiOperation";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement parent = psiElement.getParent();
        if (parent != null && (parent instanceof PsiMethod || parent instanceof PsiClass)) {
            PsiClass psiClass;
            List<PsiMethod> actionMethods = new ArrayList<>();
            if (parent instanceof PsiMethod) {
                PsiMethod javaMethod = (PsiMethod) parent;
                psiClass = javaMethod.getContainingClass();
                if (findAnnotation(javaMethod, apiOperationAnnotationClasses) != null) {
                    actionMethods.add(javaMethod);
                }
            } else {
                psiClass = (PsiClass) parent;
                for (PsiMethod psiMethod : psiClass.getMethods()) {
                    if (findAnnotation(psiMethod, apiOperationAnnotationClasses) != null) {
                        actionMethods.add(psiMethod);
                    }
                }
            }
            if (psiClass != null && !actionMethods.isEmpty()) {
                List<HttpCall> calls = actionMethods.stream().map(this::createFromApiOperationMethod).collect(Collectors.toList());
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
                return findAnnotation(javaMethod, apiOperationAnnotationClasses) != null;
            } else if (parent instanceof PsiClass) {
                PsiClass javaClass = (PsiClass) parent;
                return findAnnotation(javaClass, apiAnnotationClasses) != null;
            }
        }
        return false;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    public HttpCall createFromApiOperationMethod(PsiMethod javaMethod) {
        HttpCall httpCall = new HttpCall();
        PsiAnnotation apiOperationAnnotation = findAnnotation(javaMethod, this.apiOperationAnnotationClasses);
        httpCall.setComment(generateSeeRefer(javaMethod) );
        httpCall.setAction("POST");
        httpCall.setUrl("{{host}}/swagger-assistant/" + javaMethod.getContainingClass().getName() + "/" + javaMethod.getName());
        return httpCall;
    }


}
