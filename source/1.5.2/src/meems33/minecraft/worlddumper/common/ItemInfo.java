package meems33.minecraft.worlddumper.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;

public class ItemInfo {
	private final Map<String, Object> data;
	
	public static final String attr_itemID = "itemID";
	public static final String attr_meta = "meta";
	public static final String attr_displayName = "displayName";
	public static final String attr_unlocalizedName = "unlocalizedName";
	public static final String attr_itemClassName = "itemClassName";
	public static final String attr_modName = "modName";
	public static final String attr_itemType = "itemType";
	public static final String attr_containsExtra = "containsExtra";
	
	public static final String attr_itemClass = "itemClass";
	public static final String attr_itemTypeClass = "itemTypeClass";
	public static final String attr_itemStack = "itemStack";
	
	public ItemInfo() {
		data = new HashMap<String, Object>();
	}
	
	public Integer getItemID() {
		return (Integer)getAttribute(attr_itemID);
	}

	public Integer setItemID(Integer itemID) {
		return (Integer)data.put(attr_itemID, itemID);
	}
	
	public boolean hasItemID() {
		return hasAttribute(attr_itemID);
	}

	public Integer getMeta() {
		return (Integer) getAttribute(attr_meta);
	}

	public Integer setMeta(Integer meta) {
		return setAttribute(attr_meta, meta);
	}
	
	public boolean hasMeta() {
		return hasAttribute(attr_meta);
	}
	
	public String getDisplayName() {
		return (String)getAttribute(attr_displayName);
	}
	
	public String setDisplayName(String displayName) {
		return setAttribute(attr_displayName, displayName);
	}
	
	public boolean hasDisplayName() {
		return hasAttribute(attr_displayName);
	}
	
	public String getUnlocalizedName() {
		return (String)getAttribute(attr_unlocalizedName);
	}
	
	public String setUnlocalizedName(String unlocalizedName) {
		return setAttribute(attr_unlocalizedName, unlocalizedName);
	}
	
	public boolean hasUnlocalizedName() {
		return hasAttribute(attr_unlocalizedName);
	}
	
	public String getItemClassName() {
		return (String) getAttribute(attr_itemClassName);
	}

	public String setItemClassName(String itemClassName) {
		return setAttribute(attr_itemClassName, itemClassName);
	}
	
	public boolean hasItemClassName() {
		return hasAttribute(attr_itemClassName);
	}
	
	public Class<?> getItemClass() {
		return (Class<?>) getAttribute(attr_itemClass);
	}

	public Class<?> setItemClass(Class<?> itemClass) {
		return setAttribute(attr_itemClass, itemClass);
	}
	
	public boolean hasItemClass() {
		return hasAttribute(attr_itemClass);
	}
	
	public String getModName() {
		return (String) getAttribute(attr_modName);
	}

	public String setModName(String modName) {
		return setAttribute(attr_modName, modName);
	}
	
	public boolean hasModName() {
		return hasAttribute(attr_modName);
	}

	public String getItemType() {
		return (String) getAttribute(attr_itemType);
	}

	public String setItemType(String itemType) {
		return setAttribute(attr_itemType, itemType);
	}
	
	public boolean hasItemType() {
		return hasAttribute(attr_itemType);
	}

	public Class<?> getItemTypeClass() {
		return (Class<?>) getAttribute(attr_itemTypeClass);
	}

	public Class<?> setItemTypeClass(Class<?> itemTypeClass) {
		return setAttribute(attr_itemTypeClass, itemTypeClass);
	}
	
	public boolean hasItemTypeClass() {
		return hasAttribute(attr_itemTypeClass);
	}
	
	public Boolean isContainsExtra() {
		return (Boolean) getAttribute(attr_containsExtra);
	}

	public Boolean setContainsExtra(Boolean containsExtra) {
		return setAttribute(attr_containsExtra, containsExtra);
	}
	
	public boolean hasContainsExtra() {
		return hasAttribute(attr_containsExtra);
	}
	
	public ItemStack getItemStack() {
		return (ItemStack) getAttribute(attr_itemStack);
	}

	public ItemStack setItemStack(ItemStack itemStack) {
		return setAttribute(attr_itemStack, itemStack);
	}
	
	public boolean hasItemStack() {
		return hasAttribute(attr_itemStack);
	}
	
	public Object getAttribute(String attr) {
		return data.get(attr);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T setAttribute(String attr, T value) {
		return (T) data.put(attr, value);
	}
	
	public boolean hasAttribute(String attr) {
		return data.containsKey(attr);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("{ ");
		
		List<String> keys = new ArrayList<String>(data.keySet());
		Collections.sort(keys);
		
		for (String key : keys) {
			Object value = this.getAttribute(key);
			sb.append(key + "=" + value + " ");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
