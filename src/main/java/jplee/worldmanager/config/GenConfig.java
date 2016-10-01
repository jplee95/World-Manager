package jplee.worldmanager.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class GenConfig {
	
	private Configuration config;
	
	private boolean enableReplaceables;
	private boolean enableOreGen;
	
	private String[] replaceables;
	private String[] starting;
	private int maxProcesses;
	
	private final String[] defaultReplaceables = {
		"# minecraft:oak_stiars[*]",
		"# minecraft:furnace[facing=north]|replace=minecraft:cobblestone"
	};
	private final String[] defaultStarting = {
		
	};
	
	public GenConfig(File file) {
		config = new Configuration(file);
		this.genConfig();
	}
	
	public void genConfig() {
		config.load();
		Property prop;
		
		prop = config.get("enable", "replace", false);
		prop.setComment("Enable block replacement, Default: false");
		enableReplaceables = prop.getBoolean();

		prop = config.get("enable", "oreGen", false);
		prop.setComment("Enable ore generation overrides, Default: false");
		enableOreGen = prop.getBoolean();

		prop = config.get("enable", "startInv", false);
		prop.setComment("Enable starting inventory, Default: false");
		enableOreGen = prop.getBoolean();

		prop = config.get("general", "replace", defaultReplaceables);
		prop.setComment("This list is used for replacing generated blocks\n"
					  + "Each new line will create a new entry for the replacement process\n"
					  + "All entries are sorted by their random value from smallest to greatest\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state] - The block to be replaced\n"
					  + "  Any part of the state for this can be set to '*' to wild card it\n"
					  + "\n"
					  + "Extra modifiers for changing how it replaces\n"
					  + "  replace=unlocalized_block_id[state] - the block to replace the other block. Not having this will just replace it with air\n"
					  + "  random=double - The chance for it to succeed. Value between 0.0 and 1.0, defaults to 1.0 if not given\n"
					  + "  dimension=int - The dimension that this will happen in, defaults to any dimension\n"
					  + "  min=int - the minimum height that it will replace at, inclusive\n"
					  + "  max=int - the maximum height that it will replace at, inclusive\n"
//					  + "  oredict=boolean - should use ore dictionary for this replace (NOT IMPLEMENTED YET)\n"
//					  + "  loot=string - the loot table that will be set to lootable inventories (NOT IMPLEMENTED YET)\n"
//					  + "  match=string - matches states given, separate states with a camma, only works on equivalent states (NOT IMPLEMENTED YET)\n"
					  + "More will come in the future");
		replaceables = prop.getStringList();
		
		prop = config.get("general", "startInv", defaultStarting);
		prop.setComment("This list is for adding to the starting inventory\n"
					  + "Each new line is a new item to add the starting inventory\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "Every line needs to be setup as below, nbt and meta are optional:\n"
					  + "  unlocalized_block_id:meta count {nbt}");
		starting = prop.getStringList();
		
		
//		prop = config.get("general", "maxProcesses", -1);
//		prop.setComment("The maximum amount of chunks that can be processed in one tick, set to -1 for no limit\n"
//					  + "range:-1 to 256, default:-1\n"
//					  + "EXPERAMENTAL");
//		prop.setMinValue(-1);
//		prop.setMaxValue(256);
//		prop.requiresMcRestart();
//		maxProcesses = prop.getInt();
		
		config.save();
	}
	
	public void reloadConfig() {
		config.load();
		Property prop;

		prop = config.get("general", "replace", new String[]{ });
		this.replaceables = prop.getStringList();
	}
	
	public final String[] getReplaceables() {
		return replaceables;
	}
	
	public int getMaxProcesses() {
		return -1;
	}

	public boolean isReplaceablesEnabled() {
		return enableReplaceables;
	}

	public boolean isOreGenEnabled() {
		return enableOreGen;
	}

	public String[] getStartingInventory() {
		return starting;
	}
}
