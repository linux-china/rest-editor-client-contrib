package org.mvnsearch.intellij.plugins.rest;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * create http call from swagger
 *
 * @author linux_china
 */
public class CreateHttpCallFromSwagger extends HttpRequestBaseIntentionAction {
    private List<String> apiOperationAnnotationClasses = Arrays.asList(
            "io.swagger.annotations.ApiOperation");

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
        PsiMethod javaMethod = (PsiMethod) psiElement.getParent();
        if (javaMethod != null) {
            PsiClass psiClass = (PsiClass) javaMethod.getParent();
            HttpCall httpCall = createFromApiOperationMethod(javaMethod);
            PsiDirectory directory = psiClass.getContainingFile().getParent();
            String restFileName = psiClass.getName() + ".http";
            try {
                HttpRequestPsiFile restFile = getOrCreateHttpRequestFile(directory, restFileName);
                appendContent(restFile, httpCall);
            } catch (Exception ignore) {

            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement.getContainingFile() instanceof PsiJavaFile) {
            if (psiElement.getParent() instanceof PsiMethod) {
                PsiMethod javaMethod = (PsiMethod) psiElement.getParent();
                return findAnnotation(javaMethod, apiOperationAnnotationClasses) != null;
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
        httpCall.setComment(generateSeeRefer(javaMethod));
        httpCall.setAction("POST");
        httpCall.setUrl("http://{{host}}/swagger-assistant/" + javaMethod.getContainingClass().getName() + "/" + javaMethod.getName());
        return httpCall;
    }


}
