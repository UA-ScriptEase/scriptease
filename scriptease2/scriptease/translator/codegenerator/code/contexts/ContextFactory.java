package scriptease.translator.codegenerator.code.contexts;

import scriptease.controller.BindingVisitor;
import scriptease.controller.StoryAdapter;
import scriptease.model.CodeBlock;
import scriptease.model.StoryComponent;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ControlIt;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryComponentContainer;
import scriptease.model.complex.StoryPoint;
import scriptease.translator.codegenerator.CodeGenerationException;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingFunctionContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingNullContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingReferenceContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingResourceContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingRunTimeContext;
import scriptease.translator.codegenerator.code.contexts.knowitbindingcontext.KnowItBindingStoryPointContext;

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
		}
		// this should get checked last, otherwise the ones above can get caught
		// by it because they're subclasses.
		else if (source instanceof StoryComponent) {
			created = this.createContext(context, (StoryComponent) source);
		} else {
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
			public void processConstant(KnowItBindingResource constant) {
				ContextFactory.this.activeContext = new KnowItBindingResourceContext(
						pastContext, constant);
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
			public void processRunTime(KnowItBindingRunTime runTime) {
				ContextFactory.this.activeContext = new KnowItBindingRunTimeContext(
						pastContext, runTime);
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				ContextFactory.this.activeContext = new KnowItBindingStoryPointContext(
						pastContext, storyPoint);

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
			public void processCauseIt(CauseIt causeIt) {
				ContextFactory.this.activeContext = new CauseItContext(
						pastContext, causeIt);
			}

			@Override
			public void processControlIt(ControlIt controlIt) {
				ContextFactory.this.activeContext = new ControlItContext(
						pastContext, controlIt);
			}

			@Override
			public void processStoryComponentContainer(
					StoryComponentContainer container) {
				this.defaultProcessComplex(container);
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

	/**
	 * Creates a new Context based on the pastContext and the source
	 * {@link StoryPoint}.
	 * 
	 * @param pastContext
	 * @param source
	 * @return
	 */
	private Context createContext(final Context pastContext,
			final StoryPoint source) {
		this.activeContext = new StoryPointContext(pastContext, source);

		return this.activeContext;
	}
}
