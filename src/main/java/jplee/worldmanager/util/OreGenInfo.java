package jplee.worldmanager.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import jplee.jlib.util.InternalDataStructure;
import jplee.jlib.util.Property;
import jplee.worldmanager.WorldManager;
import jplee.worldmanager.gen.WorldGeneration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;


public class OreGenInfo extends InternalDataStructure<Object> {

	public static final Pattern lineFormat = Pattern.compile("^((?:\\w+:)?\\w+(?:\\[(?:(?:\\w+=[\\w*]+|\\*?),?)*\\])?)\\|?((?:(?:\\w+=[\\w:/\\[\\],=.]+\\|?)+))?$");
	public static final Pattern blockPattern = Pattern.compile("((?:\\w+:)?\\w+)(?:\\[((?:(?:\\w+=[\\w*]+|\\*?),?)*)\\])?");
	public static final Pattern propPattern = Pattern.compile("(\\w+)=((?:(?:(?:\\w+:)?\\w+(?:\\[(?:(?:\\w+=(?:\\w+|\\*)|\\*),?)*\\])?|(?:\\+|-)?\\d*\\.\\d+|[\\w]+),?)+)");

	private static final String stringKeys = "type";
	private static final String stringListKeys = "biome";
	private static final String intKeys = "min|max|minSize|maxSize|chance|dimension";
	private static final String boolKeys = "override";
	private static final String blockStateKeys = "replace";
	private static final String unImpKeys = "type|biome|unimplemented";
	private static final String keys = stringKeys + "|" + stringListKeys + "|" + intKeys + "|" + boolKeys + "|" + blockStateKeys;
	
	private static final Map<String,Object> defaultValues = ImmutableMap.<String,Object>builder()
		.put("replace", getBlockStateWrapper(Blocks.STONE.getDefaultState()
			.withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE), true))
		.put("dimension", WorldGeneration.ANY_DIMENSION)
		.put("type", "standard")
		.put("min", 0)
		.put("chance", 2)
		.put("max", 255)
		.put("minSize", 4)
		.put("maxSize", 8)
		.put("override", false)
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
		Matcher match = blockPattern.matcher(part);
		if(match.matches()) {
			this.set(key, new BlockStateWrapper(match.group(1), match.group(2), wildCard));
		} else {
			WorldManager.logger.error("BlockState pattern is improperly writen for %s", part);
		}
	}
	
	protected void setBlockState(String key, IBlockState state, boolean wildCard) {
		this.setBlockState(key, state.toString(), wildCard);
	}

	private static BlockStateWrapper getBlockStateWrapper(IBlockState state, boolean wildCard) {
		Matcher match = blockPattern.matcher(state.toString());
		if(match.matches()) {
			return new BlockStateWrapper(match.group(1), match.group(2), wildCard);
		} else {
			WorldManager.logger.error("BlockState pattern is improperly writen for %s", state.toString());
		}
		return new BlockStateWrapper("minecraft:air", null, wildCard);
	}
	
	@SafeVarargs
	public static OreGenInfo build(IBlockState ore, Property<Object>...properties) {
		OreGenInfo info = new OreGenInfo();
		
		info.setBlockState("ore", ore, false);
		for(Property<Object> prop : properties) {
			if(prop.key.matches(keys) && !info.hasData(prop.key)) {
				if(prop.key.matches(unImpKeys)) {
					WorldManager.logger.warning("Property %s for '%s' has not been implemented, skipping",
						prop.key, ore.toString());
					continue;
				}
				info.set(prop);
			} else {
				WorldManager.logger.error("Property %s was not recognized for %s, skipping", prop.key, ore.toString());
			}
		}
		
		return info;
	}
	
	public static OreGenInfo build(String string) {
		OreGenInfo info = new OreGenInfo();
		
		string.replaceAll("\\s+", "");
		
		if(string.endsWith("|")) {
			WorldManager.logger.error("Formating for '%s' is incorrect", string);
			return null;
		}
		
		Matcher match = lineFormat.matcher(string);
		if(match.matches()) {
			info.setBlockState("ore", match.group(1), false);
			if(match.group(2) != null) {
				Matcher propMatch = propPattern.matcher(match.group(2));
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
							info.set(prop, val);
							continue;
						}
						if(prop.matches(blockStateKeys)) {
							info.setBlockState(prop, val, true);
							continue;
						}
						if(prop.matches(intKeys)) {
							info.set(prop, Integer.parseInt(val));
							continue;
						}
						if(prop.matches(boolKeys)) {
							info.set(prop, Boolean.parseBoolean(val));
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
		info.addMissing();
		return info;
	}

	public OreGenInfo addMissing() {
		for(String key : keys.split("\\|")) {
			if(!this.hasData(key) && !key.matches(unImpKeys)) {
				this.set(key, defaultValues.get(key));
			}
		}
		return this;
	}
	
	@Override
	public String toString() {
		String str = this.getBlockStateWrapper("ore").toString();
		for(Property<Object> prop : this.data.values()) {
			if(prop.key.equals("ore")) {
				continue;
			}
			str += "|" + prop.toString();
		}
		
		return str;
	}
}
