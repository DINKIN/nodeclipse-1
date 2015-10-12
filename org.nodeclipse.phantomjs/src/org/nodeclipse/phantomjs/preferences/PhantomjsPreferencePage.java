package org.nodeclipse.phantomjs.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nodeclipse.ui.preferences.PreferenceConstants;

/**
 * @author Paul Verest
 * @since 0.11 moved from NodePreferencePage
 */
public class PhantomjsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private FileFieldEditor phanthomjsPath;
    private BooleanFieldEditor phanthomjsDebugAutorun;
    private IntegerFieldEditor phanthomjsDebugPort;

	public PhantomjsPreferencePage(){
	       super(GRID);
		    setPreferenceStore(org.nodeclipse.ui.Activator.getDefault().getPreferenceStore());
		    setImageDescriptor(org.nodeclipse.phantomjs.Activator.getImageDescriptor("icons/phantomjs.png"));
	        setDescription("PhantomJS settings");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
        phanthomjsPath = new FileFieldEditor(PreferenceConstants.PHANTOMJS_PATH, "PhanthomJS path:", getFieldEditorParent());
        addField(phanthomjsPath);

        phanthomjsDebugPort = new IntegerFieldEditor(PreferenceConstants.PHANTOMJS_DEBUG_PORT, "PhantomJS debug port:", getFieldEditorParent());
        phanthomjsDebugPort.setEnabled(false, getFieldEditorParent());
        addField(phanthomjsDebugPort);

        phanthomjsDebugAutorun = new BooleanFieldEditor(PreferenceConstants.PHANTOMJS_DEBUG_AUTORUN, "PhantomJS debug autorun", getFieldEditorParent());
        phanthomjsDebugAutorun.setEnabled(false, getFieldEditorParent());
        addField(phanthomjsDebugAutorun);

	}

}
