package net.thecodemaster.esvd.ui.preference.page;

import net.thecodemaster.esvd.ui.l10n.Message;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;

public class Acknowledgements extends AbstracPreferencePage {

	public Acknowledgements() {
		super();
	}

	public Acknowledgements(String title) {
		super(title);
	}

	public Acknowledgements(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
		setDescription(Message.PrefPageAcknowledgements.DESCRIPTION);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite top = new Composite(parent, SWT.LEFT);

		// Sets the layout for the top composite's children to populate.
		top.setLayout(new GridLayout());

		// Group authors and its children.
		Group groupAuthors = new Group(top, SWT.NONE);
		groupAuthors.setText(Message.PrefPageAcknowledgements.LABEL_GROUP_AUTHORS);
		groupAuthors.setLayout(new GridLayout());
		groupAuthors.setLayoutData(getGroupGridData());

		Label labelAuthors1 = new Label(groupAuthors, SWT.NONE);
		labelAuthors1.setText(Message.PrefPageAcknowledgements.AUTHOR_1);
		labelAuthors1.setLayoutData(getLabelGridData());

		// Group contributors and its children.
		Group groupContributors = new Group(top, SWT.NONE);
		groupContributors.setText(Message.PrefPageAcknowledgements.LABEL_GROUP_CONTRIBUTORS);
		groupContributors.setLayout(new GridLayout(2, true));
		groupContributors.setLayoutData(getGroupGridData());

		Label labelContributors1 = new Label(groupContributors, SWT.NONE);
		labelContributors1.setText(Message.PrefPageAcknowledgements.CONTRIBUTORS_1);
		labelContributors1.setLayoutData(getLabelGridData());

		Label labelContributors2 = new Label(groupContributors, SWT.NONE);
		labelContributors2.setText(Message.PrefPageAcknowledgements.CONTRIBUTORS_2);
		labelContributors2.setLayoutData(getLabelGridData());

		// Link to the thecodemaster.net web site.
		Link linkTCM = new Link(top, SWT.NONE);
		linkTCM.setText(getLinkHTML(Message.PrefPageAcknowledgements.URL_THECODEMASTER));
		linkTCM.setLayoutData(getLabelGridData());
		// Register listener for the selection event
		linkTCM.addSelectionListener(onClickOpenURL(Message.PrefPageAcknowledgements.URL_THECODEMASTER));

		return top;
	}

	private GridData getGroupGridData() {
		return new GridData(GridData.FILL, GridData.CENTER, true, false);
	}

	private GridData getLabelGridData() {
		return new GridData(GridData.CENTER, GridData.CENTER, true, false);
	}

}
