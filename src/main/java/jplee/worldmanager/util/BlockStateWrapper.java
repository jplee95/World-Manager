package jplee.worldmanager.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import jplee.worldmanager.WorldManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class BlockStateWrapper {
	private static final Pattern statePattern = Pattern.compile("(\\w+)\\=([\\w*]+)");
	
	private Block block;
	private IBlockState blockState;
	private Map<IBlockState,Boolean> allowedStates;
	private Map<String,Boolean> oreDictionaried;
	private boolean wildCard;
	private boolean oreDict;
	
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
					WorldManager.debug("  " + stringState + " (" + match.group(2) + "==" + st + ") " + (st.equals(match.group(2)) || st.equals("*")) + " " + count, new Object[0]);
					if(st.equals(match.group(2)) || (st.equals("*") && wildCard)) {
						count++;
					}
				}
			}
			if(count == setStates.size()) {
				if(!wildCard) {
					this.blockState = state;
					WorldManager.debug("===== Found state: " + state + " =====", new Object[0]);
					break;
				} else {
					this.allowedStates.put(state, count == setStates.size());
					WorldManager.debug("===== Found state: " + state + " =====", new Object[0]);
				}
			}
		}
	}
	
	public boolean isAdequateState(IBlockState state) {
		if(oreDict) {
			ItemStack stack = new ItemStack(state.getBlock(), 1);
			Boolean found = false;
			for(int id : OreDictionary.getOreIDs(stack)) {
				found = oreDictionaried.get(OreDictionary.getOreName(id));
			}
			if(found != null) {
				return found;
			}
		}
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
	
	public void useOreDictionary() {
		this.oreDict = true;
		oreDictionaried = Maps.newHashMap();
		ItemStack stack = new ItemStack(block, 1);
		for(int id : OreDictionary.getOreIDs(stack)) {
			oreDictionaried.put(OreDictionary.getOreName(id), true);
		}
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
