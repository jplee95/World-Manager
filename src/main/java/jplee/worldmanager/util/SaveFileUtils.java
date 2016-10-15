package jplee.worldmanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ZipperUtil;

public class SaveFileUtils {

	private SaveFileUtils() { }
	
	public static void cleanWorldRegistry(File savesDirectory, String worldId) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			File levelData = new File(savesDirectory, worldId + "/level.dat");
			String timeStamp = new SimpleDateFormat("MMddyyyy_HHmmss").format(Calendar.getInstance().getTime());
			File backup = new File(savesDirectory, worldId + "_" + timeStamp + ".zip");
			ZipperUtil.zip(new File(savesDirectory, worldId), backup);
			
			in = new FileInputStream(levelData);
			NBTTagCompound worldCompound = CompressedStreamTools.readCompressed(in);
			NBTTagCompound fmlCompound = worldCompound.getCompoundTag("FML");

			List<String> modListIds = Lists.newArrayList();
			for(ModContainer mod : Loader.instance().getModList()) {
				modListIds.add(mod.getModId());
			}
			
			if(fmlCompound != null) {
				for(String tagKey : fmlCompound.getCompoundTag("Registries").getKeySet()) {
					NBTBase tag = fmlCompound.getCompoundTag("Registries").getTag(tagKey);
					
					if(!modListIds.contains(tagKey.split(":", 1)[0]) && !tagKey.startsWith("minecraft")) {
						fmlCompound.removeTag(tagKey);
						continue;
					}
					
					if(tag instanceof NBTTagCompound) {
						NBTTagCompound compound = (NBTTagCompound) tag;
						NBTTagList list = null;
						if((list = compound.getTagList("dummied", 10)) != null) {
							if(!list.hasNoTags()) {
								compound.setTag("dummied", new NBTTagList());
							}
						}
						list = null;
						if((list = compound.getTagList("ids", 10)) != null) {
							if(!list.hasNoTags()) {
								NBTTagList newList = new NBTTagList();
								for(int i = 0; i < list.tagCount(); i++) {
									NBTTagCompound comp = list.getCompoundTagAt(i);
									String itemId = null;
									if((itemId = comp.getString("K")) != null) {
										if(itemId.startsWith("minecraft") || modListIds.contains(itemId.split(":", 1)[0])) {
											newList.appendTag(comp);
										}
									}
								}
								compound.setTag("ids", newList);
							}
						}
					}
				}
			}
			
			NBTTagList modList = null;
			if((modList = fmlCompound.getTagList("ModList", 10)) != null) {
				NBTTagList newList = new NBTTagList();
				for(int i = 0; i < modList.tagCount(); i++) {
					NBTTagCompound comp = modList.getCompoundTagAt(i);
					String modId = null;
					if((modId = comp.getString("ModId")) != null) {
						if(modListIds.contains(modId)) {
							newList.appendTag(comp);
						}
					}
				}
				fmlCompound.setTag("ModList", newList);
			}
			
			NBTTagCompound forgeCompound = worldCompound.getCompoundTag("Forge");
			NBTTagList defaultFluidList = null;
			if((defaultFluidList = forgeCompound.getTagList("DefaultFluidList", 8)) != null) {
				NBTTagList newList = new NBTTagList();
				for(int i = 0; i < defaultFluidList.tagCount(); i++) {
					String comp = null;
					if((comp = defaultFluidList.getStringTagAt(i)) != null) {
						if(comp.startsWith("minecraft") || modListIds.contains(comp.split(":", 1)[0])) {
							newList.appendTag(defaultFluidList.get(i));
						}
					}
				}
				forgeCompound.setTag("DefaultFluidList", newList);
			}
			
			out = new FileOutputStream(levelData);
			CompressedStreamTools.writeCompressed(worldCompound, out);
		} catch(IOException e) {
			throw e;
		} finally {
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		}
	}

}
