// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.API.Wallpaper.BlockWallPaper;
import com.SouthernWall_404.Painter.API.Wallpaper.IWallpaper;
import com.SouthernWall_404.Painter.API.Wallpaper.Wallpapers;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
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
import net.minecraft.world.level.block.Blocks;
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



    //========不需要持久化的数据========
    protected Map<Integer, Vec3> renderVec=new HashMap<>();
    //========需要持久化的数据========
    protected Map<Integer, IWallpaper> wallpapers = new HashMap<>();

    //========构造方法========
    public AbstractPaint(BlockPos blockPos,String type) {
        super(blockPos,type);

        registerRenderVec();

    }


    @Override
    public BlockState getOrigin() {
        return super.getOrigin();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }


    public Map<Integer, Vec3> getRenderVec() {
        return renderVec;
    }

    @OnlyIn(Dist.CLIENT)
    public void cycleTextureUV(Direction direction) {
        int flag=getFlag( direction);
        IWallpaper wallpaper=getWallpaper( direction);
        wallpaper.cycleTextureUV(RenderUtil.getQuadsForDirection(getOrigin(),direction).getFirst(),direction ,blockPos );
    }

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
    @Override
    protected void registerFlag(Direction direction, int flag) {
        super.registerFlag(direction, flag);

        normals.put(flag,new Vector3f(direction));
        normals.put(-flag,new Vector3f(direction));

    }

    //TODO 记得添加对于同类材质的旋转兼容
    public void cycleTextureDir(Direction face)
    {
        IWallpaper wallpaper=wallpapers.get(getFlag(face));
        if(wallpaper!=null){
            if(wallpaper instanceof BlockWallPaper blockWallPaper)blockWallPaper.cycleTextureDir();
        }
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

    public Direction getDirection(int flag) {
        if(flag<0)flag=-flag;
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            if (entry.getValue() == flag) {
                return entry.getKey();
            }
        }
        return Direction.NORTH;
    }

    public void refresh() {
        wallpapers.forEach((flag, wallpaper) -> wallpaper.refresh());
    }
    @Override
    public void initFlags() {
        registerFlag(Direction.NORTH, NORTH);
        registerFlag(Direction.SOUTH, SOUTH);
        registerFlag(Direction.WEST, WEST);
        registerFlag(Direction.EAST, EAST);
        registerFlag(Direction.UP, UP);
        registerFlag(Direction.DOWN, DOWN);


    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockPos blockPos, PoseStack poseStack, VertexConsumer buffer) {
        Minecraft mc = Minecraft.getInstance();

        wallpapers.forEach((flag, wallpaper) -> {
            wallpaper.render(this, getDirection(flag),poseStack, buffer);
        });
    }


    public void paint(Direction f, BlockState material) {
        paint(f,new BlockWallPaper(material,f));
    }
    public final void paint(Direction f,IWallpaper wallpaper) {

        putMaterial(f,wallpaper);
        refresh();
    }

    public void putMaterial(Direction f, IWallpaper wallpaper)
    {
        int flag = getFlag(f);
        if (wallpaper != null) {
            wallpapers.put(flag, wallpaper);
        }
    }

    /**
     * 移除指定方向的材质
     * @param f 方向
     */
    public void removeMaterial(Direction f) {
        int flag = getFlag(f);
        wallpapers.remove(flag);
        wallpapers.remove(-flag);
        refresh();
    }

    public abstract boolean hasNullInDirection(Direction f);


    public BlockState getMaterial(Direction f) {
        IWallpaper wallpaper = getWallpaper(f);
        if (wallpaper instanceof BlockWallPaper blockWallPaper) {
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    public IWallpaper getWallpaper(Direction f) {
        int flag = getFlag(f);
        return wallpapers.get(flag);
    }

    /**
     * 获取所有壁纸的映射
     * @return 壁纸映射
     */
    public Map<Integer, IWallpaper> getWallpapers() {
        return wallpapers;
    }

    @Override
    public int getFlag(Direction object) {
        if(hasNullInDirection(object))return -super.getFlag(object);
        return super.getFlag(object);
    }
    //========覆盖序列化方法，处理 wallpapers========

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

    /**
     * 从NBT标签反序列化粉刷数据，恢复壁纸映射和UV偏移信息
     * <p>
     * 该方法执行以下操作：
     * 1. 调用父类反序列化方法恢复基础数据
     * 2. 解析并恢复各方向的壁纸实例（wallpapers）
     * 3. 解析并恢复各方向的UV纹理偏移量（uvOffsets）
     * 4. 确保所有方向标志都有对应的UV偏移条目
     * 5. 触发刷新以重建渲染缓存
     *
     * @param provider 注册表提供者，用于IWallpaper的编解码上下文
     * @param tag      包含序列化数据的NBT复合标签，应包含"wallpapers"和"uvOffsets"列表
     */
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
}
