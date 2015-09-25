package org.nodeclipse.ui.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.nodeclipse.ui.Activator;
import org.nodeclipse.ui.nature.NodeNature;
import org.nodeclipse.ui.perspectives.NodePerspective;
import org.nodeclipse.ui.preferences.PreferenceConstants;
import org.nodeclipse.ui.util.LogUtil;
import org.nodeclipse.ui.util.VersionUtil;
import org.osgi.framework.Bundle;

/**
 * Superclass for Node, Express, PhantomJS, Nashorn JJS projects
* @author ..., Paul Verest
*/

@SuppressWarnings("restriction")
public abstract class AbstractNodeProjectWizard extends Wizard implements INewWizard {
	
	protected IPreferenceStore store = org.nodeclipse.ui.Activator.getDefault().getPreferenceStore();

    private IWorkbench workbench;
    private IStructuredSelection selection;

    private IProject newProject;

    public AbstractNodeProjectWizard() {
        setNeedsProgressMonitor(true);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
    }

    public IWorkbench getWorkbench() {
        return workbench;
    }

    protected IStructuredSelection getSelection() {
        return selection;
    }

    @Override
    public boolean performFinish() {
        newProject = createNewProject();
        if (newProject == null) {
            return false;
        }
        
        updatePerspective();
        selectAndReveal();
        return true;
    }
    
    protected abstract IProject createNewProject();
    
    //+ to let overriding
    protected String getProjectNature(){
		return NodeNature.NATURE_ID;
    }
    
    /**
     * Set project natures to current type + optionally JSDT/Tern nature
     * @param newProjectHandle IProject
     * @param location URI
     * @return
     */
    protected IProjectDescription createProjectDescription(IProject newProjectHandle, URI location) {
    	boolean addJsdtNature = store.getBoolean(PreferenceConstants.ADD_JSDT_NATURE);
    	boolean addTernNature = store.getBoolean(PreferenceConstants.ADD_TERN_NATURE);
		int numberOfAddedNatures = 1; //always at least 1
		if (addJsdtNature){
			numberOfAddedNatures++;
		}
		if (addTernNature){
			numberOfAddedNatures++;
		}
    	
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription pd = workspace.newProjectDescription(newProjectHandle.getName());
		pd.setLocationURI(location);
		String[] natures = pd.getNatureIds();
		String[] newNatures = new String[natures.length + numberOfAddedNatures];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		
		int newNaturesIndex = natures.length;
		newNatures[newNaturesIndex] = getProjectNature();
		if (addJsdtNature){
			newNatures[++newNaturesIndex] = JavaScriptCore.NATURE_ID;
		}
		if (addTernNature){
			newNatures[++newNaturesIndex] = PreferenceConstants.ADD_TERN_NATURE_VALUE;
		}		
		pd.setNatureIds(newNatures);    	
		
		pd.setComment("Created with Nodeclipse "+VersionUtil.versionString+" at "+DATE_FORMAT.format(new Date()));
		return pd;
    }
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


    protected void generateTemplates(String path, IProject projectHandle) throws CoreException {
		Bundle bundle = Activator.getDefault().getBundle();
		if (bundle == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, "bundle not found"));
		}
				
		try {
			URL location = FileLocator.toFileURL(bundle.getEntry("/"));
			File templateRoot = new File(location.getPath(), path);
			LogUtil.info("templateRoot: " + templateRoot.getAbsolutePath());
			
			RelativityFileSystemStructureProvider structureProvider = new RelativityFileSystemStructureProvider(
					templateRoot);
			ImportOperation operation = new ImportOperation(
					projectHandle.getFullPath(), templateRoot,
					structureProvider, new IOverwriteQuery() {
						public String queryOverwrite(String pathString) {
							return ALL;
						}
					}, structureProvider.getChildren(templateRoot));

			operation.setContext(getShell());
			operation.run(null);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, e.getLocalizedMessage()));
		}
	}

	protected void rewriteFile(String filename, IProject projectHandle)
			throws CoreException {
		String newLine = System.getProperty("line.separator");
		IFile file = projectHandle.getFile(filename);
		if (!file.exists()) {
			return;
//			throw new CoreException(new Status(IStatus.ERROR,
//					Activator.PLUGIN_ID, filename + "not found"));
		}
		InputStreamReader ir = new InputStreamReader(file.getContents());
		BufferedReader br = new BufferedReader(ir);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				if (line.contains("${projectname}")) {
					line = line.replace("${projectname}",
							projectHandle.getName());
				}
				sb.append(line);
				sb.append(newLine);
			}
			ByteArrayInputStream source = new ByteArrayInputStream(sb
					.toString().getBytes());
			file.setContents(source, true, true, null);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, "Cannot read " + filename));
		} finally {
			try {
				ir.close();
				br.close();
			} catch (IOException e) {
			}
			ir = null;
			br = null;
		}
	}

	protected void runJSHint(IProject projectHandle) throws CoreException {
		String builderId = "com.eclipsesource.jshint.ui.builder";
		IProjectDescription description = projectHandle.getDescription();

		if (!containsBuildCommand(description, builderId)) {
			addBuildCommand(description, builderId);
			projectHandle.setDescription(description, null);
		}

		triggerClean(projectHandle, builderId);
	}
	
	protected boolean isExistingProjectFolder(IProjectDescription description) {
		URI location = description.getLocationURI();
		String name = description.getName();
		
		File folder = null;
		if(location != null) {
			folder = FileUtil.toPath(FileUtil.canonicalURI(location)).toFile();
		} else {
			Workspace workspace = (Workspace)ResourcesPlugin.getWorkspace();
			folder = workspace.getRoot().getLocation().append(name).toFile();
		}
		
		if(folder.exists()) {
			if(folder.isDirectory()) {
				File[] files = folder.listFiles();
				if(files.length == 0) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	protected boolean containsBuildCommand(IProjectDescription description,
			String builderId) {
		for (ICommand command : description.getBuildSpec()) {
			if (command.getBuilderName().equals(builderId)) {
				return true;
			}
		}
		return false;
	}

	protected void addBuildCommand(IProjectDescription description, String builderId) {
		ICommand[] oldCommands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length + 1];
		System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
		newCommands[newCommands.length - 1] = createBuildCommand(description, builderId);
		description.setBuildSpec(newCommands);
	}

	protected ICommand createBuildCommand(IProjectDescription description, String builderId) {
		ICommand command = description.newCommand();
		command.setBuilderName(builderId);
		return command;
	}

	protected void triggerClean(IProject project, String builderName) throws CoreException {
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, builderName, null,	null);
	}

	private void selectAndReveal() {
        BasicNewResourceWizard.selectAndReveal(newProject, workbench.getActiveWorkbenchWindow());
    }

    protected void updatePerspective() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveRegistry reg = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        PerspectiveDescriptor rtPerspectiveDesc = (PerspectiveDescriptor) reg.findPerspectiveWithId(NodePerspective.ID);
        // Now set it as the active perspective.
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            page.setPerspective(rtPerspectiveDesc);
        }
    }
}

