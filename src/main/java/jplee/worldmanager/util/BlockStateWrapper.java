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
import net.minecraft.util.ResourceLocation;

/** Useful utility for wrapping a blockstate and testing for equivalent states */
public class BlockStateWrapper {
	
	/** The pattern for states */
	public static final Pattern statePattern = Pattern.compile("(\\w+)\\=([\\w*]+)");
	private static final Pattern blockPattern = Pattern.compile("((?:\\w+:)?\\w+)(?:\\[((?:(?:\\w+=[\\w*]+|\\*?),?)*)\\])?");
	
	private Block block;
	private IBlockState blockState;
	private Map<IBlockState,Boolean> allowedStates;
	@SuppressWarnings("unused")
	private Map<String,Boolean> oreDictionaried;
	private boolean wildCard;

	private final String keyBlockString;
	
	/**
	 * 
	 *  @param block - The string form of the block.
	 *  @param states - The states of the block that is addressed 
	 *  @param wildCard - allows wild carding of the blockstate
	 *  */
	public BlockStateWrapper(@Nonnull String block, @Nullable String states, boolean wildCard) {
		this.wildCard = wildCard;
		this.block = Block.REGISTRY.getObject(new ResourceLocation(block));
		Matcher matchBlock = blockPattern.matcher(this.block.getDefaultState().toString());
		keyBlockString = Block.REGISTRY.getNameForObject(this.block) + "[" + 
			(states == null || states.isEmpty() ? (matchBlock.matches() ? matchBlock.group(2) : " ") : states) + "]";
		Map<String,String> setStates = Maps.<String,String>newHashMap();
		this.allowedStates = Maps.<IBlockState,Boolean>newHashMap();
		Matcher matcher = statePattern.matcher(this.block.getDefaultState().toString());
		if(states == null || states.isEmpty()) {
			if(!wildCard) {
				this.blockState = this.block.getDefaultState();
			}
			while(matcher.find()) {
				setStates.put(matcher.group(1), matcher.group(2));
			}
		} else if(states.equals("*")) {
			if(!wildCard) {
				WorldManager.logger.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
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
						WorldManager.logger.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
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
					WorldManager.logger.debug("  " + stringState + " (" + match.group(2) + "==" + st + ") " + (st.equals(match.group(2)) || st.equals("*")) + " " + count, new Object[0]);
					if(st.equals(match.group(2)) || (st.equals("*") && wildCard)) {
						count++;
					}
				}
			}
			if(count == setStates.size()) {
				if(!wildCard) {
					this.blockState = state;
					WorldManager.logger.debug("===== Found state: " + state + " =====", new Object[0]);
					break;
				} else {
					this.allowedStates.put(state, count == setStates.size());
					WorldManager.logger.debug("===== Found state: " + state + " =====", new Object[0]);
				}
			}
		}
	}
	
	/**
	 * Test whether the states are equal or from the same block, 
	 * it will only test for the same block (includes all states)
	 * if the wrapper is wild carded
	 * @param state - The state to be tested
	 **/
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

	/**
	 * Tells whether you can pull a state from the wrapper,
	 * this will return true if the wrapper is not wild carded
	 * @return Returns a boolean for returnable state.
	 **/
	public boolean canReturnState() {
		return !this.wildCard;
	}

	/**
	 * Returns the associated blockstate from the wrapper,
	 * this will only return a state if the wrapper is not wild carded
	 * @return Returns the associated blockstate 
	 **/
	public final IBlockState getBlockState() {
		if(this.canReturnState()) {
			return blockState;
		}
		return null;
	}

	/**
	 * Returns the associated block from the wrapper
	 * @return Returns the block 
	 **/
	public final Block getBlock() {
		return this.block;
	}
	
	@Override
	public String toString() {
		return keyBlockString;
	}
}
