package emfprofiles.custom.wizards;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.business.api.helper.SiriusUtil;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.ui.business.api.session.UserSession;
import org.eclipse.sirius.viewpoint.description.Viewpoint;

public final class ViewPointUtil {

    private ViewPointUtil() {
        // intentionally left blank
    }

    public static Optional<URI> getRepresentationsURI(IProject project) {
        final IResource representationResource = project.findMember("representations." + SiriusUtil.SESSION_RESOURCE_EXTENSION);
        return Optional.ofNullable(representationResource)
                .map(IResource::getFullPath)
                .map(IPath::toString)
                .map(p -> URI.createPlatformResourceURI(p, true));
    }

    public static void selectViewpointsByName(Session session, Collection<String> viewpointNames, boolean createRepresentation,
            IProgressMonitor monitor) {
        final Collection<Viewpoint> viewpoints = ViewpointRegistry.getInstance().getViewpoints().stream()
                .filter(v -> viewpointNames.contains(v.getName()))
                .collect(Collectors.toList());
        selectViewpoints(session, viewpoints, createRepresentation, monitor);
    }

    public static void selectViewpoints(Session session, Collection<Viewpoint> viewpoints, boolean createRepresentation,
            IProgressMonitor monitor) {
        Collection<Viewpoint> selectedViewpoints = session.getSelectedViewpoints(false);
        Collection<String> viewpointNamesToSelect = viewpoints.stream()
                .filter(v -> !selectedViewpoints.contains(v))
                .map(Viewpoint::getName)
                .collect(Collectors.toList());
        UserSession.from(session).selectViewpoints(viewpointNamesToSelect);
    }

}
