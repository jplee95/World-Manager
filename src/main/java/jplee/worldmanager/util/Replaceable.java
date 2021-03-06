package jplee.worldmanager.util;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableMap;

import jplee.jlib.util.InternalDataStructure;
import jplee.jlib.util.Property;
import jplee.worldmanager.WorldManager;
import jplee.worldmanager.manager.ManagerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class Replaceable extends InternalDataStructure<Object> {

	private static final String stringKeys = "loot";
	private static final String stringListKeys = "match|biomes";
	private static final String intKeys = "min|max|dimension";
	private static final String doubleKeys = "random";
	private static final String blockStateKeys = "replace";
	private static final String untrackedKeys = "block|oredict|usingore";
	private static final String unImpKeys = "match|unimplemented";
	private static final String keys = stringKeys + "|" + stringListKeys + "|" + intKeys + "|" + doubleKeys + "|" + blockStateKeys + "|" + untrackedKeys;
	
	private static final Map<String,Object> defaultValues = ImmutableMap.<String,Object>builder()
		.put("replace", getBlockStateWrapper(Blocks.AIR.getDefaultState(), false))
		.put("dimension", ManagerUtil.ANY_DIMENSION)
		.put("min", 0)
		.put("max", 255)
		.put("random", 1.0d)
		.put("match", "")
		.put("loot", "")
		.put("oredict", "")
		.put("biomes", new String[0])
		.put("usingore", false)
		.build();
	
	public IBlockState getBlockState(String key) {
		return ((BlockStateWrapper) this.get(key)).getBlockState();
	}
	
	public Block getBlockFromBlockState(String key) {
		return ((BlockStateWrapper) this.get(key)).getBlock();
	}

	protected BlockStateWrapper getBlockStateWrapper(String key) {
		return (BlockStateWrapper) this.get(key);
	}
	
	public boolean isAdequateState(String key, IBlockState state) {
		return ((BlockStateWrapper) this.get(key)).isAdequateState(state);
	}

	protected void setBlockState(String key, String part, boolean wildCard) {
		Matcher match = MatchPatterns.blockPattern.matcher(part);
		if(match.matches()) {
			this.set(key, new BlockStateWrapper(match.group(1), match.group(2), wildCard));
		} else {
			WorldManager.logger.error("BlockState pattern is improperly writen for %s", part);
		}
	}
	
	private static BlockStateWrapper getBlockStateWrapper(IBlockState state, boolean wildCard) {
		Matcher match = MatchPatterns.blockPattern.matcher(state.toString());
		if(match.matches()) {
			return new BlockStateWrapper(match.group(1), match.group(2), wildCard);
		} else {
			WorldManager.logger.error("BlockState pattern is improperly writen for %s", state.toString());
		}
		return new BlockStateWrapper("minecraft:air", null, wildCard);
	}
	
	protected void setBlockState(String key, IBlockState state, boolean wildCard) {
		this.setBlockState(key, state.toString(), wildCard);
	}
	
	protected void addOreDictionary(String oreDictionary) {
		this.set("usingore", true);
		this.set("oredict", oreDictionary);
	}

	@SafeVarargs
	public static Replaceable build(String replaceable, boolean wildCard, Property<Object>...properties) {
		Replaceable rep = new Replaceable();
		
		rep.setBlockState("block", replaceable, wildCard);
		for(Property<Object> prop : properties) {
			if(prop.key.matches(keys) && !rep.hasData(prop.key)) {
				if(prop.key.matches(unImpKeys)) {
					WorldManager.logger.warning("Property %s for '%s' has not been implemented, skipping",
						prop.key, replaceable);
					continue;
				}
				rep.set(prop);
			} else {
				WorldManager.logger.error("Property %s was not recognized for %s, skipping", prop.key, replaceable);
			}
		}
		
		return rep;
	}
	
	public static Replaceable build(String string) {
		Replaceable replaceable = new Replaceable();
		
		string.replaceAll("\\s+", "");

		if(string.endsWith("|")) {
			WorldManager.logger.error("Formating for '%s' is incorrect", string);
			return null;
		}

		Matcher match = MatchPatterns.lineFormat.matcher(string);
		if(match.matches()) {
			if(match.group(1).startsWith("ore:")) {
				replaceable.addOreDictionary(match.group(1).substring(4));
			} else {
				replaceable.setBlockState("block", match.group(1), true);
			}
			if(match.group(2) != null) {
				Matcher propMatch = MatchPatterns.propPattern.matcher(match.group(2));
				while(propMatch.find()) {
					String prop = propMatch.group(1);
					String val = propMatch.group(2);
					
					try {
						if(prop.matches(unImpKeys)) {
							WorldManager.logger.warning("Property %s in '%s' has not been implemented, skipping",
								match.group(1), string);
							continue;
						}
						if(prop.matches(stringKeys)) {
							replaceable.set(prop, val);
							continue;
						}
						if(prop.matches(stringListKeys)) {
							String[] vals = val.split(",");
							Arrays.asList(vals).forEach(str -> str.trim());
							replaceable.set(prop, vals);
							continue;
						}
						if(prop.matches(blockStateKeys)) {
							replaceable.setBlockState(prop, val.replaceAll("\\[\\]", "\\*"), false);
							continue;
						}
						if(prop.matches(intKeys)) {
							replaceable.set(prop, Integer.parseInt(val));
							continue;
						}
						if(prop.matches(doubleKeys)) {
							replaceable.set(prop, Double.parseDouble(val));
							continue;
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					WorldManager.logger.error("Property %s was not recognized in '%s', skipping", prop, string);
				}
			}
		} else {
			WorldManager.logger.error("Formating for '%s' is incorrect", string);
			return null;
		}
		
		replaceable.addMissing();
		return replaceable;
	}
	
	public Replaceable addMissing() {
		for(String key : keys.split("\\|")) {
			if(!this.hasData(key) && !key.matches(unImpKeys)) {
				this.set(key, defaultValues.get(key));
			}
		}
		return this;
	}

	@Override
	public String toString() {
		String str = "";
		if(this.hasData("block") || hasData("usingore"))
			if(this.getBoolean("usingore")) {
				str = "ore:" + this.getString("oredict");
			} else {
				str = this.getBlockStateWrapper("block").toString();
			}
		else
			str = "invalid";
		
		for(Property<Object> prop : this.data.values()) {
			if(prop.key.matches(untrackedKeys)) {
				continue;
			}
			str += "|" + prop.toString();
		}
		
		return str;
	}
}
