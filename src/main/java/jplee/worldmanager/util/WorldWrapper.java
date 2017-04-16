package jplee.worldmanager.util;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;

import jplee.worldmanager.manager.GenerationManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.capabilities.Capability;

public class WorldWrapper extends World {

	private World world;
	
	public WorldWrapper(World world) {
		super(world.getSaveHandler(),
			  world.getWorldInfo(),
			  world.provider,
			  world.theProfiler,
			  world.isRemote);
		this.world = world;
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
		Replaceable replace = null;
		if(GenerationManager.instance.isWorldProcessable(world)) {
			if(!newState.getBlock().equals(Blocks.AIR)) {
				replace = GenerationManager.instance.getReplaceable(world, pos, newState, world.rand);
			}
		}
		if(replace != null)
			newState = replace.getBlockState("replace");
		boolean changed = world.setBlockState(pos, newState, flags);
		if(replace != null)
			GenerationManager.instance.postReplace(world, pos, replace.getString("loot"), rand);
		return changed;
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, IBlockState state) {
		Replaceable replace = null;
		if(GenerationManager.instance.isWorldProcessable(world)) {
			if(!state.getBlock().equals(Blocks.AIR)) {
				replace = GenerationManager.instance.getReplaceable(world, pos, state, world.rand);
			}
		}
		if(replace != null)
			state = replace.getBlockState("replace");
		boolean changed = world.setBlockState(pos, state);
		if(replace != null)
			GenerationManager.instance.postReplace(world, pos, replace.getString("loot"), rand);
		return changed;
	}

	@Override
	public boolean spawnEntityInWorld(Entity entityIn) {
		return world.spawnEntityInWorld(entityIn);
	}
	
	//==================================================================================
	// Wrapped functions
	//==================================================================================
	@Override
	public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
		world.addBlockEvent(pos, blockIn, eventID, eventParam);
	}
	
	@Override
	public void addEventListener(IWorldEventListener listener) {
		world.addEventListener(listener);
	}
	
	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
		world.addTileEntities(tileEntityCollection);
	}
	
	@Override
	public boolean addTileEntity(TileEntity tile) {
		return world.addTileEntity(tile);
	}
	
	@Override
	public boolean addWeatherEffect(Entity entityIn) {
		return world.addWeatherEffect(entityIn);
	}
	
	@Override
	public CrashReportCategory addWorldInfoToCrashReport(CrashReport report) {
		return world.addWorldInfoToCrashReport(report);
	}

	@Override
	public void calculateInitialSkylight() {
		world.calculateInitialSkylight();
	}
	
	@Override
	public void calculateInitialWeatherBody() {
		world.calculateInitialWeatherBody();
	}
	
	@Override
	public int calculateSkylightSubtracted(float partialTicks) {
		return world.calculateSkylightSubtracted(partialTicks);
	}
	
	@Override
	public boolean canBlockBePlaced(Block blockIn, BlockPos pos, boolean p_175716_3_, EnumFacing side, Entity entityIn,
		ItemStack itemStackIn) {
		return world.canBlockBePlaced(blockIn, pos, p_175716_3_, side, entityIn, itemStackIn);
	}
	
	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
		return world.canBlockFreeze(pos, noWaterAdj);
	}
	
	@Override
	public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
		return world.canBlockFreezeBody(pos, noWaterAdj);
	}
	
	@Override
	public boolean canBlockFreezeNoWater(BlockPos pos) {
		return world.canBlockFreezeNoWater(pos);
	}
	
	@Override
	public boolean canBlockFreezeWater(BlockPos pos) {
		return world.canBlockFreezeWater(pos);
	}
	
	@Override
	public boolean canBlockSeeSky(BlockPos pos) {
		return world.canBlockSeeSky(pos);
	}
	
	@Override
	public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
		return world.canMineBlockBody(player, pos);
	}
	
	@Override
	public boolean canSeeSky(BlockPos pos) {
		return world.canSeeSky(pos);
	}
	
	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		return world.canSnowAt(pos, checkLight);
	}
	
	@Override
	public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
		return world.canSnowAtBody(pos, checkLight);
	}
	
	@Override
	public boolean checkBlockCollision(AxisAlignedBB bb) {
		return world.checkBlockCollision(bb);
	}
	
	@Override
	public boolean checkLight(BlockPos pos) {
		return world.checkLight(pos);
	}
	
	@Override
	public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
		return world.checkLightFor(lightType, pos);
	}
	
	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb) {
		return world.checkNoEntityCollision(bb);
	}
	
	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entityIn) {
		return world.checkNoEntityCollision(bb, entityIn);
	}
	
	@Override
	public void checkSessionLock() throws MinecraftException {
		world.checkSessionLock();
	}
	
	@Override
	public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
		return world.collidesWithAnyBlock(bbox);
	}
	
	@Override
	public boolean containsAnyLiquid(AxisAlignedBB bb) {
		return world.containsAnyLiquid(bb);
	}
	
	@Override
	public int countEntities(Class<?> entityType) {
		return world.countEntities(entityType);
	}
	
	@Override
	public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
		return world.countEntities(type, forSpawnCount);
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return world.getChunkProvider();
	}

	@Override
	public Explosion createExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isSmoking) {
		return world.createExplosion(entityIn, x, y, z, strength, isSmoking);
	}
	
	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
		return world.destroyBlock(pos, dropBlock);
	}
	
	@Override
	public boolean equals(Object obj) {
		return world.equals(obj);
	}
	
	@Override
	public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side) {
		return world.extinguishFire(player, pos, side);
	}
	
	@Override
	public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb,
		T closestTo) {
		return world.findNearestEntityWithinAABB(entityType, aabb, closestTo);
	}
	
	@Override
	public int func_189649_b(int p_189649_1_, int p_189649_2_) {
		return world.func_189649_b(p_189649_1_, p_189649_2_);
	}
	
	@Override
	public int getActualHeight() {
		return world.getActualHeight();
	}
	
	@Override
	public Biome getBiome(BlockPos pos) {
		return world.getBiome(pos);
	}
	
	@Override
	public Biome getBiomeForCoordsBody(BlockPos pos) {
		return world.getBiomeForCoordsBody(pos);
	}
	
	@Override
	public BiomeProvider getBiomeProvider() {
		return world.getBiomeProvider();
	}
	
	@Override
	public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
		return world.getBlockDensity(vec, bb);
	}
	
	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		return world.getBlockLightOpacity(pos);
	}
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return world.getCapability(capability, facing);
	}
	
	@Override
	public float getCelestialAngle(float partialTicks) {
		return world.getCelestialAngle(partialTicks);
	}
	
	@Override
	public float getCelestialAngleRadians(float partialTicks) {
		return world.getCelestialAngleRadians(partialTicks);
	}
	
	@Override
	public Chunk getChunkFromBlockCoords(BlockPos pos) {
		return world.getChunkFromBlockCoords(pos);
	}
	
	@Override
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		return world.getChunkFromChunkCoords(chunkX, chunkZ);
	}
	
	@Override
	public IChunkProvider getChunkProvider() {
		return world.getChunkProvider();
	}
	
	@Deprecated @Override
	public int getChunksLowestHorizon(int x, int z) {
		return world.getChunksLowestHorizon(x, z);
	}
	
	@Override
	public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
		return world.getClosestPlayer(posX, posY, posZ, distance, spectator);
	}
	
	@Override
	public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
		return world.getClosestPlayerToEntity(entityIn, distance);
	}
	
	@Override
	public Vec3d getCloudColorBody(float partialTicks) {
		return world.getCloudColorBody(partialTicks);
	}
	
	@Override
	public Vec3d getCloudColour(float partialTicks) {
		return world.getCloudColour(partialTicks);
	}
	
	@Override
	public List<AxisAlignedBB> getCollisionBoxes(AxisAlignedBB bb) {
		return world.getCollisionBoxes(bb);
	}
	
	@Override
	public List<AxisAlignedBB> getCollisionBoxes(Entity entityIn, AxisAlignedBB aabb) {
		return world.getCollisionBoxes(entityIn, aabb);
	}
	
	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return world.getCombinedLight(pos, lightValue);
	}
	
	@Override
	public Calendar getCurrentDate() {
		return world.getCurrentDate();
	}
	
	@Override
	public float getCurrentMoonPhaseFactor() {
		return world.getCurrentMoonPhaseFactor();
	}
	
	@Override
	public float getCurrentMoonPhaseFactorBody() {
		return world.getCurrentMoonPhaseFactorBody();
	}
	
	@Override
	public String getDebugLoadedEntities() {
		return world.getDebugLoadedEntities();
	}
	
	@Override
	public EnumDifficulty getDifficulty() {
		return world.getDifficulty();
	}
	
	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return world.getDifficultyForLocation(pos);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
		return world.getEntities(entityType, filter);
	}
	
	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox,
		Predicate<? super Entity> predicate) {
		return world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
		return world.getEntitiesWithinAABB(classEntity, bb);
	}
	
	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb,
		Predicate<? super T> filter) {
		return world.getEntitiesWithinAABB(clazz, aabb, filter);
	}
	
	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb) {
		return world.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
	}
	
	@Override
	public Entity getEntityByID(int id) {
		return world.getEntityByID(id);
	}
	
	@Override
	public Vec3d getFogColor(float partialTicks) {
		return world.getFogColor(partialTicks);
	}
	
	@Override
	public GameRules getGameRules() {
		return world.getGameRules();
	}
	
	@Override
	public IBlockState getGroundAboveSeaLevel(BlockPos pos) {
		return world.getGroundAboveSeaLevel(pos);
	}
	
	@Override
	public int getHeight() {
		return world.getHeight();
	}
	
	@Override
	public BlockPos getHeight(BlockPos pos) {
		return world.getHeight(pos);
	}
	
	@Override
	public double getHorizon() {
		return world.getHorizon();
	}
	
	@Override
	public int getLastLightningBolt() {
		return world.getLastLightningBolt();
	}
	
	@Override
	public int getLight(BlockPos pos) {
		return world.getLight(pos);
	}
	
	@Override
	public int getLight(BlockPos pos, boolean checkNeighbors) {
		return world.getLight(pos, checkNeighbors);
	}
	
	@Override
	public float getLightBrightness(BlockPos pos) {
		return world.getLightBrightness(pos);
	}
	
	@Override
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		return world.getLightFor(type, pos);
	}
	
	@Override
	public int getLightFromNeighbors(BlockPos pos) {
		return world.getLightFromNeighbors(pos);
	}
	
	@Override
	public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
		return world.getLightFromNeighborsFor(type, pos);
	}
	
	@Override
	public List<Entity> getLoadedEntityList() {
		return world.getLoadedEntityList();
	}
	
	@Override
	public LootTableManager getLootTableManager() {
		return world.getLootTableManager();
	}
	
	@Override
	public MapStorage getMapStorage() {
		return world.getMapStorage();
	}
	
	@Override
	public MinecraftServer getMinecraftServer() {
		return world.getMinecraftServer();
	}
	
	@Override
	public int getMoonPhase() {
		return world.getMoonPhase();
	}
	
	@Override
	public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
		return world.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance);
	}
	
	@Override
	public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
		double maxYDistance, Function<EntityPlayer,Double> playerToDouble, Predicate<EntityPlayer> p_184150_12_) {
		return world.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble, p_184150_12_);
	}
	
	@Override
	public EntityPlayer getNearestAttackablePlayer(Entity entityIn, double maxXZDistance, double maxYDistance) {
		return world.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance);
	}
	
	@Override
	public EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
		return world.getNearestPlayerNotCreative(entityIn, distance);
	}
	
	@Override
	public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
		return world.getPendingBlockUpdates(chunkIn, p_72920_2_);
	}
	
	@Override
	public List<NextTickListEntry> getPendingBlockUpdates(StructureBoundingBox structureBB, boolean p_175712_2_) {
		return world.getPendingBlockUpdates(structureBB, p_175712_2_);
	}
	
	@Override
	public Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
		return world.getPersistentChunkIterable(chunkIterator);
	}
	
	@Override
	public ImmutableSetMultimap<ChunkPos,Ticket> getPersistentChunks() {
		return world.getPersistentChunks();
	}
	
	@Override
	public MapStorage getPerWorldStorage() {
		return world.getPerWorldStorage();
	}
	
	@Override
	public EntityPlayer getPlayerEntityByName(String name) {
		return world.getPlayerEntityByName(name);
	}
	
	@Override
	public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
		return world.getPlayerEntityByUUID(uuid);
	}
	
	@Override
	public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
		return world.getPlayers(playerType, filter);
	}
	
	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return world.getPrecipitationHeight(pos);
	}
	
	@Override
	public String getProviderName() {
		return world.getProviderName();
	}
	
	@Override
	public float getRainStrength(float delta) {
		return world.getRainStrength(delta);
	}
	
	@Override
	public int getRedstonePower(BlockPos pos, EnumFacing facing) {
		return world.getRedstonePower(pos, facing);
	}
	
	@Override
	public ISaveHandler getSaveHandler() {
		return world.getSaveHandler();
	}
	
	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}
	
	@Override
	public int getSeaLevel() {
		return world.getSeaLevel();
	}
	
	@Override
	public long getSeed() {
		return world.getSeed();
	}
	
	@Override
	public Vec3d getSkyColor(Entity entityIn, float partialTicks) {
		return world.getSkyColor(entityIn, partialTicks);
	}
	
	@Override
	public Vec3d getSkyColorBody(Entity entityIn, float partialTicks) {
		return world.getSkyColorBody(entityIn, partialTicks);
	}
	
	@Override
	public int getSkylightSubtracted() {
		return world.getSkylightSubtracted();
	}
	
	@Override
	public BlockPos getSpawnPoint() {
		return world.getSpawnPoint();
	}
	
	@Override
	public float getStarBrightness(float partialTicks) {
		return world.getStarBrightness(partialTicks);
	}
	
	@Override
	public float getStarBrightnessBody(float partialTicks) {
		return world.getStarBrightnessBody(partialTicks);
	}
	
	@Override
	public int getStrongPower(BlockPos pos) {
		return world.getStrongPower(pos);
	}
	
	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return world.getStrongPower(pos, direction);
	}
	
	@Override
	public float getSunBrightness(float p_72971_1_) {
		return world.getSunBrightness(p_72971_1_);
	}
	
	@Override
	public float getSunBrightnessBody(float p_72971_1_) {
		return world.getSunBrightnessBody(p_72971_1_);
	}
	
	@Override
	public float getSunBrightnessFactor(float partialTicks) {
		return world.getSunBrightnessFactor(partialTicks);
	}
	
	@Override
	public float getThunderStrength(float delta) {
		return world.getThunderStrength(delta);
	}
	
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
	@Override
	public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
		return world.getTopSolidOrLiquidBlock(pos);
	}
	
	@Override
	public long getTotalWorldTime() {
		return world.getTotalWorldTime();
	}
	
	@Override
	public int getUniqueDataId(String key) {
		return world.getUniqueDataId(key);
	}
	
	@Override
	public VillageCollection getVillageCollection() {
		return world.getVillageCollection();
	}
	
	@Override
	public WorldBorder getWorldBorder() {
		return world.getWorldBorder();
	}
	
	@Override
	public WorldInfo getWorldInfo() {
		return world.getWorldInfo();
	}
	
	@Override
	public long getWorldTime() {
		return world.getWorldTime();
	}
	
	@Override
	public WorldType getWorldType() {
		return world.getWorldType();
	}
	
	@Override
	public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
		return world.handleMaterialAcceleration(bb, materialIn, entityIn);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return world.hasCapability(capability, facing);
	}
	
	@Override
	public int hashCode() {
		return world.hashCode();
	}
	
	@Override
	public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
		world.immediateBlockTick(pos, state, random);
	}
	
	@Override
	public World init() {
		return world.init();
	}
	
	@Override
	public void initialize(WorldSettings settings) {
		world.initialize(settings);
	}
	
	@Override
	public boolean isAABBInMaterial(AxisAlignedBB bb, Material materialIn) {
		return world.isAABBInMaterial(bb, materialIn);
	}
	
	@Override
	public boolean isAirBlock(BlockPos pos) {
		return world.isAirBlock(pos);
	}
	
	@Override
	public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
		return world.isAnyPlayerWithinRangeAt(x, y, z, range);
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos center, int radius) {
		return world.isAreaLoaded(center, radius);
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
		return world.isAreaLoaded(center, radius, allowEmpty);
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to) {
		return world.isAreaLoaded(from, to);
	}
	
	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
		return world.isAreaLoaded(from, to, allowEmpty);
	}
	
	@Override
	public boolean isAreaLoaded(StructureBoundingBox box) {
		return world.isAreaLoaded(box);
	}
	
	@Override
	public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
		return world.isAreaLoaded(box, allowEmpty);
	}
	
	@Override
	public boolean isBlockFullCube(BlockPos pos) {
		return world.isBlockFullCube(pos);
	}
	
	@Override
	public int isBlockIndirectlyGettingPowered(BlockPos pos) {
		return world.isBlockIndirectlyGettingPowered(pos);
	}
	
	@Override
	public boolean isBlockinHighHumidity(BlockPos pos) {
		return world.isBlockinHighHumidity(pos);
	}
	
	@Override
	public boolean isBlockLoaded(BlockPos pos) {
		return world.isBlockLoaded(pos);
	}
	
	@Override
	public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
		return world.isBlockLoaded(pos, allowEmpty);
	}
	
	@Override
	public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
		return world.isBlockModifiable(player, pos);
	}
	
	@Override
	public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
		return world.isBlockNormalCube(pos, _default);
	}
	
	@Override
	public boolean isBlockPowered(BlockPos pos) {
		return world.isBlockPowered(pos);
	}
	
	@Override
	public boolean isBlockTickPending(BlockPos pos, Block blockType) {
		return world.isBlockTickPending(pos, blockType);
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

	@Override
	public boolean isDaytime() {
		return world.isDaytime();
	}
	
	@Override
	public boolean isFlammableWithin(AxisAlignedBB bb) {
		return world.isFlammableWithin(bb);
	}
	
	@Override
	public boolean isInsideBorder(WorldBorder worldBorderIn, Entity entityIn) {
		return world.isInsideBorder(worldBorderIn, entityIn);
	}
	
	@Override
	public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
		return world.isMaterialInBB(bb, materialIn);
	}
	
	@Override
	public boolean isRaining() {
		return world.isRaining();
	}
	
	@Override
	public boolean isRainingAt(BlockPos strikePosition) {
		return world.isRainingAt(strikePosition);
	}
	
	@Override
	public boolean isSidePowered(BlockPos pos, EnumFacing side) {
		return world.isSidePowered(pos, side);
	}
	
	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side) {
		return world.isSideSolid(pos, side);
	}
	
	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return world.isSideSolid(pos, side, _default);
	}
	
	@Override
	public boolean isSpawnChunk(int x, int z) {
		return world.isSpawnChunk(x, z);
	}
	
	@Override
	public boolean isThundering() {
		return world.isThundering();
	}
	
	@Override
	public boolean isUpdateScheduled(BlockPos pos, Block blk) {
		return world.isUpdateScheduled(pos, blk);
	}
	
	@Override
	public void joinEntityInSurroundings(Entity entityIn) {
		world.joinEntityInSurroundings(entityIn);
	}
	
	@Override
	public void loadEntities(Collection<Entity> entityCollection) {
		world.loadEntities(entityCollection);
	}
	
	@Override
	public WorldSavedData loadItemData(Class<? extends WorldSavedData> clazz, String dataID) {
		return world.loadItemData(clazz, dataID);
	}
	
	@Override
	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ,
		NBTTagCompound compund) {
		world.makeFireworks(x, y, z, motionX, motionY, motionZ, compund);
	}
	
	@Override
	public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState iblockstate, IBlockState newState,
		int flags) {
		world.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
	}
	
	@Override
	public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
		world.markBlockRangeForRenderUpdate(rangeMin, rangeMax);
	}
	
	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		world.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}
	
	@Override
	public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
		world.markBlocksDirtyVertical(x1, z1, x2, z2);
	}
	
	@Override
	public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
		world.markChunkDirty(pos, unusedTileEntity);
	}
	
	@Override
	public void markTileEntityForRemoval(TileEntity tileEntityIn) {
		world.markTileEntityForRemoval(tileEntityIn);
	}
	
	@Override
	public Explosion newExplosion(Entity entityIn, double x, double y, double z, float strength, boolean isFlaming,
		boolean isSmoking) {
		return world.newExplosion(entityIn, x, y, z, strength, isFlaming, isSmoking);
	}
	
	@Override
	public void notifyBlockOfStateChange(BlockPos pos, Block blockIn) {
		world.notifyBlockOfStateChange(pos, blockIn);
	}
	
	@Override
	public void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		world.notifyBlockUpdate(pos, oldState, newState, flags);
	}
	
	@Override
	public void notifyLightSet(BlockPos pos) {
		world.notifyLightSet(pos);
	}
	
	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
		world.notifyNeighborsOfStateChange(pos, blockType);
	}
	
	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
		world.notifyNeighborsOfStateExcept(pos, blockType, skipSide);
	}
	
	@Override
	public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType) {
		world.notifyNeighborsRespectDebug(pos, blockType);
	}
	
	@Override
	public void onEntityAdded(Entity entityIn) {
		world.onEntityAdded(entityIn);
	}
	
	@Override
	public void onEntityRemoved(Entity entityIn) {
		world.onEntityRemoved(entityIn);
	}
	
	@Override
	public void playBroadcastSound(int id, BlockPos pos, int data) {
		world.playBroadcastSound(id, pos, data);
	}
	
	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos pos, int data) {
		world.playEvent(player, type, pos, data);
	}
	
	@Override
	public void playEvent(int type, BlockPos pos, int data) {
		world.playEvent(type, pos, data);
	}
	
	@Override
	public void playRecord(BlockPos blockPositionIn, SoundEvent soundEventIn) {
		world.playRecord(blockPositionIn, soundEventIn);
	}
	
	@Override
	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume,
		float pitch, boolean distanceDelay) {
		world.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
	}
	
	@Override
	public void playSound(EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume,
		float pitch) {
		world.playSound(player, pos, soundIn, category, volume, pitch);
	}
	
	@Override
	public void playSound(EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
		float volume, float pitch) {
		world.playSound(player, x, y, z, soundIn, category, volume, pitch);
	}
	
	@Override
	public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
		return world.rayTraceBlocks(start, end);
	}
	
	@Override
	public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid) {
		return world.rayTraceBlocks(start, end, stopOnLiquid);
	}
	
	@Override
	public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
		boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
	}
	
	@Override
	public void removeEntity(Entity entityIn) {
		world.removeEntity(entityIn);
	}
	
	@Override
	public void removeEntityDangerously(Entity entityIn) {
		world.removeEntityDangerously(entityIn);
	}
	
	@Override
	public void removeEventListener(IWorldEventListener listener) {
		world.removeEventListener(listener);
	}
	
	@Override
	public void removeTileEntity(BlockPos pos) {
		world.removeTileEntity(pos);
	}
	
	@Override
	public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
		world.scheduleBlockUpdate(pos, blockIn, delay, priority);
	}
	
	@Override
	public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
		world.scheduleUpdate(pos, blockIn, delay);
	}
	
	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		world.sendBlockBreakProgress(breakerId, pos, progress);
	}
	
	@Override
	public void sendPacketToServer(Packet<?> packetIn) {
		world.sendPacketToServer(packetIn);
	}
	
	@Override
	public void sendQuittingDisconnectingPacket() {
		world.sendQuittingDisconnectingPacket();
	}
	
	@Override
	public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
		world.setAllowedSpawnTypes(hostile, peaceful);
	}
	
	@Override
	public boolean setBlockToAir(BlockPos pos) {
		return world.setBlockToAir(pos);
	}
	
	@Override
	public void setEntityState(Entity entityIn, byte state) {
		world.setEntityState(entityIn, state);
	}
	
	@Override
	public void setInitialSpawnLocation() {
		world.setInitialSpawnLocation();
	}
	
	@Override
	public void setItemData(String dataID, WorldSavedData worldSavedDataIn) {
		world.setItemData(dataID, worldSavedDataIn);
	}
	
	@Override
	public void setLastLightningBolt(int lastLightningBoltIn) {
		world.setLastLightningBolt(lastLightningBoltIn);
	}
	
	@Override
	public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
		world.setLightFor(type, pos, lightValue);
	}
	
	@Override
	public void setRainStrength(float strength) {
		world.setRainStrength(strength);
	}
	
	@Override
	public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
		return world.setRandomSeed(p_72843_1_, p_72843_2_, p_72843_3_);
	}
	
	@Override
	public void setSeaLevel(int seaLevelIn) {
		world.setSeaLevel(seaLevelIn);
	}
	
	@Override
	public void setSkylightSubtracted(int newSkylightSubtracted) {
		world.setSkylightSubtracted(newSkylightSubtracted);
	}
	
	@Override
	public void setSpawnPoint(BlockPos pos) {
		world.setSpawnPoint(pos);
	}
	
	@Override
	public void setThunderStrength(float strength) {
		world.setThunderStrength(strength);
	}
	
	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		world.setTileEntity(pos, tileEntityIn);
	}
	
	@Override
	public void setTotalWorldTime(long worldTime) {
		world.setTotalWorldTime(worldTime);
	}
	
	@Override
	public void setWorldTime(long time) {
		world.setWorldTime(time);
	}
	
	@Override
	public void spawnParticle(EnumParticleTypes particleType, boolean ignoreRange, double xCoord, double yCoord,
		double zCoord, double xSpeed, double ySpeed, double zSpeed, int...parameters) {
		world.spawnParticle(particleType, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}
	
	@Override
	public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord,
		double xSpeed, double ySpeed, double zSpeed, int...parameters) {
		world.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}
	
	@Override
	public void tick() {
		world.tick();
	}
	
	@Override
	public boolean tickUpdates(boolean p_72955_1_) {
		return world.tickUpdates(p_72955_1_);
	}
	
	@Override
	public String toString() {
		return world.toString();
	}
	
	@Override
	public void unloadEntities(Collection<Entity> entityCollection) {
		world.unloadEntities(entityCollection);
	}
	
	@Override
	public void updateAllPlayersSleepingFlag() {
		world.updateAllPlayersSleepingFlag();
	}
	
	@Override
	public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
		world.updateBlockTick(pos, blockIn, delay, priority);
	}
	
	@Override
	public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
		world.updateComparatorOutputLevel(pos, blockIn);
	}
	
	@Override
	public void updateEntities() {
		world.updateEntities();
	}
	
	@Override
	public void updateEntity(Entity ent) {
		world.updateEntity(ent);
	}
	
	@Override
	public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
		world.updateEntityWithOptionalForce(entityIn, forceUpdate);
	}
	
	@Override
	public void updateWeatherBody() {
		world.updateWeatherBody();
	}
	
}
