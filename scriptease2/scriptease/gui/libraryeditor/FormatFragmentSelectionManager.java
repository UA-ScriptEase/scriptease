package scriptease.gui.libraryeditor;

import scriptease.model.CodeBlock;
import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * A selection manager for the format fragments in the Library Editor, in the
 * Code Block Editor, in the Code Editor. This class is a singleton.
 * 
 * @author kschenk
 * 
 */
public class FormatFragmentSelectionManager {
	private AbstractFragment fragment;
	private CodeBlock codeBlock;

	private static FormatFragmentSelectionManager instance = new FormatFragmentSelectionManager();

	public static FormatFragmentSelectionManager getInstance() {
		return instance;
	}

	/**
	 * Sets the current selected format fragment.
	 * 
	 * @param fragment
	 * @param codeBlock
	 */
	public void setFormatFragment(AbstractFragment fragment, CodeBlock codeBlock) {
		this.fragment = fragment;
		this.codeBlock = codeBlock;
	}

	/**
	 * Returns the current selected format fragment.
	 * 
	 * @return
	 */
	public AbstractFragment getFormatFragment() {
		return this.fragment;
	}

	/**
	 * Returns the code block for the selected format fragment.
	 * 
	 * @return
	 */
	public CodeBlock getCodeBlock() {
		return this.codeBlock;
	}
}
