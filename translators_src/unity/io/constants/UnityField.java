package io.constants;

/**
 * Enumerations of possible fields in Unity. Use {@link #getName()} to get the
 * properly formatted name of a field.
 * 
 * @author kschenk
 * 
 */
public enum UnityField {
	FILEID("fileID"),

	GUID("guid"),

	M_ANIMATIONS("m_Animations"),

	M_CHILDREN("m_Children"),

	M_COMPONENT("m_Component"),

	M_EDITORHIDEFLAGS("m_EditorHideFlags"),

	M_ENABLED("m_Enabled"),

	M_FATHER("m_Father"),

	M_GAMEOBJECT("m_GameObject"),

	M_ICON("m_Icon"),

	M_ISACTIVE("m_IsActive"),

	M_LAYER("m_Layer"),

	M_LOCALPOSITION("m_LocalPosition"),

	M_LOCALROTATION("m_LocalRotation"),

	M_LOCALSCALE("m_LocalScale"),

	M_NAME("m_Name"),

	M_NAVMESHLAYER("m_NavMeshLayer"),

	M_OBJECTHIDEFLAGS("m_ObjectHideFlags"),

	M_PREFABINTERNAL("m_PrefabInternal"),

	M_PREFABPARENTOBJECT("m_PrefabParentObject"),

	M_SCRIPT("m_Script"),

	M_STATICEDITORFLAGS("m_StaticEditorFlags"),

	M_TAGSTRING("m_TagString"),

	SERIALIZEDVERSION("serializedVersion"),

	TYPE("type"),

	W("w"),

	X("x"),

	Y("y"),

	Z("z");

	private final String name;

	private UnityField(String name) {
		this.name = name;
	}

	/**
	 * Returns the properly formatted name of the field.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}
}
