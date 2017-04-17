package jplee.worldmanager.util;

import java.util.Map;
import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

/** Useful utility for wrapping a blockstate and testing for equivalent states */
public class BlockStateWrapper {
	
	private Block block;
	private IBlockState blockState;
	private Map<IBlockState,Boolean> allowedStates;
	private boolean wildCard;

	private final String keyBlockString;
	
	/**
	 * 
	 *  @param block - The string form of the block.
	 *  @param states - The states of the block that is addressed 
	 *  @param wildCard - allows wild carding of the blockstate
	 *  */
	public BlockStateWrapper(@Nonnull String block, @Nullable String states, boolean wildCard) {
		this.block = Block.REGISTRY.getObject(new ResourceLocation(block));
		this.wildCard = wildCard;
		allowedStates = Maps.newHashMap();
		Map<String,String> setStates = Maps.newHashMap();
		
		boolean defaultState = states == null || states.isEmpty();
		boolean matchAll = states != null && states == "*";
		
		String defaultStateString = "";
		if(defaultState) {
			Matcher match = MatchPatterns.blockPattern.matcher(this.block.getDefaultState().toString());
			if(match.matches()) {
				defaultStateString = match.group(2);
				if(defaultStateString == null) {
					defaultStateString = "";
				}
			}
		}
		keyBlockString = Block.REGISTRY.getNameForObject(this.block) + "[" + (defaultState ? defaultStateString : states) + "]";

		Matcher matcher = MatchPatterns.statePattern.matcher(this.block.getDefaultState().toString());
		if(defaultState) {
			if(!wildCard)
				this.blockState = this.block.getDefaultState();
			while(matcher.find())
				setStates.put(matcher.group(1), matcher.group(2));
		} else if(matchAll) {
			if(!wildCard) {
				WorldManager.logger.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
				this.blockState = this.block.getDefaultState();
			}
			while(matcher.find())
				setStates.put(matcher.group(1), wildCard ? "*" : matcher.group(2));
		} else {
			Matcher stateMatch = MatchPatterns.statePattern.matcher(states);
			while(matcher.find()) {
				while(stateMatch.find()) {
					if(!wildCard && stateMatch.group(2).equals("*")) {
						WorldManager.logger.error("A state has been wildcarded and should not have been, Setting to default state", new Object[0]);
						this.blockState = this.block.getDefaultState();
					}
					if(matcher.group(1).equals(stateMatch.group(1)))
						setStates.put(matcher.group(1), stateMatch.group(2));
					else
						setStates.put(matcher.group(1), (wildCard ? "*" : matcher.group(2)));
				}
				stateMatch.reset();
			}
		}
		
		for(IBlockState state : this.block.getBlockState().getValidStates()) {
			String stringState = state.toString().replaceAll("[\\w:]+\\[|\\]", "");
			Matcher match = MatchPatterns.statePattern.matcher(stringState);
			
			int count = 0;
			while(match.find()) {
				String st = setStates.get(match.group(1));
				if(st != null) {
					if(wildCard)
						for(String part : st.split("&")) {
							if(part.equals(match.group(2)) || part.startsWith("!")
								&& !part.substring(1).equals(match.group(2)) || st.equals("*")) {
								count++;
							}
						}
					else {
						if(st.contains("&") || st.contains("!"))
							WorldManager.logger.error("A state has a wildcard notation and should not have been, ignoring", new Object[0]);
						if(st.equals(match.group(2)))
							count++;
					}
						
				}
			}
			if(!wildCard && count == setStates.size()) {
				this.blockState = state;
				WorldManager.logger.debug("===== Found state: " + state + " =====", new Object[0]);
				break;
			} else {
				this.allowedStates.put(state, count == setStates.size());
				WorldManager.logger.debug("===== Found state: " + state + " =====", new Object[0]);
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
