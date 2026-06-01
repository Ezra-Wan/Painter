// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.API.Wallpaper.BlockWallPaper;
import com.SouthernWall_404.Painter.API.Wallpaper.IWallpaper;
import com.SouthernWall_404.Painter.API.Wallpaper.Wallpapers;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */

/**
 * 方块位置的wallPaper转包
 * - 只负责存储相关方块数据
 * - 不负责具体的渲染实现
 */
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {
    //LOGGER removed

    protected static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;



    //========缓存========
    protected Map<Integer, Vec3> renderVec=new HashMap<>();
    //========数据========
    protected Map<Integer, IWallpaper> wallpapers = new HashMap<>();

    //========构造方法========
    public AbstractPaint(BlockPos blockPos,String type) {
        super(blockPos,type);

        registerRenderVec();

    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        // 序列化 wallpapers
        ListTag wallpapersList = new ListTag();
        for (Map.Entry<Integer, IWallpaper> entry : wallpapers.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("flag", entry.getKey());
            IWallpaper wallpaper = entry.getValue();
            if (wallpaper != null) {
                CompoundTag wallpaperTag = wallpaper.serializeNBT(provider);
                wallpaperTag.putString("type", wallpaper.getType());
                entryTag.put("wallpaper", wallpaperTag);
                wallpapersList.add(entryTag);
            }
        }
        tag.put("wallpapers", wallpapersList);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);

        // 清空并恢复壁纸映射数据
        this.wallpapers.clear();
        ListTag wallpapersList = tag.getList("wallpapers", Tag.TAG_COMPOUND);
        for (int i = 0; i < wallpapersList.size(); i++) {
            CompoundTag entryTag = wallpapersList.getCompound(i);
            int flag = entryTag.getInt("flag");
            CompoundTag wallpaperTag = entryTag.getCompound("wallpaper");
            String type = wallpaperTag.getString("type");

            IWallpaper wallpaper = Wallpapers.create(type, provider, wallpaperTag);
            if (wallpaper != null) {
                wallpapers.put(flag, wallpaper);
            }
        }

        // 触发渲染系统刷新，重建可见性、环境光遮蔽和四元面缓存
        refresh();
    }

    /**
     * 创建渲染向量
     * 缓存以避免重复计算
     */
    protected void registerRenderVec()
    {
        if(renderVec==null)
        {
            renderVec=new HashMap<>();
        }
        renderVec.clear();

        flags.forEach((direction,flag)->{
            Vector3f normal = normals.get(flag);
            double offset = (double)Configs.getValue(Painter.MODID, ClientConfig.PAINT_OFFSET).get();
            Vec3 vec = new Vec3(
                    blockPos.getX() + offset * normal.getX(),
                    blockPos.getY() + offset * normal.getY(),
                    blockPos.getZ() + offset * normal.getZ()
            );
            renderVec.put(flag, vec);
            renderVec.put(-flag, vec);
        });
    }
    /**
     * 建立标识码系统，以区分方向，处理null方向面
     */
    @Override
    public void initFlags() {
        registerFlag(Direction.NORTH, NORTH);
        registerFlag(Direction.SOUTH, SOUTH);
        registerFlag(Direction.WEST, WEST);
        registerFlag(Direction.EAST, EAST);
        registerFlag(Direction.UP, UP);
        registerFlag(Direction.DOWN, DOWN);
    }

    /**
     * 辅助方法，用于建立flag
     * @param direction
     * @param flag
     */
    @Override
    protected void registerFlag(Direction direction, int flag) {
        super.registerFlag(direction, flag);

        normals.put(flag,new Vector3f(direction));
        normals.put(-flag,new Vector3f(direction));

    }
    //=========基本方法========


    public BlockPos getBlockPos() {
        return blockPos;
    }


    public Map<Integer, Vec3> getRenderVec() {
        return renderVec;
    }
    //----标识码和方向处理----
    /**
     * 获取标识码
     * @param object
     * @return
     */
    @Override
    public int getFlag(Direction object) {
        if(hasNullInDirection(object))return -super.getFlag(object);
        return super.getFlag(object);
    }
    public Direction getDirection(int flag) {
        if(flag<0)flag=-flag;
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            if (entry.getValue() == flag) {
                return entry.getKey();
            }
        }
        return Direction.NORTH;
    }
    /**
     *  用于将渲染的方块面转换为实际归属的方块面，即处理null面的情况
     * @param visualFace
     * @return
     */
    public Direction translateFace(Direction visualFace){

        if(hasNullInDirection(visualFace))return null;
        return visualFace;
    }

    //----wallpaper处理----
    public void putWallpaper(Direction f, IWallpaper wallpaper)
    {
        int flag = getFlag(f);
        if (wallpaper != null) {
            wallpapers.put(flag, wallpaper);
        }
        refresh();
    }
    public void putWallpaper(Direction f, BlockState material) {
        putWallpaper(f,new BlockWallPaper(material,f));
    }

    public IWallpaper getWallpaper(Direction f) {
        int flag = getFlag(f);
        return wallpapers.get(flag);
    }

    public BlockState getMaterial(Direction f) {
        IWallpaper wallpaper = getWallpaper(f);
        if (wallpaper instanceof BlockWallPaper blockWallPaper) {
            return blockWallPaper.getMaterial();
        }
        return null;
    }


    /**
     * 获取所有壁纸的映射
     * @return 壁纸映射
     */
    public Map<Integer, IWallpaper> getWallpapers() {
        return wallpapers;
    }
    /**
     * 移除指定方向的材质
     * @param f 方向
     */
    public void removeWallpaper(Direction f) {
        int flag = getFlag(f);
        wallpapers.remove(flag);
        wallpapers.remove(-flag);
        refresh();
    }
    public abstract boolean hasNullInDirection(Direction f);

    //=========刷新方法========
    public void refresh() {
        wallpapers.forEach((flag, wallpaper) -> wallpaper.refresh());
    }

    //========业务方法========
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockPos blockPos, PoseStack poseStack, VertexConsumer buffer) {
        Minecraft mc = Minecraft.getInstance();

        wallpapers.forEach((flag, wallpaper) -> {
            wallpaper.render(this, getDirection(flag),poseStack, buffer);
        });
    }


}
