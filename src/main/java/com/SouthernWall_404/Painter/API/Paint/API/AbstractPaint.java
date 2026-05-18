// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.API.Wallpaper.BlockWallPaper;
import com.SouthernWall_404.Painter.API.Wallpaper.IWallpaper;
import com.SouthernWall_404.Painter.API.Wallpaper.Wallpapers;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.*;

/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */

/**
 * TODO 将material替换为Wallpaper类
 */
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {
    //LOGGER removed

    protected static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;



    //========不需要持久化的数据========
    private final float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private final BitSet shapeFlags = new BitSet(3);
    protected Map<Integer, ModelRender.AmbientOcclusionFace> aoFaces = new HashMap<>();
    protected Map<Integer,Boolean> visibles =new HashMap<>();
    protected Map<Integer, Vec3> renderVec=new HashMap<>();
    //========需要持久化的数据========
    protected Map<Integer, IWallpaper> wallpapers = new HashMap<>();
    protected Map<Integer,float[]> uvOffsets =new HashMap<>();

    //========构造方法========
    public AbstractPaint(BlockPos blockPos,String type) {
        super(blockPos,type);

        registerUVs();
        registerRenderVec();

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

    private void registerUVs()
    {
        if(uvOffsets==null)
        {
            uvOffsets=new HashMap<>();
        }

        flags.forEach((direction,flag)->{
            uvOffsets.put(flag,new float[]{0,0});
            uvOffsets.put(-flag,new float[]{0,0});
        });
    }

    /**
     * 循环切换指定方向面的纹理UV偏移位置
     * 使用origin面的xLength和yLength作为直接步长
     * 逻辑：先尝试增加U，如果U超出边界(1.0)，则尝试增加V；
     * 如果V也超出边界，则重置为(0,0)
     * 
     * 注意：此方法仅在客户端调用，通过数据包同步到服务端
     */
    @OnlyIn(Dist.CLIENT)
    public void cycleTextureUV(Direction direction) {
        int flag = getFlag(direction);
        // 获取指定方向的原始四边形以读取其UV空间尺寸
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        if (originQuads == null || originQuads.isEmpty()) {
            return;
        }
        BakedQuad originQuad = originQuads.get(0);
        VerticesInfo originVertices = new VerticesInfo(originQuad);
        float stepU = originVertices.getXLength();
        float stepV = originVertices.getYLength();
        // 如果长度为非正值，使用合理的默认值
        if (stepU <= 0f) stepU = 0f;
        if (stepV <= 0f) stepV = 0f;

        // 获取当前UV偏移量
        float[] current = uvOffsets.get(flag);
        if (current == null || current.length < 2) {
            current = new float[]{0f, 0f};
        }

        float currentU = current[0];
        float currentV = current[1];

        // 先尝试增加stepU
        float newU = currentU + stepU;
        float newV = currentV;

        // 如果U超出边界(1.0)，重置U并尝试增加stepV
        if (newU > 1.0f-stepU) {
            newU = 0f;
            newV = currentV + stepV;

            // 如果V也超出边界，将两者都重置为(0,0)
            if (newV > 1.0f-stepV) {
                newV = 0f;
            }
        }

        setUVOffset(direction, newU, newV);


//        uvOffsets.put(flag, new float[]{newU, newV});
        refresh();
    }

    public void cycleTextureDir(Direction direction) {
        BlockState material = getMaterial(direction);
        if (material != null) {
            if (material.hasProperty(TrapDoorBlock.HALF) && material.getOptionalValue(TrapDoorBlock.OPEN).orElse(false))
                paint(direction, material.cycle(TrapDoorBlock.HALF));
            else if (material.hasProperty(BlockStateProperties.FACING))
                paint(direction, material.cycle(BlockStateProperties.FACING));
            else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                paint(direction, material.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        material.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
            else if (material.hasProperty(BlockStateProperties.AXIS))
                paint(direction, material.cycle(BlockStateProperties.AXIS));
            else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                paint(direction, material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
            else if (material.hasProperty(BlockStateProperties.LIT))
                paint(direction, material.cycle(BlockStateProperties.LIT));
        }
        refresh();
    }

    public List<BakedQuad> getQuadsForDirection(BlockState state, int flag) {
        Direction direction;

        if(flag<0)
        {
            direction=null;
        }else direction=getDirection(flag);

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
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

    @OnlyIn(Dist.CLIENT)
    public void refreshVisible() {
        Level level = Minecraft.getInstance().level;
        if(level==null||origin==null) {
            visibles .clear();
            return;
        };
        flags.forEach((direction, flag) -> {
            boolean shouldRender = RenderUtil.shouldRenderFace(blockPos, origin, direction);
            visibles.put(flag, shouldRender);
            visibles.put(-flag,shouldRender);
        });
    }
    @OnlyIn(Dist.CLIENT)
    public void refreshAO() {
        Level level = Minecraft.getInstance().level;
        if (level==null){
            aoFaces.clear();
            return;
        }
        for (Map.Entry<Integer, IWallpaper> entry : wallpapers.entrySet()) {
            int flag = entry.getKey();
            Direction direction = getDirection(flag);

            BlockPos blockPos=this.blockPos;
            if(!hasNullInDirection( direction)){
                blockPos=blockPos.relative(direction);
            }

            ModelRender.AmbientOcclusionFace aoFace = new ModelRender.AmbientOcclusionFace();
            aoFace.calculate(level,origin,blockPos, direction, shape, shapeFlags, true);
            aoFaces.put(flag, aoFace);
        }
    }

    public Map<Integer, float[]> getUvOffsets() {
        return uvOffsets;
    }

    public void refresh() {
        visibles.clear();
        aoFaces.clear();
        objects.clear();;
//        PaintRender.setChanged();
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

    /**
     * 使用指定的 Wallpaper 创建渲染面
     * @param flag 方向标志
     * @param wallpaper 壁纸实例
     * @return 渲染四边形列表
     */
    public List<BakedQuad> createQuad(int flag, IWallpaper wallpaper)
    {
        if (wallpaper == null) return Collections.emptyList();
        
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        if (originQuads == null || originQuads.isEmpty()) return Collections.emptyList();

        return wallpaper.createQuad(originQuads.getFirst());
    }

    public void createQuads() {
        wallpapers.forEach((flag, wallpaper) -> {
            List<BakedQuad> quads = createQuad(flag, wallpaper);
            if(!quads.isEmpty())
            {
                objects.put(flag,quads);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockPos blockPos, PoseStack poseStack, VertexConsumer buffer) {
        Minecraft mc = Minecraft.getInstance();

        if (objects.isEmpty())createQuads();
        if(visibles.isEmpty())refreshVisible();
        if(aoFaces.isEmpty())refreshAO();

        for (Map.Entry<Integer, List<BakedQuad>> entry : objects.entrySet()) {
            List<BakedQuad> quads = entry.getValue();
            int flag = entry.getKey();

            if (!visibles.getOrDefault(flag,false))continue;

            Vec3 renderVec3 = renderVec.get(flag);
            if (renderVec3 == null) {
                registerRenderVec();
            }
            
            for (BakedQuad quad : quads) {
                if (aoFaces.containsKey(flag)) {
                    BakedQuadRender.renderInOfferredAO(quad, origin, renderVec3, poseStack, buffer, aoFaces.get(flag));
                } else {
                    refreshAO();
                    if (aoFaces.containsKey(flag))
                        BakedQuadRender.renderInOfferredAO(quad, origin, renderVec3, poseStack, buffer, aoFaces.get(flag));//TODO 这里会因为输入origin而产生着色问题
                }
            }
        }
    }

    public final void paint(Direction f, BlockState blockState) {

        putMaterial(f,blockState);
        refresh();
    }

    /**
     * 设置某个方向的UV偏移，供调试或扩展UI使用
     * @param direction 方向
     * @param u U分量偏移
     * @param v V分量偏移
     */
    public void setUVOffset(Direction direction, float u, float v) {
        int flag = getFlag(direction);
        float[] off = uvOffsets.get(flag);
        if (off == null || off.length < 2) {
            off = new float[]{0f, 0f};
        }
        off[0] = u;
        off[1] = v;
        uvOffsets.put(flag, off);

        refresh();
    }

    public void putMaterial(Direction f, BlockState blockState)
    {
        int flag = getFlag(f);
        if (blockState != null) {
            wallpapers.put(flag, new BlockWallPaper(blockState, f));
        } else {
            wallpapers.remove(flag);
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
        uvOffsets.remove(flag);
        uvOffsets.remove(-flag);
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

        // Persist UV offset data
        ListTag uvList = new ListTag();
        if (uvOffsets != null) {
            for (Map.Entry<Integer, float[]> e : uvOffsets.entrySet()) {
                CompoundTag uvTag = new CompoundTag();
                uvTag.putInt("flag", e.getKey());
                float[] arr = e.getValue();
                float u = (arr != null && arr.length > 0) ? arr[0] : 0f;
                float v = (arr != null && arr.length > 1) ? arr[1] : 0f;
                uvTag.putFloat("u", u);
                uvTag.putFloat("v", v);
                uvList.add(uvTag);
            }
        }
        tag.put("uvOffsets", uvList);
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

        // 清空并恢复UV偏移数据
        uvOffsets.clear();
        if (tag.contains("uvOffsets", Tag.TAG_LIST)) {
            ListTag uvList = tag.getList("uvOffsets", Tag.TAG_COMPOUND);
            for (int i = 0; i < uvList.size(); i++) {
                CompoundTag uvTag = uvList.getCompound(i);
                int flag = uvTag.getInt("flag");
                float u = uvTag.getFloat("u");
                float v = uvTag.getFloat("v");
                uvOffsets.put(flag, new float[]{u, v});
            }
        }
        
        // 确保所有方向标志都有对应的UV偏移条目，防止空指针异常
        if (uvOffsets.isEmpty()) {
            registerUVs();
        } else {
            flags.forEach((direction, f) -> {
                uvOffsets.computeIfAbsent(f, k -> new float[]{0f, 0f});
                uvOffsets.computeIfAbsent(-f, k -> new float[]{0f, 0f});
            });
        }
        
        // 触发渲染系统刷新，重建可见性、环境光遮蔽和四元面缓存
        refresh();
    }
}
