package com.SouthernWall_404.Painter.API.Paint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public abstract class AbstractPaint extends AbstractRender<TextureAtlasSprite>{


    //========构造方法=========
    public AbstractPaint(BlockState origin,String type) {
        super(origin,type);
    }

    //========内部方法========

    @Override
    protected void update() {
        for(Map.Entry<Integer,ResourceLocation> entry:paintPaths.entrySet())
        {
            int flag=entry.getKey();
            ResourceLocation key=entry.getValue();

            TextureAtlasSprite texture=getTexture(key);

            objects.put(flag,texture);

        }
    }


    //========业务方法========


    public static TextureAtlasSprite getTexture(ResourceLocation key)
    {
       return  Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(key);
    }



}
