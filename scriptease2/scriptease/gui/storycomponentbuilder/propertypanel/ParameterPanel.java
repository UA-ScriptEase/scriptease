package scriptease.gui.storycomponentbuilder.propertypanel;

import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.model.atomic.KnowIt;

@SuppressWarnings("serial")
public class ParameterPanel extends StoryComponentPropertyPanel {

	public ParameterPanel(KnowIt parameter) {
		super(parameter);
	}

	@Override
	protected JPanel buildDisplayPanel() {
		return StoryComponentPanelFactory.getInstance().buildPanel(
				this.component);
	}

	@Override
	public String toString() {
		return "ParameterPanel [" + this.component + "]";
	}
}
