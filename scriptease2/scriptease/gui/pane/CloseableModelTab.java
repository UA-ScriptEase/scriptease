/*
 * This code originally came from an Oracle tutorial, although there have been 
 * modifications. The following copyright notice is still required.
 * 
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of Oracle or the names of its contributors may be used to
 * endorse or promote products derived from this software without specific 
 * prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package scriptease.gui.pane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import scriptease.gui.PanelFactory;
import scriptease.gui.action.file.CloseModelTabAction;
import scriptease.model.PatternModel;

/**
 * Panel intended to be used as a closeable tab for a JTabbedPane.
 * 
 * @author remiller
 * @author mfchurch
 */
@SuppressWarnings("serial")
public class CloseableModelTab extends JPanel {
	/**
	 * Builds a new Closeable Tab that will draw title information from the
	 * given parent and will display the given icon.
	 * 
	 * @param parent
	 *            the JTabbedPane to be added to.
	 * @param icon
	 *            The icon to display in the tab. Passing <code>null</code> will
	 *            show no icon.
	 */
	public CloseableModelTab(final JTabbedPane parent,
			final JComponent component, final PatternModel model, Icon icon) {
		// unset the annoying gaps that come with default FlowLayout
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		final JLabel iconLabel;
		final TabButton closeButton;

		if (parent == null)
			throw new NullPointerException("TabbedPane is null");

		// this.tabbedPane = parent;
		this.setOpaque(false);

		// make JLabel read titles from JTabbedPane
		JLabel label = new JLabel() {
			public String getText() {
				int i = parent.indexOfTabComponent(CloseableModelTab.this);

				if (i == -1)
					return null;

				return parent.getTitleAt(i);
			}
		};

		if (icon != null) {
			iconLabel = new JLabel(" ");
			iconLabel.setIcon(icon);
			this.add(iconLabel);
		}

		this.add(label);

		// add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

		closeButton = new TabButton(new CloseModelTabAction(component, model));
		closeButton.setHideActionText(true);
		this.add(closeButton);

		// add more space to the top of the component
		this.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	/**
	 * Simple tab-closing button.
	 * 
	 * Constructed because JTabbedPanes don't come with closing buttons at all
	 * by default. Figures.
	 */
	private class TabButton extends JButton {

		private TabButton(Action action) {
			super(action);

			int size = 17;
			this.setPreferredSize(new Dimension(size, size));
			this.setToolTipText("close this tab");
			// Make the button looks the same for all Laf's
			this.setUI(new BasicButtonUI());
			// Make it transparent
			this.setContentAreaFilled(false);
			// No need to be focusable
			this.setFocusable(false);
			this.setBorder(BorderFactory.createEtchedBorder());
			this.setBorderPainted(false);
			// Making nice rollover effect
			// we use the same listener for all buttons
			this.addMouseListener(tabButtonMouseListener);
			this.setRolloverEnabled(true);
		}

		// we don't want to update UI for this button
		@Override
		public void updateUI() {
		}

		// paint the cross
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.LIGHT_GRAY);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
					- delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
					- delta - 1);
			g2.dispose();
		}
	}

	/**
	 * Mouse listener that just enables and disables the border on its target
	 * button on mouse enter/exit.
	 */
	private static final MouseListener tabButtonMouseListener = new MouseAdapter() {

		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}
