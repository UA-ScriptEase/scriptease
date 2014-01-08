package scriptease.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import scriptease.controller.BindingAdapter;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.controller.observer.ResourceTreeAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.pane.ResourcePanel;
import scriptease.gui.transfer.SlotPanelTransferHandler;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingUninitialized;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;
import scriptease.util.StringOp;

/**
 * SlotPanel is a GUI slot which accepts KnowIt Bindings (binding slot). It
 * displays all of its acceptable types as well as a hint that the user can drop
 * 
 * Slot panels have three functions: display the legal types for the slot,
 * display the current binding, and provide an interface for rebinding/unbinding
 * knowIts.
 * 
 * @author mfchurch
 * @author kschenk
 * @author jyuen
 */
@SuppressWarnings("serial")
public class SlotPanel extends JPanel {
	private final KnowIt knowIt;
	private final boolean isNameEditable;

	public SlotPanel(final KnowIt knowIt, boolean isNameEditable) {
		if (knowIt == null)
			throw new IllegalStateException(
					"Cannot build a SlotPanel with a null KnowIt");
		this.knowIt = knowIt;
		this.isNameEditable = isNameEditable;

		final int borderSize = 2;

		this.setLayout(new FlowLayout(FlowLayout.LEFT, borderSize, borderSize));
		this.setEnabled(true);

		this.populate();

		this.knowIt.addStoryComponentObserver(new StoryComponentObserver() {
			@Override
			public void componentChanged(StoryComponentEvent event) {
				if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
					SlotPanel.this.populate();
				}
			}
		});

		ResourcePanel.getInstance().addObserver(this,
				new ResourceTreeAdapter() {
					@Override
					public void resourceSelected(final Resource selected) {
						updateSelectedResourceBorder(selected);

					}
				});
		updateSelectedResourceBorder(ResourcePanel.getInstance().getSelected());

	}

	/**
	 * 
	 */
	public void updateSelectedResourceBorder(final Resource selected) {
		knowIt.getBinding().process(new BindingAdapter() {
			public void processResource(KnowItBindingResource constant) {
				if (constant.getValue().equals(selected)) {
					SlotPanel.this.setBorder(BorderFactory.createLineBorder(
							Color.GREEN, 2));
				} else {
					this.defaultProcess(constant);
				}
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				SlotPanel.this.setBorder(BorderFactory.createLineBorder(
						Color.WHITE, 2));
			}
		});
	}

	/**
	 * Populate the panel with all of its components.
	 */
	public void populate() {
		this.removeAll();

		final KnowItBinding binding = this.knowIt.getBinding();
		final BindingWidget bindingWidget = this.buildBindingWidget();

		this.add(ScriptWidgetFactory.buildLegalTypesPanel(this.knowIt));
		this.add(bindingWidget);

		this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2,
				GUIOp.scaleColour(bindingWidget.getBackground(), 0.8)));

		this.setBackground(GUIOp.scaleColour(bindingWidget.getBackground(),
				0.95));

		// Set a tool tip for users in case they don't know what to drag in.
		if (binding instanceof KnowItBindingNull) {
			final String typeString;

			typeString = StringOp.getCollectionAsString(this.knowIt.getTypes(),
					", ");

			this.setToolTipText("Drag a " + typeString + " in here!");
		}
	}

	private BindingWidget buildBindingWidget() {
		final String backgroundProperty = "background";

		final KnowItBinding binding = this.knowIt.getBinding();
		final BindingWidget bindingWidget = new BindingWidget(binding);

		// The slotPanel inherits the colour of its binding.
		bindingWidget.addPropertyChangeListener(backgroundProperty,
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals(backgroundProperty)) {
							SlotPanel.this.setBackground(bindingWidget
									.getBackground());
						}
					}
				});

		// Build the input component
		binding.process(new BindingAdapter() {
			final KnowIt knowIt = SlotPanel.this.knowIt;

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				if (SlotPanel.this.isNameEditable)
					bindingWidget.add(ScriptWidgetFactory
							.buildNameEditor(this.knowIt));
				else
					bindingWidget.add(ScriptWidgetFactory
							.buildObservedNameLabel(this.knowIt));
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(reference.getValue()));
			}

			@Override
			public void processUninitialized(
					KnowItBindingUninitialized uninitialized) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(uninitialized.getValue()));
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(function.getValue()));
			}

			@Override
			public void processStoryPoint(KnowItBindingStoryPoint storyPoint) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(storyPoint.getValue()));
			}

			@Override
			public void processResource(KnowItBindingResource constant) {
				final Resource resource = constant.getValue();
				final String name = resource.getName();

				final JComponent component;

				if (resource instanceof SimpleResource) {
					final String bindingType = binding.getFirstType();
					final LibraryModel library = this.knowIt.getLibrary();

					final GUIType widgetType;

					if (library != null) {
						final GameType type = library.getType(bindingType);

						if (type != null)
							// This could possibly return null.
							widgetType = type.getGui();
						else
							widgetType = null;
					} else
						widgetType = null;

					if (widgetType == GUIType.JSPINNER) {
						component = ScriptWidgetFactory.buildSpinnerEditor(
								this.knowIt, bindingWidget, resource,
								bindingType);
					} else if (widgetType == GUIType.JCOMBOBOX) {
						component = ScriptWidgetFactory.buildComboEditor(
								this.knowIt, bindingWidget, bindingType);
					} else if (widgetType == GUIType.JTEXTFIELD) {
						component = ScriptWidgetFactory.buildValueEditor(
								this.knowIt, bindingWidget);
					} else
						component = ScriptWidgetFactory.buildLabel(name);
				} else {
					component = ScriptWidgetFactory.buildLabel(name);
				}

				bindingWidget.add(component);
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(this.knowIt));
			}
		});

		return bindingWidget;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}

		if (enabled) {
			this.setTransferHandler(SlotPanelTransferHandler.getInstance());
			this.removeMouseListener(MouseForwardingAdapter.getInstance());
			this.removeMouseMotionListener(MouseForwardingAdapter.getInstance());
		} else {
			this.setTransferHandler(null);
			this.addMouseListener(MouseForwardingAdapter.getInstance());
			this.addMouseMotionListener(MouseForwardingAdapter.getInstance());
		}
	}

	@Override
	public String toString() {
		return "SlotPanel[" + this.knowIt + "]";
	}
}
