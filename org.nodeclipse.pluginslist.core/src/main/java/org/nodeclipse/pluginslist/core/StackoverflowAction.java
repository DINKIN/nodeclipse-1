package org.nodeclipse.pluginslist.core;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

public class StackoverflowAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public StackoverflowAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		int style = IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.STATUS;
	    IWebBrowser browser;
		try {
			browser = WorkbenchBrowserSupport.getInstance().createBrowser(style,
					Constants.STACKOVERFLOW_BROWSER_ID, Constants.STACKOVERFLOW_BROWSER_NAME, Constants.STACKOVERFLOW_BROWSER_TOOLTIP);
		} catch (PartInitException e) {
			e.printStackTrace();
			MessageDialog.openError(
				window.getShell(),
				Constants.ERROR1_MESSAGE,
				Constants.ERROR1_MESSAGE+" to show "+Constants.STACKOVERFLOW_BROWSER_NAME);
			return;
		}
	    try {
			browser.openURL(Constants.stackoverflowUrl);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Selection in the workbench has been changed. We
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}