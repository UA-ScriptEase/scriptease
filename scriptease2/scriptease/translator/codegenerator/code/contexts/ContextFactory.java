package scriptease.translator.codegenerator.code.contexts;

import scriptease.controller.BindingVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryGroup;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.ActivityIt;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.PickIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.model.complex.behaviours.Behaviour;
import scriptease.model.complex.behaviours.CollaborativeTask;
import scriptease.model.complex.behaviours.IndependentTask;
import scriptease.model.semodel.dialogue.DialogueLine;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingAutomaticContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingFunctionContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingNullContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingReferenceContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingResourceContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingStoryGroupContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingStoryPointContext;
import scriptease.translator.codegenerator.code.contexts.knowitbinding.KnowItBindingUninitializedContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.AskItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.KnowItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.PickItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.StoryComponentContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.ComplexStoryComponentContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit.ActivityItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit.BehaviourContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit.CauseItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit.ControlItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.scriptit.ScriptItContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.storynode.StoryPointContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task.CollaborativeTaskContext;
import scriptease.translator.codegenerator.code.contexts.storycomponent.complexstorycomponent.task.IndependentTaskContext;
import scriptease.translator.io.model.Resource;

/**
 * ContextFactory generates a new context based on the current source. It also
 * associates this new context with the past context so it may propagate used
 * names and other properties. Whenever a new context is created, ContextFactory
 * needs to be updated in order to be able to create this new context.
 * 
 * ContextFactory implements the singleton and factory design pattern.
 * 
 * @author mfchurch
 * @author remiller
 * @author jyuen
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
	 * Creates a new context. If the object is not supported, this method will
	 * throw a {@link CodeGenerationException}. <br>
	 * <br>
	 * <b>Currently Supported Types:</b>
	 * <ul>
	 * <li>CodeBlock</li>
	 * <li>KnowItBinding</li>
	 * <li>StoryPoint</li>
	 * </ul>
	 * 
	 * @param context
	 * @param source
	 * @return
	 */
	public Context createContext(Context context, Object source) {
		final Context created;

		/*
		 * This instance of block disgusts me, but we need some way of
		 * determining which context to create, and type erasure loses that
		 * info. - remiller
		 */
		if (source instanceof CodeBlock) {
			created = this.createContext(context, (CodeBlock) source);
		} else if (source instanceof KnowItBinding) {
			created = this.createContext(context, (KnowItBinding) source);
		} else if (source instanceof StoryPoint) {
			created = this.createContext(context, (StoryPoint) source);
			// this should get checked last, otherwise the ones above can get
			// caught by it because they're subclasses.
		} else if (source instanceof StoryComponent) {
			created = this.createContext(context, (StoryComponent) source);
		} else if (source instanceof DialogueLine) {
			created = this.createContext(context, (DialogueLine) source);
		} else if (source instanceof Resource) {
			created = this.createContext(context, (Resource) source);
		} else {
			if (context instanceof FileContext)
				created = context;
			else
				throw new CodeGenerationException(
						"Cannot Generate Context for Object: " + source);
		}

		return created;
	}

	/**
	 * Creates a new Context based on the pastContext and the source
	 * {@link KnowItBinding}.
	 * 
	 * @param pastContext
	 * @param source
	 * @return
	 */
	private Context createContext(final Context pastContext,
			final KnowItBinding source) {
		source.process(new BindingVisitor() {
			@Override
			public void processFunction(KnowItBindingFunction function) {
				ContextFactory.this.activeContext = new KnowItBindingFunctionContext(
						pastContext, function);
			}

			@Override
			public void processResource(KnowItBindingResource constant) {
				ContextFactory.this.activeContext = new KnowItBindingResourceContext(
						pastContext, constant);
			}

			@Override
			public void processUninitialized(
					KnowItBindingUninitialized uninitialized) {
				ContextFactory.this.activeContext = new KnowItBindingUninitializedContext(
						pastContext, uninitialized);
			}

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				ContextFactory.this.activeContext = new KnowItBindingNullContext(
						pastContext, nullBinding);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				ContextFactory.this.activeContext = new KnowItBindingReferenceContext(
						pastContext, reference);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				ContextFactory.this.activeContext = new KnowItBindingStoryPointContext(
						pastContext, storyPoint);
			}

			@Override
			public void processStoryGroup(KnowItBindingStoryGroup storyGroup) {
				ContextFactory.this.activeContext = new KnowItBindingStoryGroupContext(
						pastContext, storyGroup);
			}

			@Override
			public void processAutomatic(KnowItBindingAutomatic automatic) {
				ContextFactory.this.activeContext = new KnowItBindingAutomaticContext(
						pastContext, automatic);
			}
		});

		return this.activeContext;
	}

	/**
	 * Creates a new Context based on the pastContext and the source
	 * {@link StoryComponent}.
	 * 
	 * @param pastContext
	 * @param source
	 * @return
	 */
	private Context createContext(final Context pastContext,
			final StoryComponent source) {
		source.process(new StoryAdapter() {
			/* Default Processes */
			protected void defaultProcessComplex(ComplexStoryComponent complex) {
				ContextFactory.this.activeContext = new ComplexStoryComponentContext(
						pastContext, complex);
			}

			@Override
			protected void defaultProcessAtomic(StoryComponent atom) {
				ContextFactory.this.activeContext = new StoryComponentContext(
						pastContext, atom);
			}

			/* COMPLEX TYPES */
			@Override
			public void processScriptIt(ScriptIt scriptIt) {
				ContextFactory.this.activeContext = new ScriptItContext(
						pastContext, scriptIt);
			}

			@Override
			public void processBehaviour(Behaviour behaviour) {
				ContextFactory.this.activeContext = new BehaviourContext(
						pastContext, behaviour);
			}

			@Override
			public void processActivityIt(ActivityIt activityIt) {
				ContextFactory.this.activeContext = new ActivityItContext(
						pastContext, activityIt);
			}

			@Override
			public void processControlIt(ControlIt controlIt) {
				ContextFactory.this.activeContext = new ControlItContext(
						pastContext, controlIt);
			}

			@Override
			public void processPickIt(PickIt pickIt) {
				ContextFactory.this.activeContext = new PickItContext(
						pastContext, pickIt);
			}

			@Override
			public void processCauseIt(CauseIt causeIt) {
				ContextFactory.this.activeContext = new CauseItContext(
						pastContext, causeIt);
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				this.defaultProcessComplex(container);
			}

			@Override
			public void processCollaborativeTask(CollaborativeTask task) {
				ContextFactory.this.activeContext = new CollaborativeTaskContext(
						pastContext, task);
			}

			@Override
			public void processIndependentTask(IndependentTask task) {
				ContextFactory.this.activeContext = new IndependentTaskContext(
						pastContext, task);
			}

			/* ATOMIC TYPES */
			@Override
			public void processAskIt(AskIt questionIt) {
				ContextFactory.this.activeContext = new AskItContext(
						pastContext, questionIt);
			}

			@Override
			public void processKnowIt(KnowIt knowIt) {
				ContextFactory.this.activeContext = new KnowItContext(
						pastContext, knowIt);
			}
		});

		return this.activeContext;
	}

	/**
	 * Creates a new Context based on the pastContext and the source
	 * {@link CodeBlock}.
	 * 
	 * @param pastContext
	 * @param source
	 * @return
	 */
	private Context createContext(final Context pastContext,
			final CodeBlock source) {
		this.activeContext = new CodeBlockContext(pastContext, source);

		return this.activeContext;
	}

	private Context createContext(final Context pastContext,
			final Resource source) {
		this.activeContext = new ResourceContext(pastContext, source);

		return this.activeContext;
	}

	private Context createContext(Context pastContext, DialogueLine source) {
		this.activeContext = new DialogueLineContext(pastContext, source);

		return this.activeContext;
	}

	private Context createContext(final Context pastContext,
			final StoryPoint source) {
		this.activeContext = new StoryPointContext(pastContext, source);

		return this.activeContext;
	}
}
