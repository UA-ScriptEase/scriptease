package scriptease.gui.action.metrics;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import scriptease.controller.ModelAdapter;
import scriptease.controller.StoryAdapter;
import scriptease.gui.WindowFactory;
import scriptease.gui.action.ActiveModelSensitiveAction;
import scriptease.gui.pane.MetricsPanel;
import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;

@SuppressWarnings("serial")
/**
 * Calculates metrics in the active model.
 * 
 * @author jyuen
 */
public class StoryComponentMetricsAction extends ActiveModelSensitiveAction {
	private static final String METRICS = "OPEN_METRICS";

	private final static StoryComponentMetricsAction instance = new StoryComponentMetricsAction();

	private static List<AskIt> askIts;
	private static List<ScriptIt> effects;
	private static List<ScriptIt> causes;
	private static List<ControlIt> delays;
	private static List<ControlIt> repeats;
	private static List<StoryPoint> storyPoints;
	private static List<KnowIt> knowIts;
	private static List<Note> notes;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static StoryComponentMetricsAction getInstance() {
		askIts = new ArrayList<AskIt>();
		effects = new ArrayList<ScriptIt>();
		causes = new ArrayList<ScriptIt>();
		delays = new ArrayList<ControlIt>();
		repeats = new ArrayList<ControlIt>();
		storyPoints = new ArrayList<StoryPoint>();
		knowIts = new ArrayList<KnowIt>();
		notes = new ArrayList<Note>();

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
		WindowFactory.getInstance().buildAndShowCustomFrame(new MetricsPanel(),
				"Metrics", false);
	}

	/**
	 * Calculates general metric values i.e. the number of Effects, Causes,
	 * AskIts, Delays, Repeats, StoryPoints, KnowIts, and Notes.
	 * 
	 * @return A map containing the metric values in each of their respective
	 *         categories.
	 */
	public Map<String, Integer> calculateMetrics() {
		SEModel model = SEModelManager.getInstance().getActiveModel();
		Map<String, Integer> metrics = new HashMap<String, Integer>();

		model.process(new ModelAdapter() {
			@Override
			public void processStoryModel(StoryModel storyModel) {

				StoryPoint root = storyModel.getRoot();
				parseStoryComponents(root);
			}
		});

		metrics.put("AskIts", askIts.size());
		metrics.put("Effects", effects.size());
		metrics.put("Causes", causes.size());
		metrics.put("Delays", delays.size());
		metrics.put("Repeats", repeats.size());
		metrics.put("StoryPoints", storyPoints.size());
		metrics.put("KnowIts", knowIts.size());
		metrics.put("Notes", notes.size());

		return metrics;
	}

	private void parseStoryComponents(final StoryPoint root) {
		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				storyPoints.add(storyPoint);

				this.defaultProcessComplex(storyPoint);

				for (StoryPoint successor : storyPoint.getSuccessors())
					successor.process(this);
			}

			@Override
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				for (StoryComponent child : complex.getChildren()) {
					child.process(this);
				}
			}

			@Override
			public void processNote(Note note) {
				notes.add(note);
			}

			@Override
			public void processControlIt(ControlIt controlIt) {
				if (controlIt.getFormat() == ControlIt.ControlItFormat.DELAY)
					delays.add(controlIt);
				else if (controlIt.getFormat() == ControlIt.ControlItFormat.REPEAT)
					repeats.add(controlIt);

				this.defaultProcessComplex(controlIt);
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				if (scriptIt.isCause()) {
					causes.add(scriptIt);
				} else if (!(scriptIt instanceof ControlIt)) {
					effects.add(scriptIt);
				}

				this.defaultProcessComplex(scriptIt);
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				knowIts.add(knowIt);
			}

			@Override
			public void processAskIt(AskIt askIt) {
				askIts.add(askIt);

				this.defaultProcessComplex(askIt);
			}

			@Override
			public void processStoryItemSequence(StoryItemSequence sequence) {
				this.defaultProcessComplex(sequence);
			}
		};

		root.process(adapter);
	}
}
