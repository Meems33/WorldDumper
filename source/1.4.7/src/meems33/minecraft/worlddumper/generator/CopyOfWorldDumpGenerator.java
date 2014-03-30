//package meems33.minecraft.worlddumper.generator;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.TreeMap;
//import java.util.logging.Level;
//
//import meems33.minecraft.worlddumper.WorldDumper;
//import meems33.minecraft.worlddumper.common.ItemInfo;
//import meems33.minecraft.worlddumper.common.ItemInfoComparator;
//import meems33.minecraft.worlddumper.common.WorldDumperUtils;
//import net.minecraft.block.Block;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.tileentity.TileEntityChest;
//import net.minecraft.tileentity.TileEntitySign;
//import net.minecraft.util.ChunkCoordinates;
//import net.minecraft.world.World;
//import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.IChunkProvider;
//import cpw.mods.fml.common.FMLLog;
//import cpw.mods.fml.common.IWorldGenerator;
//
//// TODO go left to right for items on world
//// TODO how handle water, fire?
//// TODO go higher?
//public class CopyOfWorldDumpGenerator implements IWorldGenerator {
//	private static Map<String, List<ItemInfo>> modItemInfos;
//	private static List<String> mods;
//	
//	public static final int MAX_Y_SECTIONS = 8;
//	public static final int SECTIONS_PER_CHUNK = 4 * 4 * MAX_Y_SECTIONS;
//	public static final int Y_INC = 6;
//	private static final int MAX_X_DIST = 12;
//	
//	private static class Coordinate {
//		private int x;
//		private int z;
//		
//		public Coordinate(int x, int z) {
//			this.x = x;
//			this.z = z;
//		}
//		
//		@Override
//		public boolean equals(Object other) {
//			if (other instanceof Coordinate) {
//				Coordinate c = (Coordinate) other;
//				return x == c.x && z == c.z;
//			}
//			return false;
//		}
//		
//		@Override
//		public int hashCode() {
//			return z * 31 << 7 + x;
//		}
//		
//		public int getX() { return x; }
//		public int getZ() { return z; };
//	}
//	
//	private static final Comparator<ItemInfo> ITEM_INFO_COMPARATOR = new ItemInfoComparator();
//	
//	@Override
//	public void generate(Random random, int chunkX, int chunkZ, World world,
//			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
//		try {
//			ChunkCoordinates
//			String modForChunk = getModForChunk(chunkX, chunkZ);
//			
//			if (modForChunk != null) {
//				List<ItemInfo> items = getModItemInfos().get(modForChunk);
//				int modForChunkItemStartIndex = getItemIndexForChunk(chunkX, chunkZ);
//				items = items.subList(modForChunkItemStartIndex, 
//					Math.min(items.size(), modForChunkItemStartIndex + SECTIONS_PER_CHUNK));
//				
//				int woolColor = getMods().indexOf(modForChunk) % 16;
//				
//				int inc = 0;
//				int yBase = 4;
//				
//				for (ItemInfo item : items) {
//					int xBase = (inc % 4) * 4 + chunkX * 16;
//					int zBase = (3 - (inc / 4)) * 4 + chunkZ * 16;
//					
//					buildItemSquare(world, item, xBase, yBase, zBase, woolColor);
//
//					if (inc == 15) {
//						yBase += Y_INC;
//						inc = 0;
//					} else {
//						++inc;
//					}
//				}
//				
//				chunkProvider.saveChunks(false, null);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	// builds 4x4x4 cube in +x, +y, +z direction
//	private boolean buildItemSquare(World world, ItemInfo item, 
//			int xBase, int yBase, int zBase, int woolColor) {
//		FMLLog.info("Attempting to spawn section %s:%s[%s] %s %s at (%d, %d, %d)",
//				item.getItemID(), item.getMeta(), item.isContainsExtra(),
//				item.getModName(), item.getDisplayName(), xBase, yBase, zBase);
//		
//		if (yBase < 0 || yBase >= 256 - 4) {
//			return false;
//		}
//
//		IChunkProvider chunkProvider = world.getChunkProvider();
//		boolean success = true;
//		/*
//		 * 4x4 area:
//		 * T O x x
//		 * x B x x
//		 * x x C x
//		 * S S S x
//		 * 
//		 * T = torch
//		 * O = optional backing
//		 * B = block if applicable
//		 * C = chest
//		 * S = signs
//		 */
//		boolean placeBlock = item.getItemTypeClass() != null 
//			&& net.minecraft.item.ItemBlock.class.isAssignableFrom(item.getItemTypeClass());
//		
//		// Fill block below with wool
//		for (int xsub = 0; xsub < 4; ++xsub) {
//			for (int zsub = 0; zsub < 4; ++zsub ) {
//				world.setBlockAndMetadata(xBase + xsub, yBase - 1, zBase + zsub, 35, woolColor);
//			}
//		}
//		
//		// Place torch
//		world.setBlock(xBase + 0, yBase, zBase + 0, 50);
//		
//		// Place block
//		// TODO make smarter (chunk loaders break!)
//		if (placeBlock) {
//			try {
//				if ("fire".equalsIgnoreCase(item.getDisplayName())) {
//					world.setBlock(xBase + 1, yBase, zBase + 1, 3);
//					success = world.setBlockAndMetadata(xBase + 1, yBase + 1, zBase + 1, item.getItemID(), item.getMeta());
//				} else if ("water".equalsIgnoreCase(item.getDisplayName())
//						|| "lava".equalsIgnoreCase(item.getDisplayName())) {
//					success = world.setBlockAndMetadata(xBase + 1, yBase, zBase + 1, item.getItemID(), item.getMeta());
//					
//					world.setBlock(xBase + 1 + 1, yBase, zBase + 1, 3);
//					world.setBlock(xBase + 1 - 1, yBase, zBase + 1, 3);
//					world.setBlock(xBase + 1, yBase, zBase + 1 + 1, 3);
//					world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 3);
//				} else {
//					EntityPlayer player = world.getClosestPlayer(xBase + 1, yBase, zBase + 1, -1);
//					
//					boolean tryPlaceBelow = true;
//					boolean tryPlaceOnSide = true;
//
//					if (tryPlaceBelow) {
//						// Place on top of block below
//						FMLLog.finer("Attempting to place above...");
//						success = item.getItemStack().tryPlaceItemIntoWorld(player, 
//								world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
//						
//						
//						if (!success) {
//							FMLLog.finer("Attempting to place above dirt...");
//							world.setBlock(xBase + 1, yBase - 1, zBase + 1, 3);
//							
//							success = item.getItemStack().tryPlaceItemIntoWorld(player, 
//									world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
//							
//							if (!success) {
//								world.setBlockAndMetadata(xBase + 1, yBase - 1, zBase + 1, 35, woolColor);
//							}
//						}
//						
//						if (!success) {
//							FMLLog.finer("Attempting to place above farmland...");
//							world.setBlock(xBase + 1, yBase - 1, zBase + 1, 60);
//							
//							success = item.getItemStack().tryPlaceItemIntoWorld(player, 
//									world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
//							
//							if (!success) {
//								world.setBlockAndMetadata(xBase + 1, yBase - 1, zBase + 1, 35, woolColor);
//							}
//						}
//						
//						if (!success) {
//							FMLLog.finer("Attempting to place above sand...");
//							world.setBlock(xBase + 1, yBase - 1, zBase + 1, 12);
//							world.setBlockAndMetadata(xBase + 1, yBase - 2, zBase + 1, 35, woolColor);
//							
//							success = item.getItemStack().tryPlaceItemIntoWorld(player, 
//									world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
//							
//							if (!success) {
//								world.setBlock(xBase + 1, yBase - 2, zBase + 1, 0);
//								world.setBlockAndMetadata(xBase + 1, yBase - 1, zBase + 1, 35, woolColor);
//							}
//						}
//					}
//					
//					if (!success && tryPlaceOnSide) {
//						world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 1);
//						FMLLog.finer("Attempting to place on side...");
//						success = item.getItemStack().tryPlaceItemIntoWorld(player, 
//								world, xBase + 1, yBase, zBase + 1 - 1, 3, 0.35f, 0.5f, 1.0f);
//						if (!success) {
//							world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 0);
//						}
//					}
//				}
//				chunkProvider.saveChunks(false, null);
//			} catch (Exception e) {
//				success = false;
//				e.printStackTrace();
//			}
//			
//			if (!success) {
//				FMLLog.warning("Failed to save %s:%s %s %s %s", item.getItemID(), item.getMeta(), 
//						item.getModName(), item.getDisplayName(), item.isContainsExtra());
//				world.removeBlockTileEntity(xBase + 1, yBase, zBase + 1);
//				world.setBlock(xBase + 1, yBase, zBase + 1, 0);
//				WorldDumperUtils.placeSign(world, xBase + 1, yBase, zBase + 1, "   **ERROR**   ");
//			}
//		}
//		
//		// Place chest with block
//		if (world.setBlockWithNotify(xBase + 2, yBase, zBase + 2, Block.chest.blockID)) {
//			TileEntityChest chest = (TileEntityChest)world.getBlockTileEntity(xBase + 2, yBase, zBase + 2);
//			chest.setInventorySlotContents(0, item.getItemStack());
//			world.setBlockTileEntity(xBase + 2, yBase, zBase + 2, chest);
//		}
//		
//		// Place signs
//		WorldDumperUtils.placeSign(world, xBase + 0, yBase, zBase + 3,  
//				String.format("%-15s", String.valueOf(item.getItemID()) + ":" + String.valueOf(item.getMeta())), 
//				String.format("%-15s", item.getModName()));
//		WorldDumperUtils.placeSign(world, xBase + 1, yBase, zBase + 3, item.getDisplayName());
//		WorldDumperUtils.placeSign(world, xBase + 2, yBase, zBase + 3, item.getUnlocalizedName());
//		
//		return success;
//	}
//	
//	private String getModForChunk(int chunkX, int chunkZ) {
//		if (chunkZ % 2 != 0) {
//			return null;
//		} if (chunkX < 0 || chunkZ >= MAX_X_DIST) {
//			return null;
//		}
//		
//		Map<Coordinate, String> modForChunkMap = getModForChunkMap();
//		
//		Coordinate c = new Coordinate(chunkX, chunkZ / 2);
//		
//		if (!modForChunkMap.containsKey(c)) {
//			return null;
//		} else {
//			return modForChunkMap.get(c);
//		}
//	}
//	
//	private int getItemIndexForChunk(int chunkX, int chunkZ) {
//		if (chunkZ % 2 != 0) {
//			return -1;
//		} if (chunkX < 0 || chunkZ >= MAX_X_DIST) {
//			return -1;
//		}
//		
//		Coordinate c = new Coordinate(chunkX, chunkZ / 2);
//		
//		Map<Coordinate, Integer> modForChunkItemIndex = getModForChunkItemIndex();
//
//		if (!modForChunkItemIndex.containsKey(c)) {
//			return -1;
//		} else {
//			return modForChunkItemIndex.get(c);
//		}
//	}
//	
//	private Map<Coordinate, String> modForChunkMap;
//	private Map<Coordinate, Integer> modForChunkItemIndex;
//	
//	private Map<Coordinate, Integer> getModForChunkItemIndex() {
//		if (modForChunkItemIndex == null) {
//			getModForChunkMap();
//		}
//		
//		return modForChunkItemIndex;
//	}
//	
//	private Map<Coordinate, String> getModForChunkMap() {
//		if (modForChunkMap == null) {
//			modForChunkMap = new HashMap<Coordinate, String>();
//			modForChunkItemIndex = new HashMap<Coordinate, Integer>();
//			
//			Map<String, List<ItemInfo>> allModInfos = getModItemInfos();
//			List<String> keys = new ArrayList<String>(allModInfos.keySet());
//			Collections.sort(keys);
//			
//			int cIndex = 0; // 12 long
//			for (String key : keys) {
//				List<ItemInfo> infos = allModInfos.get(key);
//				int size = infos != null? infos.size() : 0;
//				
//				int iIndex = 0;
//				if (size > 0) {
//					for (int itemsRemaining = size; itemsRemaining > 0; itemsRemaining -= SECTIONS_PER_CHUNK) {
//						Coordinate c = new Coordinate(cIndex % MAX_X_DIST, cIndex / MAX_X_DIST);
//						modForChunkMap.put(c, key);
//						modForChunkItemIndex.put(c, iIndex);
//						++cIndex;
//						iIndex += SECTIONS_PER_CHUNK;
//					}
//				}
//			}
//		}
//		
//		return modForChunkMap;
//	}
//
//	private synchronized static Map<String, List<ItemInfo>> getModItemInfos() {
//		if (modItemInfos == null) {
//			modItemInfos = new TreeMap<String, List<ItemInfo>>();
//			List<ItemInfo> allItemInfos = WorldDumper.getItemInfoHelper().getAllItemInfo();
//			
//			for (ItemInfo info : allItemInfos) {
//				if (info.hasModName()) {
//					String modName = info.getModName();
//					List<ItemInfo> infoForMod = modItemInfos.get(modName);
//					
//					if (infoForMod == null) {
//						infoForMod = new ArrayList<ItemInfo>();
//						modItemInfos.put(modName, infoForMod);
//					}
//					
//					infoForMod.add(info);
//				} else {
//					FMLLog.log(Level.WARNING, "Unknown mod for " + info);
//				}
//			}
//			
//			for (Map.Entry<String, List<ItemInfo>> e : modItemInfos.entrySet()) {
//				Collections.sort(e.getValue(), ITEM_INFO_COMPARATOR);
//			}
//		}
//		
//		return modItemInfos;
//	}
//	
//	private static List<String> getMods() {
//		if (mods == null) {
//			Map<String, List<ItemInfo>> allModInfos = getModItemInfos();
//			
//			mods = new ArrayList<String>(allModInfos.keySet());
//			Collections.sort(mods);
//		}
//		
//		return mods;
//	}
//}
