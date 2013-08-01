package scriptease.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import scriptease.controller.BindingAdapter;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.controller.observer.storycomponent.StoryComponentEvent;
import scriptease.controller.observer.storycomponent.StoryComponentEvent.StoryComponentChangeEnum;
import scriptease.controller.observer.storycomponent.StoryComponentObserver;
import scriptease.gui.transfer.SlotPanelTransferHandler;
import scriptease.model.atomic.KnowIt;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingResource;
import scriptease.model.atomic.knowitbindings.KnowItBindingStoryPoint;
import scriptease.model.semodel.librarymodel.LibraryModel;
import scriptease.translator.io.model.GameType.GUIType;
import scriptease.translator.io.model.Resource;
import scriptease.translator.io.model.SimpleResource;
import scriptease.util.GUIOp;

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
public class SlotPanel extends JPanel implements StoryComponentObserver {
	private BindingWidget bindingWidget;
	private final KnowIt knowIt;

	private final boolean isNameEditable;

	public SlotPanel(final KnowIt knowIt, boolean isNameEditable) {
		if (knowIt == null)
			throw new IllegalStateException(
					"Cannot build a SlotPanel with a null KnowIt");

		this.knowIt = knowIt;
		this.isNameEditable = isNameEditable;

		// Set a border of 2 pixels around the slot.
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		this.populate();

		this.setEnabled(true);
		this.knowIt.addStoryComponentObserver(this);
	}

	public void populate() {
		// Set the layout for this panel.
		final JPanel typesPanel;
		final KnowItBinding binding = this.knowIt.getBinding();

		this.bindingWidget = this.buildBindingWidget(this.knowIt);

		// Set the layout for the types panel.
		typesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

		typesPanel.setOpaque(false);

		this.add(ScriptWidgetFactory.populateLegalTypesPanel(typesPanel,
				this.knowIt));

		this.add(this.bindingWidget);

		this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2,
				GUIOp.scaleColour(this.bindingWidget.getBackground(), 0.8)));

		this.setBackground(GUIOp.scaleColour(
				this.bindingWidget.getBackground(), 0.95));

		// Set a tool tip for users in case they don't know what to drag in.
		if (binding instanceof KnowItBindingNull) {
			final List<String> types;

			types = new ArrayList<String>();

			types.addAll(knowIt.getTypes());

			String tooltipTypes = "";
			if (types.size() > 1) {
				for (String type : types)
					tooltipTypes += type += ", or ";

				// Remove last ", or ".
				tooltipTypes = tooltipTypes.substring(0,
						tooltipTypes.length() - 5);
			} else if (types.size() == 1)
				tooltipTypes = types.get(0);

			this.setToolTipText("Drag a binding of type " + tooltipTypes
					+ " in here!");
		}
	}

	private BindingWidget buildBindingWidget(final KnowIt knowIt) {
		final KnowItBinding binding;
		final BindingWidget bindingWidget;

		binding = knowIt.getBinding();
		bindingWidget = new BindingWidget(binding);

		// The slotPanel inherits the colour of its binding.
		bindingWidget.addPropertyChangeListener("background",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("background")) {
							SlotPanel.this.setBackground(bindingWidget
									.getBackground());
						}
					}
				});

		// Build the input component
		binding.process(new BindingAdapter() {
			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				if (isNameEditable)
					bindingWidget.add(ScriptWidgetFactory
							.buildNameEditor(knowIt));
				else
					bindingWidget.add(ScriptWidgetFactory
							.buildObservedNameLabel(knowIt));
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(reference.getValue()));
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
				final Resource constantValue = constant.getValue();
				final String name = constantValue.getName();

				if (constantValue instanceof SimpleResource) {
					final String bindingType;
					final GUIType widgetName;
					final LibraryModel library;

					bindingType = binding.getFirstType();
					library = knowIt.getLibrary();

					if (library == null)
						widgetName = null;
					else
						widgetName = library.getTypeGUI(bindingType);

					if (widgetName == null)
						bindingWidget.add(ScriptWidgetFactory.buildLabel(name,
								Color.WHITE));
					else if (widgetName.equals(GUIType.JSPINNER)) {
						bindingWidget.add(ScriptWidgetFactory
								.buildSpinnerEditor(knowIt, bindingWidget,
										constantValue, bindingType));
					} else if (widgetName.equals(GUIType.JCOMBOBOX)) {
						bindingWidget.add(ScriptWidgetFactory.buildComboEditor(
								knowIt, bindingWidget, bindingType));
					} else {
						bindingWidget.add(ScriptWidgetFactory.buildValueEditor(
								knowIt, bindingWidget));
					}
				} else {
					bindingWidget.add(ScriptWidgetFactory.buildLabel(name,
							Color.WHITE));
				}
			}

			@Override
			protected void defaultProcess(KnowItBinding binding) {
				bindingWidget.add(ScriptWidgetFactory
						.buildObservedNameLabel(knowIt));
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
	public void componentChanged(StoryComponentEvent event) {
		if (event.getType() == StoryComponentChangeEnum.CHANGE_KNOW_IT_BOUND) {
			this.bindingWidget.getBinding().process(new BindingAdapter() {
				@Override
				protected void defaultProcess(KnowItBinding binding) {
					SlotPanel.this.removeAll();
					SlotPanel.this.populate();
				}
			});

			this.repaint();
			this.revalidate();
		}
	}

	public BindingWidget getBindingWidget() {
		return this.bindingWidget;
	}

	@Override
	public String toString() {
		return "SlotPanel[" + this.knowIt + "]";
	}
}
