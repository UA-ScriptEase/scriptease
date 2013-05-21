package scriptease.gui.pane;

import java.util.Collection;

import javax.swing.JPanel;

import scriptease.controller.MetricsAnalyzer;
import scriptease.model.atomic.KnowIt;

@SuppressWarnings("serial")
public class GameObjectSearchPanel extends JPanel {

	private final Collection<KnowIt> gameObjectsInUse;
	//private final Collection<KnowIt> allGameObjects;

	public GameObjectSearchPanel(Collection<KnowIt> gameObjectsInUse) {
		this.gameObjectsInUse = MetricsAnalyzer.getInstance().getGameObjectsInUse();
		//this.allGameObjects = MetricsAnalyzer.getInstance().getAllGameObjects();
	}
}
