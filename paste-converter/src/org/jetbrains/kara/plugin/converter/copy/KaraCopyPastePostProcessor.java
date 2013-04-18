package org.jetbrains.kara.plugin.converter.copy;

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kara.plugin.converter.KaraHTMLConverter;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * @author Stanislav Erokhin
 */

public class KaraCopyPastePostProcessor implements CopyPastePostProcessor<TextBlockTransferableData> {
    private static final Logger LOG = Logger.getInstance("#org.jetbrains.jet.plugin.conversion.copy.org.jetbrains.kara.plugin.converter.KaraCopyPastePostProcessor");

    @Nullable
    @Override
    public TextBlockTransferableData collectTransferableData(PsiFile psiFile, Editor editor, int[] ints, int[] ints2) {
        return null;
    }

    private boolean isContainHTML(Transferable content) {
        for (DataFlavor flavor : content.getTransferDataFlavors()) {
            if (flavor.getMimeType().startsWith("text/html")) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public TextBlockTransferableData extractTransferableData(Transferable content) {
        try {
            if (isContainHTML(content)) {
                String text = (String) content.getTransferData(DataFlavor.stringFlavor);
                String newText = KaraHTMLConverter.instance$.converter(text, 0);
                return new ConvertedCode(newText);
            }
        } catch (Throwable e) {
            LOG.error(e);
        }
        return null;
    }

    @Override
    public void processTransferableData(Project project, final Editor editor, final RangeMarker bounds, int i, Ref<Boolean> booleanRef, TextBlockTransferableData value) {
        try {
            final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (file == null) {
                return;
            }
            boolean needConvert = okFromDialog(project);
            if (needConvert) {
                if (value instanceof ConvertedCode) {
                    final String text = ((ConvertedCode) value).getData();
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            editor.getDocument().replaceString(bounds.getStartOffset(), bounds.getEndOffset(), text);
                            editor.getCaretModel().moveToOffset(bounds.getStartOffset() + text.length());
                            PsiDocumentManager.getInstance(file.getProject()).commitDocument(editor.getDocument());
                        }
                    });
                }
            }
        } catch (Throwable t) {
            LOG.error(t);
        }
    }

    private static boolean okFromDialog(@NotNull Project project) {
        KaraPasteFromHtmlDialog dialog = new KaraPasteFromHtmlDialog(project);
        dialog.show();
        return dialog.isOK();
    }
}
