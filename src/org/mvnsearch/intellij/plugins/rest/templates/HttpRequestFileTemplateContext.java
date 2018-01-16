package org.mvnsearch.intellij.plugins.rest.templates;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Http request file template context
 *
 * @author linux_china
 */
public class HttpRequestFileTemplateContext extends TemplateContextType {

    protected HttpRequestFileTemplateContext() {
        super("HttpRequest", "Http Request");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile psiFile, int i) {
        return psiFile.getName().endsWith(".http") || psiFile.getName().endsWith(".rest");
    }
}
