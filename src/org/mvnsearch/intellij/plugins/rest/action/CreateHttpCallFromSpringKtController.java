package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ws.http.request.HttpRequestPsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * create http call for Kotlin Spring Controller
 *
 * @author linux_china
 */
@SuppressWarnings("Duplicates")
public class CreateHttpCallFromSpringKtController extends HttpRequestBaseIntentionAction {

    private List<String> mappingAnnotationClasses = Arrays.asList(
            "RequestMapping",
            "GetMapping",
            "PostMapping");
    private List<String> controllerAnnotationClasses = Arrays.asList(
            "Controller",
            "RestController");

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
        if (parent != null && (parent instanceof KtNamedFunction || parent instanceof KtClass)) {
            KtClass ktClass;
            List<KtNamedFunction> actionMethods = new ArrayList<>();
            if (parent instanceof KtNamedFunction) {
                KtNamedFunction ktFun = (KtNamedFunction) parent;
                ktClass = (KtClass) ktFun.getParent().getParent();
                if (findAnnotation(ktFun, mappingAnnotationClasses) != null) {
                    actionMethods.add(ktFun);
                }
            } else {
                ktClass = (KtClass) parent;
                if (ktClass.getBody() != null) {
                    for (PsiElement child : ktClass.getBody().getChildren()) {
                        if (child instanceof KtNamedFunction) {
                            if (findAnnotation((KtNamedFunction) child, mappingAnnotationClasses) != null) {
                                actionMethods.add((KtNamedFunction) child);
                            }
                        }
                    }
                }
            }
            if (ktClass != null && !actionMethods.isEmpty()) {
                List<HttpCall> calls = actionMethods.stream().map(this::createFromRequestMappingMethod).collect(Collectors.toList());
                PsiDirectory directory = ktClass.getContainingFile().getParent();
                String restFileName = ktClass.getName() + ".http";
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
        if (psiElement.getContainingFile() instanceof KtFile) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof KtNamedFunction) {
                KtNamedFunction ktFun = (KtNamedFunction) parent;
                return findAnnotation(ktFun, mappingAnnotationClasses) != null;
            } else if (parent instanceof KtClass) {
                KtClass javaClass = (KtClass) parent;
                return findAnnotation(javaClass, controllerAnnotationClasses) != null;
            }
        }
        return false;
    }

    private HttpCall createFromRequestMappingMethod(KtNamedFunction ktFun) {
        HttpCall httpCall = new HttpCall();
        httpCall.setComment("@see #" + ktFun.getName());
        KtClass ktClass = (KtClass) ktFun.getParent().getParent();
        //url
        KtAnnotationEntry ktAnnotationEntry = findAnnotation(ktClass, mappingAnnotationClasses);
        KtAnnotationEntry mappingAnnotationOnMethod = findAnnotation(ktFun, mappingAnnotationClasses);
        String path = getAttributeValue(ktAnnotationEntry, "value", true);
        path = path + getAttributeValue(mappingAnnotationOnMethod, "value", true);
        httpCall.setUrl("{{host}}" + path);
        //action
        if (mappingAnnotationOnMethod != null && Objects.equals(mappingAnnotationOnMethod.getTypeReference().getText(), "PostMapping")) {
            httpCall.setAction("POST");
        } else {
            httpCall.setAction("GET");
        }
        //parameter
        if (httpCall.getAction().equals("GET")) {
            for (KtParameter psiParameter : ktFun.getValueParameterList().getParameters()) {
                List<KtAnnotationEntry> annotations = psiParameter.getAnnotationEntries();
                for (KtAnnotationEntry annotation : annotations) {
                    if (Objects.equals(annotation.getText(), "@RequestParam")) {
                        httpCall.addParam(psiParameter.getTypeReference().getText(), "");
                    }
                }
            }
        }
        if (httpCall.getAction().equals("POST")) {
            for (KtParameter psiParameter : ktFun.getValueParameterList().getParameters()) {
                List<KtAnnotationEntry> annotations = psiParameter.getAnnotationEntries();
                for (KtAnnotationEntry annotation : annotations) {
                    if (Objects.equals(annotation.getText(), "@RequestBody")) {
                        String paramType = psiParameter.getTypeReference().getText();
                        switch (paramType) {
                            case "String":
                                httpCall.setContentType("text/plain");
                                break;
                            case "byte[]":
                                httpCall.setContentType("application/binary");
                                break;
                            default:
                                httpCall.setContentType("application/json");
                                PsiClass psiClass = JavaPsiFacade.getInstance(psiParameter.getProject()).findClass(paramType, GlobalSearchScope.allScope(psiParameter.getProject()));
                                httpCall.setPayload(generateJsonExampleForPsiClass(psiClass));
                                break;
                        }
                    }
                }
            }
        }
        return httpCall;
    }


}
