package scriptease.gui.action.metrics;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.MetricAnalyzer;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.MetricsPanel;

@SuppressWarnings("serial")
/**
 * Listens for prompt to open metrics.
 * 
 * @author jyuen
 */
public class StoryComponentMetricsAction extends ActiveModelSensitiveAction {
	private static final String METRICS = "Metrics";

	// Singleton
	private static StoryComponentMetricsAction instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static StoryComponentMetricsAction getInstance() {
		if (instance == null) {
			instance = new StoryComponentMetricsAction();
		}

		return StoryComponentMetricsAction.instance;
	}

	/**
	 * Defines a <code>StoryComponentMetricsAction</code> object with a mnemonic
	 * and accelerator.
	 */
	private StoryComponentMetricsAction() {
		super(StoryComponentMetricsAction.METRICS);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		MetricAnalyzer.getInstance().processStoryComponents();

		WindowFactory.getInstance().buildAndShowCustomFrame(new MetricsPanel(),
				"Metrics", false);
	}
}
