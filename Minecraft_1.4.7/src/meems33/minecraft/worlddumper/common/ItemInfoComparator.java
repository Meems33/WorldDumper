package meems33.minecraft.worlddumper.common;

import java.util.Comparator;

public class ItemInfoComparator implements Comparator<ItemInfo> {
	@Override
	public int compare(ItemInfo arg0, ItemInfo arg1) {
		int cmp1 = compareAttribute(ItemInfo.attr_itemID, arg0, arg1);
		
		if (cmp1 != 0) { return cmp1; }
		
		int cmp2 = compareAttribute(ItemInfo.attr_meta, arg0, arg1);
		
		if (cmp2 != 0) { return cmp2; }
		
		return compareAttribute(ItemInfo.attr_displayName, arg0, arg1);
	} 
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int compareAttribute(String attr, ItemInfo arg0, ItemInfo arg1) {
		if (arg0.getAttribute(attr) == null && arg1.getAttribute(attr) == null) {
			return 0;
		} else if (arg0.getAttribute(attr) == null) {
			return 1;
		} else if (arg1.getAttribute(attr) == null) {
			return -1;
		} else {
			return ((Comparable)arg0.getAttribute(attr)).compareTo(arg1.getAttribute(attr));
		}
	}
}
