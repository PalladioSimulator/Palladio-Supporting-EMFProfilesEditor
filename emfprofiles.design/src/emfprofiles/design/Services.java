package emfprofiles.design;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.presentation.EcoreActionBarContributor.ExtendedLoadResourceAction.TargetPlatformPackageDialog;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.modelversioning.emfprofile.Extension;
import org.modelversioning.emfprofile.Profile;
import org.modelversioning.emfprofile.Stereotype;

/**
 * The services class used by VSM.
 */
public class Services {
    
	public Services() {
		
	}
    public Set<EClass> loadEClasses(Profile profile) {
    	Set<EClass> result = new HashSet<EClass>();
    	for(Stereotype stereotype : profile.getStereotypes()) {
	        for (EReference reference : stereotype.getEReferences()) {
	            result.add(reference.getEReferenceType());
	        }
	        for (Extension extension : stereotype.getExtensions()) {
	        	result.add(extension.getTarget());
	        }
	    }
    	return result;
    }
    
    public EPackage selectEPackage(EObject self) {
    	System.out.println("test ");
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        TargetPlatformPackageDialog classpathPackageDialog = new TargetPlatformPackageDialog(shell);
        classpathPackageDialog.setMultipleSelection(false);
        EPackage result = null;
        if (classpathPackageDialog.open() == Dialog.OK) {
            //Object [] result = classpathPackageDialog.getResult();
            String uri = classpathPackageDialog.getFirstResult().toString();
        	result = EPackage.Registry.INSTANCE.getEPackage(uri);
        }
        /*Session session = SessionManager.INSTANCE.getSession(self);
        session.addSemanticResource(result.eResource().getURI(), new NullProgressMonitor());*/
        return result;
    }
    
    public EObject print(EObject obj) {
    	System.out.println(obj);
    	return obj;
    }
}
