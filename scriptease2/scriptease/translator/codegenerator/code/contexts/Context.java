package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.controller.StoryComponentUtils;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryGroup;
import scriptease.model.complex.StoryNode;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.StoryModel;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.io.model.Resource;

/**
 * Context represents the object based context used in code generation. It
 * provides implementation for every possible evaluation or action do-able by
 * code generation. However these calls must be overridden in order to give true
 * value to the methods. For example getting the parameters ScriptIt will vary
 * in implementation, thus the ScriptItContext must implement these methods in a
 * meaningful manner. Context also provides information such as the current
 * indentation of the code, the propagation of includes, symbols and used names
 * in code generation. <br>
 * <br>
 * When a method is called in a context which does not make sense, it will
 * return a default unimplemented value as an indicator that the call was not
 * appropriate in the current context. For example, asking for the binding of a
 * slot does not make sense, however we do not limit the translator author from
 * attempting these calls, thus the translator author must understand the
 * fundamentals of ScriptEase in order to correctly write a translator.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
public abstract class Context {
	private String indent = "";
	private final CodeGenerationNamifier namifier;
	private final StoryModel model;

	// This is a cached list of all story nodes. This prevents multiple
	// expensive calls to StoryNode#getDescendants()
	private final Collection<StoryNode> storyNodes;

	/**
	 * The collection of code blocks known by the Context. These are lazy
	 * loaded, and should thus only be called using the {@link #getCodeBlocks()}
	 * method.
	 */
	private Collection<CodeBlock> codeBlocks = null;

	protected LocationInformation locationInfo;

	private static final String UNIMPLEMENTED = "<unimplemented in context>";

	public Context(StoryModel model, Collection<StoryNode> storyNodes,
			String indent, CodeGenerationNamifier existingNames) {
		this.model = model;
		this.storyNodes = storyNodes;
		this.indent = indent;
		this.namifier = existingNames;
	}

	public Context(Context other) {
		this(other.getModel(), other.getStoryNodes(), other.getIndent(), other
				.getNamifier());
	}

	/**
	 * Sets the Context's location information, which provides useful
	 * information for determining the Context's subject and slot
	 * 
	 * @param data
	 *            .getSlot()
	 */
	public final void setLocationInfo(LocationInformation locationInfo) {
		this.locationInfo = locationInfo;
	}

	public final LocationInformation getLocationInfo() {
		return this.locationInfo;
	}

	/**
	 * @return the translator that is being used to translate ScriptEase
	 *         patterns into game code.
	 */
	public final Translator getTranslator() {
		return this.model.getTranslator();
	}

	/**
	 * @return The model that is being translated into code.
	 */
	public final StoryModel getModel() {
		return this.model;
	}

	/**
	 * @return the Context's model
	 */
	public final StoryPoint getStartStoryPoint() {
		return this.model.getRoot();
	}

	public final CodeGenerationNamifier getNamifier() {
		return this.namifier;
	}

	/**
	 * Returns the indent.
	 * 
	 * @return the indent
	 */
	public final String getIndent() {
		return this.indent;
	}

	/**
	 * Increases the indent by adding the passed string to the existing string.
	 * 
	 * @param indent
	 */
	public final void increaseIndent(String indent) {
		this.indent += indent;
	}

	/**
	 * Reduces the indent by the number of characters in the passed string.
	 * 
	 * @param indent
	 */
	public final void reduceIndent(String indent) {
		this.indent = this.indent.substring(indent.length());
	}

	/**
	 * Returns all of the codeBlocks associated with the Context's current slot.
	 * These are lazy loaded.
	 * 
	 * @return
	 */
	public Collection<CodeBlock> getCodeBlocks() {
		if (this.codeBlocks == null) {

			this.codeBlocks = new ArrayList<CodeBlock>();

			// for each story point
			for (StoryNode point : this.storyNodes) {
				for (ScriptIt scriptIt : StoryComponentUtils
						.getDescendantScriptIts(point)) {
					final Collection<CodeBlock> codeBlocksForSlot;

					codeBlocksForSlot = scriptIt
							.getCodeBlocksForLocation(this.locationInfo);

					this.codeBlocks.addAll(codeBlocksForSlot);
				}
			}
		}

		return this.codeBlocks;
	}

	public CodeBlock getMainCodeBlock() {
		unimplemented("getMainCodeBlock");
		return null;
	}

	public KnowItBinding getBinding() {
		unimplemented("getBinding");
		return null;
	}

	public Collection<StoryComponent> getChildren() {
		unimplemented("getChildren");
		return null;
	}

	public StoryComponentContainer getIfChild() {
		unimplemented("getIfChild");
		return null;
	}

	public StoryComponentContainer getElseChild() {
		unimplemented("getElseChild");
		return null;
	}

	public Collection<KnowIt> getImplicits() {
		unimplemented("getImplicits");
		return null;
	}

	public Collection<KnowIt> getVariables() {
		unimplemented("getVariables");
		return null;
	}

	public StoryComponent getOwner() {
		unimplemented("getOwner");
		return null;
	}

	public Collection<KnowIt> getParameters() {
		unimplemented("getParameters");
		return null;
	}

	public Collection<KnowIt> getSlotParameters() {
		unimplemented("getSlotParameters");
		return null;
	}

	public KnowIt getParameter(String keyword) {
		unimplemented("getParameter (" + keyword + ")");
		return null;
	}

	/**
	 * Gets the first cause that matches the location information.
	 * 
	 * @return
	 */
	public CauseIt getFirstCause() {
		for (StoryNode node : this.storyNodes) {
			for (StoryComponent child : node.getChildren()) {
				if (child instanceof StoryPoint) {
					for (StoryComponent storyPointChild : ((StoryPoint) child)
							.getChildren()) {
						return this.getFirstValidCause(storyPointChild);
					}
				} else
					return this.getFirstValidCause(child);
			}
		}

		return null;
	}

	private CauseIt getFirstValidCause(StoryComponent component) {
		if (component instanceof CauseIt) {
			final CauseIt causeIt = (CauseIt) component;

			final Collection<CodeBlock> codeBlocks;

			codeBlocks = causeIt.getCodeBlocksForLocation(this.locationInfo);

			if (!codeBlocks.isEmpty())
				return causeIt;
		}

		return null;
	}

	/**
	 * Returns unique causes based on the
	 * {@link CauseIt#isEquivalentToCause(CauseIt)} method.
	 * 
	 * @return
	 */
	public Collection<CauseIt> getCauses() {
		final Collection<CauseIt> causes = new ArrayList<CauseIt>();

		for (StoryNode point : this.storyNodes) {
			for (StoryComponent child : point.getChildren()) {
				if (child instanceof StoryPoint) {
					for (StoryComponent storyPointChild : ((StoryPoint) child)
							.getChildren()) {

						if (storyPointChild instanceof CauseIt) {
							final CauseIt causeIt = (CauseIt) storyPointChild;

							final Collection<CodeBlock> codeBlocks;

							codeBlocks = causeIt
									.getCodeBlocksForLocation(this.locationInfo);

							if (!codeBlocks.isEmpty()) {
								boolean causeExists = false;

								for (CauseIt cause : causes) {
									// Don't add equivalent causes to the list
									if (cause.isEquivalentToCause(causeIt)) {
										causeExists = true;
										break;
									}
								}

								if (!causeExists)
									causes.add(causeIt);
							}
						}
					}
				} else if (child instanceof CauseIt) {
					final CauseIt causeIt = (CauseIt) child;

					final Collection<CodeBlock> codeBlocks;

					codeBlocks = causeIt
							.getCodeBlocksForLocation(this.locationInfo);

					if (!codeBlocks.isEmpty()) {
						boolean causeExists = false;

						for (CauseIt cause : causes) {
							// Don't add equivalent causes to the list
							if (cause.isEquivalentToCause(causeIt)) {
								causeExists = true;
								break;
							}
						}

						if (!causeExists)
							causes.add(causeIt);
					}
				}
			}
		}

		return causes;
	}

	public Collection<StoryNode> getStoryNodes() {
		return this.storyNodes;
	}

	public Collection<StoryPoint> getStoryPoints() {
		final Collection<StoryPoint> storyPoints = new ArrayList<StoryPoint>();

		for (StoryNode storyNode : this.storyNodes) {
			if (storyNode instanceof StoryPoint)
				storyPoints.add((StoryPoint) storyNode);
			else if (storyNode instanceof StoryGroup) {
				for (StoryComponent child : storyNode.getChildren()) {
					if (child instanceof StoryPoint)
						storyPoints.add((StoryPoint) child);
				}
			}
		}

		return storyPoints;
	}

	/**
	 * Gets the descendants in an ordered list. This is slower than
	 * {@link #getStoryNodes()}, so make sure you know what you are getting
	 * into.
	 * 
	 * @return
	 */
	public Collection<? extends StoryNode> getOrderedStoryNodes() {
		return this.model.getRoot().getOrderedDescendants();
	}

	/**
	 * Gets the descendants in an ordered list. This is slower than
	 * {@link #getStoryPoints()}, so make sure you know what you are getting
	 * into.
	 * 
	 * @return
	 */
	public Collection<? extends StoryPoint> getOrderedStoryPoints() {
		final Collection<StoryNode> storyNodes = this.model.getRoot()
				.getOrderedDescendants();
		
		final Collection<StoryPoint> storyPoints = new ArrayList<StoryPoint>();
		
		for (StoryNode storyNode : storyNodes) {
			if (storyNode instanceof StoryPoint)
				storyPoints.add((StoryPoint) storyNode);
			else if (storyNode instanceof StoryGroup) {
				for (StoryComponent child : storyNode.getChildren()) {
					if (child instanceof StoryPoint)
						storyPoints.add((StoryPoint) child);
				}
			}
		}

		return storyPoints;
	}

	/**
	 * Return the dialogue roots of the model.
	 * 
	 * @return
	 */
	public Collection<DialogueLine> getDialogueRoots() {
		return this.model.getDialogueRoots();
	}

	/**
	 * Returns the immediate children of the {@link StoryPoint}.
	 * 
	 * @return
	 */
	public Collection<StoryNode> getStoryPointChildren() {
		this.unimplemented("getStoryPointChildren");
		return null;
	}

	/**
	 * Returns the immediate parents of the {@link StoryPoint}.
	 * 
	 * @return
	 */
	public Collection<StoryNode> getStoryPointParents() {
		this.unimplemented("getStoryPointParents");
		return null;
	}

	public KnowIt getSubject() {
		unimplemented("getSubject");
		return null;
	}

	public ScriptIt getScriptIt(String keyword) {
		unimplemented("getScriptIt: " + keyword);
		return null;
	}

	public KnowIt getSlotParameter(String keyword) {
		unimplemented("getKnowIt: " + keyword);
		return null;
	}

	public AskIt getAskIt() {
		unimplemented("getAskIt");
		return null;
	}

	public String getCode() {
		unimplemented("getCode");
		return null;
	}

	public String getCommitting() {
		unimplemented("getComitting");
		return null;
	}

	public String getCondition() {
		unimplemented("getCondition");
		return null;
	}

	public String getFanIn() {
		unimplemented("getFanIn");
		return null;
	}

	public String getFormattedValue() {
		unimplemented("getFormattedValue");
		return null;
	}

	public String getInclude() {
		unimplemented("getInclude");
		return null;
	}

	public String getName() {
		unimplemented("getName");
		return null;
	}

	protected String getNameOf(StoryComponent component) {
		unimplemented("getNameOf(" + component + ")");
		return null;
	}

	public String getTemplateID() {
		unimplemented("getTemplateID");
		return null;
	}

	public String getType() {
		unimplemented("getType");
		return null;
	}

	public String getUniqueName(Pattern legalFormat) {
		unimplemented("getUniqueName(" + legalFormat + ")");
		return null;
	}

	public String getValue() {
		unimplemented("getValue");
		return null;
	}

	public Set<String> getIncludeFiles() {
		unimplemented("getIncludeFiles");
		return null;
	}

	public String getStoryPointName() {
		unimplemented("getStoryPointName");
		return null;
	}

	public String getDisplayText() {
		unimplemented("getDisplayText");
		return null;
	}

	public String getUniqueID() {
		unimplemented("getUniqueID");
		return null;
	}

	public String getUnique32CharName() {
		unimplemented("getUnique32CharName");
		return null;
	}

	public String getControlItFormat() {
		unimplemented("getControlItFormat");
		return null;
	}

	public Collection<CauseIt> getIdenticalCauses() {
		unimplemented("getIdenticalCauses");
		return null;
	}

	public Object getCause() {
		unimplemented("getCause");
		return null;
	}

	public String getSlotConditional() {
		unimplemented("getSlotConditional");
		return null;
	}

	public Collection<KnowIt> getParametersWithSlot() {
		unimplemented("getParameteresWithImplicits");
		return null;
	}

	public String getParentName() {
		unimplemented("getParentName");
		return null;
	}

	public Collection<DialogueLine> getOrderedDialogueLines() {
		unimplemented("getOrderedDialogueLines");
		return null;
	}

	public Collection<DialogueLine> getChildLines() {
		unimplemented("getChildLines");
		return null;
	}

	public String getText() {
		unimplemented("getText");
		return null;
	}

	public String getSpeaker() {
		unimplemented("getSpeaker");
		return null;
	}

	public String getEnabled() {
		unimplemented("getEnabled");
		return null;
	}

	/**
	 * Throws a CodeGenerationException if the called method is not implemented
	 * in a subclass, or the implementation is not correctly called.
	 * 
	 * @param methodName
	 */
	private void unimplemented(String methodName) {
		throw (new CodeGenerationException(UNIMPLEMENTED + ": " + methodName
				+ " unimplemented in " + this.getClass().getName()));
	}

	public KnowIt getAudio() {
		unimplemented("getAudio");
		return null;
	}

	public KnowIt getImage() {
		unimplemented("getImage");
		return null;
	}

	public Resource getResource() {
		unimplemented("getResource");
		return null;
	}
}
