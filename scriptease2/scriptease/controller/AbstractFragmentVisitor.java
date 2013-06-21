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
 * Generic controller object that is a collection of double dispatch methods to
 * correspond with a call to AbstractFragment.process(). Pass an implementation
 * of <code>AbstractFragmentVisitor</code> to a {@link AbstractFragment}'s
 * {@link AbstractFragment#process()} method to get type-specific behaviour.<br>
 * <br>
 * Classes should not implement this interface directly since it is strongly
 * recommended (and stylistically required) that they subclass
 * {@link AbstractFragmentAdapter}. <br>
 * <br>
 * <code>AbstractFragmentVisitor</code> is an implementation of the Visitor
 * design pattern.
 * 
 * @author mfchurch
 * 
 * @see AbstractFragmentAdaptor
 */
public interface AbstractFragmentVisitor {

	public void processLineFragment(LineFragment fragment);

	public void processFormatDefinitionFragment(
			FormatDefinitionFragment fragment);

	public void processIndentFragment(IndentFragment fragment);

	public void processScopeFragment(ScopeFragment fragment);

	public void processSeriesFragment(SeriesFragment fragment);

	public void processSimpleDataFragment(SimpleDataFragment fragment);

	public void processLiteralFragment(LiteralFragment fragment);

	public void processFormatReferenceFragment(FormatReferenceFragment fragment);
}
