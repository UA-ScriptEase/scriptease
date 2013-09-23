package scriptease.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A UserErrorPane is a JPanel, best suited for use as a glass pane, that fades
 * in and out displaying the provided error message.
 * 
 * @author jyuen
 */
@SuppressWarnings("serial")
public class UserInformationPane extends JPanel {

	private static final int DELAY = 50;
	private static final int READ_TIME = 4000;

	private static final float OPACITY_INCREMENTATION = 0.025f;
	private static final float START_OPACITY = 0.1f;
	private static final float FINAL_OPACITY = 0.8f;

	/**
	 * The types of possible information displayed to the user.
	 * 
	 * @author jyuen
	 */
	public enum UserInformationType {
		INFO, WARNING, ERROR;
	};

	String message;
	Point startPoint;
	UserInformationType type;

	Timer timer;

	float opacity;

	public UserInformationPane(String message, UserInformationType type) {
		super();

		this.message = message;
		this.startPoint = MouseInfo.getPointerInfo().getLocation();
		this.opacity = UserInformationPane.START_OPACITY;
		this.type = type;

		this.timer = new Timer(UserInformationPane.DELAY, new FadeInTimer());

		this.timer.setRepeats(true);
		this.timer.start();
	}

	public void restart(String message, UserInformationType type) {
		this.timer.stop();

		this.message = message;
		this.startPoint = MouseInfo.getPointerInfo().getLocation();
		this.opacity = UserInformationPane.START_OPACITY;
		this.type = type;

		this.timer = new Timer(UserInformationPane.DELAY, new FadeInTimer());

		this.timer.setRepeats(true);
		this.timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		final FontMetrics metrics = g.getFontMetrics(g.getFont());

		final int HEIGHT = metrics.getHeight() + 10;
		final int WIDTH = metrics.stringWidth(message) + 30;

		if (startPoint != null) {
			switch (this.type) {
			case INFO:
				g.setColor(new Color(0, 0, 0, opacity));
				break;
			case WARNING:
				g.setColor(new Color(0.88f, 0.47f, 0.22f, opacity));
				break;
			case ERROR:
				g.setColor(new Color(1, 0, 0, opacity));
				break;
			}

			g.fillRoundRect(startPoint.x, startPoint.y, WIDTH, HEIGHT, 0, 0);

			g.setColor(new Color(1, 1, 1, opacity));

			g.drawString(message, startPoint.x + 15, startPoint.y + 16);
		}
	}

	/**
	 * Must override the contains method if we plan to use the UserErrorPane as
	 * a glass panel. Otherwise, mouse cursors underneath the glass pane will
	 * not be shown.
	 */
	@Override
	public boolean contains(int x, int y) {
		final Component[] components = this.getComponents();

		for (Component component : components) {
			final Point containerPoint;

			containerPoint = SwingUtilities.convertPoint(this, x, y, component);

			if (component.contains(containerPoint)) {
				return true;
			}
		}
		return false;
	}

	private class FadeInTimer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (opacity + UserInformationPane.OPACITY_INCREMENTATION <= UserInformationPane.FINAL_OPACITY) {
				opacity += UserInformationPane.OPACITY_INCREMENTATION;
				UserInformationPane.this.repaint();
			} else {
				UserInformationPane.this.timer.stop();
				UserInformationPane.this.timer = new Timer(
						UserInformationPane.READ_TIME, new ReadTimer());
				UserInformationPane.this.timer.setRepeats(false);
				UserInformationPane.this.timer.start();
			}
		}
	}

	private class ReadTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			UserInformationPane.this.timer.stop();
			UserInformationPane.this.timer = new Timer(
					UserInformationPane.DELAY, new FadeOutTimer());
			UserInformationPane.this.timer.setRepeats(true);
			UserInformationPane.this.timer.start();
		}
	}

	private class FadeOutTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (opacity - UserInformationPane.OPACITY_INCREMENTATION >= 0.0f) {
				opacity -= UserInformationPane.OPACITY_INCREMENTATION;
				UserInformationPane.this.repaint();
			} else {
				opacity = 0;
				UserInformationPane.this.repaint();
				UserInformationPane.this.timer.stop();
			}
		}
	}
}
