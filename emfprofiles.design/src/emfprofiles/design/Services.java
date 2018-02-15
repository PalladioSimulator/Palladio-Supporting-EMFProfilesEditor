package emfprofiles.design;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.presentation.EcoreActionBarContributor.ExtendedLoadResourceAction.TargetPlatformPackageDialog;
import org.eclipse.jface.dialogs.Dialog;
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
    	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        TargetPlatformPackageDialog classpathPackageDialog = new TargetPlatformPackageDialog(shell);
        classpathPackageDialog.setMultipleSelection(false);
        EPackage result = null;
        if (classpathPackageDialog.open() == Dialog.OK) {
            String uri = classpathPackageDialog.getFirstResult().toString();
        	result = EPackage.Registry.INSTANCE.getEPackage(uri);
        }
        return result;
    }
    
    /*public EObject print(EObject obj) {
    	System.out.println(obj);
    	return obj;
    }*/
}
