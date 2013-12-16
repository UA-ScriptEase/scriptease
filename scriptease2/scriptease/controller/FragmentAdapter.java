package scriptease.controller;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;
import scriptease.translator.codegenerator.code.fragments.FormatReferenceFragment;
import scriptease.translator.codegenerator.code.fragments.LiteralFragment;
import scriptease.translator.codegenerator.code.fragments.SimpleDataFragment;
import scriptease.translator.codegenerator.code.fragments.container.FormatDefinitionFragment;
import scriptease.translator.codegenerator.code.fragments.container.IndentFragment;
import scriptease.translator.codegenerator.code.fragments.container.LineFragment;
import scriptease.translator.codegenerator.code.fragments.container.ScopeFragment;
import scriptease.translator.codegenerator.code.fragments.container.SeriesFragment;

/**
 * Default implementation of AbstractFragmentVisitor that does nothing. Ever. <br>
 * <br>
 * It is <b>stylistically required</b> that all other AbstractFragmentVisitor
 * implementations extend this class, allowing us to avoid having to update all
 * of the visitors whenever the interface changes. Subclasses also get the perk
 * of only having to override the methods they <i>do</i> support.<br>
 * <br>
 * FragmentAdapter is an Adapter (of the Adapter design pattern) to
 * FragmentVisitor.
 * 
 * @author mfchurch
 */
public abstract class FragmentAdapter implements FragmentVisitor {

	@Override
	public void processFormatDefinitionFragment(
			FormatDefinitionFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processLiteralFragment(LiteralFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processFormatReferenceFragment(FormatReferenceFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processIndentFragment(IndentFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processLineFragment(LineFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processScopeFragment(ScopeFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processSeriesFragment(SeriesFragment fragment) {
		this.defaultProcess(fragment);
	}

	@Override
	public void processSimpleDataFragment(SimpleDataFragment fragment) {
		this.defaultProcess(fragment);
	}

	/**
	 * The default process method that is called by every
	 * process<i>Z</i>(<i>Z</i> <i>z</i>) method in this class' standard
	 * methods. <br>
	 * <br>
	 * Override this method if you want to provide a non-null default behaviour
	 * for every non-overridden process<i>Z</i> method. Unless it is overridden,
	 * it does nothing.
	 * 
	 * @param component
	 *            The AbstractFragment to process with a default behaviour.
	 */
	protected void defaultProcess(AbstractFragment component) {
	}
}
