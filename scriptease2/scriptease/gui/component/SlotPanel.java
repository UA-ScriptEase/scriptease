package scriptease.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

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
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

		this.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		this.populate();

		this.setEnabled(true);
		this.knowIt.addStoryComponentObserver(this);
	}

	public void populate() {
		// Set the layout for this panel.
		final JPanel typesPanel;

		// Set the layout for the types panel.
		typesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

		typesPanel.setOpaque(false);

		this.add(ScriptWidgetFactory.populateLegalTypesPanel(typesPanel,
				this.knowIt));

		this.bindingWidget = this.buildBindingWidget(this.knowIt);

		this.add(this.bindingWidget);

		this.setBackground(GUIOp.scaleColour(
				this.bindingWidget.getBackground(), 0.95));
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
			public void processConstant(KnowItBindingResource constant) {
				final Resource constantValue = constant.getValue();
				final String name = constantValue.getName();

				if (constantValue instanceof SimpleResource) {
					final String bindingType;
					final GUIType widgetName;

					bindingType = binding.getFirstType();
					widgetName = knowIt.getLibrary().getTypeGUI(bindingType);

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
			//this.setTransferHandler(new ProxyTransferHandler(this.bindingWidget));
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
				public void processConstant(KnowItBindingResource constant) {
					/*
					 * We need this listener because the slot panel does not
					 * otherwise get updated if we do a group binding. This
					 * ensures that the slot panel is updated.
					 */
					if (!(constant.getValue() instanceof SimpleResource)) {
						this.defaultProcess(constant);
					}
				}

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
}
