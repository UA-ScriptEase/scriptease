package scriptease.gui.action.metrics;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
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
	private static final String METRICS = "Metrics";

	// Singleton
	private static StoryComponentMetricsAction instance = null;
	
	private final List<AskIt> askIts;
	private final List<ScriptIt> effects;
	private final List<ScriptIt> causes;
	private final List<ControlIt> delays;
	private final List<ControlIt> repeats;
	private final List<StoryPoint> storyPoints;
	private final List<KnowIt> knowIts;
	private final List<Note> notes;

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

		this.askIts = new ArrayList<AskIt>();
		this.effects = new ArrayList<ScriptIt>();
		this.causes = new ArrayList<ScriptIt>();
		this.delays = new ArrayList<ControlIt>();
		this.repeats = new ArrayList<ControlIt>();
		this.storyPoints = new ArrayList<StoryPoint>();
		this.knowIts = new ArrayList<KnowIt>();
		this.notes = new ArrayList<Note>();
		
		this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		this.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		WindowFactory.getInstance().buildAndShowCustomFrame(new MetricsPanel(),
				"Metrics", true);
	}

	/**
	 * Calculates general metric values i.e. the number of Effects, Causes,
	 * AskIts, Delays, Repeats, StoryPoints, KnowIts, and Notes.
	 * 
	 * @return A map containing the metric values in each of their respective
	 *         categories.
	 */
	public Map<String, Integer> calculateGeneralMetrics() {
		Map<String, Integer> metrics = new HashMap<String, Integer>();
		
		processStoryComponents();

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

	/**
	 * Calculates the frequency of specific causes.
	 * 
	 * @return A map containing each cause and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteCauses() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(causes);
	}

	/**
	 * Calculates the frequency of specific effects.
	 * 
	 * @return A map containing each effect and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteEffects() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(effects);
	}

	/**
	 * Calculates the frequency of specific descriptions.
	 * 
	 * @return A map containing each description and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteKnowIts() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(knowIts);
	}

	/**
	 * Calculates the frequency of specific questions.
	 * 
	 * @return A map containing each question and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteAskIts() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(askIts);
	}

	/**
	 * Calculates the frequency of specific repeats.
	 * 
	 * @return A map containing each repeat and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteRepeats() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(repeats);
	}

	/**
	 * Calculates the frequency of specific delays.
	 * 
	 * @return A map containing each delay and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteDelays() {
		processStoryComponents();
		return calculateFavouriteMetricsFor(delays);
	}
	
	/**
	 * Calculates the frequency of a specific story component.
	 * 
	 * @return A map containing each component and their occurrence.
	 */
	private Map<String, Integer> calculateFavouriteMetricsFor(
			Collection<? extends StoryComponent> storyComponents) {
		
		Map<String, Integer> metrics = new HashMap<String, Integer>();
		
		for (StoryComponent storyComponent : storyComponents) {
			String stringComponent = storyComponent.getDisplayText();
			
			// Increment value if story component already exists.
			if (metrics.containsKey(stringComponent)) {
				int frequency = metrics.get(stringComponent);
				frequency++;

				metrics.put(stringComponent, frequency);

			// Add count to existing story component.
			} else 
				metrics.put(stringComponent, 1);
		}
		
		return metrics;
	}
	
	private void processStoryComponents() {
		SEModel model = SEModelManager.getInstance().getActiveModel();
		
		this.askIts.clear();
		this.effects.clear();
		this.causes.clear();
		this.delays.clear();
		this.repeats.clear();
		this.storyPoints.clear();
		this.knowIts.clear();
		this.notes.clear();

		model.process(new ModelAdapter() {
			@Override
			public void processStoryModel(StoryModel storyModel) {

				StoryPoint root = storyModel.getRoot();
				parseStoryComponents(root);
			}
		});
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
