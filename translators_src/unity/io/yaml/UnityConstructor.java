package io.yaml;

import io.Scene;

import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * SnakeYAML Constructor for translating YAML .unity files into Java objects. <br>
 * <br>
 * "Constructor" is a bit of a misleading term in my opinion. The Constructor is
 * less about constructing the Java object, and more about choosing the
 * appropriate converter.<br>
 * <br>
 * Each Unity type ID gets its own *Converter class (ex: SceneConverter) to
 * handle it.
 * 
 * @author remiller
 */
public class UnityConstructor extends Constructor {
	private static final String UNITY_TAG = "tag:unity3d.com,2011:";

	public UnityConstructor() {
		Map<String, UnityConstruct> typeIds = new HashMap<String, UnityConstruct>();

		GenericConverter generic = new GenericConverter();

		typeIds.put("1", generic); // GameObject
		typeIds.put("2", generic); // Component
		typeIds.put("3", generic); // LevelGameManager
		typeIds.put("4", generic); // Transform
		typeIds.put("5", generic); // TimeManager
		typeIds.put("6", generic); // GlobalGameManager
		typeIds.put("8", generic); // Behaviour
		typeIds.put("9", generic); // GameManager
		typeIds.put("11", generic); // AudioManager
		typeIds.put("12", generic); // ParticleAnimator
		typeIds.put("13", generic); // InputManager
		typeIds.put("15", generic); // EllipsoidParticleEmitter
		typeIds.put("17", generic); // Pipeline
		typeIds.put("18", generic); // EditorExtension
		typeIds.put("20", generic); // Camera
		typeIds.put("21", generic); // Material
		typeIds.put("23", generic); // MeshRenderer
		typeIds.put("25", generic); // Renderer
		typeIds.put("26", generic); // ParticleRenderer
		typeIds.put("27", generic); // Texture
		typeIds.put("28", generic); // Texture2D
		typeIds.put("29", generic); // Scene
		typeIds.put("30", generic); // RenderManager
		typeIds.put("33", generic); // MeshFilter
		typeIds.put("41", generic); // OcclusionPortal
		typeIds.put("43", generic); // Mesh
		typeIds.put("45", generic); // Skybox
		typeIds.put("47", generic); // QualitySettings
		typeIds.put("48", generic); // Shader
		typeIds.put("49", generic); // TextAsset
		typeIds.put("52", generic); // NotificationManager
		typeIds.put("54", generic); // Rigidbody
		typeIds.put("55", generic); // PhysicsManager
		typeIds.put("56", generic); // Collider
		typeIds.put("57", generic); // Joint
		typeIds.put("59", generic); // HingeJoint
		typeIds.put("64", generic); // MeshCollider
		typeIds.put("65", generic); // BoxCollider
		typeIds.put("71", generic); // AnimationManager
		typeIds.put("74", generic); // AnimationClip
		typeIds.put("75", generic); // ConstantForce
		typeIds.put("76", generic); // WorldParticleCollider
		typeIds.put("78", generic); // TagManager
		typeIds.put("81", generic); // AudioListener
		typeIds.put("82", generic); // AudioSource
		typeIds.put("83", generic); // AudioClip
		typeIds.put("84", generic); // RenderTexture
		typeIds.put("87", generic); // MeshParticleEmitter
		typeIds.put("88", generic); // ParticleEmitter
		typeIds.put("89", generic); // Cubemap
		typeIds.put("92", generic); // GUILayer
		typeIds.put("94", generic); // ScriptMapper
		typeIds.put("96", generic); // TrailRenderer
		typeIds.put("98", generic); // DelayedCallManager
		typeIds.put("102", generic); // TextMesh
		typeIds.put("104", generic); // RenderSettings
		typeIds.put("108", generic); // Light
		typeIds.put("109", generic); // CGProgram
		typeIds.put("111", generic); // Animation
		typeIds.put("114", generic); // MonoBehaviour
		typeIds.put("115", generic); // MonoScript
		typeIds.put("116", generic); // MonoManager
		typeIds.put("117", generic); // Texture3D
		typeIds.put("119", generic); // Projector
		typeIds.put("120", generic); // LineRenderer
		typeIds.put("121", generic); // Flare
		typeIds.put("122", generic); // Halo
		typeIds.put("123", generic); // LensFlare
		typeIds.put("124", generic); // FlareLayer
		typeIds.put("125", generic); // HaloLayer
		typeIds.put("126", generic); // NavMeshLayers
		typeIds.put("127", generic); // HaloManager
		typeIds.put("128", generic); // Font
		typeIds.put("129", generic); // PlayerSettings
		typeIds.put("130", generic); // NamedObject
		typeIds.put("131", generic); // GUITexture
		typeIds.put("132", generic); // GUIText
		typeIds.put("133", generic); // GUIElement
		typeIds.put("134", generic); // PhysicMaterial
		typeIds.put("135", generic); // SphereCollider
		typeIds.put("136", generic); // CapsuleCollider
		typeIds.put("137", generic); // SkinnedMeshRenderer
		typeIds.put("138", generic); // FixedJoint
		typeIds.put("140", generic); // RaycastCollider
		typeIds.put("141", generic); // BuildSettings
		typeIds.put("142", generic); // AssetBundle
		typeIds.put("143", generic); // CharacterController
		typeIds.put("144", generic); // CharacterJoint
		typeIds.put("145", generic); // SpringJoint
		typeIds.put("146", generic); // WheelCollider
		typeIds.put("147", generic); // ResourceManager
		typeIds.put("148", generic); // NetworkView
		typeIds.put("149", generic); // NetworkManager
		typeIds.put("150", generic); // Preloputata
		typeIds.put("152", generic); // MovieTexture
		typeIds.put("153", generic); // ConfigurableJoint
		typeIds.put("154", generic); // TerrainCollider
		typeIds.put("155", generic); // MasterServerInterface
		typeIds.put("156", generic); // TerrainData
		typeIds.put("157", generic); // LightmapSettings
		typeIds.put("158", generic); // WebCamTexture
		typeIds.put("159", generic); // EditorSettings
		typeIds.put("160", generic); // InteractiveCloth
		typeIds.put("161", generic); // ClothRenderer
		typeIds.put("163", generic); // SkinnedCloth
		typeIds.put("164", generic); // AudioReverbFilter
		typeIds.put("165", generic); // AudioHighPassFilter
		typeIds.put("166", generic); // AudioChorusFilter
		typeIds.put("167", generic); // AudioReverbZone
		typeIds.put("168", generic); // AudioEchoFilter
		typeIds.put("169", generic); // AudioLowPassFilter
		typeIds.put("170", generic); // AudioDistortionFilter
		typeIds.put("180", generic); // AudioBehaviour
		typeIds.put("181", generic); // AudioFilter
		typeIds.put("182", generic); // WindZone
		typeIds.put("183", generic); // Cloth
		typeIds.put("184", generic); // SubstanceArchive
		typeIds.put("185", generic); // ProceduralMaterial
		typeIds.put("186", generic); // ProceduralTexture
		typeIds.put("191", generic); // OffMeshLink
		typeIds.put("192", generic); // OcclusionArea
		typeIds.put("193", generic); // Tree
		typeIds.put("194", generic); // NavMesh
		typeIds.put("195", generic); // NavMeshAgent
		typeIds.put("196", generic); // NavMeshSettings
		typeIds.put("197", generic); // LightProbeCloud
		typeIds.put("198", generic); // ParticleSystem
		typeIds.put("199", generic); // ParticleSystemRenderer
		typeIds.put("205", generic); // LODGroup
		typeIds.put("220", generic); // LightProbeGroup
		typeIds.put("1001", generic); // Prefab
		typeIds.put("1002", generic); // EditorExtensionImpl
		typeIds.put("1003", generic); // AssetImporter
		typeIds.put("1004", generic); // AssetDatabase
		typeIds.put("1005", generic); // Mesh3DSImporter
		typeIds.put("1006", generic); // TextureImporter
		typeIds.put("1007", generic); // ShaderImporter
		typeIds.put("1020", generic); // AudioImporter
		typeIds.put("1026", generic); // HierarchyState
		typeIds.put("1027", generic); // GUIDSerializer
		typeIds.put("1028", generic); // AssetMetaData
		typeIds.put("1029", generic); // DefaultAsset
		typeIds.put("1030", generic); // DefaultImporter
		typeIds.put("1031", generic); // TextScriptImporter
		typeIds.put("1034", generic); // NativeFormatImporter
		typeIds.put("1035", generic); // MonoImporter
		typeIds.put("1037", generic); // AssetServerCache
		typeIds.put("1038", generic); // LibraryAssetImporter
		typeIds.put("1040", generic); // ModelImporter
		typeIds.put("1041", generic); // FBXImporter
		typeIds.put("1042", generic); // TrueTypeFontImporter
		typeIds.put("1044", generic); // MovieImporter
		typeIds.put("1045", generic); // EditorBuildSettings
		typeIds.put("1046", generic); // DDSImporter
		typeIds.put("1048", generic); // InspectorExpandedState
		typeIds.put("1049", generic); // AnnotationManager
		typeIds.put("1050", generic); // MonoAssemblyImporter
		typeIds.put("1051", generic); // EditorUserBuildSettings
		typeIds.put("1052", generic); // PVRImporter
		typeIds.put("1112", generic); // SubstanceImporter

		for (String id : typeIds.keySet()) {
			this.registerConverter(UNITY_TAG + id, typeIds.get(id));
		}
	}

	/**
	 * Registers a converter for the given tag. Convenience method for
	 * {@link #registerConverter(Tag, Construct, Class)}
	 * 
	 * @param tag
	 *            The tag to register for.
	 * @param converter
	 *            The converter to be used when the given tag is encountered.
	 * @param clazz
	 *            The class expected to come out of the conversion process.
	 * 
	 * @see #registerConverter(Tag, Construct, Class)
	 */
	private void registerConverter(String tag, UnityConstruct converter) {
		this.registerConverter(new Tag(tag), converter);
	}

	/**
	 * Registers a converter for the given tag.
	 * 
	 * @param tag
	 *            The tag to register for.
	 * @param converter
	 *            The converter to be used when the given tag is encountered.
	 * @param clazz
	 *            The class expected to come out of the conversion process.
	 * 
	 * @see #registerConverter(String, Construct, Class)
	 */
	private void registerConverter(Tag tag, UnityConstruct converter) {
		this.yamlConstructors.put(tag, converter);
		this.addTypeDescription(new TypeDescription(converter.getResultClass(), tag));
	}

	private class GenericConverter extends UnityConstruct {
		public GenericConverter() {
			super(Object.class);
		}

		public Object construct(Node node) {
			final NodeId nodeId = node.getNodeId();
			final Object converted;

			if (nodeId == NodeId.scalar) {
				ScalarNode sNode = (ScalarNode) node;
				converted = UnityConstructor.this.constructScalar(sNode);
			} else if (nodeId == NodeId.mapping) {
				MappingNode mNode = (MappingNode) node;
				converted = UnityConstructor.this.constructMapping(mNode);
			} else if (nodeId == NodeId.sequence) {
				SequenceNode qNode = (SequenceNode) node;
				converted = UnityConstructor.this.constructSequence(qNode);
			} else {
				converted = null;
				throw new IllegalStateException(
						"YAML Node is not one of the three basic types.");
			}

			return converted;
		}
	}

	private class SceneConverter extends UnityConstruct {
		public SceneConverter() {
			super(Scene.class);
		}

		public Object construct(Node node) {
			final String msg = "Not reading Scene objects yet";
			System.err.println(msg);
			return msg;
		}
	}
}
