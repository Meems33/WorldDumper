package meems33.minecraft.worlddumper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import meems33.minecraft.worlddumper.common.IItemInfoHelper;
import meems33.minecraft.worlddumper.common.ItemInfo;
import meems33.minecraft.worlddumper.common.ItemInfoComparator;
import meems33.minecraft.worlddumper.common.WorldDumperUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.FMLLog;

public class WorldDumpGenerator {
	public static final String ALL_MODS = "all";
	
	public static final int MAX_Y_SECTIONS = 8;
	public static final int SECTIONS_PER_CHUNK = 4 * 4 * MAX_Y_SECTIONS;
	public static final int Y_INC = 6;
	
	private static final Comparator<ItemInfo> ITEM_INFO_COMPARATOR = new ItemInfoComparator();
	
	private IItemInfoHelper itemInfoHelper;
	private Map<String, List<ItemInfo>> modItemInfos;
	private EntityPlayer proxyPlayer;
	
	public WorldDumpGenerator(IItemInfoHelper itemInfoHelper, EntityPlayer proxyPlayer) {
		this.itemInfoHelper = itemInfoHelper;
		
		this.modItemInfos = new TreeMap<String, List<ItemInfo>>();
		List<ItemInfo> allItemInfos = itemInfoHelper.getAllItemInfos();
		
		for (ItemInfo info : allItemInfos) {
			if (info.hasModName()) {
				String modName = info.getModName() != null ? info.getModName()
						.toLowerCase() : null;
				List<ItemInfo> infoForMod = modItemInfos.get(modName);
				
				if (infoForMod == null) {
					infoForMod = new ArrayList<ItemInfo>();
					modItemInfos.put(modName, infoForMod);
				}
				
				infoForMod.add(info);
			} else {
				FMLLog.log(Level.WARNING, "Unknown mod for " + info);
			}
		}
		
		this.proxyPlayer = proxyPlayer;
	}
	
	private List<List<ItemInfo>> generatePseudoChunks(String modName) {
		List<List<ItemInfo>> rt = new ArrayList<List<ItemInfo>>();
		
		int startChunkIdx = 0;
		for (Map.Entry<String, List<ItemInfo>> e : modItemInfos.entrySet()) {
			if (!ALL_MODS.equalsIgnoreCase(modName) && !e.getKey().equalsIgnoreCase(modName)) {
				continue;
			}
			
			Collections.sort(e.getValue(), ITEM_INFO_COMPARATOR);
			
			int numItems = e.getValue() != null ? e.getValue().size() : 0;
			
			if (numItems == 0) { continue; }
			
			int numChunks = ((numItems - 1) / SECTIONS_PER_CHUNK) + 1;
			
			for (int i = 0; i < numChunks; ++i) {
				rt.add(new ArrayList<ItemInfo>());
			}
			
			int itemIdx = 0;
			for (ItemInfo info : e.getValue()) {
				int chunkIdx = (itemIdx / 16) % numChunks + startChunkIdx;
				
				rt.get(chunkIdx).add(info);
				
				++itemIdx;
			}
			
			startChunkIdx += numChunks;
		}
		
		return rt;
	}
	
	public boolean generate(World world, String modName, int chunkXBase, int chunkYBase, int chunkZBase) {
		return generate(world, modName, chunkXBase, chunkYBase, chunkZBase, 
				Math.max((int)Math.ceil(Math.sqrt(itemInfoHelper.getTotalNumItems() * 3)), 8));
	}
	
	public boolean generate(World world, String modName, int chunkXBase, int chunkYBase, int chunkZBase, int maxX) {
		boolean success = true;
		
		List<List<ItemInfo>> pseudoChunkItems = generatePseudoChunks(modName);
		
		int pIdx = 0;
		int woolColor = 0;
		String prevModName = "asijvij3rj9f@@1__";
		for (List<ItemInfo> chunkItemInfos : pseudoChunkItems) {
			int chunkX = pIdx % maxX + chunkXBase;
			int chunkZ = ((int)(pIdx / maxX)) * 2 + chunkZBase;
			if (!chunkItemInfos.isEmpty() && !prevModName.equalsIgnoreCase(chunkItemInfos.get(0).getModName())) {
				woolColor = (woolColor + 1) % 16;
				prevModName = chunkItemInfos.get(0).getModName();
				proxyPlayer.sendChatToPlayer("Attempting to generate world dump for mod \"" 
						+ prevModName + "\"...");
			}
			
			if (!buildChunk(world, chunkItemInfos, chunkX, chunkYBase, chunkZ, woolColor)) {
				success = false;
			}
			
			++pIdx;
		}
		
		return success;
	}
		
	private boolean buildChunk(World world, List<ItemInfo> itemInfos,
			int chunkX, int yMin, int chunkZ, int woolColor) {
		boolean success = true;
		int inc = 0;
		int yBase = yMin;
		
		for (ItemInfo item : itemInfos) {
			int xBase = (inc / 4) * 4 + chunkX * 16;
			int zBase = (3 - (inc % 4)) * 4 + chunkZ * 16;
			
			if (!buildItemSquare(world, item, xBase, yBase, zBase, woolColor)) {
				success = false;
			}

			if (inc == 15) {
				yBase += Y_INC;
				inc = 0;
			} else {
				++inc;
			}
		}
		
		// TODO this is not correct
		if (!world.getChunkProvider().saveChunks(false, null)) {
			FMLLog.warning("Failed to save chunk!");
			success = false;
		}
		
		return success;
	}
	
	// builds 4x4x4 cube in +x, +y, +z direction
	private boolean buildItemSquare(World world, ItemInfo item, 
			int xBase, int yBase, int zBase, int woolColor) {
		FMLLog.info("Attempting to spawn section %s:%s[%s] %s %s at (%d, %d, %d)",
				item.getItemID(), item.getMeta(), item.isContainsExtra(),
				item.getModName(), item.getDisplayName(), xBase, yBase, zBase);
		
		if (yBase < 0 || yBase >= 256 - 4) {
			return false;
		}

		IChunkProvider chunkProvider = world.getChunkProvider();
		boolean success = true;
		/*
		 * 4x4 area:
		 * T O x x
		 * x B x x
		 * x x C x
		 * S S S x
		 * 
		 * T = torch
		 * O = optional backing
		 * B = block if applicable
		 * C = chest
		 * S = signs
		 */
		boolean placeBlock = item.getItemTypeClass() != null 
			&& net.minecraft.item.ItemBlock.class.isAssignableFrom(item.getItemTypeClass());
		
		// Fill block below with wool
		for (int xsub = 0; xsub < 4; ++xsub) {
			for (int zsub = 0; zsub < 4; ++zsub ) {
				world.setBlock(xBase + xsub, yBase - 1, zBase + zsub, 35, woolColor, 2);
			}
		}
		
		// Place torch
		world.setBlock(xBase + 0, yBase, zBase + 0, 50);
		
		// Place block
		if (placeBlock) {
			if (!WorldDumperUtils.shouldSkipBlock(itemInfoHelper, item)) {
				try {
					if ("fire".equalsIgnoreCase(item.getDisplayName())) {
						world.setBlock(xBase + 1, yBase, zBase + 1, 3);
						success = world.setBlock(xBase + 1, yBase + 1, zBase + 1, item.getItemID(), item.getMeta(), 2);
					} else if ("water".equalsIgnoreCase(item.getDisplayName())
							|| "lava".equalsIgnoreCase(item.getDisplayName())) {
						success = world.setBlock(xBase + 1, yBase, zBase + 1, item.getItemID(), item.getMeta(), 2);
						
						world.setBlock(xBase + 1 + 1, yBase, zBase + 1, 3);
						world.setBlock(xBase + 1 - 1, yBase, zBase + 1, 3);
						world.setBlock(xBase + 1, yBase, zBase + 1 + 1, 3);
						world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 3);
					} else {
						EntityPlayer player = proxyPlayer;
						
						boolean tryPlaceBelow = true;
						boolean tryPlaceOnSide = true;
		
						if (tryPlaceBelow) {
							// Place on top of block below
							FMLLog.finer("Attempting to place above...");
							success = tryPlaceItemIntoWorld(item.getItemStack(), player, 
									world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
							
							
							if (!success) {
								FMLLog.finer("Attempting to place above dirt...");
								world.setBlock(xBase + 1, yBase - 1, zBase + 1, 3);
								
								success = tryPlaceItemIntoWorld(item.getItemStack(), player, 
										world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
								
								if (!success) {
									world.setBlock(xBase + 1, yBase - 1, zBase + 1, 35, woolColor, 2);
								}
							}
							
							if (!success) {
								FMLLog.finer("Attempting to place above farmland...");
								world.setBlock(xBase + 1, yBase - 1, zBase + 1, 60);
								
								success = tryPlaceItemIntoWorld(item.getItemStack(), player, 
										world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
								
								if (!success) {
									world.setBlock(xBase + 1, yBase - 1, zBase + 1, 35, woolColor, 2);
								}
							}
							
							if (!success) {
								FMLLog.finer("Attempting to place above sand...");
								world.setBlock(xBase + 1, yBase - 1, zBase + 1, 12);
								world.setBlock(xBase + 1, yBase - 2, zBase + 1, 35, woolColor, 2);
								
								success = tryPlaceItemIntoWorld(item.getItemStack(), player, 
										world, xBase + 1, yBase - 1, zBase + 1, 1, 0.35f, 0.5f, 1.0f);
								
								if (!success) {
									world.setBlock(xBase + 1, yBase - 2, zBase + 1, 0);
									world.setBlock(xBase + 1, yBase - 1, zBase + 1, 35, woolColor, 2);
								}
							}
						}
						
						if (!success && tryPlaceOnSide) {
							world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 1);
							FMLLog.finer("Attempting to place on side...");
							success = tryPlaceItemIntoWorld(item.getItemStack(), player, 
									world, xBase + 1, yBase, zBase + 1 - 1, 3, 0.35f, 0.5f, 1.0f);
							if (!success) {
								world.setBlock(xBase + 1, yBase, zBase + 1 - 1, 0);
							}
						}
					}
					
					// TODO this is not correct
					chunkProvider.saveChunks(false, null);
				} catch (Exception e) {
					success = false;
					e.printStackTrace();
				}
			} else {
				FMLLog.fine("Ignoring block %s", item);
				WorldDumperUtils.placeSign(world, xBase + 1, yBase, zBase + 1, " ** ignored ** ");
				success = true;
			}
			
			if (!success) {
				FMLLog.warning("Failed to save %s:%s %s %s %s", item.getItemID(), item.getMeta(), 
						item.getModName(), item.getDisplayName(), item.isContainsExtra());
				world.removeBlockTileEntity(xBase + 1, yBase, zBase + 1);
				world.setBlock(xBase + 1, yBase, zBase + 1, 0);
				WorldDumperUtils.placeSign(world, xBase + 1, yBase, zBase + 1, "  ** ERROR **  ");
			}
		}
		
		// Place chest with block
		if (world.setBlock(xBase + 2, yBase, zBase + 2, Block.chest.blockID)) {
			TileEntityChest chest = (TileEntityChest)world.getBlockTileEntity(xBase + 2, yBase, zBase + 2);
			chest.setInventorySlotContents(0, item.getItemStack());
			world.setBlockTileEntity(xBase + 2, yBase, zBase + 2, chest);
		}
		
		// Place signs
		WorldDumperUtils.placeSign(world, xBase + 0, yBase, zBase + 3,  
				String.format("%-15s", String.valueOf(item.getItemID()) + ":" + String.valueOf(item.getMeta())), 
				String.format("%-15s", item.getModName()));
		WorldDumperUtils.placeSign(world, xBase + 1, yBase, zBase + 3, item.getDisplayName());
		WorldDumperUtils.placeSign(world, xBase + 2, yBase, zBase + 3, item.getUnlocalizedName());
		
		return success;
	}
	
	private boolean tryPlaceItemIntoWorld(ItemStack stack, EntityPlayer par1EntityPlayer, World par2World, int par3, int par4, int par5, int par6, float par7, float par8, float par9)
	{
		ItemStack s2 = stack.copy();
		s2.stackSize = 1;
		
		return s2.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par3, par4, par5, par6, par7, par8, par9);
	}
	
	private List<ItemInfo> getItemInfos(String modName) {
		modName = modName != null? modName.trim().toLowerCase() : null;
		
		return modItemInfos.get(modName);
	}
}
