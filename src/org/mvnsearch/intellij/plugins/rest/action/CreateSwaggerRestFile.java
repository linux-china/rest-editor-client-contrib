package org.mvnsearch.intellij.plugins.rest.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

import java.io.IOException;
import java.util.List;

/**
 * create rest file for Swagger json
 *
 * @author linux_china
 */
public class CreateSwaggerRestFile extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(DataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.getName().endsWith("-swagger.json")) {
            //parse swagger json and get http calls
            StringBuilder builder = new StringBuilder();
            List<HttpCall> calls = SwaggerGenerator.getInstance().parseSwagger(virtualFile.getCanonicalPath());
            for (HttpCall call : calls) {
                builder.append(call.toString());
                builder.append("\n");
            }
            //write content to rest virtual file
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    String newFileName = virtualFile.getName().replace(".json", ".http");
                    VirtualFile directory = virtualFile.getParent();
                    VirtualFile httpFile = directory.createChildData(virtualFile, newFileName);
                    httpFile.setBinaryContent(builder.toString().getBytes());
                    directory.refresh(true, false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(DataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.getName().endsWith("-swagger.json")) {
            getTemplatePresentation().setEnabled(true);
        }
        super.update(e);
    }
}
