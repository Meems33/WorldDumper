package meems33.minecraft.worlddumper.common;

import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.FMLLog;

public class ItemInfoDumper {
	final String ERROR_STRING = "**ERROR**";
	private String fileName;
	
	public ItemInfoDumper() {
		this("ItemDump_" + System.currentTimeMillis() + ".csv");
	}
	
	public ItemInfoDumper(String fileName) {
		this.fileName = fileName;
	}
	
	public void doDump(IItemInfoHelper itemInfoHelper) {
		// TODO world name in file name
		PrintWriter writer = null;
		try {
			List<ItemInfo> allItemInfo = itemInfoHelper.getAllItemInfos();
			
			FMLLog.log(Level.FINE, "Dumping item data to " + fileName);
			Minecraft.getMinecraft().thePlayer.addChatMessage("Dumping item data to " + fileName);
			writer = new PrintWriter(fileName, "UTF-8");
			
			writer.append("ID,Meta,Type,Mod,Class,Unlocalized Name,Display Name,Has Nbt");
			writer.append(System.getProperty("line.separator"));
			
			for (ItemInfo info : allItemInfo) {
				StringBuilder sb = new StringBuilder();
				//info.append("ID,Item Meta,Type,Mod Name,Class,Item Name,Display Name,Has Nbt");
				sb.append(info.hasItemID()? info.getItemID() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasMeta()? info.getMeta() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasItemType()? info.getItemType() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasModName()? info.getModName() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasItemClassName()? info.getItemClassName() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasUnlocalizedName()? info.getUnlocalizedName() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasDisplayName()? info.getDisplayName() : ERROR_STRING);
				sb.append(",");
				sb.append(info.hasContainsExtra()? info.isContainsExtra() : ERROR_STRING);

				writer.write(sb.toString() + System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
