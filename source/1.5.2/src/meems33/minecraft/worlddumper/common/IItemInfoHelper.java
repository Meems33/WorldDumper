package meems33.minecraft.worlddumper.common;

import java.util.Collection;
import java.util.List;

public interface IItemInfoHelper {
	public List<String> getModsWithItems();
	public List<ItemInfo> getAllItemInfos();
	public int getTotalNumItems();
	
	public Collection<String> getIgnoreListByID();
	public Collection<String> getIgnoreListByDisplayName();
}
