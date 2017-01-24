package jplee.worldmanager.gen.ore;

import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;


public class WorldGenVanillaMinable extends WorldGenerator {

	private final IBlockState oreBlock;
	
	private final int minNumberOfBlocks;
	private final int maxNumberOfBlocks;
	private final int flag;
	
	private final Predicate<IBlockState> predicate;
	
	public WorldGenVanillaMinable(IBlockState ore, int minCount, int maxCount, int flag) {
		this(ore, minCount, maxCount, flag, BlockMatcher.forBlock(Blocks.STONE));
	}
	
	public WorldGenVanillaMinable(IBlockState ore, int minCount, int maxCount, int flag, Predicate<IBlockState> testReplaceable) {
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
		float rad = rand.nextFloat() * (float) Math.PI;
		int numberOfBlocks = rand.nextInt(maxNumberOfBlocks - minNumberOfBlocks) + minNumberOfBlocks;
		
        double maxX = (double)((float)(position.getX() + 8) + MathHelper.sin(rad) * (float)numberOfBlocks / 8.0F);
        double minX = (double)((float)(position.getX() + 8) - MathHelper.sin(rad) * (float)numberOfBlocks / 8.0F);
        double maxZ = (double)((float)(position.getZ() + 8) + MathHelper.cos(rad) * (float)numberOfBlocks / 8.0F);
        double minZ = (double)((float)(position.getZ() + 8) - MathHelper.cos(rad) * (float)numberOfBlocks / 8.0F);
        double maxY = (double)(position.getY() + rand.nextInt(3) - 2);
        double minY = (double)(position.getY() + rand.nextInt(3) - 2);

        for(int i1 = 0; i1 < numberOfBlocks; ++i1) {
            float f1 = (float)i1 / (float)numberOfBlocks;
            
            double x1 = maxX + (minX - maxX) * (double)f1;
            double y1 = maxY + (minY - maxY) * (double)f1;
            double z1 = maxZ + (minZ - maxZ) * (double)f1;
            
            double d9 = rand.nextDouble() * (double)numberOfBlocks / 16.0D;
            double d10 = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
            double d11 = (double)(MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0D;
            
            int x2 = MathHelper.floor_double(x1 - d10 / 2.0D);
            int y2 = MathHelper.floor_double(y1 - d11 / 2.0D);
            int z2 = MathHelper.floor_double(z1 - d10 / 2.0D);
            int x3 = MathHelper.floor_double(x1 + d10 / 2.0D);
            int y3 = MathHelper.floor_double(y1 + d11 / 2.0D);
            int z3 = MathHelper.floor_double(z1 + d10 / 2.0D);

            for(int x4 = x2; x4 <= x3; ++x4) {
                double d12 = ((double)x4 + 0.5D - x1) / (d10 / 2.0D);
                if(d12 * d12 < 1.0D) {
                    for(int y4 = y2; y4 <= y3; ++y4) {
                        double d13 = ((double)y4 + 0.5D - y1) / (d11 / 2.0D);
                        if(d12 * d12 + d13 * d13 < 1.0D) {
                            for(int z4 = z2; z4 <= z3; ++z4) {
                                double d14 = ((double)z4 + 0.5D - z1) / (d10 / 2.0D);
                                if(d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    BlockPos blockpos = new BlockPos(x4, y4, z4);
                                    IBlockState state = worldIn.getBlockState(blockpos);
                                    if(state.getBlock().isReplaceableOreGen(state, worldIn, blockpos, this.predicate)) {
                                        worldIn.setBlockState(blockpos, this.oreBlock, this.flag);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
		return true;
	}

}
