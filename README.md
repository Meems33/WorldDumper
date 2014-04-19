WorldDumper
===========

A utility to create Minecraft item dump lists and generate all blocks in a world. This tool has two usages:
- item dumping: A csv can be generated that includes information about every player obtainable item. A player obtainable item
is one that would show up in NEI.
- world dumping: Generates a world in which every player obtainable item is placed along with information about the item. This
can be used to determine the success of upgrading minecraft mods or minecraft versions.

Installation
===========
1.4.7: 
  1. Download and install Minecraft Forge 6.6.2.534. This is the last stable forge release for 1.4.7.
  2. Put the appropriate WorldDumper zip into your mods/ directory
  
1.5.2:
  1. Download and install Minecraft Forge 7.8.1.738. This is the last stable forge release for 1.5.2.
  2. Put the appropriate WorldDumper zip into your mods/ directory
  
1.6.4:
  1. Download and install Minecraft Forge 9.11.1.965. This is the last stable forge release for 1.6.4.
  2. Put the appropriate WorldDumper zip into your mods/ directory

Usage
===========
*** Warning: commands will alter world. Suggest using on an empty superflat world! ***

While in game as an operator, the following commands can be used:
/worlddump items
	Creates a csv file with every player obtainable item. The csv file contains the following information based on the CLIENT configuration files:
	ID,Meta,Type,Mod,Class,Unlocalized Name,Display Name,Has Nbt
	
	ID: The numerical ID of the item
	Meta: The meta id of the item
	Type: If the item is a block or an item
	Mod: The mod the item comes from
	Class: The Java class of the item
	Unlocalized Name: A string to identify an item for localization
	Display Name: The name of the item the player sees
	Has NBT: If the item uses NBT tags
		
/worlddump listmods
	List the mods that have items that will be used with other worlddump commands
	
/worlddump world <mod_name|ALL> <x> <y> <z>
	*** WARNING: Modifies world. Suggest using on an empty superflat world! ***
	Places every item in a world along with information about the item. Every dump includes a chest with the item in it. 
	Blocks are placed if possible.
	
	mod_name: The name of the mod to dump. If ALL then all mods are dumped.
	<x>: The x coordinate used to determine the x chunk to start the dump at
	<y>: The y coordinate used to start the dump at
	<z>: The z coordinate used to determine the z chunk to start the dump at

Screenshots
===========
![alt tag](https://raw.github.com/Meems33/WorldDumper/tree/master/screenshots/worlddump_example1.png)
