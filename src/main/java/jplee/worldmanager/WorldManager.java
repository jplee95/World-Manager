package jplee.worldmanager;

import java.io.File;
import java.io.IOException;

import jplee.jlib.util.Log;
import jplee.worldmanager.command.CommandWorldManager;
import jplee.worldmanager.config.WorldManagerConfig;
import jplee.worldmanager.event.GuiChunkDebugEvent;
import jplee.worldmanager.event.WorldBlockEvent;
import jplee.worldmanager.event.WorldEntityEvent;
import jplee.worldmanager.event.WorldGenEvent;
import jplee.worldmanager.manager.BlockManager;
import jplee.worldmanager.manager.EntityManager;
import jplee.worldmanager.manager.GenerationManager;
import jplee.worldmanager.util.SaveFileUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = WorldManager.MODID, name = WorldManager.NAME, version = WorldManager.VERSION,
dependencies = WorldManager.DEPENDENCIES, acceptedMinecraftVersions = WorldManager.MINECRAFT_VERSION,
guiFactory = WorldManager.GUI_FACTORY, acceptableRemoteVersions = "*", canBeDeactivated = false)
public class WorldManager {
	public static final String NAME = "World Manager";
	public static final String MODID = "worldmanager";
	public static final String VERSION = "1.1.1";
	public static final String DEPENDENCIES = "required-after:Forge@[12.18.2.2099,)";
	public static final String MINECRAFT_VERSION = "[1.10.2]";
	public static final String GUI_FACTORY = "jplee.worldmanager.config.WorldManagerGuiFactory";

	public static final String CHUNK_REPLACE_TAG = "wmReplace";
	public static final String PLAYER_START_TAG = "wmStart";
	public static final String DIMENSION_GAMERULE_TAG = "wmGamerule";

	@Mod.Instance
	public static WorldManager instance;

	public static final Log logger = new Log();
	private static WorldManagerConfig config;

	private static boolean showDebugInfo = false;
	private static WorldGenEvent worldEvents = new WorldGenEvent();

	public static void showDebug(boolean show) {
		showDebugInfo = show;
	}

	public static boolean isDebugShowing() {
		return showDebugInfo;
	}

	public static boolean isStartInvEnabled() {
		return config.isStartInvEnabled();
	}
	
	public static Configuration getBaseConfig() {
		return config.getBaseConfig();
	}

	public static void reloadConfig(boolean saveConfig) {
		config.loadConfig(saveConfig);
		BlockManager.instance.loadFromConfig(config);
		EntityManager.instance.loadFromConfig(config);
		GenerationManager.instance.loadFromConfig(config);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.attachLogger(event.getModLog());
		File configFolder = event.getSuggestedConfigurationFile();
		config = new WorldManagerConfig(configFolder, event.getSide() == Side.SERVER);

		GenerationManager.instance.loadFromConfig(config);
		GenerationManager.instance.registerWorldGenerators();
		EntityManager.instance.loadFromConfig(config);
		BlockManager.instance.loadFromConfig(config);
		
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(config);
		MinecraftForge.EVENT_BUS.register(worldEvents);
		MinecraftForge.TERRAIN_GEN_BUS.register(worldEvents);
		MinecraftForge.ORE_GEN_BUS.register(worldEvents);
		MinecraftForge.EVENT_BUS.register(new WorldBlockEvent());
		MinecraftForge.EVENT_BUS.register(new WorldEntityEvent());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if(event.getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new GuiChunkDebugEvent());
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {}

	@Mod.EventHandler
	public void serverWillStart(FMLServerAboutToStartEvent event) {
		if(event.getSide() == Side.SERVER) {
			if(config.shouldCleanWorldReg()) {
				MinecraftServer server = event.getServer();
				logger.info("Cleaning %s registires", server.getFolderName());
				try {
					SaveFileUtils.cleanWorldRegistry(server.getDataDirectory(), server.getFolderName());
				} catch(IOException e) {
					logger.error("Unable to read and write to file for %s", server.getFolderName());
					e.printStackTrace();
				}
			}
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandWorldManager());
	}

	@Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		logger.info("Clearing world catch");
		GenerationManager.instance.clearQueuedChunks();
		EntityManager.instance.clearEntityInfoCatch();
		BlockManager.instance.clearFallEvents();
	}
}
