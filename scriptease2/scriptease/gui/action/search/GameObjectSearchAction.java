package scriptease.gui.action.search;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.MetricsAnalyzer;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;

/**
 * Listens for prompt to open a search (and optional replace) window for game
 * objects in the current active story model.
 * 
 * @author jyuen
 */
public class GameObjectSearchAction extends ActiveModelSensitiveAction {
	private static final String GAME_OBJECT_SEARCH = "Search for Game Objects";

	// Singleton
	private static GameObjectSearchAction instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static GameObjectSearchAction getInstance() {
		if (instance == null) {
			instance = new GameObjectSearchAction();
		}

		return GameObjectSearchAction.instance;
	}

	/**
	 * Defines a <code>GameObjectSearchAction</code> object with a mnemonic and
	 * accelerator.
	 */
	private GameObjectSearchAction() {
		super(GameObjectSearchAction.GAME_OBJECT_SEARCH);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
	}

	@Override
	/**
	 * Create the metrics dialog and call Metric Analyzer to process the story
	 * components so we can make data out of it!
	 */
	public void actionPerformed(ActionEvent arg0) {
		MetricsAnalyzer.getInstance().processStoryComponents();

		createSearchFrame();
	}

	private void createSearchFrame() {
		WindowFactory.getInstance().buildAndShowCustomFrame(
				new GameObjectSearchPanel(), "Find/Replace Game Objects", true);
	}
}
