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
import net.minecraft.item.ItemStack;


public class BlockProperty extends InternalDataStructure<Object> {

	private static final String stringKeys = "";
	private static final String stringListKeys = "";
	private static final String itemKeys = "drop";
	private static final String intKeys = "dimension|strength";
	private static final String doubleKeys = "hold";
	private static final String boolKeys = "gravity|silktouch";
	private static final String untrackedKeys = "block|oredict|usingore";
	private static final String unImpKeys = "strength|unimplemented";
	private static final String keys = stringKeys + "|" + stringListKeys + "|" + itemKeys + "|" + intKeys + "|" + doubleKeys + "|" + boolKeys + "|" + untrackedKeys;

	private static final Map<String,Object> defaultValues = ImmutableMap.<String,Object>builder()
		.put("hold", 0.0)
		.put("gravity", false)
		.put("oredict", "")
		.put("usingore", false)
		.put("drop", new ItemStack(Blocks.AIR))
		.put("silktouch", true)
		.put("dimension", ManagerUtil.ANY_DIMENSION)
		.put("strength", 0)
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
	
	protected static BlockStateWrapper getBlockStateWrapper(IBlockState state, boolean wildCard) {
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
	public static BlockProperty build(String block, boolean wildCard, Property<Object>...properties) {
		BlockProperty property = new BlockProperty();
		
		property.setBlockState("block", block, wildCard);
		for(Property<Object> prop :properties) {
			if(prop.key.matches(keys) && !property.hasData(prop.key)) {
				if(prop.key.matches(unImpKeys)) {
					WorldManager.logger.warning("Property %s for '%s' has not been implemented, skipping",
						prop.key, block);
					continue;
				}
				property.set(prop);
			} else {
				WorldManager.logger.error("Property %s was not recognized for %s, skipping", prop.key, block);
			}
		}
		return property;
	}
	
	public static BlockProperty build(String string) {
		BlockProperty property = new BlockProperty();

		if(string.endsWith("|")) {
			WorldManager.logger.error("Formating for '%s' is incorrect", string);
			return null;
		}

		Matcher match = MatchPatterns.lineFormat.matcher(string);
		if(match.matches()) {
			if(match.group(1).startsWith("ore:")) {
				property.addOreDictionary(match.group(1).replaceAll("\\s+", "").substring(4));
			} else {
				property.setBlockState("block", match.group(1).replaceAll("\\s+", ""), true);
			}
			if(match.group(2) != null) {
				String value = match.group(2).substring(1);
				Matcher propMatch = MatchPatterns.propPattern.matcher(value);
				while(propMatch.find()) {
					String prop = propMatch.group(1).replaceAll("\\s+", "");
					String prestr = propMatch.group(2);
					String val = prestr.trim();
					
					try {
						if(prop.matches(unImpKeys)) {
							WorldManager.logger.warning("Property %s in '%s' has not been implemented, skipping",
								prop, string);
							continue;
						}
						if(prop.matches(stringKeys)) {
							property.set(prop, val);
							continue;
						}
						if(prop.matches(stringListKeys)) {
							String[] vals = val.replaceAll("\\s+", "").split(",");
							Arrays.asList(vals).forEach(str -> str.trim());
							property.set(prop, vals);
							continue;
						}
						if(prop.matches(itemKeys)) {
							ItemStack stack = ItemUtils.parsItem(val);
							property.set(prop, stack);
							continue;
						}
						if(prop.matches(intKeys)) {
							if(prop.equals("strength"))
								property.set(prop, Math.max(0, Math.min(16, Integer.parseInt(val))));
							else	
								property.set(prop, Integer.parseInt(val));
							continue;
						}
						if(prop.matches(doubleKeys)) {
							property.set(prop, Double.parseDouble(val));
							continue;
						}
						if(prop.matches(boolKeys)) {
							property.set(prop, Boolean.parseBoolean(val));
							continue;
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					WorldManager.logger.error("Property %s was not recognized in '%s', skipping", prop, string);
				}
			}
		}
		
		property.addMissing();
		return property;
	}
	
	public BlockProperty addMissing() {
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
				str = String.valueOf(this.getBlockStateWrapper("block"));
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
