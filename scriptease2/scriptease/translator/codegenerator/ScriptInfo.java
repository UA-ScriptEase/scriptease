package scriptease.translator.codegenerator;

import scriptease.translator.io.model.IdentifiableGameConstant;

/**
 * Simple metadata class for scripts and their slots.
 * 
 * @author remiller
 * @author mfchurch
 * 
 */
public final class ScriptInfo {
	private final String code;
	private final LocationInformation locationInfo;

	/**
	 * Builds a ScriptInfo that contains the given script metadata.
	 * 
	 * @param code
	 *            The code fragment that represents the content of the script.
	 * @param slot
	 *            The slot
	 * @param object
	 *            The object
	 */
	public ScriptInfo(String code, LocationInformation locationInfo) {
		this.code = code;
		this.locationInfo = locationInfo;
	}

	public String getCode() {
		return this.code;
	}

	public String getSlot() {
		return this.locationInfo.getSlot();
	}

	public IdentifiableGameConstant getSubject() {
		return this.locationInfo.getSubject();
	}
}
