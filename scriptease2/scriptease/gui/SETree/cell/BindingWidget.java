package scriptease.gui.SETree.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import scriptease.controller.AbstractNoOpBindingVisitor;
import scriptease.controller.BindingVisitor;
import scriptease.controller.MouseForwardingAdapter;
import scriptease.gui.SETree.transfer.BindingWidgetTransferHandler;
import scriptease.gui.SETree.ui.BindingWidgetUI;
import scriptease.gui.SETree.ui.ScriptEaseUI;
import scriptease.model.atomic.knowitbindings.KnowItBinding;
import scriptease.model.atomic.knowitbindings.KnowItBindingConstant;
import scriptease.model.atomic.knowitbindings.KnowItBindingDescribeIt;
import scriptease.model.atomic.knowitbindings.KnowItBindingFunction;
import scriptease.model.atomic.knowitbindings.KnowItBindingNull;
import scriptease.model.atomic.knowitbindings.KnowItBindingQuestPoint;
import scriptease.model.atomic.knowitbindings.KnowItBindingReference;
import scriptease.model.atomic.knowitbindings.KnowItBindingRunTime;
import scriptease.translator.io.model.IdentifiableGameConstant;

/**
 * Represents a binding or binding slot that can be dropped onto slots via drag
 * and drop. BindingWidgets display their type(s). They do a sort of double-duty
 * to be the binding as well as the empty binding slot, depending on if the
 * binding it is representing is null or not. This is just the bubble part of a
 * binding. Labels are added separately.
 * 
 * @author remiller
 * @see BindingWidgetUI
 */
@SuppressWarnings("serial")
public class BindingWidget extends JPanel implements Cloneable {
	private final KnowItBinding binding;
	/*
	 * this transfer handler isn't redundantly stored: we remove the super
	 * version in setEnable(false) - remiller
	 */
	private TransferHandler transferHandler;

	public BindingWidget(final KnowItBinding binding) {
		this.binding = binding;
		// we don't want horizontal/vertical gaps, so make FlowLayout do this
		this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.updateToolTip(binding);
		this.updateBackgroundColour(binding);
		this.setOpaque(false);
		this.setUI(new BindingWidgetUI());

		// Default transferHandler
		TransferHandler transferHandler = BindingWidgetTransferHandler
				.getInstance();
		this.setTransferHandler(transferHandler);
	}

	/**
	 * Updates the tooltip to reflect the current binding
	 * 
	 * @param binding
	 */
	private void updateToolTip(KnowItBinding binding) {
		binding.process(new AbstractNoOpBindingVisitor() {
			@Override
			public void processConstant(KnowItBindingConstant constant) {
				if (constant.isIdentifiableGameConstant()) {
					String blueprint = ((IdentifiableGameConstant) constant
							.getValue()).getTemplateID();
					if (blueprint != null && !blueprint.isEmpty())
						BindingWidget.this.setToolTipText(blueprint);
				}
			}
		});
	}

	@Override
	public BindingWidget clone() {
		BindingWidget clone = new BindingWidget(this.binding);
		clone.setLocation(this.getLocation());
		clone.setTransferHandler(this.transferHandler);
		for (MouseListener listener : this.getMouseListeners())
			clone.addMouseListener(listener);
		for (MouseMotionListener listener : this.getMouseMotionListeners())
			clone.addMouseMotionListener(listener);
		return clone;
	}

	/**
	 * Gets the binding that this widget currently represents.
	 * 
	 * @return the KnowItBinding backing this widget.
	 */
	public KnowItBinding getBinding() {
		return this.binding;
	}

	@Override
	public void setTransferHandler(TransferHandler newHandler) {
		this.transferHandler = newHandler;
		super.setTransferHandler(newHandler);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			this.setTransferHandler(this.transferHandler);
			this.removeMouseListener(MouseForwardingAdapter.getInstance());
			this.removeMouseMotionListener(MouseForwardingAdapter.getInstance());
		} else {
			this.setTransferHandler(null);
			this.addMouseListener(MouseForwardingAdapter.getInstance());
			this.addMouseMotionListener(MouseForwardingAdapter.getInstance());
		}
		for (Component component : this.getComponents()) {
			if (!(component instanceof JLabel))
				component.setEnabled(enabled);
		}
	}

	/**
	 * Updates the background colour of the label if necessary and returns the
	 * chosen background colour.
	 * 
	 * @param binding
	 *            The binding from which the color of this widget will be
	 *            resolved.
	 * @return The colour of the label after the update has been performed,
	 *         regardless of whether the colour actually changed or not.
	 */
	private void updateBackgroundColour(KnowItBinding binding) {
		binding.process(new BindingVisitor() {
			@Override
			public void processConstant(KnowItBindingConstant constant) {
				if (constant.getValue() instanceof IdentifiableGameConstant)
					updateBackground(ScriptEaseUI.COLOUR_GAME_OBJECT);
				else
					updateBackground(ScriptEaseUI.COLOUR_SIMPLE);
			}

			@Override
			public void processReference(KnowItBindingReference reference) {
				// Colour should be determined based off the final binding
				// resolution
				KnowItBinding referenced = reference.resolveBinding();
				referenced.process(this);
			}

			@Override
			public void processFunction(KnowItBindingFunction function) {
				updateBackground(ScriptEaseUI.COLOUR_KNOWN_OBJECT);
			}

			@Override
			public void processDescribeIt(KnowItBindingDescribeIt described) {
				updateBackground(ScriptEaseUI.COLOUR_KNOWN_OBJECT);
			}

			@Override
			public void processRunTime(KnowItBindingRunTime runTime) {
				updateBackground(ScriptEaseUI.COLOUR_KNOWN_OBJECT);
			}

			@Override
			public void processNull(KnowItBindingNull nullBinding) {
				updateBackground(ScriptEaseUI.COLOUR_UNBOUND);
			}

			@Override
			public void processQuestPoint(KnowItBindingQuestPoint questPoint) {
				updateBackground(ScriptEaseUI.COLOUR_KNOWN_OBJECT);
			}

			private void updateBackground(Color color) {
				// only update the BG if the chosen colour is different.
				if (!color.equals(BindingWidget.this.getBackground()))
					BindingWidget.this.setBackground(color);
			}
		});
	}

	@Override
	public String toString() {
		return "Binding Widget [" + this.binding.toString() + "]";
	}
}
