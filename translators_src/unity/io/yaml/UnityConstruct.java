package io.yaml;

import org.yaml.snakeyaml.constructor.AbstractConstruct;

/**
 * Generic converter superclass to be helpful in holding some of the information
 * together so that we don't need to store three separate things.
 * 
 * @author remiller
 */
public abstract class UnityConstruct extends AbstractConstruct {
	private Class<? extends Object> resultClass;

	/**
	 * 
	 * @param clazz
	 *            The class expected to come out of the conversion process.
	 */
	public UnityConstruct(Class<? extends Object> clazz) {
		this.resultClass = clazz;
	}

	public Class<? extends Object> getResultClass() {
		return this.resultClass;
	}
}
