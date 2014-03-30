package meems33.minecraft.worlddumper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meems33.minecraft.worlddumper.common.IItemInfoHelper;
import meems33.minecraft.worlddumper.common.ItemInfo;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.ItemData;

public class ItemInfoHelper implements IItemInfoHelper {
	private List<ItemInfo> itemInfoCache;
	private List<String> modNameCache;
	private Collection<String> ignoreByID;
	private Collection<String> ignoreByDisplayName;
	
	public List<String> getModsWithItems() {
		if (modNameCache == null) {
			Set<String> tmp = new HashSet<String>();
			
			List<ItemInfo> allItems = getAllItemInfos();
			for (ItemInfo info : allItems) {
				if (info.hasModName()) { 
					tmp.add(info.getModName());
				}
			}
			
			modNameCache = new ArrayList<String>(tmp);
			Collections.sort(modNameCache);
		}
		
		return modNameCache;
	}
	
	public List<ItemInfo> getAllItemInfos() {
		if (itemInfoCache == null) {
			itemInfoCache = new ArrayList<ItemInfo>();
			Map<Integer, ItemData> idMap = getIdMap();
			Map<String, Class<? extends TileEntity>> nameToClassMap = getNameToClassMap();
			
			ArrayList<ItemStack> itemsDiscovered = new ArrayList<ItemStack>();
			Item[] aitem = Item.itemsList;
			int i = aitem.length;
			int j;
	
			for (j = 0; j < i; ++j) {
				Item item = aitem[j];
	
				if (item != null) {
					item.getSubItems(item.itemID, (CreativeTabs) null,
							itemsDiscovered); // Add the subitems
				}
			}
	
			List<String> info = new ArrayList<String>();
			info.add("ID,Item Meta,Type,Mod Name,Class,Item Name,Display Name,Has Tile Entity");
	
			for (ItemStack itemStack : itemsDiscovered) {
				ItemInfo itemInfo = new ItemInfo();
				
				itemInfo.setItemID(itemStack.itemID);
				
				try {
					itemInfo.setMeta(itemStack.getItemDamage());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try { 
					itemInfo.setMeta(itemStack.getItemDamage()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				try { 
					itemInfo.setDisplayName(itemStack.getDisplayName());
				} catch (Exception e) {
					e.printStackTrace(); 
				}
				
				try { 
					itemInfo.setUnlocalizedName(itemStack.getItemName());
				} catch (Exception e) {
					e.printStackTrace(); 
				}
				
				try {
					itemInfo.setItemClassName(itemStack.getItem() != null? 
							itemStack.getItem().getClass().getName() : null);
					itemInfo.setItemClass(itemStack.getItem() != null? 
						itemStack.getItem().getClass() : null);
				} catch (Exception e) {
					e.printStackTrace(); 
				}
				
				try {
					itemInfo.setModName(!idMap.isEmpty() && (ItemData)idMap.get(itemStack.itemID) != null?
						((ItemData)idMap.get(itemStack.itemID)).getModId() : null);
				} catch (Exception e) {
					e.printStackTrace();
				}
						
				try { 
					itemInfo.setItemType(itemStack.getItem() != null? 
						(itemStack.getItem() 
								instanceof net.minecraft.item.ItemBlock?
								"Block" : "Item") : null);
					itemInfo.setItemTypeClass(itemStack.getItem() != null? itemStack.getItem().getClass() : null);
				} catch (Exception e) {
					e.printStackTrace();
				}
						
				try { 
					String displayName = itemInfo.getDisplayName();
					itemInfo.setContainsExtra(!nameToClassMap.isEmpty()?
							(nameToClassMap.get(displayName) != null) : null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				itemInfo.setItemStack(itemStack);
				
				itemInfoCache.add(itemInfo);
			}
		}
		
		return itemInfoCache;

	}
	
	@SuppressWarnings("unchecked")
	private Map<Integer, ItemData> getIdMap() {
		Map<Integer, ItemData> rt = Collections.EMPTY_MAP;
		Field f = null;
		
		Boolean oldAccess = null;
		try {
			f = GameData.class.getDeclaredField("idMap");
		
			oldAccess = f.isAccessible();
			f.setAccessible(true);
			
			rt = (Map<Integer, ItemData>)f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oldAccess != null) f.setAccessible(oldAccess);
		}
		
		return rt;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Class<? extends TileEntity>> getNameToClassMap() {
		Map<String, Class<? extends TileEntity>> rt = Collections.emptyMap();
		Field f = null;
		
		Boolean oldAccess = null;
		try {
			try {
				f = TileEntity.class.getDeclaredField("nameToClassMap");
			} catch (NoSuchFieldException ne) {
				try {
					f = TileEntity.class.getDeclaredField("field_70326_a");
				} catch (NoSuchFieldException ne2) {
					
					for (Field searchField : net.minecraft.tileentity.TileEntity.class.getDeclaredFields()) {
						if (java.lang.reflect.Modifier.isStatic(searchField.getModifiers()) 
								&& Map.class.isAssignableFrom(searchField.getType())) {
							Boolean oldAccess2 = searchField.isAccessible();
							try {
								searchField.setAccessible(true);
								Map<?, ?> sMap = (Map<?, ?>)searchField.get(null);
								
								if (sMap.size() > 0 && sMap.keySet().iterator().next()
										instanceof String) {
									f = searchField;
									break;
								}
							} finally {
								if (oldAccess2 != null) { searchField.setAccessible(oldAccess2); }
							}
						}
					}
				}
			}
			oldAccess = f.isAccessible();
			f.setAccessible(true);
			
			rt = (Map<String, Class<? extends TileEntity>>)f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oldAccess != null) f.setAccessible(oldAccess);
		}
		
		return rt;
	}

	@Override
	public int getTotalNumItems() {
		return getModsWithItems().size();
	}

	@Override
	public Collection<String> getIgnoreListByID() {
		return ignoreByID;
	}

	@Override
	public Collection<String> getIgnoreListByDisplayName() {
		return ignoreByDisplayName;
	}
	
	protected void setIgnoreListByID(Collection<String> lst) {
		this.ignoreByID = lst;
	}
	
	protected void setIgnoreListByDisplayName(Collection<String> lst) {
		this.ignoreByDisplayName = lst;
	}
}
