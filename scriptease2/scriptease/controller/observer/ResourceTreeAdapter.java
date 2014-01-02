package scriptease.controller.observer;

import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;

/**
 * Adapter for {@link ResourceTreeObserver}.
 * 
 * @author kschenk
 * 
 */
public abstract class ResourceTreeAdapter implements ResourceTreeObserver {
	
	@Override
	public void resourceTreeFilled() {
		
	}
	
	@Override
	public void resourceAddButtonClicked(GameType type) {
	}

	@Override
	public void resourceEditButtonClicked(Resource resource) {
	}

	@Override
	public void resourceRemoveButtonClicked(Resource resource) {
	};

	@Override
	public void resourceSelected(Resource resource) {
	}
}
