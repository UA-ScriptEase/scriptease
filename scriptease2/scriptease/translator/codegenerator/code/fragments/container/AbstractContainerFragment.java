package scriptease.translator.codegenerator.code.fragments.container;

import java.util.Collection;
import java.util.List;

import scriptease.translator.codegenerator.code.fragments.AbstractFragment;

/**
 * An abstract class for fragments that can contain sub fragments.
 * 
 * @author kschenk
 *
 */
public abstract class AbstractContainerFragment extends AbstractFragment{

	public AbstractContainerFragment(String text) {
		super(text);
	}

	/**
	 * Sets the subfragments in the container fragment.
	 */
	public abstract void setSubFragments(List<AbstractFragment> subFragments);

	/**
	 * Returns the subfragments in the container fragment.
	 */
	public abstract Collection<AbstractFragment> getSubFragments();

}
