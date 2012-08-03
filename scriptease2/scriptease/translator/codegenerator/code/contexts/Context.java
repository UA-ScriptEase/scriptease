package scriptease.translator.codegenerator.code.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import scriptease.controller.ComplexStoryComponentDescendantCollector;
import scriptease.controller.get.QuestPointNodeGetter;
import scriptease.gui.graph.nodes.GraphNode;
import scriptease.gui.quests.QuestNode;
import scriptease.gui.quests.QuestPoint;
import scriptease.gui.quests.QuestPointNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.Translator;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.LocationInformation;
import scriptease.translator.codegenerator.code.CodeGenerationNamifier;
import scriptease.translator.io.model.GameObject;

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
 * 
 */
public class Context {
	private String indent = "";
	private final QuestNode model;
	protected final Translator translator;
	private CodeGenerationNamifier namifier;
	protected LocationInformation locationInfo;

	private static final String UNIMPLEMENTED = "<unimplemented in context>";

	public Context(QuestNode model, String indent,
			CodeGenerationNamifier existingNames, Translator translator) {

		this.translator = translator;
		this.indent = indent;

		this.model = model;
		this.setNamifier(new CodeGenerationNamifier(existingNames, translator
				.getLanguageDictionary()));
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
		return this.translator;
	}

	/**
	 * @return the Context's model
	 */
	public final QuestNode getModel() {
		return this.model;
	}

	protected final void setNamifier(CodeGenerationNamifier namifier) {
		this.namifier = namifier;
	}

	public final CodeGenerationNamifier getNamifier() {
		return namifier;
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
	 * Gets all of the story components in this context.
	 * 
	 * @return
	 */
	protected Collection<StoryComponent> getComponents() {
		final Collection<StoryComponent> components = new ArrayList<StoryComponent>();
		final Collection<QuestPoint> questPoints;

		// Get all the QuestPoints from the model
		questPoints = QuestPointNodeGetter.getQuestPoints(this.model);

		// for each quest point
		for (QuestPoint questPoint : questPoints) {
			// Get all the components from each QuestPoint
			ComplexStoryComponentDescendantCollector getter = new ComplexStoryComponentDescendantCollector();
			components.addAll(getter.collectDescendants(questPoint));
		}

		return components;
	}

	/**
	 * Returns all of the scriptIts from the Context's symbolTable which match
	 * the current slot
	 * 
	 * @return
	 */
	public Iterator<ScriptIt> getScriptIts() {
		final Collection<ScriptIt> scriptIts = new ArrayList<ScriptIt>();

		for (StoryComponent key : this.getComponents()) {
			if (key instanceof ScriptIt) {
				ScriptIt scriptIt = (ScriptIt) key;
				Collection<CodeBlock> codeBlocksForSlot = scriptIt
						.getCodeBlocksForLocation(this.locationInfo);
				if (!codeBlocksForSlot.isEmpty())
					scriptIts.add(scriptIt);
			}
		}
		return scriptIts.iterator();
	}

	/**
	 * Returns all of the codeBlocks associated with the Context's current slot
	 * 
	 * @return
	 */
	public Iterator<CodeBlock> getCodeBlocks() {
		final Collection<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();

		for (StoryComponent key : this.getComponents()) {
			if (key instanceof ScriptIt) {
				final ScriptIt scriptIt = (ScriptIt) key;
				Collection<CodeBlock> codeBlocksForSlot = scriptIt
						.getCodeBlocksForLocation(this.locationInfo);
				codeBlocks.addAll(codeBlocksForSlot);
			}
		}
		return codeBlocks.iterator();
	}

	/**
	 * This finds the CodeBlocks for special circumstances as described in the
	 * class that implements GameObject in the translator. In Neverwinter
	 * Nights, this includes code blocks in i_se_aux.
	 * 
	 * @return
	 */
	public List<CodeBlock> getBindingCodeBlocks() {
		final List<CodeBlock> codeBlocks = new ArrayList<CodeBlock>();

		for (StoryComponent key : this.getComponents()) {
			// The method first checks if there is a GameObject for the
			// StoryComponent.
			if (key instanceof KnowIt) {
				KnowItBinding binding = ((KnowIt) key).getBinding();
				if (binding instanceof KnowItBindingConstant) {
					KnowItBindingConstant kibConstant = (KnowItBindingConstant) binding;
					if (kibConstant.getValue() instanceof GameObject) {
						// Then gets a string representing the name of the code
						// block.
						String referenceValue = kibConstant.getScriptValue();

						// Gets the code block from the API Dictionary using the
						// reference string.
						List<CodeBlock> specialCodeBlocks = this.translator
								.getApiDictionary().getCodeBlocksByName(
										referenceValue);
						
						if (specialCodeBlocks != null)
							codeBlocks.addAll(specialCodeBlocks);
					}
				}
			}
		}
		return codeBlocks;
	}

	public CodeBlock getMainCodeBlock() {
		unimplemented("getMainCodeBlock");
		return null;
	}

	/**
	 * Returns only the scriptIts which are not causes from the Context's
	 * symbolTable
	 * 
	 * @return
	 */
	public Iterator<ScriptIt> getScriptItEffects() {
		final Collection<ScriptIt> effectScriptIts = new ArrayList<ScriptIt>();
		Iterator<ScriptIt> scriptIts = this.getScriptIts();
		while (scriptIts.hasNext()) {
			final ScriptIt scriptIt = scriptIts.next();
			if (!scriptIt.isCause())
				effectScriptIts.add(scriptIt);
		}
		return effectScriptIts.iterator();
	}

	public Iterator<KnowIt> getArguments() {
		unimplemented("getArguments");
		return new ArrayList<KnowIt>().iterator();
	}

	public Iterator<AskIt> getAskIts() {
		unimplemented("getAskIts");
		return new ArrayList<AskIt>().iterator();
	}

	public KnowItBinding getBinding() {
		unimplemented("getBinding");
		return null;
	}

	public Iterator<StoryComponent> getChildren() {
		unimplemented("getChildren");
		return new ArrayList<StoryComponent>().iterator();
	}

	public StoryItemSequence getIfChild() {
		unimplemented("getIfChild");
		return null;
	}

	public StoryItemSequence getElseChild() {
		unimplemented("getElseChild");
		return null;
	}

	public Iterator<KnowIt> getImplicits() {
		unimplemented("getImplicits");
		return null;
	}

	public Iterator<String> getIncludes() {
		final List<String> includes = new ArrayList<String>();
		Iterator<CodeBlock> codeBlocks = this.getCodeBlocks();
		while (codeBlocks.hasNext()) {
			final CodeBlock codeBlock = codeBlocks.next();
			includes.addAll(codeBlock.getIncludes());
		}
		return includes.iterator();
	}

	public Iterator<KnowIt> getVariables() {
		unimplemented("getKnowIts");
		return new ArrayList<KnowIt>().iterator();
	}

	public StoryComponent getOwner() {
		unimplemented("getOwner");
		return null;
	}

	public Iterator<KnowIt> getParameters() {
		unimplemented("getParameters");
		return new ArrayList<KnowIt>().iterator();
	}

	public KnowIt getParameter(String keyword) {
		unimplemented("getParameter (" + keyword + ")");
		return null;
	}

	public Iterator<ScriptIt> getCauses() {
		final Collection<ScriptIt> causes = new ArrayList<ScriptIt>();
		Iterator<ScriptIt> scriptIts = this.getScriptIts();
		while (scriptIts.hasNext()) {
			final ScriptIt scriptIt = scriptIts.next();
			if (scriptIt.isCause())
				causes.add(scriptIt);
		}
		return causes.iterator();
	}

	public Iterator<ScriptIt> getEffects() {
		final Collection<ScriptIt> effects = new ArrayList<ScriptIt>();
		Iterator<ScriptIt> scriptIts = this.getScriptIts();
		while (scriptIts.hasNext()) {
			final ScriptIt scriptIt = scriptIts.next();
			if (!scriptIt.isCause())
				effects.add(scriptIt);
		}
		return effects.iterator();
	}

	public KnowIt getSubject() {
		unimplemented("getSubject");
		return null;
	}

	public ScriptIt getScriptIt(String keyword) {
		unimplemented("getScriptIt");
		return null;
	}

	public AskIt getAskIt() {
		unimplemented("getAskIt");
		return null;
	}

	public Iterator<QuestNode> getQuestNodes() {
		unimplemented("getQuests");
		return new ArrayList<QuestNode>().iterator();
	}

	public Iterator<QuestPointNode> getQuestPointNodes() {
		return QuestPointNodeGetter.getQuestPointNodes(this.model).iterator();
	}

	public GraphNode getEndPoint() {
		unimplemented("getEndPoint");
		return null;
	}

	/**
	 * Default to the first QuestPoint in the model's Quest Graph
	 * 
	 * @return
	 */
	public GraphNode getStartPoint() {
		return this.model.getStartPoint();
	}

	public Iterator<GraphNode> getChildrenNodes() {
		unimplemented("getChildrenNodes");
		return null;
	}

	public Iterator<GraphNode> getParentNodes() {
		unimplemented("getParentNodes");
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

	public String getQuestContainer() {
		unimplemented("getQuestContainer");
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
}
