package jplee.worldmanager.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class GenConfig {
	
	private Configuration config;
	private boolean isServer;

	private String[] replaceables;
	private String[] starting;
	private String[] oreGen;
	
	private boolean replaceablesBlacklist;
	private boolean oreGenBlacklist;
	
	private int[] replaceablesDimensionsList;
	private int[] oreGenDimensionsList;
	
	private boolean enableReplaceables = false;
	private boolean enableOreGen = false;
	private boolean enableStartInv = false;
	private boolean cleanWorldReg = false;
	private boolean enableExtenedWorldEdit = true;
	
	private final String[] defaultReplaceables = {
		"# minecraft:oak_stiars[*]",
		"# minecraft:furnace[facing=north]|replace=minecraft:cobblestone"
	};
	private final String[] defaultStarting = {
		"# minecraft:dirt 2"
	};
	private final String[] defaultOreGen = {
		"# minecraft:gold_block|min=8|max=45|minSize=1|maxSize=3|chance=2"
	};
	
	public GenConfig(File file, boolean isServer) {
		this.config = new Configuration(file);
		this.isServer = isServer;
		this.loadConfig(true);
	}
	
	public void loadConfig(boolean saveConfig) {
		this.config.load();
		Property prop;

//		prop = this.config.get("asm", "extenedWorldEdit", true);
//		prop.setComment("Enable extended world edit gui, Default: true");
//		this.enableExtenedWorldEdit = prop.getBoolean();

		prop = this.config.get("enable", "replace", false);
		prop.setComment("Enable block replacement, Default: false");
		this.enableReplaceables = prop.getBoolean();

		prop = this.config.get("enable", "oreGen", false);
		prop.setComment("Enable ore generation overrides, Default: false");
		this.enableOreGen = prop.getBoolean();

		prop = this.config.get("enable", "startInv", false);
		prop.setComment("Enable starting inventory, Default: false");
		this.enableStartInv = prop.getBoolean();

		
		prop = this.config.get("list", "replaceables", new int[0]);
		prop.setComment("A world blacklist for replaceables to not process in.\n"
					  + "When 'replaceablesBlacklist' is false this will be used as a whitelist.");
		this.replaceablesDimensionsList = prop.getIntList();
		
		prop = this.config.get("list", "replaceablesBlacklist", true);
		prop.setComment("Set this to false to have a white list for replaceables. Default: true");
		this.replaceablesBlacklist = prop.getBoolean();
		
		prop = this.config.get("list", "oreGen", new int[0]);
		prop.setComment("A world blacklist for ore generation to not process in.\n"
			  		  + "When 'oreGenBlacklist' is false this will be used as a whitelist.");
		this.oreGenDimensionsList = prop.getIntList();
		
		prop = this.config.get("list", "oreGenBlacklist", true);
		prop.setComment("Set this to false to have a whitelist for ore gen. Default: true");
		this.oreGenBlacklist = prop.getBoolean();
		
		
		if(isServer || config.hasKey("server", "cleanWorldRegistry")) {
			prop = this.config.get("server", "cleanWorldRegistry", false);
			prop.setComment("This will clean all the registry of unused locations. Default: false\n"
						  + "Do not use this unless you know what you are doing!\n"
						  + "This will create a backup of the world!\n"
						  + "This will return to false after server start!");
			if(isServer) {
				this.cleanWorldReg = prop.getBoolean();
				if(this.cleanWorldReg) prop.set(false);
			}else if(prop.getBoolean()) {
				prop.set(false);
			}
		} 
		if(!isServer) {
			this.cleanWorldReg = false;
		}
		
		
		prop = this.config.get("general", "replace", this.defaultReplaceables);
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
					  + "  loot=string - the loot table that will be set to lootable inventories\n"
					  + "  match=string - matches states given, separate states with a comma, only works on equivalent states (NOT IMPLEMENTED)\n"
					  + "More will come in the future");
		this.replaceables = prop.getStringList();
		
		prop = this.config.get("general", "startInv", this.defaultStarting);
		prop.setComment("This list is for adding to the starting inventory\n"
					  + "Each new line is a new item to add the starting inventory\n"
					  + "There is a max of 18 items to prevent inventory overflow\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "Every line needs to be setup as below. nbt, count and meta are optional:\n"
					  + "  unlocalized_block_id:meta count {nbt}");
		this.starting = prop.getStringList();
		
		prop = this.config.get("general", "oreGen", this.defaultOreGen);
		prop.setComment("This list is for overriding / adding to world ore generation\n"
					  + "Each new line is a new ore generation\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state]\n"
					  + "\n"
					  + "Modifies for ore generation\n"
					  + "  replace=unlocalized_block_id[state] - the block to replace. Default: minecraft:stone\n"
					  + "   - You can use ore dictionary: ore:oreName"
					  + "  dimension=int - The dimension that the ore will generate in. Default: any dimesion\n"
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
					  + "  override=boolean - Should override the original ore generation. Default: false\n"
					  + "  \n"
					  + "");
		this.oreGen = prop.getStringList();
		
		if(saveConfig)
			this.config.save();
	}
	
	public final String[] getReplaceables() {
		return this.replaceables;
	}
	
	public String[] getStartingInventory() {
		return this.starting;
	}
	
	public String[] getOreGeneration() {
		return this.oreGen;
	}
	
	public boolean isExtWorldEditEnabled() {
		return this.enableExtenedWorldEdit;
	}
	
	public boolean isReplaceablesEnabled() {
		return this.enableReplaceables;
	}
	
	public boolean isOreGenEnabled() {
		return this.enableOreGen;
	}
	
	public boolean isStartInvEnabled() {
		return this.enableStartInv;
	}
	
	public boolean shouldCleanWorldReg() {
		return this.cleanWorldReg;
	}
	
	public boolean isReplaceablesBlacklist() {
		return replaceablesBlacklist;
	}

	public boolean isOreGenBlacklist() {
		return oreGenBlacklist;
	}

	public int[] getReplaceablesDimensionsList() {
		return replaceablesDimensionsList;
	}
	
	public int[] getOreGenDimensionsList() {
		return oreGenDimensionsList;
	}

}
