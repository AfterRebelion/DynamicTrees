package com.ferreusveritas.dynamictrees.render;

import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFallingTree extends Render<EntityFallingTree>{
	
	protected RenderFallingTree(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFallingTree entity) {
		return null;
	}
	
	@Override
	public void doRender(EntityFallingTree entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if(!entity.isActivated()) {
			return;
		}
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos cutPos = entity.getDestroyData().cutPos;
		EnumFacing cutDir = entity.getDestroyData().cutDir;
		Map<BlockPos, IExtendedBlockState> stateMap = entity.getDestroyData().destroyedBranches;
		World world = entity.getEntityWorld();
		Vec3d mc = entity.getMassCenter();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(mc.x + x, mc.y + y, mc.z + z);
		GlStateManager.rotate(-entityYaw, 0, 1, 0);
		GlStateManager.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);
		
		IExtendedBlockState exState = stateMap.get(BlockPos.ORIGIN);
		
		if(exState != null) {
			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			
			//Draw the ring texture cap on the cut block
			int brightnessIn = exState.getPackedLightmapCoords(world, cutPos);
			for(EnumFacing face: EnumFacing.VALUES) {
				exState = exState.withProperty(BlockBranchBasic.CONNECTIONS[face.getIndex()], face == cutDir.getOpposite() ? 8 : 0);
			}
			float offset = (8 - ((BlockBranch) exState.getBlock()).getRadius(exState)) / 16f;
			IBakedModel model = dispatcher.getModelForState(exState.getClean());
			renderBlockModel(world, model, exState, new Vec3d(BlockPos.ORIGIN.offset(cutDir)).scale(offset), brightnessIn, bufferbuilder, new EnumFacing[] { cutDir } );
			
			//Draw the rest of the tree/branch
			for( Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
				exState = entry.getValue();
				model = dispatcher.getModelForState(exState.getClean());
				renderBlockModel(world, model, exState, new Vec3d(entry.getKey()), brightnessIn, bufferbuilder, everyFace);
			}
			
			//Draw the leaves
			/*for( Map.Entry<BlockPos, IBlockState> entry : entity.getDestroyData().destroyedLeaves.entrySet()) {
				IBlockState state = entry.getValue();
				model = dispatcher.getModelForState(state);
				renderBlockModel(world, model, state, new Vec3d(entry.getKey()), brightnessIn, bufferbuilder, everyFace);
			}*/
			
			tessellator.draw();
		}
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}
	
	public static final EnumFacing everyFace[] = { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	
	public void renderBlockModel(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, Vec3d offset, int brightnessIn, BufferBuilder buffer, EnumFacing[] sides) {
		
		for (EnumFacing enumfacing : sides) {
			for(BakedQuad bakedquad: modelIn.getQuads(stateIn, enumfacing, 0)) {
				buffer.addVertexData(bakedquad.getVertexData());
				buffer.putBrightness4(brightnessIn, brightnessIn, brightnessIn, brightnessIn);
				
				if(bakedquad.shouldApplyDiffuseLighting()) {
					float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
					buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
					buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
					buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
					buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
				}
				
				buffer.putPosition(offset.x, offset.y, offset.z);
			}
		}
		
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

