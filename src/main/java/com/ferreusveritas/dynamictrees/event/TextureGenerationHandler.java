package com.ferreusveritas.dynamictrees.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.client.ThickRingTextureAtlasSprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextureGenerationHandler {
	
	private static final Map<ResourceLocation, ResourceLocation> thickRingTextures = new HashMap<ResourceLocation, ResourceLocation>();
	
	public static class DualResourceLocation extends ResourceLocation {
		private ResourceLocation alternate;
		
		public DualResourceLocation(ResourceLocation primary, ResourceLocation alternate) {
			super(primary.getNamespace(), primary.getPath());
			this.alternate = alternate;
		}
		
		public ResourceLocation getAlternate() {
			return alternate;
		}
	}
	
	public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
		ResourceLocation outputRes = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
		thickRingTextures.put(ringsRes, outputRes);
		return outputRes;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		
		for (Entry<ResourceLocation, ResourceLocation> entry : thickRingTextures.entrySet()) {
			
			if(entry.getKey() instanceof DualResourceLocation) {
				dualStitch(event, (DualResourceLocation) entry.getKey(), entry.getValue());
				continue;
			}
			
			ResourceLocation textureLocation = new ResourceLocation(entry.getValue().getNamespace(), String.format("%s/%s%s", event.getMap().getBasePath(), entry.getValue().getPath(), ".png"));
			
			IResource resource;
			try {
				resource = resourceManager.getResource(textureLocation);
			} catch (IOException e) {
				resource = null;
			}
			
			if (resource != null) {
				event.getMap().registerSprite(entry.getValue());
			} else {
				event.getMap().setTextureEntry(new ThickRingTextureAtlasSprite(entry.getValue(), entry.getKey()));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void dualStitch(TextureStitchEvent.Pre event, DualResourceLocation key, ResourceLocation value) {
		event.getMap().setTextureEntry(new ThickRingTextureAtlasSprite(value, key, key.getAlternate()));
	}

	
}
