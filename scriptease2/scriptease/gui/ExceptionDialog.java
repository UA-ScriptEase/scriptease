package scriptease.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import scriptease.ScriptEase;
import scriptease.controller.logger.NetworkHandler;

/**
 * Dialog for exceptions that occurred running ScriptEase. We will allow the
 * user to send a report to the ScriptEase team with a log detailing the
 * exception if they wish.
 * 
 * @author remiller
 * @author jyuen
 */
@SuppressWarnings("serial")
public class ExceptionDialog extends JDialog {
	private final JButton reportButton = new JButton("Report");
	private final JButton ignoreButton = new JButton("Ignore");
	private final JButton detailsButton = new JButton();
	private final JButton exitButton = new JButton("Exit ScriptEase");
	private JComponent separatorOrTrace;
	private JComponent commentBox;
	private JTextArea commentText;
	private JLabel commentLabel;

	private static final String COMMENT_MESSAGE = "Briefly explain what you were doing.";
	private static final String SHOW_DETAILS = "<html><font color=#0000ee><u>Details...</u></font>";
	private static final String HIDE_DETAILS = "<html><font color=#0000ee><u>Hide</u></font>";

	private final String message;

	/**
	 * Builds a JDialog that gives the user the ability to either:
	 * 
	 * <ol>
	 * <li>Send a report to us
	 * <li>Send no report, effectively dismissing the window
	 * <li>See more details, which will then display the stack trace of the
	 * exception
	 * <li>Exit ScriptEase, which should call the proper exit routine in
	 * ScriptEase.java
	 * </ol>
	 */
	public ExceptionDialog(Frame parent, String title, String messageBrief,
			String message) {
		super(parent, title, true);

		this.message = "<html><b>" + messageBrief + "</b><br><br>" + message
				+ "</html>";

		this.buildGUI();
		this.setupButtonListeners();
	}

	/**
	 * builds the distinct ExceptionDialog GUI.
	 */
	private void buildGUI() {
		final JPanel content = new JPanel();
		final JPanel buttons = new JPanel();

		// overall layout manager
		final GroupLayout contentLayout = new GroupLayout(content);
		content.setLayout(contentLayout);
		contentLayout.setAutoCreateContainerGaps(true);
		contentLayout.setAutoCreateGaps(true);

		// buttons layout manager
		final BoxLayout buttonsLayout = new BoxLayout(buttons,
				BoxLayout.LINE_AXIS);
		buttons.setLayout(buttonsLayout);

		final JLabel messageLabel = new JLabel(this.message);
		this.commentLabel = new JLabel();

		buttons.add(this.reportButton);
		buttons.add(Box.createHorizontalStrut(5));
		buttons.add(this.ignoreButton);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(this.exitButton);

		// For some reason using HTML makes JLabel text render as bold.
		// Doing this de-bolds only the text that should be normal.
		// Very weird. - remiller
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN));

		Icon warningIcon = UIManager.getIcon("OptionPane.errorIcon");
		// wrap in a JLabel to make it a Component
		JLabel iconLabel = new JLabel(warningIcon);

		// the details button is very special. I didn't want it to appear as
		// a normal button since it's a lot less important for users -
		// remiller
		this.detailsButton.setMargin(new Insets(0, 0, 0, 0));
		this.detailsButton.setBorderPainted(false);
		this.detailsButton.setContentAreaFilled(false);
		this.detailsButton.setCursor(Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.detailsButton.setFocusable(false);
		this.detailsButton.setText(ExceptionDialog.SHOW_DETAILS);
		// This is set to zero since it seems to ignore the max size
		// anyways, but seems to need one to not stretch over the whole pane
		this.detailsButton.setMaximumSize(new Dimension(0, this.detailsButton
				.getHeight()));

		// starts as a separator
		this.separatorOrTrace = new JSeparator(SwingConstants.HORIZONTAL);
		// starts commentBox as hidden
		this.commentBox = new JScrollPane();
		this.commentBox.setVisible(false);

		// horizontal perspective
		contentLayout.setHorizontalGroup(contentLayout
				.createSequentialGroup()
				.addComponent(iconLabel)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(
						contentLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(messageLabel)
								.addComponent(this.detailsButton)
								.addComponent(this.separatorOrTrace)
								.addComponent(buttons)
								.addComponent(this.commentLabel)
								.addComponent(this.commentBox)));

		// vertical perspective
		contentLayout.setVerticalGroup(contentLayout
				.createSequentialGroup()
				.addGroup(
						contentLayout.createParallelGroup()
								.addComponent(iconLabel)
								.addComponent(messageLabel))
				.addComponent(this.detailsButton)
				.addComponent(this.separatorOrTrace)
				// space out details from other buttons
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(buttons, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(this.commentLabel).addComponent(this.commentBox));

		this.setContentPane(content);
		this.setResizable(false);
		this.pack();
	}

	private void setupButtonListeners() {
		// details shows the JVM stack trace
		this.detailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExceptionDialog.this.showOrHideDetails();
			}
		});

		// report goes though the report procedure, asking for information
		this.reportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ExceptionDialog.this.reportButton.getText()
						.equals("Report"))
					ExceptionDialog.this.showCommentBox();
				else {

					NetworkHandler.getInstance().sendReport(
							ExceptionDialog.this.commentText.getText());
					ExceptionDialog.this.reportButton.setText("Report");
					ExceptionDialog.this.setVisible(false);
					ExceptionDialog.this.dispose();
				}
			}
		});

		// ignore just destroys the popup
		this.ignoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExceptionDialog.this.setVisible(false);
				ExceptionDialog.this.dispose();
			}
		});

		// exit button calls the nice version of exit
		this.exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptEase.getInstance().exit();
			}
		});
	}

	/**
	 * shows or hides the log report depending on current state.
	 * 
	 * This method is really quite gross. I'd like to rebuild it, but I can't
	 * think of a better way that doesn't involve unnecessary objects --remiller
	 */
	private void showOrHideDetails() {
		String trace = NetworkHandler.getInstance().generateReport("");
		LayoutManager layout = this.getContentPane().getLayout();

		JComponent oldComp = ExceptionDialog.this.separatorOrTrace;
		this.setResizable(!this.isResizable());

		// are we expanding or hiding the trace?
		boolean expanding = (oldComp instanceof JSeparator);

		if (expanding) {
			JTextArea text = new JTextArea(trace);
			text.setEditable(false);
			this.separatorOrTrace = new JScrollPane(text);
		} else
			this.separatorOrTrace = new JSeparator();

		if (layout instanceof GroupLayout) {
			if (expanding)
				this.separatorOrTrace.setPreferredSize(new Dimension(600, 200));

			((GroupLayout) layout).replace(oldComp,
					ExceptionDialog.this.separatorOrTrace);

			if (expanding)
				this.detailsButton.setText(ExceptionDialog.HIDE_DETAILS);
			else
				this.detailsButton.setText(ExceptionDialog.SHOW_DETAILS);

			this.pack();
		} else {
			if (expanding)
				JOptionPane.showMessageDialog(this, trace,
						"JVM Stack Trace (Bad layout manager)",
						JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Expands a comment box and the option to send a bug report
	 */
	private void showCommentBox() {
		JComponent cmtBox = ExceptionDialog.this.commentBox;
		// does the commentBox already exist?

		this.setResizable(true);

		this.commentLabel.setText(ExceptionDialog.COMMENT_MESSAGE);
		this.commentText = new JTextArea();
		this.commentText.setEditable(true);
		this.commentBox = new JScrollPane(this.commentText);
		this.commentBox.setPreferredSize(new Dimension(600, 200));

		LayoutManager layout = this.getContentPane().getLayout();
		((GroupLayout) layout).replace(cmtBox, ExceptionDialog.this.commentBox);

		this.reportButton.setText("Send");
		this.pack();
	}
}
