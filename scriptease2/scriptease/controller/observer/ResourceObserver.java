package scriptease.controller.observer;

import scriptease.translator.io.model.EditableResource;
import scriptease.translator.io.model.Resource;

/**
 * Notifies changes to resources. Since only {@link EditableResource}s can be
 * changed, this only applies to them.
 * 
 * @author kschenk
 * 
 */
public interface ResourceObserver {

	public void nameChanged(EditableResource resource, String name);

	public void childRemoved(EditableResource resource, Resource child);

	public void childAdded(EditableResource resource, Resource child);
}
