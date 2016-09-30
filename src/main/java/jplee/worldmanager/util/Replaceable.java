package jplee.worldmanager.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class Replaceable {

	private final Map<String,Property> properties;

	private static final Pattern blockPattern = Pattern.compile("(\\w+\\:\\w+)(?:\\[([\\w=,*]+)\\])?");
	private static final Pattern statePattern = Pattern.compile("(\\w+)\\=([\\w*]+)");
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
	
	public static Replaceable build(IBlockState replaceable, boolean wildCard, Property...properties) {
		Replaceable rep = new Replaceable();
		rep.addBlockState("", replaceable, wildCard);
		for(Property prop : properties) {
			if(!rep.hasProperty(prop.key)) {
				rep.addProperty(prop);
			}
		}
		checkAndInsertMissing(rep);
		return rep;
	}
	
	public static Replaceable build(String string) {
		Replaceable replaceable = new Replaceable();
		boolean once = false;
		boolean first = true;
		
		String[] parts = string.split("\\|");
		Matcher matcher = blockPattern.matcher(parts[0]);
		if(matcher.matches()) {
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
					if(match.group(1).equals("oredict")) {
						replaceable.addBoolean("oredict", Boolean.parseBoolean(match.group(2)));
						WorldManager.warning("Property %s in %s has not been implemented", match.group(1), string);
						continue;
					}
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
						WorldManager.warning("Property %s in %s has not been implemented", match.group(1), string);
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
		checkAndInsertMissing(replaceable);
		
		return replaceable;
	}
	
	
	
	private static void checkAndInsertMissing(Replaceable replaceable) {
		if(!replaceable.hasProperty("random")) {
			replaceable.addNumber("random", 1.0);
		}
		if(!replaceable.hasProperty("replace")) {
			replaceable.addBlockState("replace", Blocks.AIR.getDefaultState(), false);
		}
		if(!replaceable.hasProperty("oredict")) {
			replaceable.addBoolean("oredict", false);
		}
		if(!replaceable.hasProperty("min")) {
			replaceable.addNumber("min", -1);
		}
		if(!replaceable.hasProperty("max")) {
			replaceable.addNumber("max", -1);
		}
	}
	
	public static class Property<T extends Object> {
		
		public final T object;
		public final String key;
		
		public Property(String key, T object) {
			this.object = object;
			this.key = key;
		}
	}
	
	protected class BlockStateWrapper {

		private Block block;
		private IBlockState blockState;
		private Map<IBlockState,Boolean> allowedStates;
		private boolean wildCard;
		
		public BlockStateWrapper(@Nonnull String block, @Nullable String states, boolean wildCard) {
			this.wildCard = wildCard;
			this.block = Block.REGISTRY.getObject(new ResourceLocation(block));
			Map<String,String> setStates = Maps.<String,String>newHashMap();
			this.allowedStates = Maps.<IBlockState,Boolean>newHashMap();
			Matcher matcher = statePattern.matcher(this.block.getDefaultState().toString());
			if(states == null) {
				if(!wildCard) {
					this.blockState = this.block.getDefaultState();
				}
				while(matcher.find()) {
					setStates.put(matcher.group(1), matcher.group(2));
				}
			} else if(states.equals("*")) {
				if(!wildCard) {
					WorldManager.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
					this.blockState = this.block.getDefaultState();
				}
				while(matcher.find()) {
					setStates.put(matcher.group(1), wildCard ? "*" : matcher.group(2));
				}
			} else {
				Matcher stateMatch = statePattern.matcher(states);
				while(matcher.find()) {
					while(stateMatch.find()) {
						if(!wildCard && stateMatch.group(2).equals("*")) {
							WorldManager.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
							this.blockState = this.block.getDefaultState();
						}
						if(matcher.group(1).equals(stateMatch.group(1))) {
							setStates.put(matcher.group(1), stateMatch.group(2));
						} else {
							setStates.put(matcher.group(1), (wildCard ? "*" : matcher.group(2)));
						}
					}
					stateMatch.reset();
				}
			}
			
			for(IBlockState state : this.block.getBlockState().getValidStates()) {
				String stringState = state.toString().replaceAll("[\\w:]+\\[|\\]", "");
				Matcher match = statePattern.matcher(stringState);
				
				int count = 0;
				while(match.find()) {
					String st = setStates.get(match.group(1));
					if(st != null) {
//						WorldManager.info("  " + stringState + " (" + match.group(2) + "==" + st + ") " + (st.equals(match.group(2)) || st.equals("*")) + " " + count, new Object[0]); //===============================================
						if(st.equals(match.group(2)) || (st.equals("*") && wildCard)) {
							count++;
						}
					}
				}
				if(count == setStates.size()) {
					if(!wildCard) {
						this.blockState = state;
//						WorldManager.info("===== Found state: " + state + " =====", new Object[0]);
						break;
					} else {
						this.allowedStates.put(state, count == setStates.size());
//						WorldManager.info("===== Found state: " + state + " =====", new Object[0]);
					}
				}
			}
		}
		
 		public boolean isAdequateState(IBlockState state) {
			if(!this.wildCard) {
				if(state == null && this.blockState == null) return true;
				if(state != this.blockState) return false;
				if(state == this.blockState) return true;
				return state.toString().equals(this.blockState.toString());
			} else if(state != null) {
				Boolean test = this.allowedStates.get(state);
				return test == null ? false : test;
			}
			return false;
		}
		
		public boolean canReturnState() {
			return !this.wildCard;
		}
		
		public final IBlockState getBlockState() {
			if(this.canReturnState()) {
				return blockState;
			}
			return null;
		}
		
		public final Block getBlock() {
			return this.block;
		}
	}
	
	@Override
	public String toString() {
		BlockStateWrapper wrapper = this.getPropertyAsBlockStateWrapper("block");
		return super.toString() + "|" + wrapper.block.toString();
	}
}
