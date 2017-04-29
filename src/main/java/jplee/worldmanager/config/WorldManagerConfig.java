package jplee.worldmanager.config;

import java.io.File;

import jplee.worldmanager.WorldManager;
import jplee.worldmanager.manager.BlockManager;
import jplee.worldmanager.manager.EntityManager;
import jplee.worldmanager.manager.GenerationManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldManagerConfig {
	
	protected Configuration config;
	private boolean isServer;

	protected String[] replaceables;
	protected String[] starting;
	protected String[] oreGen;
	protected String[] blockProperties;
	
	protected boolean replaceablesBlacklist;
	protected boolean oreGenBlacklist;
	protected boolean blockPropertyBlacklist;
	protected boolean hostileSpawnHeightBlackList;
	
	protected int[] replaceablesDimensionsWorlds;
	protected int[] oreGenDimensionsWorlds;
	protected int[] blockPropertiesWorlds;
	protected int[] hostileSpawnHeightWorlds;
	
	protected boolean enableReplaceables = false;
	protected boolean enableOreGen = false;
	protected boolean enableStartInv = false;
	protected boolean enableBlockProperties = false;
	
	protected boolean enableReplacementOverride = false;
	
	protected boolean cleanWorldReg = false;
	protected boolean enableExtenedWorldEdit = true;
	
	protected int maxHostileSpawnHeight = -1;
	
	protected final String[] defaultReplaceables = {
		"# minecraft:oak_stiars[*]",
		"# minecraft:furnace[facing=north]|replace=minecraft:cobblestone"
	};
	protected final String[] defaultStarting = {
		"# minecraft:dirt 2"
	};
	protected final String[] defaultOreGen = {
		"# minecraft:gold_block|min=8|max=45|minSize=1|maxSize=3|chance=2"
	};
	
	protected final String[] defaultProperties = {
		"# minecraft:dirt|gravity=true"
	};
	
	public WorldManagerConfig(File file, boolean isServer) {
		config = new Configuration(file);
		this.isServer = isServer;
		loadConfig(true);
	}
	
	public void loadConfig(boolean saveConfig) {
		loadConfig(saveConfig, false);
	}
	
	public void loadConfig(boolean saveConfig, boolean fromGui) {
		if(!fromGui)
			config.load();
		Property prop;

//		prop = config.get("asm", "extenedWorldEdit", true);
//		if(saveConfig)
//		prop.setComment("Enable extended world edit gui, Default: true");
//		enableExtenedWorldEdit = prop.getBoolean();

		prop = config.get("enable", "replace", false);
		if(saveConfig)
			prop.setComment("Enable block replacement, Default: false");
		enableReplaceables = prop.getBoolean();

		prop = config.get("enable", "oreGen", false);
		if(saveConfig)
			prop.setComment("Enable ore generation, Default: false");
		enableOreGen = prop.getBoolean();

		prop = config.get("enable", "startInv", false);
		if(saveConfig)
			prop.setComment("Enable starting inventory, Default: false");
		enableStartInv = prop.getBoolean();
		
		prop = config.get("enable", "blockProperties", false);
		if(saveConfig)
			prop.setComment("Enable block properties, Default: false");
		enableBlockProperties = prop.getBoolean();

		prop = config.get("enable", "replaceOverride", false);
		if(saveConfig)
			prop.setComment("If there is any form of unintended interactions in generation\n"
						  + "with this and another mod set this to true. This will cause a little\n"
						  + "more generation lag if enabled.");
		enableReplacementOverride = prop.getBoolean();
		
		
		prop = config.get("list", "replaceablesWorlds", new int[0]);
		if(saveConfig)
			prop.setComment("A world blacklist for replaceables to not process in.\n"
					  + "When 'replaceablesBlacklist' is false this will be used as a whitelist.");
		replaceablesDimensionsWorlds = prop.getIntList();
		
		prop = config.get("list", "replaceablesBlacklist", true);
		if(saveConfig)
			prop.setComment("Set this to false to have a white list for replaceables. Default: true");
		replaceablesBlacklist = prop.getBoolean();
		
		prop = config.get("list", "oreGenWorlds", new int[0]);
		if(saveConfig)
			prop.setComment("A world blacklist for ore generation to not process in.\n"
			  		  + "When 'oreGenBlacklist' is false this will be used as a whitelist.");
		oreGenDimensionsWorlds = prop.getIntList();
		
		prop = config.get("list", "oreGenBlacklist", true);
		if(saveConfig)
			prop.setComment("Set this to false to have a whitelist for ore gen. Default: true");
		oreGenBlacklist = prop.getBoolean();
		
		prop = config.get("list", "blockPropertiesWorlds", new int[0]);
		if(saveConfig)
			prop.setComment("A World blacklist for block properties to have no effect in.\n"
					  	  + "When 'blockPropertiesBlacklist' is false this will be used as a whitelist.");
		blockPropertiesWorlds = prop.getIntList();
		
		prop = config.get("list", "blockPropertiesBlacklist", true);
		if(saveConfig)
			prop.setComment("Set this to false to have a whitelist for block properties. Default: true");
		blockPropertyBlacklist = prop.getBoolean();
		
		prop = config.get("list", "hostileSpawnHeightWorlds", new int[0]);
		if(saveConfig)
			prop.setComment("A World blacklist for hostel max spawn height to have no effect in.\n"
						  + "When 'hostelSpawnHeightBlacklist' is false this will be used as a whitelist.");
		hostileSpawnHeightWorlds = prop.getIntList();
		
		prop = config.get("list", "hostelSpawnHeightBlacklist", true);
		if(saveConfig)
			prop.setComment("Set this to false to have a whitelist for max hostel spawn height. Default: true");
		hostileSpawnHeightBlackList = prop.getBoolean();
		
		if(/*isServer || */config.hasKey("server", "cleanWorldRegistry")) {
			prop = config.get("server", "cleanWorldRegistry", false);
			if(saveConfig)
				prop.setComment("This will clean all the registry of unused locations. Default: false\n"
							  + "Do not use this unless you know what you are doing!\n"
							  + "This will create a backup of the world!\n"
							  + "This will return to false after server start!");
			if(isServer) {
				cleanWorldReg = prop.getBoolean();
				if(cleanWorldReg) prop.set(false);
			}else if(prop.getBoolean()) {
				prop.set(false);
			}
		} 
		if(!isServer) {
			cleanWorldReg = false;
		}
		
		prop = config.get("general", "replace", defaultReplaceables);
		if(saveConfig)
			prop.setComment("This list is used for replacing generated blocks\n"
					  + "Each new line will create a new entry for the replacement process\n"
					  + "All entries are sorted by their random value from smallest to greatest\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state] - The block to be replaced\n"
					  + "  Any part of the state for this can be set to '*' to wild card it\n"
					  + "Or you can use ore dictionary as below:\n"
					  + "  ore:oreName\n"
					  + "\n"
					  + "Modifiers for changing how it replaces\n"
					  + "  replace=unlocalized_block_id[state] - the replacement block. Can not wild card, Default: minecraft:air\n"
					  + "  random=double - The chance for it to succeed. Range: 0.0 to 1.0, Default: 1.0\n"
					  + "  dimension=int - The dimension that this will happen in. Range: any integer, Default: any dimension\n"
					  + "  biome=string - replace only in biomes given, separate biomes with a comma (NOT IMPLEMENTED)\n"
					  + "  min=int - the minimum height that it will replace at. Inclusive, Range: 0 to 255, Default: 0\n"
					  + "  max=int - the maximum height that it will replace at. Inclusive, Range: 0 to 255, Default: 255\n"
					  + "  loot=string - the loot table that will be set to lootable inventories (Currently vanilla only inventories or extentions of)\n"
					  + "  match=string - matches states given, separate states with a comma, only works on equivalent states (NOT IMPLEMENTED)\n"
					  + "More will come in the future");
		replaceables = prop.getStringList();
		
		prop = config.get("general", "startInv", defaultStarting);
		if(saveConfig)
			prop.setComment("This list is for adding to the starting inventory\n"
					  + "Each new line is a new item to add the starting inventory\n"
					  + "There is a max of 18 items to prevent inventory overflow\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "Every line needs to be setup as below. nbt, count and meta are optional:\n"
					  + "  unlocalized_block_id:meta count {nbt}");
		starting = prop.getStringList();
		
		prop = config.get("general", "oreGen", defaultOreGen);
		if(saveConfig)
			prop.setComment("This list is for overriding / adding to world ore generation\n"
					  + "Each new line is a new ore generation\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state]\n"
					  + "\n"
					  + "Modifiers for ore generation\n"
					  + "  replace=unlocalized_block_id[state] - the block to replace. Default: minecraft:stone\n"
					  + "   - You can use ore dictionary: ore:oreName\n"
					  + "  dimension=int - The dimension that the ore will generate in. Default: any dimension\n"
					  + "  biome=string - replace only in biomes given, separate biomes with a comma (NOT IMPLEMENTED YET)\n"
					  + "  type=string - The type of generation that will happen (NOT IMPLEMENTED). Default: standard\n"
					  + "   - plain - will generate the ores in a plane style generation\n"
					  + "   - scatter - will generate the ores in a sparce all over the gen location\n"
					  + "   - standard - will generate the ores with the stand minecraft ore generation\n"
					  + "  min=int - the minimum height that it will generate at. Inclusive, Range: 0 to 255, Default: 0\n"
					  + "  max=int - the maximum height that it will generate at. Inclusive, Range: 0 to 255, Default: 255\n"
					  + "  minSize=int - The minimum amount of ore that will generate. Default: 4\n"
					  + "  maxSize=int - The maximum amount of ore that will generate. Default: 8\n"
					  + "  chance=int - The chances that this ore will generate in the chunk. Default: 8\n"
					  + "  override=boolean - Should override the original ore generation. Default: false"
					  + "");
		oreGen = prop.getStringList();
		
		prop = config.get("general", "blockProperties", defaultProperties);
		if(saveConfig)
			prop.setComment("This list is for adding properties to already existing blocks\n"
			  		  + "Each new line is a new entry for that block\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state]\n"
					  + "  Any part of the state for this can be set to '*' to wild card it\n"
					  + "\n"
					  + "Modifiers for blocks properties\n"
					  + "  dimension=int - the dimension that this will effect, Default: any dimension\n"
					  + "  drop=unlocalized_block_id:meta count {nbt} - the item that will be dropped\n"
					  + "   - nbt, count and meta are optional\n"
					  + "  silktouch=boolean - if the block should keep itself if tool has silk touch, only effective if defined drop, Default: true"
					  + "  gravity=bool - should the block be affected by gravity, Default: false\n"
					  + "  hold=double - the chance that the block will hold onto an adjacent block, Range: 0.0 to 1.0, Default: 1.0\n"
					  + "  strength=int - the distance the block can be from a ground source, Range: 0 to 16, Default: 0 (NOT IMPLEMENTED YET)");
		blockProperties = prop.getStringList();
		
		prop = config.get("general", "maxHostileSpawnHeight", -1);
		if(saveConfig)
			prop.setComment("This controls the maximum spawn height of naturally spawned hostile mobs, Range: -1, Max World Height, Default: -1");
		maxHostileSpawnHeight = prop.getInt();
		
		if(saveConfig)
			config.save();
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(WorldManager.MODID)) {
			if(config.hasChanged()) {
				loadConfig(true, true);
				BlockManager.instance.loadFromConfig(this);
				EntityManager.instance.loadFromConfig(this);
				GenerationManager.instance.loadFromConfig(this);
			}
		}
	}

	public Configuration getBaseConfig() {
		return config;
	}
	
	public final String[] getReplaceables() {
		return replaceables;
	}
	
	public String[] getStartingInventory() {
		return starting;
	}
	
	public String[] getOreGeneration() {
		return oreGen;
	}
	
	public String[] getBlockProperties() {
		return blockProperties;
	}
	
	public boolean isExtWorldEditEnabled() {
		return enableExtenedWorldEdit;
	}
	
	public boolean isReplaceablesEnabled() {
		return enableReplaceables;
	}
	
	public boolean isOreGenEnabled() {
		return enableOreGen;
	}
	
	public boolean isStartInvEnabled() {
		return enableStartInv;
	}
	
	public boolean isBlockPropertiesEnabled() {
		return enableBlockProperties;
	}
	
	public boolean shouldCleanWorldReg() {
		return cleanWorldReg;
	}
	
	public boolean isReplaceablesBlacklist() {
		return replaceablesBlacklist;
	}

	public boolean isOreGenBlacklist() {
		return oreGenBlacklist;
	}
	
	public boolean isBlockPropertyBlacklist() {
		return blockPropertyBlacklist;
	}

	public int[] getReplaceablesDimensionsList() {
		return replaceablesDimensionsWorlds;
	}
	
	public int[] getOreGenDimensionsList() {
		return oreGenDimensionsWorlds;
	}
	
	public int[] getPropertyDimensionsList() {
		return blockPropertiesWorlds;
	}
	
	public int getMaxHostileSpawnHeight() {
		return maxHostileSpawnHeight;
	}
	
	public int[] getHostileSpawnHeightWorlds() {
		return hostileSpawnHeightWorlds;
	}
	
	public boolean isHostileSpawnHeightBlackList() {
		return hostileSpawnHeightBlackList;
	}
	
	public boolean isEnableReplacementOverride() {
		return enableReplacementOverride;
	}

}
