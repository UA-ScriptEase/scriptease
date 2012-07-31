package scriptease.translator.codegenerator.code.fragments.container;

import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.FormatFragment;

/**
 * An abstract class for fragments that can contain sub fragments.
 * 
 * @author kschenk
 *
 */
public abstract class AbstractContainerFragment extends FormatFragment{

	public AbstractContainerFragment(String text) {
		super(text);
	}

	/**
	 * Sets the subfragments in the container fragment.
	 */
	public abstract void setSubFragments(List<FormatFragment> subFragments);

	/**
	 * Returns the subfragments in the container fragment.
	 */
	public abstract Collection<FormatFragment> getSubFragments();

}
