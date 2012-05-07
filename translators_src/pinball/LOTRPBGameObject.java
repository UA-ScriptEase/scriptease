package translators.Pinball;

import java.util.Collection;

import scriptease.translator.io.model.GameObject;

public class LOTRPBGameObject implements GameObject {
	private String switchName;
	private Collection<String> types;
	private String resolutionName;

	public LOTRPBGameObject(String switchName, Collection<String> types){
		this.switchName = switchName;
		this.setTypes(types);
		this.resolutionName = switchName;
	}

	/**
	 * There may exist GameObjects which have different names than they would
	 * want to resolve to in code. Ex: Sword Lock High becomes Sword Lock
	 * Release in code.
	 * 
	 * @param switchName
	 * @param resolutionName
	 * @param type
	 */
	public LOTRPBGameObject(String switchName, String resolutionName,
			Collection<String> types) {
		this.switchName = switchName;
		this.resolutionName = resolutionName;
		this.setTypes(types);
	}

	@Override
	public int getObjectID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getResolutionCode() {
		return resolutionName;
	}

	@Override
	public int getResolutionMethod() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setResolutionMethod(int methodType) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return this.switchName;
	}

	@Override
	public String getTemplateID() {
		return this.switchName;
	}

	@Override
	public String toString(){
		return this.switchName;
	}

	public Collection<String> getTypes(){
		return this.types;
	}

	@Override
	public void setTypes(Collection<String> types) {
		this.types = types;
	}

	@Override
	public String getFirstType() {
		return this.types.iterator().next();
	}
}
