package io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnityTranslatorConstants {
	private final static int NUMBER_OF_TYPES = 1113;
	public static final List<String> TYPE_LIST = new ArrayList<String>(
			Collections.<String> nCopies(NUMBER_OF_TYPES, null));

	static {
		TYPE_LIST.set(1, "GameObject");
		TYPE_LIST.set(2, "Component");
		TYPE_LIST.set(3, "LevelGameManager");
		TYPE_LIST.set(4, "Transform");
		TYPE_LIST.set(5, "TimeManager");
		TYPE_LIST.set(6, "GlobalGameManager");
		TYPE_LIST.set(8, "Behaviour");
		TYPE_LIST.set(9, "GameManager");
		TYPE_LIST.set(11, "AudioManager");
		TYPE_LIST.set(12, "ParticleAnimator");
		TYPE_LIST.set(13, "InputManager");
		TYPE_LIST.set(15, "EllipsoidParticleEmitter");
		TYPE_LIST.set(17, "Pipeline");
		TYPE_LIST.set(18, "EditorExtension");
		TYPE_LIST.set(20, "Camera");
		TYPE_LIST.set(21, "Material");
		TYPE_LIST.set(23, "MeshRenderer");
		TYPE_LIST.set(25, "Renderer");
		TYPE_LIST.set(26, "ParticleRenderer");
		TYPE_LIST.set(27, "Texture");
		TYPE_LIST.set(28, "Texture2D");
		TYPE_LIST.set(29, "Scene");
		TYPE_LIST.set(30, "RenderManager");
		TYPE_LIST.set(33, "MeshFilter");
		TYPE_LIST.set(41, "OcclusionPortal");
		TYPE_LIST.set(43, "Mesh");
		TYPE_LIST.set(45, "Skybox");
		TYPE_LIST.set(47, "QualitySettings");
		TYPE_LIST.set(48, "Shader");
		TYPE_LIST.set(49, "TextAsset");
		TYPE_LIST.set(52, "NotificationManager");
		TYPE_LIST.set(54, "Rigidbody");
		TYPE_LIST.set(55, "PhysicsManager");
		TYPE_LIST.set(56, "Collider");
		TYPE_LIST.set(57, "Joint");
		TYPE_LIST.set(59, "HingeJoint");
		TYPE_LIST.set(64, "MeshCollider");
		TYPE_LIST.set(65, "BoxCollider");
		TYPE_LIST.set(71, "AnimationManager");
		TYPE_LIST.set(74, "AnimationClip");
		TYPE_LIST.set(75, "ConstantForce");
		TYPE_LIST.set(76, "WorldParticleCollider");
		TYPE_LIST.set(78, "TagManager");
		TYPE_LIST.set(81, "AudioListener");
		TYPE_LIST.set(82, "AudioSource");
		TYPE_LIST.set(83, "AudioClip");
		TYPE_LIST.set(84, "RenderTexture");
		TYPE_LIST.set(87, "MeshParticleEmitter");
		TYPE_LIST.set(88, "ParticleEmitter");
		TYPE_LIST.set(89, "Cubemap");
		TYPE_LIST.set(92, "GUILayer");
		TYPE_LIST.set(94, "ScriptMapper");
		TYPE_LIST.set(96, "TrailRenderer");
		TYPE_LIST.set(98, "DelayedCallManager");
		TYPE_LIST.set(102, "TextMesh");
		TYPE_LIST.set(104, "RenderSettings");
		TYPE_LIST.set(108, "Light");
		TYPE_LIST.set(109, "CGProgram");
		TYPE_LIST.set(111, "Animation");
		TYPE_LIST.set(114, "MonoBehaviour");
		TYPE_LIST.set(115, "MonoScript");
		TYPE_LIST.set(116, "MonoManager");
		TYPE_LIST.set(117, "Texture3D");
		TYPE_LIST.set(119, "Projector");
		TYPE_LIST.set(120, "LineRenderer");
		TYPE_LIST.set(121, "Flare");
		TYPE_LIST.set(122, "Halo");
		TYPE_LIST.set(123, "LensFlare");
		TYPE_LIST.set(124, "FlareLayer");
		TYPE_LIST.set(125, "HaloLayer");
		TYPE_LIST.set(126, "NavMeshLayers");
		TYPE_LIST.set(127, "HaloManager");
		TYPE_LIST.set(128, "Font");
		TYPE_LIST.set(129, "PlayerSettings");
		TYPE_LIST.set(130, "NamedObject");
		TYPE_LIST.set(131, "GUITexture");
		TYPE_LIST.set(132, "GUIText");
		TYPE_LIST.set(133, "GUIElement");
		TYPE_LIST.set(134, "PhysicMaterial");
		TYPE_LIST.set(135, "SphereCollider");
		TYPE_LIST.set(136, "CapsuleCollider");
		TYPE_LIST.set(137, "SkinnedMeshRenderer");
		TYPE_LIST.set(138, "FixedJoint");
		TYPE_LIST.set(140, "RaycastCollider");
		TYPE_LIST.set(141, "BuildSettings");
		TYPE_LIST.set(142, "AssetBundle");
		TYPE_LIST.set(143, "CharacterController");
		TYPE_LIST.set(144, "CharacterJoint");
		TYPE_LIST.set(145, "SpringJoint");
		TYPE_LIST.set(146, "WheelCollider");
		TYPE_LIST.set(147, "ResourceManager");
		TYPE_LIST.set(148, "NetworkView");
		TYPE_LIST.set(149, "NetworkManager");
		TYPE_LIST.set(150, "PreloadedData");
		TYPE_LIST.set(152, "MovieTexture");
		TYPE_LIST.set(153, "ConfigurableJoint");
		TYPE_LIST.set(154, "TerrainCollider");
		TYPE_LIST.set(155, "MasterServerInterface");
		TYPE_LIST.set(156, "TerrainData");
		TYPE_LIST.set(157, "LightmapSettings");
		TYPE_LIST.set(158, "WebCamTexture");
		TYPE_LIST.set(159, "EditorSettings");
		TYPE_LIST.set(160, "InteractiveCloth");
		TYPE_LIST.set(161, "ClothRenderer");
		TYPE_LIST.set(163, "SkinnedCloth");
		TYPE_LIST.set(164, "AudioReverbFilter");
		TYPE_LIST.set(165, "AudioHighPassFilter");
		TYPE_LIST.set(166, "AudioChorusFilter");
		TYPE_LIST.set(167, "AudioReverbZone");
		TYPE_LIST.set(168, "AudioEchoFilter");
		TYPE_LIST.set(169, "AudioLowPassFilter");
		TYPE_LIST.set(170, "AudioDistortionFilter");
		TYPE_LIST.set(180, "AudioBehaviour");
		TYPE_LIST.set(181, "AudioFilter");
		TYPE_LIST.set(182, "WindZone");
		TYPE_LIST.set(183, "Cloth");
		TYPE_LIST.set(184, "SubstanceArchive");
		TYPE_LIST.set(185, "ProceduralMaterial");
		TYPE_LIST.set(186, "ProceduralTexture");
		TYPE_LIST.set(191, "OffMeshLink");
		TYPE_LIST.set(192, "OcclusionArea");
		TYPE_LIST.set(193, "Tree");
		TYPE_LIST.set(194, "NavMesh");
		TYPE_LIST.set(195, "NavMeshAgent");
		TYPE_LIST.set(196, "NavMeshSettings");
		TYPE_LIST.set(197, "LightProbeCloud");
		TYPE_LIST.set(198, "ParticleSystem");
		TYPE_LIST.set(199, "ParticleSystemRenderer");
		TYPE_LIST.set(205, "LODGroup");
		TYPE_LIST.set(220, "LightProbeGroup");
		TYPE_LIST.set(1001, "Prefab");
		TYPE_LIST.set(1002, "EditorExtensionImpl");
		TYPE_LIST.set(1003, "AssetImporter");
		TYPE_LIST.set(1004, "AssetDatabase");
		TYPE_LIST.set(1005, "Mesh3DSImporter");
		TYPE_LIST.set(1006, "TextureImporter");
		TYPE_LIST.set(1007, "ShaderImporter");
		TYPE_LIST.set(1020, "AudioImporter");
		TYPE_LIST.set(1026, "HierarchyState");
		TYPE_LIST.set(1027, "GUIDSerializer");
		TYPE_LIST.set(1028, "AssetMetaData");
		TYPE_LIST.set(1029, "DefaultAsset");
		TYPE_LIST.set(1030, "DefaultImporter");
		TYPE_LIST.set(1031, "TextScriptImporter");
		TYPE_LIST.set(1034, "NativeFormatImporter");
		TYPE_LIST.set(1035, "MonoImporter");
		TYPE_LIST.set(1037, "AssetServerCache");
		TYPE_LIST.set(1038, "LibraryAssetImporter");
		TYPE_LIST.set(1040, "ModelImporter");
		TYPE_LIST.set(1041, "FBXImporter");
		TYPE_LIST.set(1042, "TrueTypeFontImporter");
		TYPE_LIST.set(1044, "MovieImporter");
		TYPE_LIST.set(1045, "EditorBuildSettings");
		TYPE_LIST.set(1046, "DDSImporter");
		TYPE_LIST.set(1048, "InspectorExpandedState");
		TYPE_LIST.set(1049, "AnnotationManager");
		TYPE_LIST.set(1050, "MonoAssemblyImporter");
		TYPE_LIST.set(1051, "EditorUserBuildSettings");
		TYPE_LIST.set(1052, "PVRImporter");
		TYPE_LIST.set(1112, "SubstanceImporter");
	}
}
