package com.ferreusveritas.dynamictrees.models.bakedmodels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.SpeciesProperty;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * 
 * @author ferreusveritas
 *
 */
@SideOnly(Side.CLIENT)
public class BakedModelSapling implements IBakedModel {
	
	private static IBakedModel[] modelMap;
	private static IBakedModel errorSaplingModel;
	private static TextureAtlasSprite particleTexture;
	
	public static IBakedModel getModelForSapling(Species species) {
		int modelId = species.saplingModelId;
		IBakedModel bakedModel = modelMap[modelId];
		return bakedModel != null ? bakedModel : errorSaplingModel;
	}
	
	public BakedModelSapling(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		try {
			IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(ModConstants.MODID, "block/saplings/error"));
			if(model != null) {
				errorSaplingModel = model.bake(state, format, bakedTextureGetter);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		particleTexture = bakedTextureGetter.apply(new ResourceLocation("blocks/dirt"));
		
		modelMap = new IBakedModel[Species.REGISTRY.getEntries().size()];
		
		int modelId = 0;
		
		for(Entry<ResourceLocation, Species> entry : Species.REGISTRY.getEntries()) {
			Species species = entry.getValue();
			ResourceLocation resLoc = species.getSaplingName();
			species.saplingModelId = modelId;
			if(species != Species.NULLSPECIES) {
				try {
					IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(resLoc.getNamespace(), "block/saplings/" + resLoc.getPath()));
					if(model != null) {
						modelMap[modelId] = model.bake(state, format, bakedTextureGetter);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			modelId++;
		}
		
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		List<BakedQuad> quads = new ArrayList<>(12);
		
		if (state != null && state.getBlock() instanceof BlockDynamicSapling && state instanceof IExtendedBlockState) {
			Species species = ((IExtendedBlockState) state).getValue(SpeciesProperty.SPECIES);
			if(species != null) {//Shouldn't happen but better to be safe
				quads.addAll(getModelForSapling(species).getQuads(ModBlocks.blockDynamicSapling.getDefaultState(), side, rand));
			}
		}
		
		return quads;
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}
	
	@Override
	public boolean isGui3d() {
		return true;
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particleTexture;
	}
	
	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}
	
}
