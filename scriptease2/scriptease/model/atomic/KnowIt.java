package scriptease.model.atomic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import scriptease.controller.BindingAdapter;
import scriptease.controller.StoryVisitor;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.controller.undo.UndoManager;
import scriptease.model.StoryComponent;
import scriptease.model.TypedComponent;
import scriptease.model.atomic.describeits.DescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingAutomatic;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.complex.AskIt;
import scriptease.model.complex.CauseIt;
import scriptease.model.complex.ComplexStoryComponent;
import scriptease.model.complex.ScriptIt;
import scriptease.model.complex.StoryPoint;
import scriptease.model.semodel.SEModel;
import scriptease.model.semodel.SEModelManager;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.model.semodel.librarymodel.TypeConverter;
import scriptease.translator.Translator;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.ListOp;

/**
 * Represents a piece of data within the script, and can be thought of as a
 * variable in code. It is an abstraction of all the ways data (i.e. in
 * variables) can appear and be used within a program.<br>
 * <br>
 * <i>Binding</i> a <code>KnowIt</code> means assigning it a single value to
 * which it can resolve, similar to a variable assignment. A <code>KnowIt</code>
 * can be bound to one of the following:
 * <ol>
 * <li><b>KnowIt</b> - KnowIt to KnowIt bindings are the simplest form of
 * binding. They forward the binding resolution to the KnowIt who is the target
 * of such a binding. The most likely instance of this happening are binding
 * parameter KnowIts to other KnowIts higher in scope.</li>
 * <li><b>DoIt</b> - Binding a KnowIt to a DoIt will assign the KnowIt that
 * DoIt's return value.</li>
 * <li><b>Constant</b> - This is a constant in either the target scripting
 * language or a reference to a {@link Resource}.</li>
 * <li><b>null</b> - the default binding when a KnowIt is created.</li>
 * </ol>
 * 
 * @author friesen
 * @author remiller
 * @author graves
 * @author mfchurch
 * @author jyuen
 */
public final class KnowIt extends StoryComponent implements TypedComponent,
		StoryComponentObserver {
	private KnowItBinding knowItBinding;
	private Collection<String> types;
	private String originalDisplayText;

	/**
	 * Creates a non unique KnowIt with the passed in name.
	 * 
	 * @param name
	 */
	public KnowIt(String name) {
		this(LibraryModel.getNonLibrary(), name);
	}

	/**
	 * Creates a non unique KnowIt with the passed in name and type.
	 * 
	 * @param name
	 */
	public KnowIt(String name, String type) {
		this(LibraryModel.getNonLibrary(), name, type);
	}

	public KnowIt(LibraryModel library) {
		this(library, "");
	}

	public KnowIt(LibraryModel library, String name) {
		this(library, name, new ArrayList<String>());

	}

	/**
	 * The constructor for a KnowIt with only one type.
	 * 
	 * @param name
	 * @param type
	 */
	public KnowIt(LibraryModel library, String name, final String type) {
		this(library, name, ListOp.createList(type));
	}

	/**
	 * The constructor for <code>KnowIt</code>s.<br>
	 * The initial binding for <code>KnowIt</code>s is <code>null</code>.
	 * 
	 * @param name
	 *            The name of the <code>KnowIt</code>.
	 * @param types
	 *            The types of the <code>KnowIt</code>.
	 */
	public KnowIt(LibraryModel library, String name, Collection<String> types) {
		super(library, name);
		// null binding by default
		this.types = new HashSet<String>();
		this.originalDisplayText = "";
		this.knowItBinding = new KnowItBindingNull();
		this.clearBinding();

		if (types != null) {
			this.types = types;
		}
	}

	@Override
	public KnowIt clone() {
		final KnowIt clone;
		final LibraryModel library;

		clone = (KnowIt) super.clone();
		library = this.getLibrary();

		// Add the types before setting the binding, or it may be rejected
		clone.types = new ArrayList<String>(this.types);

		clone.setBinding(this.knowItBinding.clone());

		if (library != null) {
			final DescribeIt describeIt;

			describeIt = library.getDescribeIt(this);

			if (describeIt != null)
				library.addDescribeIt(describeIt, clone);
		}

		return clone;
	}

	@Override
	public void process(StoryVisitor processController) {
		processController.processKnowIt(this);
	}

	/**
	 * Change the <code>KnowIt</code>'s binding to the given
	 * <code>Resource</code>.
	 * 
	 * @param resource
	 *            The <code>Resource</code> to bind the <code>KnowIt</code> to.
	 */
	public void setBinding(Resource resource) {
		if (resource == null)
			throw new IllegalArgumentException(
					"KnowIt cannot be bound to Resource null");
		KnowItBindingResource bindingValue = new KnowItBindingResource(resource);
		this.setBinding(bindingValue);
	}

	/**
	 * Change the <code>KnowIt</code>'s binding to the given <code>KnowIt</code>
	 * .
	 * 
	 * @param value
	 *            The <code>KnowIt</code> to bind the <code>KnowIt</code> to.
	 */
	public void setBinding(KnowIt value) {
		if (value == null)
			throw new IllegalArgumentException(
					"KnowIt cannot be bound to KnowIt null");
		KnowItBindingReference bindingValue = new KnowItBindingReference(value);
		this.setBinding(bindingValue);
	}

	/**
	 * Change the <code>KnowIt</code>'s binding to the given
	 * <code>ScriptIt</code>.
	 * 
	 * @param value
	 *            The <code>ScriptIt</code> to bind the <code>KnowIt</code> to.
	 */
	public void setBinding(ScriptIt value) {
		if (value == null)
			throw new IllegalArgumentException(
					"KnowIt cannot be bound to ScriptIt null");
		KnowItBindingFunction bindingValue = new KnowItBindingFunction(value);
		this.setBinding(bindingValue);
	}

	/**
	 * Change the <code>KnowIt</code>'s binding to the given
	 * <code>StoryPoint</code>.
	 * 
	 * @param value
	 *            The StoryPoint to be known by this KnowIt
	 */
	public void setBinding(StoryPoint value) {
		if (value == null)
			throw new IllegalArgumentException(
					"KnowIt cannot be bound to ScriptIt null");
		KnowItBindingStoryPoint bindingValue = new KnowItBindingStoryPoint(
				value);
		this.setBinding(bindingValue);
	}

	/**
	 * Shortcut for setting the KnowIt's binding to KnowItBindingNull
	 */
	public void clearBinding() {
		this.knowItBinding.process(new BindingAdapter() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				// do nothing if already null
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				setBinding(new KnowItBindingNull());
			}
		});
	}

	/**
	 * Sets the binding to the given KnowItBinding. Clients should prefer to use
	 * the other setBinding(...) methods that are of the specific binding type.
	 * 
	 * @param value
	 *            The binding that the KnowIt will point to.
	 * @see #setBinding(DescribeIt)
	 * @see #setBinding(KnowIt)
	 * @see #setBinding(StoryPoint)
	 * @see #setBinding(ScriptIt)
	 */
	public void setBinding(KnowItBinding value) {
		if (value == null)
			value = new KnowItBindingNull();

		// Ignore compatibility during undoing or redoing as the model may be
		// invalid for the duration of the undo/redo
		if (!UndoManager.getInstance().isUndoingOrRedoing()
				&& !value.compatibleWith(this)) {
			return;
		}

		value.process(new BindingAdapter() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				// Find an appropriate Default binding for the type if it's GUI
				// type isn't null.
				KnowItBinding bindingValue = nullBinding;

				final SEModel model;

				model = SEModelManager.getInstance().getActiveModel();

				if (model != null) {
					for (String type : KnowIt.this.types) {
						final GameType gameType = model.getType(type);

						if (gameType != null && gameType.getGui() != null) {
							bindingValue = new KnowItBindingResource(
									SimpleResource.buildSimpleResource(type));
						}
					}
				}

				this.defaultProcess(bindingValue);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				defaultProcess(function);
				// Observer the reference
				final ScriptIt scriptIt = function.getValue();
				scriptIt.setOwner(KnowIt.this);
				addObservers(scriptIt);
				addParameterObservers(scriptIt);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				defaultProcess(reference);
				// Observer the reference
				final KnowIt knowIt = reference.getValue();

				if (knowIt.getOwner() == null)
					knowIt.setOwner(KnowIt.this);

				addObservers(knowIt);
			}

			@Override
			public void processUninitialized(
					KnowItBindingUninitialized uninitialized) {
				defaultProcess(uninitialized);

				final KnowIt knowIt = uninitialized.getValue();
				addObservers(knowIt);
			}

			@Override
			public void processAutomatic(KnowItBindingAutomatic automatic) {
				defaultProcess(automatic);
				automatic.setOwner(KnowIt.this);
			}

			@Override
			protected void defaultProcess(KnowItBinding newBinding) {
				/*
				 * if the KnowIt was previously referencing another
				 * StoryComponent, unregister it so we don't receive updates
				 * from it anymore
				 */
				KnowIt.this.knowItBinding.process(new BindingAdapter() {
					@Override
					public void processFunction(KnowItBindingFunction function) {
						removeObservers(function.getValue());
					}

					@Override
					public void processReference(
							KnowItBindingReference reference) {
						removeObservers(reference.getValue());
					}

					@Override
					public void processUninitialized(
							KnowItBindingUninitialized uninitialized) {
						removeObservers(uninitialized.getValue());
					}

					@Override
					public void processStoryPoint(
							KnowItBindingStoryPoint storyPoint) {
						removeObservers(storyPoint.getValue());
					}
				});
				KnowIt.this.knowItBinding = newBinding;
				notifyObservers(new StoryComponentEvent(KnowIt.this,
						StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND));
			}

			/**
			 * Add observers from the given component and its parameters
			 * 
			 * @param component
			 */
			private void addObservers(StoryComponent component) {
				component.addStoryComponentObserver(KnowIt.this);
			}

			private void addParameterObservers(ScriptIt component) {
				for (KnowIt param : component.getParameters())
					param.addStoryComponentObserver(KnowIt.this);
			}

			private void removeParameterObservers(ScriptIt component) {
				for (KnowIt param : component.getParameters())
					param.removeStoryComponentObserver(KnowIt.this);
			}

			/**
			 * Removes observers from the given component and it's parameters
			 * 
			 * @param component
			 */
			private void removeObservers(StoryComponent component) {
				component.removeStoryComponentObserver(KnowIt.this);
				if (component instanceof ScriptIt) {
					removeParameterObservers((ScriptIt) component);
				}
			}
		});

	}

	public String getScriptValue() {
		return this.knowItBinding.getScriptValue();
	}

	/**
	 * Gets the <code>KnowIt</code>'s current binding.
	 * 
	 * @return The current binding of the <code>KnowIt</code>.
	 */
	public KnowItBinding getBinding() {
		return this.knowItBinding;
	}

	@Override
	public Collection<String> getTypes() {
		return this.types;
	}

	/**
	 * Get a Collection of acceptable type keywords. A GameType is considered to
	 * be acceptable if it is in the immediate KnowIt's types, or if it can be
	 * converted from using a GameTypeConverter. If no Translator is loaded,
	 * this acts the same as getTypes().
	 * 
	 * @return
	 */
	public Collection<String> getAcceptableTypes() {
		final Translator translator = this.getLibrary().getTranslator();
		final Collection<String> acceptedTypes = new ArrayList<String>();

		// The KnowIts types are acceptable
		acceptedTypes.addAll(this.types);

		// Acceptable Types also include types that can be converted from
		if (translator != null) {
			final LibraryModel library = translator.getLibrary();
			final TypeConverter converter = library.getTypeConverter();

			for (String type : this.types) {
				acceptedTypes.addAll(converter.getConvertableTypes(type));
			}
		}
		return acceptedTypes;
	}

	public void setTypes(Collection<GameType> types) {
		this.setTypesByName(GameType.getTypeNames(types));
	}

	/**
	 * Sets the types of the KnowIt and notifies all observers of a
	 * {@link StoryComponentChangeEnum#CHANGE_KNOW_IT_TYPE} event.
	 * 
	 * @param types
	 */
	public void setTypesByName(Collection<String> types) {
		this.types = types;
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_KNOW_IT_TYPE));
	}

	/**
	 * Gets the KnowIt's first type, if the knowIt has no types, returns null
	 * 
	 * @return the KnowIt's first type
	 */
	public String getDefaultType() {
		if (!this.types.isEmpty())
			return this.types.iterator().next();
		else {
			System.err
					.println("Tried getting default type from a typeless knowIt");
			return null;
		}
	}

	public void addType(String type) {
		this.types.add(type);
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_KNOW_IT_TYPE));
	}

	public void clearTypes() {
		this.types.clear();
		this.clearBinding();
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_KNOW_IT_TYPE));
	}

	public void removeType(String type) {
		this.types.remove(type);
		KnowItBinding binding = this.getBinding();
		if (binding.getTypes().contains(type)) {
			this.clearBinding();
			System.err
					.println("The Binding's type "
							+ type
							+ " is no longer supported by the KnowIt Type. Binding has been cleared");
		}
		this.notifyObservers(new StoryComponentEvent(this,
				StoryComponentChangeEnum.CHANGE_KNOW_IT_TYPE));
	}

	@Override
	public String toString() {
		return "KnowIt [" + this.getDisplayText() + "]";
	}

	/**
	 * Two KnowIts are equal if they are equal StoryComponents and they share
	 * the same binding
	 */
	@Override
	public boolean equals(Object other) {
		if (super.equals(other) && other instanceof KnowIt) {
			KnowItBinding binding = ((KnowIt) other).getBinding();
			return (binding.equals(this.knowItBinding));
		}
		return false;
	}

	@Override
	public void componentChanged(final StoryComponentEvent event) {
		final StoryComponentChangeEnum type = event.getType();

		if (type == StoryComponentChangeEnum.CHANGE_MOVED) {
			// If the reference has been moved, bind it to this new binding
			if (event.getSource() instanceof KnowIt) {
				final KnowIt knowIt = (KnowIt) event.getSource();
				this.setBinding(knowIt);
			}

		} else if (type == StoryComponentChangeEnum.CHANGE_REMOVED) {
			// If the reference has been removed, unbind
			this.clearBinding();

		} else if (type == StoryComponentChangeEnum.CHANGE_DISABILITY) {
			// If the reference has been disabled, we need to disable the
			// effects using it
			if (event.getSource() instanceof KnowIt) {
				final KnowIt binding = (KnowIt) event.getSource();
				if (!binding.isEnabled())
					this.disableOwner();
			}
		} else {
			// Forward reference updates to this KnowIt's observers
			this.knowItBinding.process(new BindingAdapter() {

				@Override
				public void processReference(KnowItBindingReference reference) {
					KnowIt.this.notifyObservers(event);
				}

				@Override
				public void processFunction(KnowItBindingFunction function) {
					KnowIt.this.notifyObservers(event);
				}

				@Override
				public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
					KnowIt.this.notifyObservers(event);
				}
			});
		}
	}

	@Override
	public void revalidateKnowItBindings() {
		final KnowItBinding binding;

		binding = this.getBinding();

		if (!binding.compatibleWith(this)) {
			final KnowItBinding newBinding;

			if (binding instanceof KnowItBindingReference) {
				// Try to fix reference bindings
				final KnowIt referred;
				// This is the previous knowIt we had a reference to.
				// We need to find out if the same KnowIt exists where we added
				// it to.
				referred = ((KnowItBindingReference) binding).getValue();

				final KnowIt replacement;
				final KnowItBinding replacementBinding;

				replacement = this.getReplacementForReference(referred);
				replacementBinding = new KnowItBindingReference(replacement);

				if (replacement != null
						&& replacementBinding.compatibleWith(this)) {
					newBinding = replacementBinding;
				} else
					newBinding = new KnowItBindingNull();
			} else
				newBinding = new KnowItBindingNull();

			this.setBinding(newBinding);
		} else
			binding.process(new BindingAdapter() {
				@Override
				public void processFunction(KnowItBindingFunction function) {
					function.getValue().revalidateKnowItBindings();
				}

				@Override
				public void processUninitialized(
						KnowItBindingUninitialized uninitialized) {
					final KnowIt value = uninitialized.getValue();

					StoryComponent owner = getOwner();
					while (owner != null) {
						if (owner instanceof ScriptIt) {
							final ScriptIt scriptIt = (ScriptIt) owner;

							if (scriptIt.getParameters().contains(value)) {
								break;
							}
						} else
							owner = owner.getOwner();
					}

					if (owner == null)
						setBinding(new KnowItBindingNull());
				}
			});
	}

	/**
	 * Finds the replacement for a lost reference.
	 * 
	 * @param reference
	 * @return
	 */
	private KnowIt getReplacementForReference(final KnowIt reference) {
		// Kind of ugly, but the only way we can return something.
		StoryComponent owner = this.getOwner();

		while (owner != null) {
			if (owner instanceof ComplexStoryComponent) {
				if (owner instanceof ScriptIt) {
					for (KnowIt implicit : ((ScriptIt) owner).getImplicits()) {
						if (implicit.equals(reference))
							return implicit;
					}
				}

				for (StoryComponent child : ((ComplexStoryComponent) owner)
						.getChildren()) {
					if (child instanceof KnowIt && child.equals(reference))
						return (KnowIt) child;
				}
			}
			owner = owner.getOwner();
		}

		return null;
	}

	@Override
	public void setEnabled(Boolean enabled) {
		super.setEnabled(enabled);

		final KnowItBinding binding = this.getBinding();
		if (binding instanceof KnowItBindingFunction) {
			final KnowItBindingFunction function = (KnowItBindingFunction) binding;
			final ScriptIt scriptIt = function.getValue();

			scriptIt.setEnabled(enabled);
		}
	}

	/**
	 * Disables the owner story component of this KnowIt - should only be used
	 * for non description KnowIts
	 */
	public void disableOwner() {
		final StoryComponent owner = this.getOwner();

		if (owner == null)
			return;

		if (owner instanceof AskIt)
			// Disable the question if it references this binding
			owner.setEnabled(false);
		else {
			// Or else disable the effect or description
			final StoryComponent scriptIt = owner.getOwner();
			if (scriptIt instanceof ScriptIt && !(scriptIt instanceof CauseIt)) {

				final StoryComponent description = scriptIt.getOwner();

				if (description != null && description instanceof KnowIt)
					description.setEnabled(false);
				else
					scriptIt.setEnabled(false);
			}
		}
	}

	/**
	 * Gets the original display text for a knowit. Used for dynamic updating of
	 * activity parameters.
	 * 
	 * @return
	 */
	public String getOriginalDisplayText() {
		return this.originalDisplayText;
	}

	/**
	 * Sets the original display text for a knowit. Used when activity
	 * parameters are changed.
	 * 
	 * @param text
	 */
	public void setOriginalDisplayText(String text) {
		this.originalDisplayText = text;
	}
}
