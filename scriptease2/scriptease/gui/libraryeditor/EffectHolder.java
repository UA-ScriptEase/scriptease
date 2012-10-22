package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Image;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
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
public class EffectHolder extends JPanel {

	private ScriptIt effect;
	private final Collection<String> allowableTypes;

	public EffectHolder(Collection<String> allowableTypes) {
		super();

		this.allowableTypes = allowableTypes;

		this.setBorder(BorderFactory.createLoweredBevelBorder());

		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Sets the component inside the holder.
	 * 
	 * @param component
	 */
	public boolean setEffect(ScriptIt effect) {
		if (((ScriptIt) effect).isCause())
			return false;

		this.effect = effect;
		this.removeAll();

		final StoryComponentPanel panel;

		panel = StoryComponentPanelFactory.getInstance()
				.buildStoryComponentPanel(effect);

		panel.setVisible(true);

		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		final Image image = GUIOp.getScreenshot(panel);

		this.add(new JLabel(new ImageIcon(image)));

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

}
