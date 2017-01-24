package jplee.worldmanager.util.message;

import io.netty.buffer.ByteBuf;
import jplee.jlib.server.network.MessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChunkUpdateMessage implements IMessage {

	private int dimension;
	private int chunkX;
	private int chunkZ;
	
	public ChunkUpdateMessage() { }
	
	public ChunkUpdateMessage(World world, ChunkPos pos) {
		this.dimension = world.provider.getDimension();
		this.chunkX = pos.chunkXPos;
		this.chunkZ = pos.chunkZPos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.dimension = buf.readInt();
		this.chunkX = buf.readInt();
		this.chunkZ = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.dimension);
		buf.writeInt(this.chunkX);
		buf.writeInt(this.chunkZ);
	}

	public static class Handler extends MessageHandler.Client<ChunkUpdateMessage> {

		@Override
		public IMessage handleClientMessage(EntityPlayer player, ChunkUpdateMessage message, MessageContext context) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				player.getEntityWorld().getChunkFromChunkCoords(message.chunkX, message.chunkZ).setChunkModified();
			});
			return null;
		}
	}
}
