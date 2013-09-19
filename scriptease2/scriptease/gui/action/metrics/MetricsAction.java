package scriptease.gui.action.metrics;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import scriptease.controller.StoryMetrics;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.MetricsPanel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;

@SuppressWarnings("serial")
/**
 * Listens for prompt to open metrics.
 * 
 * @author jyuen
 */
public class MetricsAction extends ActiveModelSensitiveAction {
	private static final String METRICS = "Metrics";

	// Singleton
	private static MetricsAction instance = null;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static MetricsAction getInstance() {
		if (instance == null) {
			instance = new MetricsAction();
		}

		return MetricsAction.instance;
	}

	/**
	 * Defines a <code>StoryComponentMetricsAction</code> object with a mnemonic
	 * and accelerator.
	 */
	private MetricsAction() {
		super(MetricsAction.METRICS);

		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	}

	@Override
	/**
	 * Create the metrics dialog and call Metric Analyzer to process the story
	 * components so we can make data out of it!
	 */
	public void actionPerformed(ActionEvent e) {
		final StoryModel model;

		model = SEModelManager.getInstance().getActiveStoryModel();

		if (model != null) {
			final StoryMetrics metrics = new StoryMetrics(model);
			this.createDialogPane(metrics);
		}
	}

	private void createDialogPane(final StoryMetrics metrics) {
		final JDialog dialog;

		dialog = WindowFactory.getInstance().buildDialog(MetricsAction.METRICS,
				new MetricsPanel(metrics), false);

		dialog.setVisible(true);
	}
}