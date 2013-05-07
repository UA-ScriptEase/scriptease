package scriptease.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import scriptease.model.SEModel;
import scriptease.model.SEModelManager;
import scriptease.model.StoryComponent;
import scriptease.model.StoryModel;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.TranslatorManager;
import scriptease.translator.apimanagers.DescribeItManager;

public class MetricAnalyzer {

	// Singleton
	private static MetricAnalyzer instance = null;

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
	public static MetricAnalyzer getInstance() {
		if (instance == null) {
			instance = new MetricAnalyzer();
		}

		return MetricAnalyzer.instance;
	}
	
	protected MetricAnalyzer() {
		this.askIts = new ArrayList<AskIt>();
		this.effects = new ArrayList<ScriptIt>();
		this.causes = new ArrayList<ScriptIt>();
		this.delays = new ArrayList<ControlIt>();
		this.repeats = new ArrayList<ControlIt>();
		this.storyPoints = new ArrayList<StoryPoint>();
		this.knowIts = new ArrayList<KnowIt>();
		this.notes = new ArrayList<Note>();
	}

	/**
	 * Processes the story components in the active model.
	 */
	public void processStoryComponents() {
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

	/**
	 * Calculates general metric values i.e. the number of Effects, Causes,
	 * AskIts, Delays, Repeats, StoryPoints, KnowIts, and Notes.
	 * 
	 * @return A map containing the metric values in each of their respective
	 *         categories.
	 */
	public Map<String, Integer> calculateGeneralMetrics() {
		Map<String, Integer> metrics = new HashMap<String, Integer>();

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
	 * Calculates the use of the cause sections (inactive, active, and always)
	 * 
	 * @return A map containing metric values for the frequency each block is
	 *         used.
	 */
	public Map<String, Integer> calculateCauseBlockMetrics() {
		Map<String, Integer> metrics = new HashMap<String, Integer>();

		metrics.put("Active", 0);
		metrics.put("Inactive", 0);
		metrics.put("Always", 0);

		for (ScriptIt cause : causes) {
			StoryItemSequence activeBlock = cause.getActiveBlock();
			StoryItemSequence inactiveBlock = cause.getInactiveBlock();
			StoryItemSequence alwaysBlock = cause.getAlwaysBlock();

			metrics.put("Active", metrics.get("Active")
					+ activeBlock.getChildren().size());
			metrics.put("Inactive", metrics.get("Inactive")
					+ inactiveBlock.getChildren().size());
			metrics.put("Always", metrics.get("Always")
					+ alwaysBlock.getChildren().size());
		}

		return metrics;
	}

	/**
	 * Calculates the frequency of specific causes.
	 * 
	 * @return A map containing each cause and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteCauses() {
		return calculateFavouriteMetricsFor(causes);
	}

	/**
	 * Calculates the frequency of specific effects.
	 * 
	 * @return A map containing each effect and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteEffects() {
		return calculateFavouriteMetricsFor(effects);
	}

	/**
	 * Calculates the frequency of specific descriptions.
	 * 
	 * @return A map containing each description and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteKnowIts() {
		return calculateFavouriteMetricsFor(knowIts);
	}

	/**
	 * Calculates the frequency of specific questions.
	 * 
	 * @return A map containing each question and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteAskIts() {
		return calculateFavouriteMetricsFor(askIts);
	}

	/**
	 * Calculates the frequency of specific repeats.
	 * 
	 * @return A map containing each repeat and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteRepeats() {
		return calculateFavouriteMetricsFor(repeats);
	}

	/**
	 * Calculates the frequency of specific delays.
	 * 
	 * @return A map containing each delay and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteDelays() {
		return calculateFavouriteMetricsFor(delays);
	}

	/**
	 * Calculates the frequency of a specific story component. TODO: fix sort
	 * bug.
	 * 
	 * @return A sorted map containing each component and their occurrence.
	 */
	private Map<String, Integer> calculateFavouriteMetricsFor(
			Collection<? extends StoryComponent> storyComponents) {

		Map<String, Integer> metrics = new HashMap<String, Integer>();
		SortedMap<String, Integer> sortedMetrics;

		for (StoryComponent storyComponent : storyComponents) {
			String stringComponent = storyComponent.getDisplayText();

			// Increment value if story component already exists.
			if (metrics.containsKey(stringComponent)) {

				int frequency = metrics.get(stringComponent) + 1;
				metrics.put(stringComponent, frequency);

			} else
				metrics.put(stringComponent, 1);
		}

		// Sort the story components based on occurrence.
		sortedMetrics = new TreeMap<String, Integer>(new FrequencyComparator(
				metrics));
		sortedMetrics.putAll(metrics);

		return sortedMetrics;
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
				DescribeItManager describeItManager = TranslatorManager
						.getInstance().getActiveDescribeItManager();

				if (describeItManager.getDescribeIt(knowIt) != null)
					knowIts.add(knowIt);
				else {
					knowIt.getBinding().process(new BindingAdapter() {
						@Override
						public void processReference(
								KnowItBindingReference reference) {
							super.processReference(reference);
						}
					});
				}
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

	/**
	 * Inner class used to sort map values by descending order.
	 * 
	 * @author jyuen
	 */
	private class FrequencyComparator implements Comparator<String> {

		private Map<String, Integer> data;

		public FrequencyComparator(Map<String, Integer> data) {
			this.data = data;
		}

		@Override
		public int compare(String storyComponent1, String storyComponent2) {
			Integer frequency1 = this.data.get(storyComponent1);
			Integer frequency2 = this.data.get(storyComponent2);
			return frequency2.compareTo(frequency1);
		}
	}
}
