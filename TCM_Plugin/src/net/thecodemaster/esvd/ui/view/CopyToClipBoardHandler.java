package net.thecodemaster.esvd.ui.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import net.thecodemaster.esvd.Activator;
import net.thecodemaster.esvd.logger.PluginLogger;
import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;

public class CopyToClipBoardHandler extends Action {

	private static final char									LINE_BREAK	= '\n';
	private static final char									INDENTATION	= '\t';
	private final Clipboard										clipboard;
	private final ViewSecurityVulnerabilities	view;
	private final TreeViewer									treeViewer;

	public CopyToClipBoardHandler(Composite parent, ViewSecurityVulnerabilities view, TreeViewer treeViewer) {
		this.view = view;
		this.treeViewer = treeViewer;

		setText(Message.View.COPY_TO_CLIPBOARD_TEXT);
		setToolTipText(Message.View.COPY_TO_CLIPBOARD_TOOLTIP);
		setEnabled(true);
		setImageDescriptor(Activator.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		clipboard = new Clipboard(parent.getDisplay());
	}

	@Override
	public void run() {
		StringBuffer buf = new StringBuffer();

		// 01 - Get the selected elements.
		TreeItem[] selection = treeViewer.getTree().getSelection();
		if ((null == selection) || (selection.length == 0)) {
			return;
		}

		// 02 - Add header.
		addHeader(buf);

		// 03 - Get the number of columns of the treeview.
		int nrColumns = treeViewer.getTree().getColumns().length;

		// 04 - Iterate over the list and add each element.
		for (TreeItem treeItem : selection) {
			addElements(buf, treeItem, nrColumns);
		}

		// 05 - For converting plain text in a String into Platform.
		TextTransfer plainTextTransfer = TextTransfer.getInstance();

		try {
			// 06 - Adding to the clipboard.
			clipboard.setContents(new String[] { convertLineTerminators(buf.toString()) },
					new Transfer[] { plainTextTransfer });
		} catch (SWTError e) {
			PluginLogger.logError(e);

			String errorMessage = String.format(Message.Error.COPY_TO_CLIPBOARD_MESSAGE_FAIL, e.getMessage());
			MessageDialog.openError(view.getViewSite().getShell(), Message.Error.COPY_TO_CLIPBOARD_TITLE_FAIL, errorMessage);
		}
	}

	private void addHeader(StringBuffer buf) {
		TreeColumn[] columns = treeViewer.getTree().getColumns();

		for (TreeColumn treeColumn : columns) {
			buf.append(treeColumn.getText());
			buf.append(INDENTATION);
		}
		buf.append(LINE_BREAK);
	}

	private void addElements(StringBuffer buf, TreeItem item, int nrColumns) {
		for (int i = 0; i < nrColumns; i++) {
			buf.append(item.getText(i));
			buf.append(INDENTATION);
		}

		buf.append(LINE_BREAK);
	}

	private String convertLineTerminators(String allLines) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		BufferedReader bufferedReader = new BufferedReader(new StringReader(allLines));
		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return allLines; // return the call hierarchy unfiltered.
		}
		return stringWriter.toString();
	}

}
