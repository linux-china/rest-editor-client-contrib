package org.mvnsearch.intellij.plugins.rest.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ws.http.request.HttpRequestVariableSubstitutor;
import com.intellij.ws.http.request.psi.HttpMethod;
import com.intellij.ws.http.request.psi.HttpRequest;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
            Notifications.Bus.notify(new Notification("Http Request", "curl export", curlCommand, NotificationType.INFORMATION), project);
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
        Map<String, String> restClientEnv = getRestClientEnv(project);
        HttpRequestVariableSubstitutor substitutor = HttpRequestVariableSubstitutor.getDefault(project);
        StringBuilder builder = new StringBuilder("curl -v");
        //method
        String method = httpRequest.getHttpMethod();
        builder.append(" -X " + method);
        //header
        builder.append(
                httpRequest.getHeaderFieldList().stream()
                        .map(headerField -> " -H '" + headerField.getName() + ": " + replaceByEnv(restClientEnv, headerField.getValue(substitutor)) + "'")
                        .collect(Collectors.joining(" "))
        );
        //body
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            if (httpRequest.getRequestBody() != null) {
                String requestBody = httpRequest.getRequestBody().getText();
                if (requestBody != null && !requestBody.isEmpty()) {
                    builder.append(" -d '" + replaceByEnv(restClientEnv, requestBody).replaceAll(System.lineSeparator(), "\\\\" + System.lineSeparator()) + "'");
                }
            }
        }
        String httpUrl = httpRequest.getHttpUrl(substitutor);
        builder.append(" " + replaceByEnv(restClientEnv, httpUrl));
        return builder.toString();
    }

    public Map<String, String> getRestClientEnv(Project project) {
        Map<String, String> env = new HashMap<>();
        try {
            VirtualFile envJsonFile = project.getBaseDir().findChild("rest-client.env.json");
            if (envJsonFile != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map allEnv = objectMapper.readValue(envJsonFile.getInputStream(), LinkedHashMap.class);
                if (!allEnv.isEmpty()) {
                    Map.Entry entry = (Map.Entry) allEnv.entrySet().iterator().next();
                    env = (Map<String, String>) entry.getValue();
                }
            }
        } catch (Exception ignore) {

        }
        return env;
    }

    public String replaceByEnv(Map<String, String> restClientEnv, String text) {
        StringBuilder builder = new StringBuilder();
        String keyword = "{{";
        int start = 0;
        int offset = 0;
        while ((offset = text.indexOf(keyword, start)) > 0) {
            builder.append(text, start, offset);
            Integer end = text.indexOf("}}", offset + keyword.length());
            String var = text.substring(offset + keyword.length(), end);
            if (restClientEnv.containsKey(var)) {
                builder.append(restClientEnv.get(var));
            } else {
                builder.append("{{" + var + "}}");
            }
            start = end + 2;
        }
        builder.append(text, start, text.length());
        return builder.toString();
    }


}
