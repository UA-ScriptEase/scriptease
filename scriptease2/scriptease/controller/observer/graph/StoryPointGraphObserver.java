package scriptease.controller.observer.graph;

import java.util.List;

import javax.swing.JComponent;

import scriptease.controller.ModelAdapter;
import scriptease.gui.PanelFactory;
import scriptease.model.PatternModel;
import scriptease.model.PatternModelManager;
import scriptease.model.StoryModel;
import scriptease.model.complex.StoryPoint;

/**
 * Observer for graphs that acts on Story Points when they have children or
 * parents added or removed from them in the Graph. Also updates the
 * StoryComponentTree when a node is selected.
 * 
 * @author kschenk
 * 
 */
public class StoryPointGraphObserver implements SEGraphObserver<StoryPoint> {
	@Override
	public void childAdded(StoryPoint child, StoryPoint parent) {
		parent.addSuccessor(child);
	}

	@Override
	public void childRemoved(StoryPoint child, StoryPoint parent) {
		parent.removeSuccessor(child);
	}

	@Override
	public void parentAdded(StoryPoint child, StoryPoint parent) {
		parent.addSuccessor(child);
	}

	@Override
	public void parentRemoved(StoryPoint child, StoryPoint parent) {
		final int initialFanIn;

		initialFanIn = child.getFanIn();

		if (initialFanIn > 1)
			child.setFanIn(initialFanIn - 1);

		parent.removeSuccessor(child);
	}

	@Override
	public void nodeSelected(final StoryPoint node) {
		final PatternModel activeModel;

		activeModel = PatternModelManager.getInstance().getActiveModel();

		activeModel.process(new ModelAdapter() {
			@Override
			public void processStoryModel(StoryModel storyModel) {
				List<JComponent> components = PanelFactory.getInstance()
						.getComponentsForModel(storyModel);

				for (JComponent component : components)
					PanelFactory.getInstance().setRootForTreeInComponent(
							component, node);
			}
		});
	}
}
