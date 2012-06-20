package scriptease.translator.codegenerator.code.contexts;

import scriptease.controller.AbstractNoOpStoryVisitor;
import scriptease.controller.BindingVisitor;
import scriptease.gui.quests.QuestPointNode;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingQuestPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryItemSequence;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingConstantContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingFunctionContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingNullContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingQuestPointContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingReferenceContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingRunTimeContext;

/**
 * ContextFactory generates a new context based on the current source. It also
 * associates this new context with the past context so it may propagate used
 * names and other properties. Whenever a new context is created, ContextFactory
 * needs to be updated in order to be able to create this new context.
 * 
 * ContextFactory implements the singleton and factory design pattern.
 * 
 * @author mfchurch
 * 
 */
public class ContextFactory {
	private static ContextFactory instance;
	private Context activeContext;

	 public static ContextFactory getInstance() {
		if (instance == null)
			instance = new ContextFactory();
		return instance;
	}

	/**
	 * Fabricates a new Context based on the pastContext and the source returns
	 * new _TYPE_Context(pastContext, (_TYPE_)source)
	 * 
	 * @param pastContext
	 * @param source
	 * @return
	 */
	public Context createContext(final Context pastContext, final Object source) {
		if (source instanceof String) {
			this.activeContext = pastContext;
		} else if (source instanceof KnowItBinding) {
			((KnowItBinding) source).process(new BindingVisitor() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					activeContext = new KnowItBindingFunctionContext(
							pastContext, function);
				}

				@Override
				public void processConstant(KnowItBindingConstant constant) {
					activeContext = new KnowItBindingConstantContext(
							pastContext, constant);
				}

				@Override
				public void processNull(KnowItBindingNull nullBinding) {
					activeContext = new KnowItBindingNullContext(pastContext,
							nullBinding);
				}

				@Override
				public void processReference(KnowItBindingReference reference) {
					activeContext = new KnowItBindingReferenceContext(
							pastContext, reference);
				}

				@Override
				public void processRunTime(KnowItBindingRunTime runTime) {
					activeContext = new KnowItBindingRunTimeContext(
							pastContext, runTime);
				}

				@Override
				public void processDescribeIt(KnowItBindingDescribeIt described) {
					ScriptIt resolvedDoIt = described.getValue()
							.getResolvedScriptIt();
					if (resolvedDoIt != null)
						activeContext = new KnowItBindingFunctionContext(
								pastContext, new KnowItBindingFunction(
										resolvedDoIt));
				}

				@Override
				public void processQuestPoint(KnowItBindingQuestPoint questPoint) {
					activeContext = new KnowItBindingQuestPointContext(
							pastContext, questPoint);
					
				}
			});
		} else if (source instanceof StoryComponent) {
			((StoryComponent) source).process(new AbstractNoOpStoryVisitor() {
				protected void defaultProcessComplex(
						ComplexStoryComponent complex) {
					activeContext = new ComplexStoryComponentContext(
							pastContext, complex);
				}
				
				/* COMPLEX TYPES */
				@Override
				public void processScriptIt(ScriptIt scriptIt) {
					activeContext = new ScriptItContext(pastContext, scriptIt);
				}
				
				@Override
				public void processStoryItemSequence(StoryItemSequence sequence) {
					activeContext = new StoryItemSequenceContext(pastContext,
							sequence);
				}

				@Override
				public void processStoryComponentContainer(
						StoryComponentContainer container) {
					this.defaultProcessComplex(container);
				}

				/* ATOMIC TYPES */
				@Override
				public void processAskIt(AskIt questionIt) {
					activeContext = new AskItContext(pastContext, questionIt);
				}

				@Override
				public void processKnowIt(KnowIt knowIt) {
					activeContext = new KnowItContext(pastContext, knowIt);
				}
			});
		} else if (source instanceof CodeBlock) {
			activeContext = new CodeBlockContext(pastContext,
					(CodeBlock) source);
		} else if (source instanceof QuestPointNode) {
			activeContext = new QuestPointNodeContext(pastContext,
					(QuestPointNode) source);
		} else
			throw new IllegalStateException(
					"Cannot Generate Context for Object: " + source);
		return activeContext;
	}
}
