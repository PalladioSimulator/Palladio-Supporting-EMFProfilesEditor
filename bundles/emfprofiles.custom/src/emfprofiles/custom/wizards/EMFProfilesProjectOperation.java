package emfprofiles.custom.wizards;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.ui.tools.api.project.ModelingProjectManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.modelversioning.emfprofile.Profile;
import org.modelversioning.emfprofile.diagram.part.EMFProfileDiagramEditorUtil;
import org.modelversioning.emfprofile.project.EMFProfileProjectNature;
import org.modelversioning.emfprofile.project.EMFProfileProjectNatureUtil;
import org.modelversioning.emfprofile.project.ui.wizard.ProfileProjectData;

public class EMFProfilesProjectOperation extends WorkspaceModifyOperation {

	private static final String BUILD_PROP_FILE_NAME = "build.properties";
	private static final String PDE_PLUGIN_NATURE = "org.eclipse.pde.PluginNature";
    private static final String VIEWPOINT_NAME = "emfprofiles.viewpoint";

	private ProfileProjectData projectData;
	private IProject project;
	private Resource profileDiagramResource;

	public EMFProfilesProjectOperation(ProfileProjectData projectData) {
		super();
		this.projectData = projectData;
		this.project = projectData.getProjectHandle();
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
	    SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
	   
	    subMonitor.beginTask("Creating new EMF Profile project", 5);    
	    
		subMonitor.subTask("Creating project");
		createProject(subMonitor.newChild(1));
		subMonitor.worked(1);
		
        subMonitor.subTask("Creating contents");
        createContents(subMonitor.newChild(1));
        subMonitor.worked(1);
        
        subMonitor.subTask("Setting up the modeling project");
        setUpModelingProject();           
        subMonitor.worked(1);
        
        projectData.getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        
        configureNatures(project);
        
		subMonitor.subTask("Creating manifest");
		createPluginXml(subMonitor.newChild(1));
		configureBinBuildProperties();
		
        createManifest(projectData.getProjectHandle().getLocation().toString());

        subMonitor.worked(1);
        projectData.getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

//        subMonitor.subTask("Opening representation");
//        openRepresentation();
//        subMonitor.worked(1);
        

	}

    private void setUpModelingProject() throws CoreException {
        ModelingProjectManager.INSTANCE.convertToModelingProject(projectData.getProjectHandle(), new NullProgressMonitor());
        
        final URI representationsURI = ViewPointUtil.getRepresentationsURI(projectData.getProjectHandle());
        final Session session = SessionManager.INSTANCE.getSession(representationsURI, new NullProgressMonitor());
        List<String> viewpointNames = new ArrayList<String>();
        viewpointNames.add(VIEWPOINT_NAME);
        ViewPointUtil.selectViewpointsByName(session, viewpointNames, true, new NullProgressMonitor());
    }

	private void createProject(IProgressMonitor monitor)
			throws CoreException {
		if (!project.exists()) {
			if (!Platform.getLocation().equals(projectData.getLocationPath())) {
				IProjectDescription desc = project.getWorkspace()
						.newProjectDescription(project.getName());
				desc.setLocation(projectData.getLocationPath());
				project.create(desc, monitor);
			} else {
				project.create(monitor);
			}
			project.open(null);
		}
	}

	private void configureNatures(IProject project) throws CoreException {
		if (!project.hasNature(PDE_PLUGIN_NATURE))
			addPDENature();
		if (!project.hasNature(EMFProfileProjectNature.NATURE_ID))
			EMFProfileProjectNatureUtil.addNature(project);
		    
	}

	public void addPDENature() throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		if (!Arrays.asList(natures).contains(PDE_PLUGIN_NATURE)) {
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 1, natures.length);
			newNatures[0] = PDE_PLUGIN_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		}
	}

	private void createPluginXml(IProgressMonitor monitor) throws CoreException {
		IFile pluginXmlFile = project
				.getFile(EMFProfileProjectNature.PLUGIN_XML_FILE_NAME);
		if (!pluginXmlFile.exists()) {
			StringBuffer pluginXmlContent = new StringBuffer();
			pluginXmlContent
					.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			pluginXmlContent.append("<?eclipse version=\"3.4\"?>\n"); //$NON-NLS-1$
			pluginXmlContent.append("<plugin>\n"); 
			
			// extension point to register emf profile
			pluginXmlContent
					.append("  <extension point=\"org.modelversioning.emfprofile.profile\">\n"); //$NON-NLS-1$
			pluginXmlContent
					.append("     <profile profile_resource=\"profile.emfprofile_diagram\"/>\n"); //$NON-NLS-1$
			pluginXmlContent.append("  </extension>\n"); //$NON-NLS-1$

			pluginXmlContent.append("</plugin>\n"); //$NON-NLS-1$
			InputStream source = new ByteArrayInputStream(pluginXmlContent
					.toString().getBytes());
			pluginXmlFile.create(source, true, monitor);
		}
	}

	private void configureBinBuildProperties() throws CoreException {
		IFile buildPropFile = project.getFile(BUILD_PROP_FILE_NAME);
		if (!buildPropFile.exists()) {
			StringBuffer buildPropContent = new StringBuffer();
			buildPropContent.append("bin.includes = ");
			buildPropContent.append(EMFProfileProjectNature.PLUGIN_XML_FILE_NAME + ",\\\n"); //$NON-NLS-1$
            buildPropContent.append("               "); //$NON-NLS-1$
			buildPropContent.append("META-INF/,\\\n");
			buildPropContent.append("               "); //$NON-NLS-1$
			buildPropContent.append(EMFProfileProjectNature.DEFAULT_PROFILE_DIAGRAM_FILE_NAME + "\n"); //$NON-NLS-1$
			InputStream source = new ByteArrayInputStream(buildPropContent
					.toString().getBytes());
			buildPropFile.create(source, true, null);
		}
	}

	protected void createContents(IProgressMonitor monitor)
			throws CoreException,
			InvocationTargetException, InterruptedException {
		try {
			createProfile(monitor, project);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}


	private void createProfile(IProgressMonitor monitor, IProject project)
			throws IOException {
		profileDiagramResource = EMFProfileDiagramEditorUtil.createDiagram(
				EMFProfileProjectNatureUtil
						.getDefaultProfileDiagramURI(project), SubMonitor.convert(monitor, 0));
		setProfileDiagramData();
		saveProfileDiagramResource();
	}

	private void saveProfileDiagramResource() throws IOException {
		TransactionalEditingDomain editingDomain = TransactionUtil
				.getEditingDomain(profileDiagramResource);
		profileDiagramResource.save(null);
		((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
	}

	private void setProfileDiagramData() {
		final Profile profile = getProfileFromResource();
		TransactionalEditingDomain editingDomain = TransactionUtil
				.getEditingDomain(profileDiagramResource);
		editingDomain.getCommandStack().execute(
				new RecordingCommand(editingDomain) {
					@Override
					protected void doExecute() {
						profile.setName(projectData.getProfileName());
						profile.setNsURI(projectData.getProfileNamespace());
					}
				});
	}

	private Profile getProfileFromResource() {
		if (profileDiagramResource == null)
			return null;
		for (TreeIterator<EObject> contents = profileDiagramResource
				.getAllContents(); contents.hasNext();) {
			EObject next = contents.next();
			if (next instanceof Profile) {
				return (Profile) next;
			}
		}
		return null;
	}
	

    private void createManifest(String projectPath) {
        String metainfPath = projectPath + Path.SEPARATOR + "META-INF";
        File metainf = new File(metainfPath);
        metainf.mkdir();
        String manifestPath = metainfPath + Path.SEPARATOR + "MANIFEST.MF";
        File manifest = new File(manifestPath);
        
        File buildFile = new File(projectPath + Path.SEPARATOR + "build.properties");

        try {
            manifest.createNewFile();
            buildFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(manifest));
            writer.write("Manifest-Version: 1.0\n");
            writer.write("Bundle-ManifestVersion: 2\n");
            writer.write("Bundle-Name: " + projectData.getProfileName()+ "\n");
            writer.write("Bundle-SymbolicName: " + projectData.getProfileNamespace() + "." + projectData.getProfileName() + " ;singleton:=true\n");
            writer.write("Bundle-Version: 1.0.0.qualifier\n");
            writer.write("Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n");
            writer.write("Require-Bundle: org.modelversioning.emfprofile," +
                    "org.modelversioning.emfprofile.registry\n");
            
            writer.close();
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
