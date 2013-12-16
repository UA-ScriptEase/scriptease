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
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.ScriptEaseKeywords;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.librarymodel.LibraryModel;

/**
 * Calculates metrics for the provided story model.
 * 
 * @author jyuen
 */
public class StoryMetrics {

	private final StoryModel model;

	private final Map<String, Integer> numberComponents;
	private final Map<String, Integer> storyPointComplexity;
	private final Map<String, Float> storyComponentComplexity;

	private final Map<String, Integer> favouriteCauses;
	private final Map<String, Integer> favouriteEffects;
	private final Map<String, Integer> favouriteDescriptions;
	private final Map<String, Integer> favouriteQuestions;
	private final Map<String, Integer> favouriteRepeats;
	private final Map<String, Integer> favouriteDelays;

	public StoryMetrics(StoryModel model) {
		final Collection<AskIt> questions;
		final Collection<ScriptIt> effects;
		final Collection<CauseIt> causes;
		final Collection<ControlIt> delays;
		final Collection<ControlIt> repeats;
		final Collection<StoryPoint> storyPoints;
		final Collection<KnowIt> descriptions;
		final Collection<Note> notes;
		final Collection<KnowIt> gameObjects;
		final Collection<KnowIt> implicits;

		this.model = model;

		questions = new HashSet<AskIt>();
		effects = new HashSet<ScriptIt>();
		causes = new HashSet<CauseIt>();
		delays = new HashSet<ControlIt>();
		repeats = new HashSet<ControlIt>();
		storyPoints = new HashSet<StoryPoint>();
		descriptions = new HashSet<KnowIt>();
		notes = new HashSet<Note>();
		gameObjects = new HashSet<KnowIt>();
		implicits = new HashSet<KnowIt>();

		this.processStoryComponents(questions, effects, causes, delays,
				repeats, storyPoints, descriptions, notes, gameObjects,
				implicits);

		this.numberComponents = this.calculateNumberComponents(questions,
				effects, causes, delays, repeats, storyPoints, descriptions,
				notes, gameObjects, implicits);

		this.storyPointComplexity = this.calculateStoryPointComplexity();

		this.storyComponentComplexity = this.calculateStoryComponentComplexity(
				causes, storyPoints);

		this.favouriteCauses = this.calculateFavouriteCauses(causes);
		this.favouriteEffects = this.calculateFavouriteEffects(effects);
		this.favouriteDescriptions = this
				.calculateFavouriteDescriptions(descriptions);
		this.favouriteQuestions = this.calculateFavouriteQuestions(questions);
		this.favouriteRepeats = this.calculateFavouriteRepeats(repeats);
		this.favouriteDelays = this.calculateFavouriteDelays(delays);
	}

	/**
	 * Calculates general metric values i.e. the number of Effects, Causes,
	 * AskIts, Delays, Repeats, StoryPoints, KnowIts, and Notes.
	 * 
	 * @return A map containing the metric values in each of their respective
	 *         categories.
	 */
	public Map<String, Integer> calculateNumberComponents(
			final Collection<AskIt> questions,
			final Collection<ScriptIt> effects,
			final Collection<CauseIt> causes,
			final Collection<ControlIt> delays,
			final Collection<ControlIt> repeats,
			final Collection<StoryPoint> storyPoints,
			final Collection<KnowIt> descriptions,
			final Collection<Note> notes, final Collection<KnowIt> gameObjects,
			final Collection<KnowIt> implicits) {

		final Map<String, Integer> metrics = new HashMap<String, Integer>();

		metrics.put(ScriptEaseKeywords.QUESTIONS, questions.size());
		metrics.put(ScriptEaseKeywords.EFFECTS, effects.size());
		metrics.put(ScriptEaseKeywords.CAUSES, causes.size());
		metrics.put(ScriptEaseKeywords.DELAYS, delays.size());
		metrics.put(ScriptEaseKeywords.REPEATS, repeats.size());
		metrics.put(ScriptEaseKeywords.STORY_POINT, storyPoints.size());
		metrics.put(ScriptEaseKeywords.DESCRIPTIONS, descriptions.size());
		metrics.put(ScriptEaseKeywords.NOTES, notes.size());
		metrics.put(ScriptEaseKeywords.GAME_OBJECTS, gameObjects.size());
		metrics.put(ScriptEaseKeywords.IMPLICITS, implicits.size());

		return metrics;
	}

	public Map<String, Integer> calculateStoryPointComplexity() {
		final String LONGEST_BRANCH = "Longest Branch";
		final String END_POINTS = "End Points";

		final Map<String, Integer> metrics = new HashMap<String, Integer>();
		final Map<StoryNode, Integer> depthMap;

		final int longestPathLength;
		int endPoints = 0;

		depthMap = this.model.getRoot().createDepthMap();

		longestPathLength = Collections.max(depthMap.values()) + 1;

		for (StoryNode storyNode : depthMap.keySet()) {
			if (storyNode.getSuccessors().isEmpty()) {
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
	public Map<String, Float> calculateStoryComponentComplexity(
			final Collection<CauseIt> causes,
			final Collection<StoryPoint> storyPoints) {
		final Map<String, Float> metrics = new HashMap<String, Float>();

		final int numCauses = causes.size();
		final int numStoryPoints = storyPoints.size();

		float totalDelaysInCauses = 0;
		float totalRepeatsInCauses = 0;
		float totalEffectsInCauses = 0;
		float totalQuestionsInCauses = 0;
		float totalDescriptionsInCauses = 0;
		float totalCausesInStoryPoints = 0;

		// Return if there are no causes or story points.
		if (numCauses == 0 || numStoryPoints == 0)
			return metrics;

		// Check the complexity of causes.
		for (CauseIt cause : causes) {
			for (StoryComponent child : cause.getChildren()) {

				if (child instanceof ControlIt) {
					final ControlIt controlIt = (ControlIt) child;

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

			final List<StoryComponent> children;

			children = storyPoint.getChildren();

			for (StoryComponent child : children) {
				if (child instanceof ScriptIt)
					totalCausesInStoryPoints++;
			}
		}

		metrics.put(
				ScriptEaseKeywords.EFFECTS + "/" + ScriptEaseKeywords.CAUSE,
				totalEffectsInCauses / numCauses);
		metrics.put(ScriptEaseKeywords.QUESTIONS + "/"
				+ ScriptEaseKeywords.CAUSE, totalQuestionsInCauses / numCauses);
		metrics.put(ScriptEaseKeywords.DELAYS + "/" + ScriptEaseKeywords.CAUSE,
				totalDelaysInCauses / numCauses);
		metrics.put(
				ScriptEaseKeywords.REPEATS + "/" + ScriptEaseKeywords.CAUSE,
				totalRepeatsInCauses / numCauses);
		metrics.put(ScriptEaseKeywords.DESCRIPTIONS + "/"
				+ ScriptEaseKeywords.CAUSE, totalDescriptionsInCauses
				/ numCauses);
		metrics.put(ScriptEaseKeywords.CAUSES + "/"
				+ ScriptEaseKeywords.STORY_POINT, totalCausesInStoryPoints
				/ numStoryPoints);

		return metrics;
	}

	/**
	 * Calculates the frequency of specific causes.
	 * 
	 * @return A map containing each cause and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteCauses(
			final Collection<CauseIt> causes) {
		return calculateFavouriteMetricsFor(causes);
	}

	/**
	 * Calculates the frequency of specific effects.
	 * 
	 * @return A map containing each effect and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteEffects(
			final Collection<ScriptIt> effects) {
		return calculateFavouriteMetricsFor(effects);
	}

	/**
	 * Calculates the frequency of specific descriptions.
	 * 
	 * @return A map containing each description and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteDescriptions(
			final Collection<KnowIt> descriptions) {
		return calculateFavouriteMetricsFor(descriptions);
	}

	/**
	 * Calculates the frequency of specific questions.
	 * 
	 * @return A map containing each question and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteQuestions(
			final Collection<AskIt> questions) {
		return calculateFavouriteMetricsFor(questions);
	}

	/**
	 * Calculates the frequency of specific repeats.
	 * 
	 * @return A map containing each repeat and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteRepeats(
			final Collection<ControlIt> repeats) {
		return calculateFavouriteMetricsFor(repeats);
	}

	/**
	 * Calculates the frequency of specific delays.
	 * 
	 * @return A map containing each delay and their occurrence.
	 */
	public Map<String, Integer> calculateFavouriteDelays(
			final Collection<ControlIt> delays) {
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

		final Map<String, Integer> metrics = new HashMap<String, Integer>();
		final SortedMap<String, Integer> sortedMetrics;

		for (StoryComponent storyComponent : storyComponents) {
			final String stringComponent;

			stringComponent = storyComponent.getDisplayText();

			// Increment value if story component already exists.
			if (metrics.containsKey(stringComponent)) {
				final int frequency;
				frequency = metrics.get(stringComponent) + 1;
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

	public void processStoryComponents(final Collection<AskIt> questions,
			final Collection<ScriptIt> effects,
			final Collection<CauseIt> causes,
			final Collection<ControlIt> delays,
			final Collection<ControlIt> repeats,
			final Collection<StoryPoint> storyPoints,
			final Collection<KnowIt> descriptions,
			final Collection<Note> notes, final Collection<KnowIt> gameObjects,
			final Collection<KnowIt> implicits) {

		questions.clear();
		effects.clear();
		causes.clear();
		delays.clear();
		repeats.clear();
		storyPoints.clear();
		descriptions.clear();
		notes.clear();
		gameObjects.clear();
		implicits.clear();

		final StoryAdapter adapter;

		adapter = new StoryAdapter() {
			@Override
			public void processStoryPoint(StoryPoint storyPoint) {
				if (!storyPoints.contains(storyPoint)) {
					storyPoints.add(storyPoint);

					this.defaultProcessComplex(storyPoint);

					for (StoryNode successor : storyPoint.getSuccessors())
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

		private final Map<String, Integer> data;

		public FrequencyComparator(Map<String, Integer> data) {
			this.data = data;
		}

		@Override
		public int compare(String storyComponent1, String storyComponent2) {
			final Integer frequency1 = this.data.get(storyComponent1);
			final Integer frequency2 = this.data.get(storyComponent2);
			return frequency2.compareTo(frequency1);
		}
	}

	/**
	 * Export the .csv file to the user's requested directory.
	 */
	public void exportMetrics(File metricsFile) {
		final Collection<ArrayList<String>> data;

		data = new ArrayList<ArrayList<String>>();

		processDataToCSV(ScriptEaseKeywords.STORY_COMPONENTS,
				ScriptEaseKeywords.FREQUENCY, this.numberComponents, data);

		processDataToCSV(
				ScriptEaseKeywords.COMPLEXITY,
				ScriptEaseKeywords.AVERAGE + " " + ScriptEaseKeywords.FREQUENCY,
				this.storyComponentComplexity, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.CAUSES, ScriptEaseKeywords.FREQUENCY,
				this.favouriteCauses, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.EFFECTS, ScriptEaseKeywords.FREQUENCY,
				this.favouriteEffects, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DESCRIPTIONS,
				ScriptEaseKeywords.FREQUENCY, this.favouriteDescriptions, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.QUESTIONS, ScriptEaseKeywords.FREQUENCY,
				this.favouriteQuestions, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.REPEATS, ScriptEaseKeywords.FREQUENCY,
				this.favouriteRepeats, data);

		processDataToCSV(ScriptEaseKeywords.FAVOURITE + " "
				+ ScriptEaseKeywords.DELAYS, ScriptEaseKeywords.FREQUENCY,
				this.favouriteDelays, data);

		processDataToCSV(ScriptEaseKeywords.STORY_POINT_COMPLEXITY,
				ScriptEaseKeywords.FREQUENCY, this.storyPointComplexity, data);

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

	/*
	 * *************** Getters for metrics. ********************
	 */

	public Map<String, Integer> getNumberComponents() {
		return numberComponents;
	}

	public Map<String, Integer> getStoryPointComplexity() {
		return storyPointComplexity;
	}

	public Map<String, Float> getStoryComponentComplexity() {
		return storyComponentComplexity;
	}

	public Map<String, Integer> getFavouriteCauses() {
		return favouriteCauses;
	}

	public Map<String, Integer> getFavouriteEffects() {
		return favouriteEffects;
	}

	public Map<String, Integer> getFavouriteDescriptions() {
		return favouriteDescriptions;
	}

	public Map<String, Integer> getFavouriteQuestions() {
		return favouriteQuestions;
	}

	public Map<String, Integer> getFavouriteRepeats() {
		return favouriteRepeats;
	}

	public Map<String, Integer> getFavouriteDelays() {
		return favouriteDelays;
	}
}
