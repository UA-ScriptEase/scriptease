package scriptease.gui.action.metrics;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import scriptease.controller.MetricAnalyzer;
import scriptease.controller.MetricsExporter;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.MetricsPanel;

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
	public void actionPerformed(ActionEvent arg0) {
		MetricAnalyzer.getInstance().processStoryComponents();
		
		createDialogPane();
	}
	
	private void createDialogPane() {
		final int WIDTH = 800;
		final int HEIGHT = 600;
		
		final String EXPORT = "Export as CSV";
		final String CLOSE = "Close";
		
		final JDialog dialog;
		final JPanel buttonsPanel;
		final JButton exportButton = new JButton(EXPORT);
		final JButton closeButton = new JButton(CLOSE);
		
		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		dialog = WindowFactory.getInstance().buildDialog("Metrics",
				new MetricsPanel(), false);
		
		
		buttonsPanel.add(exportButton);
		buttonsPanel.add(closeButton);

		dialog.getContentPane().add(buttonsPanel);	
		dialog.setSize(WIDTH, HEIGHT);
		dialog.getContentPane().setLayout(
				new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
		
		exportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				MetricsExporter.getInstance().exportMetrics();
			}
		});
		
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
			}
		});

		dialog.setVisible(true);
	}
}