package jplee.worldmanager.gen.ore;

import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;


public class WorldGenScateredMinable extends WorldGenerator {

	private final IBlockState oreBlock;
	
	private final int minNumberOfBlocks;
	private final int maxNumberOfBlocks;
	private final int flag;
	
	private final Predicate<IBlockState> predicate;
	
	public WorldGenScateredMinable(IBlockState ore, int minCount, int maxCount, int flag) {
		this(ore, minCount, maxCount, flag, BlockMatcher.forBlock(Blocks.STONE));
	}
	
	public WorldGenScateredMinable(IBlockState ore, int minCount, int maxCount, int flag, Predicate<IBlockState> testReplaceable) {
		this.oreBlock = ore;
		
		if(minCount > maxCount) {
			int i = minCount;
			minCount = maxCount;
			maxCount = i;
		}
		
		this.minNumberOfBlocks = minCount;
		this.maxNumberOfBlocks = maxCount;
		this.flag = flag;
		
		this.predicate = testReplaceable;
	}
	
	@Override
	public boolean generate(World worldIn, Random rand, BlockPos position) {
		// TODO Auto-generated method stub
		return false;
	}

}
