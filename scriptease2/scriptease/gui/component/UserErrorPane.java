package scriptease.gui.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class UserErrorPane extends JPanel {

	private static final int DELAY = 50;
	private static final float OPACITY_INCREMENTATION = 0.025f;

	final String message;
	final Point startPoint;

	Timer timer;

	float opacity;

	public UserErrorPane(String message) {
		super();

		this.message = message;
		this.startPoint = MouseInfo.getPointerInfo().getLocation();
		this.opacity = 0.1f;

		this.timer = new Timer(UserErrorPane.DELAY, new FadeInTimer());

		this.timer.setRepeats(true);
		this.timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		final FontMetrics metrics = g.getFontMetrics(g.getFont());

		final int HEIGHT = metrics.getHeight() + 10;
		final int WIDTH = metrics.stringWidth(message) + 30;

		if (startPoint != null) {
			g.setColor(new Color(1, 0, 0, opacity));
			
			g.fillRoundRect(startPoint.x, startPoint.y, WIDTH, HEIGHT, 0, 0);

			g.setColor(new Color(1, 1, 1, opacity));

			g.drawString(message, startPoint.x + 15, startPoint.y + 16);
		}
	}

	private class FadeInTimer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (opacity + UserErrorPane.OPACITY_INCREMENTATION <= 0.8f) {
				opacity += UserErrorPane.OPACITY_INCREMENTATION;
				UserErrorPane.this.repaint();
			} else {
				UserErrorPane.this.timer.stop();
				UserErrorPane.this.timer = new Timer(5000, new ReadTimer());
				UserErrorPane.this.timer.setRepeats(false);
				UserErrorPane.this.timer.start();
			}
		}
	}

	private class ReadTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			UserErrorPane.this.timer.stop();
			UserErrorPane.this.timer = new Timer(UserErrorPane.DELAY,
					new FadeOutTimer());
			UserErrorPane.this.timer.setRepeats(true);
			UserErrorPane.this.timer.start();
		}
	}

	private class FadeOutTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (opacity - UserErrorPane.OPACITY_INCREMENTATION >= 0.0f) {
				opacity -= UserErrorPane.OPACITY_INCREMENTATION;
				UserErrorPane.this.repaint();
			} else {
				opacity = 0;
				UserErrorPane.this.repaint();
				UserErrorPane.this.timer.stop();
			}
		}
	}
}
