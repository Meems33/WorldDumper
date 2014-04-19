
package meems33.minecraft.worlddumper;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import meems33.minecraft.worlddumper.common.IItemInfoHelper;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
// import cpw.mods.fml.common.Mod.EventHandler; // used in 1.6.2
import cpw.mods.fml.common.Mod.PreInit; // used in 1.5.2
import cpw.mods.fml.common.Mod.Init; // used in 1.5.2
import cpw.mods.fml.common.Mod.PostInit; // used in 1.5.2
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = "mod_WorldDumper", name = "WorldDumper", version = "0.0.4")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class WorldDumper  {
	private static final String[]	DEFAULT_IDS_IGNORE = new String[] { "0:0", "34:0", "36:0" };
	private static final String[]	DEFAULT_DISPLAY_NAME_IGNORE	= new String[] {
			"Chunk Loader", "Dimensional Anchor"				};
	
	private static IItemInfoHelper itemInfoHelper;
	private Configuration config;

	// The instance of your mod that Forge uses.
	@Instance("mod_WorldDumper")
	public static WorldDumper	instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "meems33.minecraft.worlddumper.CommonProxy", 
			serverSide = "meems33.minecraft.worlddumper.CommonProxy")
	public static CommonProxy	proxy;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		// Stub Method
		config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();
		
		boolean configChanged = false;
		configChanged |= initializePropertyIfNotPresent(config, "ignore", "ids", DEFAULT_IDS_IGNORE);
		configChanged |= initializePropertyIfNotPresent(config, "ignore", "names", DEFAULT_DISPLAY_NAME_IGNORE);
		
		if (configChanged) {
			config.save();
		}
		
		// load config
		String[] ignoreByIDsRaw = config.get("ignore", "ids", new String[0]).getStringList();
		if (ignoreByIDsRaw != null) {
			Collection<String> ignoreByIDs = new HashSet<String>();
			for (int i = 0; i < ignoreByIDsRaw.length; ++i) {
				if (ignoreByIDsRaw[i] != null && ignoreByIDsRaw[i].matches("\\d+(:\\d+)?")) {
					ignoreByIDs.add(ignoreByIDsRaw[i]);
				} else {
					FMLLog.warning("Invalid ignore ids property: %s", ignoreByIDsRaw[i]);
				}
			}

			((ItemInfoHelper)getItemInfoHelper()).setIgnoreListByID(ignoreByIDs);
			System.out.println(ignoreByIDs);
		}
		
		String[] ignoreByDisplayNameRaw = config.get("ignore", "names", new String[0]).getStringList();
		if (ignoreByDisplayNameRaw != null) {
			Collection<String> ignoreByDisplayName = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			
			for (int i = 0; i < ignoreByDisplayNameRaw.length; ++i) {
				if (ignoreByDisplayNameRaw[i] != null) {
					ignoreByDisplayName.add(ignoreByDisplayNameRaw[i].trim());
				} else {
					FMLLog.warning("Invalid ignore names property: %s", ignoreByDisplayNameRaw[i]);
				}
			}

			((ItemInfoHelper)getItemInfoHelper()).setIgnoreListByDisplayName(ignoreByDisplayName);
			System.out.println(ignoreByDisplayName);
		}
		
	}

	@Init
	public void load(FMLInitializationEvent event) {
		FMLLog.log(Level.FINE, "Init WorldDumper");

		proxy.registerHandlers();
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method
	}
	
	@ServerStarting
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new WorldDumpCommandHandler());
	}

	public static IItemInfoHelper getItemInfoHelper() {
		if (itemInfoHelper == null) {
			itemInfoHelper = new ItemInfoHelper();
		}
		
		return itemInfoHelper;
	}
	
	@SuppressWarnings("unused")
	private boolean initializePropertyIfNotPresent(Configuration config,
			String category, String key, String value) {
		if (!config.getCategory(category).containsKey(key)) {
			config.getCategory(category).put(key, new Property(key, value, Property.Type.STRING));
			
			return true;
		}
		
		return false;
	}
	
	private boolean initializePropertyIfNotPresent(Configuration config,
			String category, String key, String[] value) {
		if (!config.getCategory(category).containsKey(key)) {
			config.getCategory(category).put(key, new Property(key, value, Property.Type.STRING));
			
			return true;
		}
		
		return false;
	}
}