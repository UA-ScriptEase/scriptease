package scriptease.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import scriptease.controller.io.FileIO;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.Note;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Calculates metrics in the active model.
 * 
 * @author jyuen
 */
public class MetricsAnalyzer {
	// Singleton
	private static MetricsAnalyzer instance = null;

	private final Collection<AskIt> questions;
	private final Collection<ScriptIt> effects;
	private final Collection<CauseIt> causes;
	private final Collection<ControlIt> delays;
	private final Collection<ControlIt> repeats;
	private final Collection<StoryPoint> storyPoints;
	private final Collection<KnowIt> descriptions;
	private final Collection<Note> notes;
	private final Collection<KnowIt> gameObjects;
	private final Collection<KnowIt> implicits;

	/**
	 * Gets the sole instance of this particular type of Action
	 * 
	 * @return The sole instance of this particular type of Action
	 */
	public static MetricsAnalyzer getInstance() {
		if (instance == null) {
			instance = new MetricsAnalyzer();
		}

		return MetricsAnalyzer.instance;
	}

	protected MetricsAnalyzer() {
		this.questions = new HashSet<AskIt>();
		this.effects = new HashSet<ScriptIt>();
		this.causes = new HashSet<CauseIt>();
		this.delays = new HashSet<ControlIt>();
		this.repeats = new HashSet<ControlIt>();
		this.storyPoints = new HashSet<StoryPoint>();
		this.descriptions = new HashSet<KnowIt>();
		this.notes = new HashSet<Note>();
		this.gameObjects = new HashSet<KnowIt>();
		this.implicits = new HashSet<KnowIt>();
	}

	/**
	 * Calculates general metric values i.e. the number of Effects, Causes,
	 * AskIts, Delays, Repeats, StoryPoints, KnowIts, and Notes.
	 * 
	 * @return A map containing the metric values in each of their respective
	 *         categories.
	 */
	public Map<String, Integer> getNumStoryComponents() {
		final Map<String, Integer> metrics = new HashMap<String, Integer>();

		metrics.put("Questions", questions.size());
		metrics.put("Effects", effects.size());
		metrics.put("Causes", causes.size());
		metrics.put("Delays", delays.size());
		metrics.put("Repeats", repeats.size());
		metrics.put("StoryPoints", storyPoints.size());
		metrics.put("Descriptions", descriptions.size());
		metrics.put("Notes", notes.size());
		metrics.put("Game Objects", gameObjects.size());
		metrics.put("Implicits", implicits.size());

		return metrics;
	}

	public Map<String, Integer> getStoryPointComplexity() {
		final String LONGEST_BRANCH = "Longest Branch";
		final String END_POINTS = "End Points";

		final Map<String, Integer> metrics = new HashMap<String, Integer>();
		final Map<StoryPoint, Integer> depthMap;

		final int longestPathLength;
		int endPoints = 0;

		depthMap = SEModelManager.getInstance().getActiveRoot()
				.createDepthMap();

		longestPathLength = Collections.max(depthMap.values()) + 1;

		for (StoryPoint storypoint : depthMap.keySet()) {
			if (storypoint.getSuccessors().isEmpty()) {
				System.out.println(storypoint.getDisplayText());
				endPoints++;
			}
		}

		metrics.put(LONGEST_BRANCH, longestPathLength);
		metrics.put(END_POINTS, endPoints);

		return metrics;
	}

	/**
	 * Calculates the average complexity of story components. i.e. The average
	 * number of effects per cause, average number of causes per story point,
	 * etc.
	 * 
	 * @return A map containing the metric values in each of the respective
	 *         categories.
	 */
	public Map<String, Float> getStoryComponentComplexity() {
		Map<String, Float> metrics = new HashMap<String, Float>();

		float totalDelaysInCauses = 0;
		float totalRepeatsInCauses = 0;
		float totalEffectsInCauses = 0;
		float totalQuestionsInCauses = 0;
		float totalDescriptionsInCauses = 0;
		float totalCausesInStoryPoints = 0;

		int numCauses = causes.size();
		int numStoryPoints = storyPoints.size();

		// Return if there are no causes or story points.
		if (numCauses == 0 || numStoryPoints == 0)
			return metrics;

		// Check the complexity of causes.
		for (CauseIt cause : causes) {

			final List<StoryComponent> children = new ArrayList<StoryComponent>();

			children.addAll(cause.getActiveBlock().getChildren());
			children.addAll(cause.getInactiveBlock().getChildren());
			children.addAll(cause.getAlwaysBlock().getChildren());

			for (StoryComponent child : children) {

				if (child instanceof ControlIt) {
					ControlIt controlIt = (ControlIt) child;

					if (controlIt.getFormat() == ControlIt.ControlItFormat.DELAY)
						totalDelaysInCauses++;
					else if (controlIt.getFormat() == ControlIt.ControlItFormat.REPEAT)
						totalRepeatsInCauses++;
				}

				else if (child instanceof ScriptIt) {
					totalEffectsInCauses++;
				}

				else if (child instanceof AskIt) {
					totalQuestionsInCauses++;
				}

				else if (child instanceof KnowIt) {
					final SEModel model;

					model = SEModelManager.getInstance().getActiveModel();

					if (model instanceof StoryModel) {
						for (LibraryModel library : ((StoryModel) model)
								.getLibraries()) {

							if (library.getDescribeIt(child) != null) {
								totalDescriptionsInCauses++;
								break;
							}
						}
					}
				}
			}
		}

		// Check the complexity of story points.
		for (StoryPoint storyPoint : storyPoints) {

			List<StoryComponent> children = storyPoint.getChildren();

			for (StoryComponent child : children) {
				if (child instanceof ScriptIt)
					totalCausesInStoryPoints++;
			}
		}

		metrics.put("Effects/Cause", totalEffectsInCauses / numCauses);
		metrics.put("Questions/Cause", totalQuestionsInCauses / numCauses);
		metrics.put("Delays/Cause", totalDelaysInCauses / numCauses);
		metrics.put("Repeats/Cause", totalRepeatsInCauses / numCauses);
		metrics.put("Descriptions/Cause", totalDescriptionsInCauses / numCauses);
		metrics.put("Causes/Story Point", totalCausesInStoryPoints
				/ numStoryPoints);

		return metrics;
	}

	/**
	 * Calculates the use of the cause sections (inactive, active, and always)
	 * 
	 * @return A map containing metric values for the frequency each block is
	 *         used.
	 */
	public Map<String, Integer> getCauseBlockMetrics() {
		Map<String, Integer> metrics = new HashMap<String, Integer>();

		metrics.put("Active", 0);
		metrics.put("Inactive", 0);
		metrics.put("Always", 0);

		for (CauseIt cause : causes) {
			StoryComponentContainer activeBlock = cause.getActiveBlock();
			StoryComponentContainer inactiveBlock = cause.getInactiveBlock();
			StoryComponentContainer alwaysBlock = cause.getAlwaysBlock();

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
	 * Returns the game objects currently used by the active story model.
	 * 
	 * @return
	 */
	public Collection<KnowIt> getGameObjectsInUse() {
		return this.gameObjects;
	}

	/**
	 * Calculates the frequency of specific causes.
	 * 
	 * @return A map containing each cause and their occurrence.
	 */
	public Map<String, Integer> getFavouriteCauses() {
		return calculateFavouriteMetricsFor(causes);
	}

	/**
	 * Calculates the frequency of specific effects.
	 * 
	 * @return A map containing each effect and their occurrence.
	 */
	public Map<String, Integer> getFavouriteEffects() {
		return calculateFavouriteMetricsFor(effects);
	}

	/**
	 * Calculates the frequency of specific descriptions.
	 * 
	 * @return A map containing each description and their occurrence.
	 */
	public Map<String, Integer> getFavouriteDescriptions() {
		return calculateFavouriteMetricsFor(descriptions);
	}

	/**
	 * Calculates the frequency of specific questions.
	 * 
	 * @return A map containing each question and their occurrence.
	 */
	public Map<String, Integer> getFavouriteQuestions() {
		return calculateFavouriteMetricsFor(questions);
	}

	/**
	 * Calculates the frequency of specific repeats.
	 * 
	 * @return A map containing each repeat and their occurrence.
	 */
	public Map<String, Integer> getFavouriteRepeats() {
		return calculateFavouriteMetricsFor(repeats);
	}

	/**
	 * Calculates the frequency of specific delays.
	 * 
	 * @return A map containing each delay and their occurrence.
	 */
	public Map<String, Integer> getFavouriteDelays() {
		return calculateFavouriteMetricsFor(delays);
	}

	/**
	 * Calculates the frequency of a specific story component.
	 * 
	 * TODO: fix sort bug.
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

	public void processStoryComponents() {
		this.questions.clear();
		this.effects.clear();
		this.causes.clear();
		this.delays.clear();
		this.repeats.clear();
		this.storyPoints.clear();
		this.descriptions.clear();
		this.notes.clear();

		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				if (!storyPoints.contains(storyPoint)) {
					storyPoints.add(storyPoint);

					this.defaultProcessComplex(storyPoint);

					for (StoryPoint successor : storyPoint.getSuccessors())
						successor.process(this);
				}
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
				final boolean process;

				if (controlIt.getFormat() == ControlIt.ControlItFormat.DELAY) {
					process = delays.contains(controlIt);
					if (!process)
						delays.add(controlIt);
				} else if (controlIt.getFormat() == ControlIt.ControlItFormat.REPEAT) {
					process = repeats.contains(controlIt);
					if (!process)
						repeats.add(controlIt);
				} else
					process = false;

				if (process) {
					controlIt.processParameters(this);
					this.defaultProcessComplex(controlIt);
				}
			}

			@Override
			public void processCauseIt(CauseIt causeIt) {
				if (!causes.contains(causeIt)) {
					causes.add(causeIt);

					causeIt.processParameters(this);
					this.defaultProcessComplex(causeIt);
				}
			}

			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				if (!effects.contains(scriptIt)) {
					effects.add(scriptIt);

					scriptIt.processParameters(this);
					this.defaultProcessComplex(scriptIt);
				}
			}

			@Override
			public void processKnowIt(final KnowIt knowIt) {
				if (!descriptions.contains(knowIt)) {
					final SEModel model;

					model = SEModelManager.getInstance().getActiveModel();

					if (model instanceof StoryModel) {
						for (LibraryModel library : ((StoryModel) model)
								.getLibraries()) {

							if (library.getDescribeIt(knowIt) != null) {
								descriptions.add(knowIt);
								break;
							}
						}
					}
				}

				if (!descriptions.contains(knowIt)) {
					knowIt.getBinding().process(new BindingAdapter() {

						@Override
						public void processReference(
								KnowItBindingReference reference) {
							reference.getValue().getBinding().process(this);
						}

						@Override
						public void processFunction(
								KnowItBindingFunction function) {
							implicits.add(knowIt);
						};

						@Override
						public void processResource(
								KnowItBindingResource resource) {
							if (resource.isIdentifiableGameConstant())
								gameObjects.add(knowIt);
						};

					});
				}
			}

			@Override
			public void processAskIt(AskIt askIt) {
				if (!questions.contains(askIt)) {
					questions.add(askIt);

					askIt.getCondition().process(this);
					this.defaultProcessComplex(askIt);
				}
			}
		};

		SEModelManager.getInstance().getActiveRoot().process(adapter);
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

	/**
	 * Export the .csv file to the user's requested directory.
	 */
	public void exportMetrics(File metricsFile) {
		final Collection<ArrayList<String>> data;

		final String STORY_COMPONENTS = "Story Components";
		final String FREQUENCY = "Frequency";
		final String AVERAGE = "Average";
		final String COMPLEXITY = "Complexity";
		final String CAUSE_BLOCK = "Cause Block";
		final String FAVOURITE = "Favourite";
		final String CAUSES = "Causes";
		final String EFFECTS = "Effects";
		final String DESCRIPTIONS = "Descriptions";
		final String QUESTIONS = "Questions";
		final String REPEATS = "Repeats";
		final String DELAYS = "Delays";
		final String STORY_POINT_COMPLEXITY = "Story Point Complexity";

		data = new ArrayList<ArrayList<String>>();

		processDataToCSV(STORY_COMPONENTS, FREQUENCY,
				this.getNumStoryComponents(), data);

		processDataToCSV(COMPLEXITY, AVERAGE + " " + FREQUENCY,
				this.getStoryComponentComplexity(), data);

		processDataToCSV(CAUSE_BLOCK, FREQUENCY, this.getCauseBlockMetrics(),
				data);

		processDataToCSV(FAVOURITE + " " + CAUSES, FREQUENCY,
				this.getFavouriteCauses(), data);

		processDataToCSV(FAVOURITE + " " + EFFECTS, FREQUENCY,
				this.getFavouriteEffects(), data);

		processDataToCSV(FAVOURITE + " " + DESCRIPTIONS, FREQUENCY,
				this.getFavouriteDescriptions(), data);

		processDataToCSV(FAVOURITE + " " + QUESTIONS, FREQUENCY,
				this.getFavouriteQuestions(), data);

		processDataToCSV(FAVOURITE + " " + REPEATS, FREQUENCY,
				this.getFavouriteRepeats(), data);

		processDataToCSV(FAVOURITE + " " + DELAYS, FREQUENCY,
				this.getFavouriteDelays(), data);

		processDataToCSV(STORY_POINT_COMPLEXITY, FREQUENCY,
				this.getStoryPointComplexity(), data);

		FileIO.getInstance().saveCSV(data, metricsFile);
	}

	private void processDataToCSV(String xComponentName, String yComponentName,
			Map<String, ? extends Number> values,
			final Collection<ArrayList<String>> data) {

		ArrayList<String> tempRow = new ArrayList<String>();

		tempRow.add(xComponentName);
		tempRow.add(yComponentName);
		data.add(tempRow);

		for (Entry<String, ?> entry : values.entrySet()) {
			tempRow = new ArrayList<String>();
			tempRow.add(entry.getKey());

			if (entry.getValue() instanceof Float)
				tempRow.add(Float.toString((Float) entry.getValue()));
			else if (entry.getValue() instanceof Integer)
				tempRow.add(Integer.toString((Integer) entry.getValue()));

			data.add(tempRow);
		}

		tempRow = new ArrayList<String>();
		tempRow.add("");
		data.add(tempRow);
	}
}
