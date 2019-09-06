package emfprofiles.custom.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.modelversioning.emfprofile.project.ui.wizard.ProfileProjectData;
import org.modelversioning.emfprofile.project.ui.wizard.ProfileProjectNewPage;

public class EMFProfilesProjectWizard extends Wizard implements INewWizard {


    private ProfileProjectData projectData;
    private IWorkbench workbench;

    
    private ProfileProjectNewPage mainPage;

    public EMFProfilesProjectWizard() {
        setWindowTitle("New EMF Profile Project");
        setNeedsProgressMonitor(true);
        projectData = new ProfileProjectData();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        
//        pluginPage = new EMFProfilesPluginPage("Plug-in Creation");
//        this.addPage(pluginPage);
        
        mainPage = new ProfileProjectNewPage("main", projectData, selection); //$NON-NLS-1$
        mainPage.setTitle("EMF Profile Project");
        mainPage.setDescription("Create a new EMF Profile project.");
        this.addPage(mainPage);
        
        
    }

    @Override
    public boolean performFinish() {
        mainPage.updateData();

        try {
            createEMFProfileProject();
            addToWorkingSets();
            return true;
        } catch (InvocationTargetException e) {
            System.err.println(e.getMessage());
//          EMFProfileUIPlugin.logException(e);
        } catch (InterruptedException e) {
        }
        return false;
    }
    
    private void createEMFProfileProject() throws InvocationTargetException,
            InterruptedException {
        getContainer().run(false, true,
                new EMFProfilesProjectOperation(projectData));
        //PluginConverter converter = (PluginConverter) Activator.getDefault().getBundle().getBundleContext().getServiceReference(PluginConverter.class.getName());
        //Dictionary<String, String> dict = new HashMap();
        //converter.writeManifest(new File(projectData.getLocationPath().toString()), (Dictionary<String, String>) new HashMap<String, String>(), false);

    }

    private void addToWorkingSets() {
        IWorkingSet[] workingSets = mainPage.getSelectedWorkingSets();
        if (workingSets.length > 0)
            workbench.getWorkingSetManager().addToWorkingSets(
                    mainPage.getProjectHandle(), workingSets);
    }
        
}
