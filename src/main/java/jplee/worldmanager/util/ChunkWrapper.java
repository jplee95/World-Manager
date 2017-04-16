package jplee.worldmanager.util;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;


public class ChunkWrapper extends Chunk {

	Chunk chunk;
	
	public ChunkWrapper(Chunk chunk) {
		super(chunk.getWorld(), chunk.xPosition, chunk.zPosition);
		this.chunk = chunk;
	}

	@Override
	public void addEntity(Entity entityIn) {
		super.addEntity(entityIn);
	}
	
	@Override
	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		super.addTileEntity(pos, tileEntityIn);
	}
	
	@Override
	public void addTileEntity(TileEntity tileEntityIn) {
		super.addTileEntity(tileEntityIn);
	}
	
	@Override
	public boolean canSeeSky(BlockPos pos) {
		return super.canSeeSky(pos);
	}
	
	@Override
	public void checkLight() {
		super.checkLight();
	}
	
	@Override
	public void enqueueRelightChecks() {
		super.enqueueRelightChecks();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public void fillChunk(PacketBuffer buf, int p_186033_2_, boolean p_186033_3_) {
		super.fillChunk(buf, p_186033_2_, p_186033_3_);
	}
	
	@Override
	public void generateSkylightMap() {
		super.generateSkylightMap();
	}
	
	@Override
	public boolean getAreLevelsEmpty(int startY, int endY) {
		return super.getAreLevelsEmpty(startY, endY);
	}
	
	@Override
	public Biome getBiome(BlockPos pos, BiomeProvider provider) {
		return super.getBiome(pos, provider);
	}
	
	@Override
	public byte[] getBiomeArray() {
		return super.getBiomeArray();
	}
	
	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		return super.getBlockLightOpacity(pos);
	}
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return super.getBlockState(pos);
	}
	
	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		return super.getBlockState(x, y, z);
	}
	
	@Override
	public ExtendedBlockStorage[] getBlockStorageArray() {
		return super.getBlockStorageArray();
	}
	
	@Override
	public ChunkPos getChunkCoordIntPair() {
		return super.getChunkCoordIntPair();
	}
	
	@Override
	public <T extends Entity> void getEntitiesOfTypeWithinAAAB(Class<? extends T> entityClass, AxisAlignedBB aabb,
		List<T> listToFill, Predicate<? super T> p_177430_4_) {
		super.getEntitiesOfTypeWithinAAAB(entityClass, aabb, listToFill, p_177430_4_);
	}
	
	@Override
	public void getEntitiesWithinAABBForEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill,
		Predicate<? super Entity> p_177414_4_) {
		super.getEntitiesWithinAABBForEntity(entityIn, aabb, listToFill, p_177414_4_);
	}
	
	@Override
	public ClassInheritanceMultiMap<Entity>[] getEntityLists() {
		return super.getEntityLists();
	}
	
	@Override
	public int getHeight(BlockPos pos) {
		return super.getHeight(pos);
	}
	
	@Override
	public int[] getHeightMap() {
		return super.getHeightMap();
	}
	
	@Override
	public int getHeightValue(int x, int z) {
		return super.getHeightValue(x, z);
	}
	
	@Override
	public long getInhabitedTime() {
		return super.getInhabitedTime();
	}
	
	@Override
	public int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos) {
		return super.getLightFor(p_177413_1_, pos);
	}
	
	@Override
	public int getLightSubtracted(BlockPos pos, int amount) {
		return super.getLightSubtracted(pos, amount);
	}
	
	@Override
	public int getLowestHeight() {
		return super.getLowestHeight();
	}
	
	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return super.getPrecipitationHeight(pos);
	}
	
	@Override
	public Random getRandomWithSeed(long seed) {
		return super.getRandomWithSeed(seed);
	}
}
