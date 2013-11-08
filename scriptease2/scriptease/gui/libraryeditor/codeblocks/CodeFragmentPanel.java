package scriptease.gui.libraryeditor.codeblocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import scriptease.controller.FragmentAdapter;
import scriptease.gui.libraryeditor.FormatFragmentSelectionManager;
import scriptease.gui.ui.ScriptEaseUI;
import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

public class CodeFragmentPanel extends JPanel {
	private Color color;
	private final CodeBlock codeBlock;
	private final AbstractFragment fragment;

	public CodeFragmentPanel(CodeBlock codeBlock, AbstractFragment fragment) {
		this.codeBlock = codeBlock;
		this.fragment = fragment;

		final Border lineBorder;
		final Border titledBorder;

		lineBorder = BorderFactory.createLineBorder(color);
//		titledBorder = BorderFactory.createTitledBorder(lineBorder, title,
//				TitledBorder.LEADING, TitledBorder.TOP, new Font("SansSerif",
//						Font.BOLD, 12), color);
//
//		this.setName(title);
//		this.setBorder(titledBorder);
//
//		this.setOpaque(true);
//		this.setBackground(ScriptEaseUI.FRAGMENT_DEFAULT_COLOR);
//
//		this.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseReleased(MouseEvent e) {
//				final AbstractFragment selectedFragment;
//
//				selectedFragment = CodeEditorPanel.this.panelToFragmentMap
//						.get(this);
//
//				FormatFragmentSelectionManager.getInstance().setFormatFragment(
//						selectedFragment, CodeEditorPanel.this.codeBlock);
//				updatePanelSelectionHighlight(selectedFragment);
//			}
//		});

		if (fragment != null)
			fragment.process(new FragmentAdapter() {

			});

	}
}
