package meems33.minecraft.worlddumper.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;

public class WorldDumperUtils {
	public static final DateFormat DATE_FORMATER = new SimpleDateFormat("yyMMddhhmmss");
	
	public static List<String> getPotentialMatches(List<String> searchList, String key) {
		List<String> rt = new ArrayList<String>();
		
		if (key == null) { key = ""; }
		
		int idx = Collections.binarySearch(searchList, key);
		
		if (idx >= 0) {
			rt.add(searchList.get(idx));
		} else {
			idx = (-1 * idx) - 1;
			for (int i = idx; i > 0; --i) {
				if (searchList.get(i).startsWith(key)) {
					rt.add(searchList.get(i));
				} else {
					break;
				}
			}
			
			for (int i = idx; i < searchList.size(); ++i) {
				if (searchList.get(i).startsWith(key)) {
					rt.add(searchList.get(i));
				} else {
					break;
				}
			}
		}

		return rt;
	}
	
	public static boolean placeSign(World world, int x, int y, int z, String text) {
		if (text == null) { text = ""; }
		
		return placeSign(world, x, y, z,
				text.substring(0, Math.min(15, text.length())),
				text.length() > 15 ? text.substring(15, Math.min(15 * 2, text.length())) : "",
				text.length() > 15 * 2 ? text.substring(15 * 2, Math.min(15 * 3, text.length())) : "",
				text.length() > 15 * 3 ? text.substring(15 * 3, Math.min(15 * 4, text.length())) : ""
				);
	}
	
	public static boolean placeSign(World world, int x, int y, int z, String text1, String text2) {
		if (text1 == null) { text1 = ""; }
		if (text2 == null) { text2 = ""; }
		
		return placeSign(world, x, y, z,
				text1,
				text2.substring(0, Math.min(14, text2.length())),
				text2.length() > 15 ? text2.substring(15, Math.min(15 * 2, text2.length())) : "",
				text2.length() > 15 * 2 ? text2.substring(15 * 2, Math.min(15 * 3, text2.length())) : ""
				);
	}
	
	public static boolean placeSign(World world, int x, int y, int z, String text1, String text2, String text3, String text4) {
		if (world.setBlockWithNotify(x, y, z, Block.signPost.blockID)) {
			TileEntitySign sign1 = (TileEntitySign)world.getBlockTileEntity(x, y, z);
			
			sign1.signText[0] = text1 != null? text1.substring(0, Math.min(15, text1.length())) : "";
			sign1.signText[1] = text2 != null? text2.substring(0, Math.min(15, text2.length())) : "";
			sign1.signText[2] = text3 != null? text3.substring(0, Math.min(15, text3.length())) : "";
			sign1.signText[3] = text4 != null? text4.substring(0, Math.min(15, text4.length())) : "";
			sign1.setEditable(true);

			world.setBlockTileEntity(x, y, z, sign1);
			
			return true;
		}
		
		return false;
	}
	
	public static boolean containsCaseInsensitive(Collection<String> list, String str) {
		
		String strLower = str != null? str.toLowerCase() : null;
		
		for (String str2 : list) {
			if ((strLower != null && strLower.equalsIgnoreCase(str2)) 
					|| (strLower == null && str2 == null)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String[] getPseudoArgs(String[] args) {
		List<String> rt = new ArrayList<String>(args.length);
		
		boolean toggle = false;
		
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			
			for (char c : arg.toCharArray()) {
				if ('"' == c) {
					// If we were in the middle of parens
					if (toggle) {
						rt.add(sb.toString());
						sb.setLength(0);
					}
					
					toggle = !toggle;
				} else {
					sb.append(c);
				}
			}
			
			// If not in the middle of parens
			if (!toggle) {
				rt.add(sb.toString());
				sb.setLength(0);
			}
		}
		
		if (sb.length() != 0) {
			rt.add(sb.toString());
		}
		
		return rt.toArray(new String[0]);
	}
	
	public static boolean shouldSkipBlock(IItemInfoHelper itemInfoHelper, ItemInfo itemInfo) {
		if (itemInfoHelper.getIgnoreListByID() != null) {
			if (itemInfoHelper.getIgnoreListByID().contains(itemInfo.getItemID() + ":" + itemInfo.getMeta())
					|| itemInfoHelper.getIgnoreListByID().contains(itemInfo.getItemID())) {
				return true;
			}
		}
		
		if (itemInfoHelper.getIgnoreListByDisplayName() != null
				&& containsCaseInsensitive(
						itemInfoHelper.getIgnoreListByDisplayName(),
						itemInfo.getDisplayName())) {
			return true;
		}
		
		return false;
	}
}
