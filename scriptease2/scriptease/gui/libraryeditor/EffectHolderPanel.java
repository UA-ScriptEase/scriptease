package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.controller.observer.SetEffectObserver;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.complex.ScriptIt;
import scriptease.util.GUIOp;

/**
 * Creates a JPanel that allows Effect panels to be dragged into.
 * 
 * @author kschenk
 * 
 */
@SuppressWarnings("serial")
public class EffectHolderPanel extends JPanel {

	private final Collection<SetEffectObserver> setEffectObservers;

	private ScriptIt effect;
	// TODO We need to reset allowable types when type is changed in story
	// component.
	private final Collection<String> allowableTypes;

	/**
	 * Creates a new EffectHolderPanel with the allowable types.
	 * 
	 * @param allowableTypes
	 */
	public EffectHolderPanel(Collection<String> allowableTypes) {
		super();

		this.setEffectObservers = new ArrayList<SetEffectObserver>();
		this.allowableTypes = allowableTypes;

		this.setBorder(BorderFactory.createLoweredBevelBorder());
		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());

		this.setEffect(null);
	}

	/**
	 * Sets the component inside the holder.
	 * 
	 * @param component
	 */
	public boolean setEffect(ScriptIt effect) {
		if (effect != null && ((ScriptIt) effect).isCause())
			return false;

		this.effect = effect;
		this.removeAll();

		final JPanel panel;

		if (effect != null)
			panel = StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(effect);
		else {
			panel = new JPanel();
			panel.add(new JLabel("No Effect"));
		}

		panel.setVisible(true);

		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		final Image image = GUIOp.getScreenshot(panel);

		this.add(new JLabel(new ImageIcon(image)));

		// Notify observers
		for (SetEffectObserver observer : this.setEffectObservers) {
			observer.effectChanged(effect);
		}

		this.repaint();
		this.revalidate();

		return true;
	}

	/**
	 * Returns the component inside the holder.
	 * 
	 * @return
	 */
	public ScriptIt getEffect() {
		return this.effect;
	}

	/**
	 * Returns the allowable types of the effect holder.
	 * 
	 * @return
	 */
	public Collection<String> getAllowableTypes() {
		return this.allowableTypes;
	}

	/**
	 * Sets the allowable types of the Effect Holder Panel. Only allowable types
	 * are allowed by the transfer handler to be dragged into the panel.
	 * 
	 * @param allowableTypes
	 * @return
	 */
	public boolean setAllowableTypes(Collection<String> allowableTypes) {
		this.allowableTypes.removeAll(this.allowableTypes);
		return this.allowableTypes.addAll(allowableTypes);
	}

	/**
	 * Adds a SetEffectObserver that listens for changes to the effect.
	 * 
	 * @param observer
	 */
	public void addSetEffectObserver(SetEffectObserver observer) {
		this.setEffectObservers.add(observer);
	}

	/**
	 * Returns the Collection of SetEffectObservers
	 * 
	 * @param observer
	 * @return
	 */
	public Collection<SetEffectObserver> getSetEffectObservers(
			SetEffectObserver observer) {
		return this.setEffectObservers;
	}

	/**
	 * Removes a SetEffectObserver.
	 * 
	 * @param observer
	 */
	public void removeSetEffectObserver(SetEffectObserver observer) {
		this.setEffectObservers.remove(observer);
	}
}
