package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;

import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/**
 * Get your Cheeto fingers off! Only the DynamicTrees mod should use this and only for vanilla trees
 * 
 * @author ferreusveritas
 *
 */
public class TreeFamilyVanilla extends TreeFamily {
	
	public final BlockPlanks.EnumType woodType;
	
	public TreeFamilyVanilla(BlockPlanks.EnumType wood) {
		super(new ResourceLocation(ModConstants.MODID, wood.getName().replace("_","")));
		
		woodType = wood;
		
		//Setup tree references
		boolean isOld = wood.getMetadata() < 4;
		setPrimitiveLog((isOld ? Blocks.LOG : Blocks.LOG2).getDefaultState().withProperty(isOld ? BlockOldLog.VARIANT : BlockNewLog.VARIANT, wood));
				
		//Setup common species
		getCommonSpecies().setDynamicSapling(ModBlocks.blockDynamicSapling.getDefaultState().withProperty(BlockSapling.TYPE, wood));
		getCommonSpecies().generateSeed();
	}
	
}
