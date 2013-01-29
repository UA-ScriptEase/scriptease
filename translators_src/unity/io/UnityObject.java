package io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import scriptease.translator.io.model.GameObject;

public class UnityObject implements GameObject {
	public static final String UNITY_TAG = "tag:unity3d.com,2011:";

	private static final Map<Integer, String> typeMap = new HashMap<Integer, String>();
	static {
		typeMap.put(1, "GameObject");
		typeMap.put(2, "Component");
		typeMap.put(3, "LevelGameManager");
		typeMap.put(4, "Transform");
		typeMap.put(5, "TimeManager");
		typeMap.put(6, "GlobalGameManager");
		typeMap.put(8, "Behaviour");
		typeMap.put(9, "GameManager");
		typeMap.put(11, "AudioManager");
		typeMap.put(12, "ParticleAnimator");
		typeMap.put(13, "InputManager");
		typeMap.put(15, "EllipsoidParticleEmitter");
		typeMap.put(17, "Pipeline");
		typeMap.put(18, "EditorExtension");
		typeMap.put(20, "Camera");
		typeMap.put(21, "Material");
		typeMap.put(23, "MeshRenderer");
		typeMap.put(25, "Renderer");
		typeMap.put(26, "ParticleRenderer");
		typeMap.put(27, "Texture");
		typeMap.put(28, "Texture2D");
		typeMap.put(29, "Scene");
		typeMap.put(30, "RenderManager");
		typeMap.put(33, "MeshFilter");
		typeMap.put(41, "OcclusionPortal");
		typeMap.put(43, "Mesh");
		typeMap.put(45, "Skybox");
		typeMap.put(47, "QualitySettings");
		typeMap.put(48, "Shader");
		typeMap.put(49, "TextAsset");
		typeMap.put(52, "NotificationManager");
		typeMap.put(54, "Rigidbody");
		typeMap.put(55, "PhysicsManager");
		typeMap.put(56, "Collider");
		typeMap.put(57, "Joint");
		typeMap.put(59, "HingeJoint");
		typeMap.put(64, "MeshCollider");
		typeMap.put(65, "BoxCollider");
		typeMap.put(71, "AnimationManager");
		typeMap.put(74, "AnimationClip");
		typeMap.put(75, "ConstantForce");
		typeMap.put(76, "WorldParticleCollider");
		typeMap.put(78, "TagManager");
		typeMap.put(81, "AudioListener");
		typeMap.put(82, "AudioSource");
		typeMap.put(83, "AudioClip");
		typeMap.put(84, "RenderTexture");
		typeMap.put(87, "MeshParticleEmitter");
		typeMap.put(88, "ParticleEmitter");
		typeMap.put(89, "Cubemap");
		typeMap.put(92, "GUILayer");
		typeMap.put(94, "ScriptMapper");
		typeMap.put(96, "TrailRenderer");
		typeMap.put(98, "DelayedCallManager");
		typeMap.put(102, "TextMesh");
		typeMap.put(104, "RenderSettings");
		typeMap.put(108, "Light");
		typeMap.put(109, "CGProgram");
		typeMap.put(111, "Animation");
		typeMap.put(114, "MonoBehaviour");
		typeMap.put(115, "MonoScript");
		typeMap.put(116, "MonoManager");
		typeMap.put(117, "Texture3D");
		typeMap.put(119, "Projector");
		typeMap.put(120, "LineRenderer");
		typeMap.put(121, "Flare");
		typeMap.put(122, "Halo");
		typeMap.put(123, "LensFlare");
		typeMap.put(124, "FlareLayer");
		typeMap.put(125, "HaloLayer");
		typeMap.put(126, "NavMeshLayers");
		typeMap.put(127, "HaloManager");
		typeMap.put(128, "Font");
		typeMap.put(129, "PlayerSettings");
		typeMap.put(130, "NamedObject");
		typeMap.put(131, "GUITexture");
		typeMap.put(132, "GUIText");
		typeMap.put(133, "GUIElement");
		typeMap.put(134, "PhysicMaterial");
		typeMap.put(135, "SphereCollider");
		typeMap.put(136, "CapsuleCollider");
		typeMap.put(137, "SkinnedMeshRenderer");
		typeMap.put(138, "FixedJoint");
		typeMap.put(140, "RaycastCollider");
		typeMap.put(141, "BuildSettings");
		typeMap.put(142, "AssetBundle");
		typeMap.put(143, "CharacterController");
		typeMap.put(144, "CharacterJoint");
		typeMap.put(145, "SpringJoint");
		typeMap.put(146, "WheelCollider");
		typeMap.put(147, "ResourceManager");
		typeMap.put(148, "NetworkView");
		typeMap.put(149, "NetworkManager");
		typeMap.put(150, "PreloadedData");
		typeMap.put(152, "MovieTexture");
		typeMap.put(153, "ConfigurableJoint");
		typeMap.put(154, "TerrainCollider");
		typeMap.put(155, "MasterServerInterface");
		typeMap.put(156, "TerrainData");
		typeMap.put(157, "LightmapSettings");
		typeMap.put(158, "WebCamTexture");
		typeMap.put(159, "EditorSettings");
		typeMap.put(160, "InteractiveCloth");
		typeMap.put(161, "ClothRenderer");
		typeMap.put(163, "SkinnedCloth");
		typeMap.put(164, "AudioReverbFilter");
		typeMap.put(165, "AudioHighPassFilter");
		typeMap.put(166, "AudioChorusFilter");
		typeMap.put(167, "AudioReverbZone");
		typeMap.put(168, "AudioEchoFilter");
		typeMap.put(169, "AudioLowPassFilter");
		typeMap.put(170, "AudioDistortionFilter");
		typeMap.put(180, "AudioBehaviour");
		typeMap.put(181, "AudioFilter");
		typeMap.put(182, "WindZone");
		typeMap.put(183, "Cloth");
		typeMap.put(184, "SubstanceArchive");
		typeMap.put(185, "ProceduralMaterial");
		typeMap.put(186, "ProceduralTexture");
		typeMap.put(191, "OffMeshLink");
		typeMap.put(192, "OcclusionArea");
		typeMap.put(193, "Tree");
		typeMap.put(194, "NavMesh");
		typeMap.put(195, "NavMeshAgent");
		typeMap.put(196, "NavMeshSettings");
		typeMap.put(197, "LightProbeCloud");
		typeMap.put(198, "ParticleSystem");
		typeMap.put(199, "ParticleSystemRenderer");
		typeMap.put(205, "LODGroup");
		typeMap.put(220, "LightProbeGroup");
		typeMap.put(1001, "Prefab");
		typeMap.put(1002, "EditorExtensionImpl");
		typeMap.put(1003, "AssetImporter");
		typeMap.put(1004, "AssetDatabase");
		typeMap.put(1005, "Mesh3DSImporter");
		typeMap.put(1006, "TextureImporter");
		typeMap.put(1007, "ShaderImporter");
		typeMap.put(1020, "AudioImporter");
		typeMap.put(1026, "HierarchyState");
		typeMap.put(1027, "GUIDSerializer");
		typeMap.put(1028, "AssetMetaData");
		typeMap.put(1029, "DefaultAsset");
		typeMap.put(1030, "DefaultImporter");
		typeMap.put(1031, "TextScriptImporter");
		typeMap.put(1034, "NativeFormatImporter");
		typeMap.put(1035, "MonoImporter");
		typeMap.put(1037, "AssetServerCache");
		typeMap.put(1038, "LibraryAssetImporter");
		typeMap.put(1040, "ModelImporter");
		typeMap.put(1041, "FBXImporter");
		typeMap.put(1042, "TrueTypeFontImporter");
		typeMap.put(1044, "MovieImporter");
		typeMap.put(1045, "EditorBuildSettings");
		typeMap.put(1046, "DDSImporter");
		typeMap.put(1048, "InspectorExpandedState");
		typeMap.put(1049, "AnnotationManager");
		typeMap.put(1050, "MonoAssemblyImporter");
		typeMap.put(1051, "EditorUserBuildSettings");
		typeMap.put(1052, "PVRImporter");
		typeMap.put(1112, "SubstanceImporter");
	}

	private final int uniqueID;
	private final String tag;
	private final Map<String, Object> propertyMap;

	public UnityObject(int uniqueID, String tag) {
		this.uniqueID = uniqueID;
		this.tag = tag;
		this.propertyMap = new HashMap<String, Object>();
	}

	public void setProperties(Map<String, Object> map) {
		this.propertyMap.clear();
		this.propertyMap.putAll(map);
	}

	@Override
	public String getTag() {
		return this.tag;
	}

	public int getUniqueID() {
		return this.uniqueID;
	}

	public Map<String, Object> getPropertyMap() {
		return this.propertyMap;
	}

	@Override
	public Collection<String> getTypes() {
		final Collection<String> types;

		types = new ArrayList<String>();

		types.add(typeMap.get(this.getTypeNumber()));

		return types;
	}

	@Override
	public String getName() {
		for (String key : this.propertyMap.keySet())
			return key;
		return "";
	}

	@Override
	public String getTemplateID() {
		return this.tag;
	}

	@Override
	public String getCodeText() {
		return this.getName();
	}

	public int getTypeNumber() {
		return Integer.parseInt(this.tag.split(":")[2]);
	}

	@Override
	public void setResolutionMethod(int methodType) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getResolutionMethod() {
		// TODO Auto-generated method stub
		return 0;
	}
}
