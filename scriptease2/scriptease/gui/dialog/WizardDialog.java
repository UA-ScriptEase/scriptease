package scriptease.gui.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

/**
 * WizardDialog represents a generic Wizard navigator. It uses the given parent
 * frame, the given title and collections of JPanels, and the runnable to
 * execute when completed.
 * 
 * @author mfchurch
 * 
 */
@SuppressWarnings("serial")
public class WizardDialog extends JDialog implements ActionListener {
	private Collection<JPanel> pages;
	private Runnable finish;
	private String title;
	private JButton backButton;
	private JButton nextButton;
	private JButton cancelButton;
	private JButton finishButton;
	private int currentPage;

	public WizardDialog(Frame parent, String title, Collection<JPanel> pages,
			Runnable finish) {
		super(parent, true);
		this.title = title;
		this.pages = pages;
		this.finish = finish;
		this.currentPage = 0;
	}

	/**
	 * Enables/Disables the finished button
	 * 
	 * @param value
	 */
	public void setFinishEnabled(boolean value) {
		this.finishButton.setEnabled(value);
	}

	/**
	 * Displays the Wizard window until finished or cancelled
	 */
	public void display() {
		final JPanel buttonPanel;
		final JPanel cardPanel;
		final CardLayout cardLayout;

		final Box buttonBox;

		buttonPanel = new JPanel();
		buttonBox = new Box(BoxLayout.X_AXIS);

		// Setup the cardPanel
		cardPanel = new JPanel();
		cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

		// Populate the cardPanel with the panes (indexed by their name)
		for (JPanel panel : this.pages) {
			cardPanel.add(panel, panel.getName());
		}

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		// Add buttons and listeners
		this.backButton = new JButton("Back");
		this.nextButton = new JButton("Next");
		this.finishButton = new JButton("Finish");
		this.cancelButton = new JButton("Cancel");

		this.backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the next page
				cardLayout.previous(cardPanel);
				WizardDialog.this.currentPage--;
				if (WizardDialog.this.currentPage == 0)
					WizardDialog.this.backButton.setEnabled(false);
				WizardDialog.this.nextButton.setEnabled(true);
			}
		});

		this.nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Show the next page
				cardLayout.next(cardPanel);
				WizardDialog.this.currentPage++;
				if (WizardDialog.this.currentPage == WizardDialog.this.pages
						.size() - 1)
					WizardDialog.this.nextButton.setEnabled(false);
				WizardDialog.this.backButton.setEnabled(true);
			}
		});

		this.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				WizardDialog.this.setVisible(false);
				WizardDialog.this.dispose();
			}
		});

		this.finishButton.addActionListener(this);

		// Finalize the layout
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

		buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
		buttonBox.add(this.backButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(this.nextButton);
		buttonBox.add(Box.createHorizontalStrut(30));
		buttonBox.add(this.finishButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(this.cancelButton);
		buttonPanel.add(buttonBox, BorderLayout.EAST);

		this.add(cardPanel, BorderLayout.NORTH);
		this.add(buttonPanel, BorderLayout.SOUTH);

		// Hide back and next if there is less than 2 pages
		if (this.pages.size() < 2) {
			this.backButton.setVisible(false);
			this.nextButton.setVisible(false);
		}
		this.finishButton.setEnabled(false);
		this.backButton.setEnabled(false);

		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		this.setTitle(this.title);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WizardDialog.this.setVisible(false);
		WizardDialog.this.dispose();
		WizardDialog.this.finish.run();
	}
}
