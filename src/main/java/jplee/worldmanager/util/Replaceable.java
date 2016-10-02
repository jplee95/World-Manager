package jplee.worldmanager.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class Replaceable {

	private final Map<String,Property> properties;

	private static final Pattern blockPattern = Pattern.compile("(\\w+\\:\\w+)(?:\\[([\\w=,*]+)\\])?");
	private static final Pattern propPattern = Pattern.compile("(\\w+){1}\\=(.+){1}");
	
	private Replaceable() {
		this.properties = Maps.newHashMap();
	}
	
	public Object getProperty(String key) {
		if(hasProperty(key))
			return properties.get(key).object;
		return null;
	}
	
	public int getPropertyAsInt(String key) {
		return (Integer) getProperty(key);
	}
	
	public float getPropertyAsFloat(String key) {
		return (Float) getProperty(key);
	}

	public double getPropertyAsDouble(String key) {
		return (Double) getProperty(key);
	}
	
	public long getPropertyAsLong(String key) {
		return (Long) getProperty(key);
	}
	
	public boolean getPropertyAsBoolean(String key) {
		return (Boolean) getProperty(key);
	}
	
	public String getPropertyAsString(String key) {
		return (String) getProperty(key);
	}
	
	public IBlockState getPropertyAsBlockState(String key) {
		return ((BlockStateWrapper) getProperty(key)).getBlockState();
	}
	
	public Block getBlockFromBlockStateProperty(String key) {
		return ((BlockStateWrapper) getProperty(key)).getBlock();
	}

	protected BlockStateWrapper getPropertyAsBlockStateWrapper(String key) {
		return (BlockStateWrapper) getProperty(key);
	}
	
	public boolean isAdequateState(String key, IBlockState state) {
		return ((BlockStateWrapper) getProperty(key)).isAdequateState(state);
	}
	
	public boolean hasProperty(String key) {
		return properties.get(key) != null;
	}
	
	protected void addProperty(Property prop) {
		this.properties.put(prop.key, prop);
	}
	
	protected void addBlockState(String key, String part, boolean wildCard) {
		Matcher match = blockPattern.matcher(part);
		if(match.matches()) {
			addProperty(new Property<BlockStateWrapper>(key.isEmpty() ? "block" : key, new BlockStateWrapper(match.group(1), match.group(2), wildCard)));
		} else {
			WorldManager.error("BlockState pattern is improperly writen " + part, new Object[0]);
		}
	}
	
	protected void addBlockState(String key, IBlockState state, boolean wildCard) {
		this.addBlockState(key, state.toString(), wildCard);
	}
	
	protected <T extends Number> void addNumber(String key, T number) {
		addProperty(new Property<T>(key, number));
	}
	
	protected void addString(String key, String string) {
		addProperty(new Property<String>(key, string));
	}
	
	protected void addBoolean(String key, boolean bool) {
		addProperty(new Property<Boolean>(key, bool));
	}

	protected void addOreDictionary(String oreDictionary) {
		this.addBoolean("usingore", true);
		this.addString("oredict", oreDictionary);
	}

	public static Replaceable build(IBlockState replaceable, boolean wildCard, Property...properties) {
		Replaceable rep = new Replaceable();
		rep.addBlockState("", replaceable, wildCard);
		for(Property prop : properties) {
			if(!rep.hasProperty(prop.key)) {
				rep.addProperty(prop);
			}
		}
		return rep;
	}
	
	public static Replaceable build(String string) {
		Replaceable replaceable = new Replaceable();
		boolean once = false;
		boolean first = true;
		
		String[] parts = string.split("\\|");
		Matcher matcher = blockPattern.matcher(parts[0]);
		if(matcher.matches()) {
			if(parts[0].startsWith("ore:"))
				replaceable.addOreDictionary(parts[0].substring(4));
			else 
				replaceable.addBlockState("", parts[0], true);
			
			once = !once;
		}
		if(parts.length > 1) {
			for(String part : parts) {
				if(!part.contains("=") && !first || !once) {
					WorldManager.error("The replaceable block was not set or was set more than once for %s", string);
					break;
				}
				Matcher match = propPattern.matcher(part);
				if(match.matches()) {
					if(match.group(1).equals("replace")) {
						replaceable.addBlockState("replace", match.group(2), false);
						continue;
					}
					if(match.group(1).equals("random")) {
						replaceable.addNumber("random", Math.min(Math.max(Double.parseDouble(match.group(2)),0),1));
						continue;
					} 
//					if(match.group(1).equals("oredict")) {
//						replaceable.addBoolean("oredict", Boolean.parseBoolean(match.group(2)));
//						((BlockStateWrapper) replaceable.getProperty("block")).useOreDictionary();
//						WorldManager.warning("Property %s in %s has not been implemented", match.group(1), string);
//						continue;
//					}
					if(match.group(1).equals("dimension")) {
						replaceable.addNumber("dimension", Integer.parseInt(match.group(2)));
						continue;
					}
					if(match.group(1).equals("max")) {
						replaceable.addNumber("max", Integer.parseInt(match.group(2)));
						continue;
					}
					if(match.group(1).equals("min")) {
						replaceable.addNumber("min", Integer.parseInt(match.group(2)));
						continue;
					}
					if(match.group(1).equals("loot")) {
						replaceable.addString("loot", match.group(2));
//						WorldManager.warning("Property %s in %s has not been implemented", match.group(1), string);
						continue;
					}
					if(match.group(1).equals("match")) {
						replaceable.addString("match", match.group(2));
						WorldManager.warning("Property %s in %s has not been implemented", match.group(1), string);
						continue;
					}
					WorldManager.error("Property %s was not recognized in %s", match.group(1), string);
				} else if(!first) {
					WorldManager.error("Property patern is improperly writen for %s", string);
					break;
				}
				first = false;
			}
		}
		replaceable.addMissing();
		
		return replaceable;
	}
	
	public Replaceable addMissing() {
		if(!this.hasProperty("random")) {
			this.addNumber("random", 1.0);
		}
		if(!this.hasProperty("replace")) {
			this.addBlockState("replace", Blocks.AIR.getDefaultState(), false);
		}
		if(!this.hasProperty("oredict")) {
			this.addString("oredict", "");
		}
		if(!this.hasProperty("loot")) {
			this.addString("loot", "");
		}
		if(!this.hasProperty("usingore")) {
			this.addBoolean("usingore", false);
		}
		if(!this.hasProperty("min")) {
			this.addNumber("min", -1);
		}
		if(!this.hasProperty("max")) {
			this.addNumber("max", -1);
		}
		return this;
	}
	
	public static class Property<T extends Object> {
		
		public final T object;
		public final String key;
		
		public Property(String key, T object) {
			this.object = object;
			this.key = key;
		}
	}
	
	@Override
	public String toString() {
		BlockStateWrapper wrapper = this.getPropertyAsBlockStateWrapper("block");
		boolean usingOre = this.getPropertyAsBoolean("usingore");
		return this.getClass().getName() + "|" + (usingOre ? this.getPropertyAsString("oredict") : wrapper.getBlock().toString());
	}
}
