package net.thecodemaster.evd.ui.view;

import java.util.Comparator;
import java.util.List;

import net.thecodemaster.evd.helper.Creator;
import net.thecodemaster.evd.ui.l10n.Message;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeColumn;

class ViewSorter extends ViewerSorter {

	// Simple data structure for grouping sort information by column.
	private class SortInfo {
		Comparator<ViewDataModel>	comparator;
		boolean										descending;
	}

	private final TreeViewer			viewer;
	private final List<SortInfo>	infos;
	private SortInfo							currentSorter;

	public ViewSorter(TreeViewer viewer) {
		this.viewer = viewer;
		infos = Creator.newList();
	}

	public void addColumn(TreeColumn column) {
		SortInfo sortInfo = new SortInfo();
		sortInfo.comparator = getComparator(column.getText());
		sortInfo.descending = false;

		infos.add(sortInfo);

		createSelectionListener(column, sortInfo);
	}

	private Comparator<ViewDataModel> getComparator(String text) {
		if (text.equals(Message.View.DESCRIPTION)) {
			return new Comparator<ViewDataModel>() {
				@Override
				public int compare(ViewDataModel i1, ViewDataModel i2) {
					return i1.getMessage().compareTo(i2.getMessage());
				}
			};
		}
		if (text.equals(Message.View.VULNERABILITY)) {
			return new Comparator<ViewDataModel>() {
				@Override
				public int compare(ViewDataModel i1, ViewDataModel i2) {
					return i1.getTypeVulnerability() - i2.getTypeVulnerability();
				}
			};
		}
		if (text.equals(Message.View.LOCATION)) {
			return new Comparator<ViewDataModel>() {
				@Override
				public int compare(ViewDataModel i1, ViewDataModel i2) {
					return i1.getLineNumber() - i2.getLineNumber();
				}
			};
		}
		if (text.equals(Message.View.RESOURCE)) {
			return new Comparator<ViewDataModel>() {
				@Override
				public int compare(ViewDataModel i1, ViewDataModel i2) {
					return i1.getResource().getName().compareTo(i2.getResource().getName());
				}
			};
		}
		if (text.equals(Message.View.PATH)) {
			return new Comparator<ViewDataModel>() {
				@Override
				public int compare(ViewDataModel i1, ViewDataModel i2) {
					String fullPath1 = (null != i1.getFullPath()) ? i1.getFullPath() : "";
					String fullPath2 = (null != i2.getFullPath()) ? i2.getFullPath() : "";

					return fullPath1.compareTo(fullPath2);
				}
			};
		}

		return null;
	}

	private void createSelectionListener(final TreeColumn column, final SortInfo info) {
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortUsing(info);
			}
		});
	}

	protected void sortUsing(SortInfo info) {
		for (SortInfo sortInfo : infos) {
			if (sortInfo == info) {
				currentSorter = sortInfo;
				sortInfo.descending = !sortInfo.descending;
				break;
			}
		}
		viewer.refresh();
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		if (null != currentSorter) {
			int result = currentSorter.comparator.compare((ViewDataModel) obj1, (ViewDataModel) obj2);
			if (result != 0) {
				if (currentSorter.descending) {
					return -result;
				}
				return result;
			}
		}

		for (SortInfo sortInfo : infos) {
			int result = sortInfo.comparator.compare((ViewDataModel) obj1, (ViewDataModel) obj2);
			if (result != 0) {
				if (sortInfo.descending) {
					return -result;
				}
				return result;
			}
		}

		return 0;
	}
}
