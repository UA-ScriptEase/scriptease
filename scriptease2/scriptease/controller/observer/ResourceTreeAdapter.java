package scriptease.controller.observer;

import scriptease.translator.io.model.Resource;

/**
 * Adapter for {@link ResourceTreeObserver}.
 * 
 * @author kschenk
 * 
 */
public abstract class ResourceTreeAdapter implements ResourceTreeObserver {
	@Override
	public void resourceAddButtonClicked(String type) {
	}

	@Override
	public void resourceEditButtonClicked(Resource resource) {
	}

	public void resourceRemoveButtonClicked(Resource resource) {
	};

	@Override
	public void resourceSelected(Resource resource) {
	}
}
