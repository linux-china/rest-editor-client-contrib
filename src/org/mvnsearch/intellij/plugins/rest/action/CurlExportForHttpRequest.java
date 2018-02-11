package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ws.http.request.HttpRequestVariableSubstitutor;
import com.intellij.ws.http.request.psi.HttpMethod;
import com.intellij.ws.http.request.psi.HttpRequest;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.stream.Collectors;

/**
 * export curl command
 *
 * @author linux_china
 */
@SuppressWarnings("Duplicates")
public class CurlExportForHttpRequest extends PsiElementBaseIntentionAction {

    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Export curl Command";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Export curl Command";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement parent = psiElement.getParent();
        if (parent instanceof HttpMethod) {
            HttpMethod httpMethod = (HttpMethod) parent;
            String curlCommand = generateCurlCommand((HttpRequest) httpMethod.getParent());
            CopyPasteManager.getInstance().setContents(new StringSelection(curlCommand));
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        return psiElement.getParent() instanceof HttpMethod;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    /**
     * @noinspection StringConcatenationInsideStringBufferAppend
     */
    public String generateCurlCommand(HttpRequest httpRequest) {
        Project project = httpRequest.getProject();
        HttpRequestVariableSubstitutor substitutor = HttpRequestVariableSubstitutor.getDefault(project);
        StringBuilder builder = new StringBuilder("curl -v");
        //method
        String method = httpRequest.getHttpMethod();
        builder.append(" -X " + method);
        //header
        builder.append(
                httpRequest.getHeaderFieldList().stream()
                        .map(headerField -> " -H '" + headerField.getName() + ": " + headerField.getValue(substitutor) + "'")
                        .collect(Collectors.joining(" "))
        );
        //body
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            if (httpRequest.getRequestBody() != null && httpRequest.getRequestBody().getText() != null) {
                builder.append(" -d '" + httpRequest.getRequestBody().getText().replaceAll(System.lineSeparator(), "\\\\" + System.lineSeparator()) + "'");
            }
        }
        String httpUrl = httpRequest.getHttpUrl(substitutor);
        builder.append(" " + httpUrl);
        return builder.toString();
    }


}
