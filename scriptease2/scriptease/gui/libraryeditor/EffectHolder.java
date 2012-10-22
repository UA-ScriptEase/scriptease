package scriptease.gui.libraryeditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scriptease.gui.storycomponentpanel.StoryComponentPanel;
import scriptease.gui.storycomponentpanel.StoryComponentPanelFactory;
import scriptease.gui.transfer.StoryComponentPanelTransferHandler;
import scriptease.model.StoryComponent;
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

	private StoryComponent component;

	public EffectHolder() {
		super();

		this.setBorder(BorderFactory.createLoweredBevelBorder());

		this.setTransferHandler(StoryComponentPanelTransferHandler
				.getInstance());
	}

	/**
	 * Sets the component inside the holder.
	 * 
	 * @param component
	 */
	public boolean setComponent(StoryComponent component) {
		if (component instanceof ScriptIt) {
			if (((ScriptIt) component).isCause())
				return false;

			this.component = component;
			this.removeAll();

			final StoryComponentPanel panel;

			panel = StoryComponentPanelFactory.getInstance()
					.buildStoryComponentPanel(component);

			panel.setVisible(true);

			panel.setBackground(Color.WHITE);
			panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

			final Image image = GUIOp.getScreenshot(panel);

			this.add(new JLabel(new ImageIcon(image)));

			this.repaint();
			this.revalidate();

			return true;
		}

		return false;
	}

	/**
	 * Returns the component inside the holder.
	 * 
	 * @return
	 */
	public StoryComponent getPanel() {
		return this.component;
	}

}
