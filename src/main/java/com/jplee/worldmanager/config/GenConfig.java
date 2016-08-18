package com.jplee.worldmanager.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class GenConfig {
	
	private Configuration config;
	
	private String[] replaceables;
	
	public GenConfig(File file) {
		config = new Configuration(file);
		this.genConfig();
	}
	
	public void genConfig() {
		config.load();
		Property prop;
		
		prop = config.get("general", "replace", new String[]{ });
		prop.setComment("This list is used for replacing generated blocks\n"
					  + "Each new line will create a new entry for the replacement process\n"
					  + "you can comment out lines with '#' if needed\n"
					  + "The setup is separated with '|' and must start with:\n"
					  + "  unlocalized_block_id[state] - The block to be replaced\n"
					  + "  The state can be set to '*' to wild card it or have part a state equal to it to wild card\n\n"
					  + "Extra modifiers for changing how it replaces\n"
					  + "  replace=unlocalized_block_id[state] - the block to replace the other block. Not having this will just replace it with air\n"
					  + "  random=double - The chance for it to succeed. Value between 0 and 1\n"
					  + "  dimension=int - The dimension that this will happen in\n"
//					  + "  oredict=boolean - should use ore dictionary for this replace (NOT IMPLEMENTED YET) \n"
					  + "\nMore will come in the future");
		replaceables = prop.getStringList();
		
		config.save();
	}
	
	public void reloadConfig() {
		config.load();
		Property prop;

		prop = config.get("replace", "general", new String[]{ });
		this.replaceables = prop.getStringList();
	}
	
	public final String[] getReplaceables() {
		return replaceables;
	}
}
