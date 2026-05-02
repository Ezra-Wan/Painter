# Painter 架构开发指南

本文档详细说明 Painter 模组的渲染架构设计、类继承关系以及如何扩展新的粉刷类型。

---

## 📐 架构概览

Painter 采用三层抽象架构，从接口到具体实现逐步细化：

```
IRender<T>                    ← 渲染接口（最顶层抽象）
    ↑
    | implements
    |
AbstractRender<T, F>          ← 渲染基类（通用渲染逻辑）
    ↑
    | extends
    |
AbstractPaint                 ← 粉刷基类（方块粉刷专用）
    ↑
    | extends
    |
┌───────────┬───────────────┐
|           |               |
SimpleBlock  SlabBlock      ← 具体实现（可扩展更多类型）
Paint        Paint
```

---

## 🏗️ 核心类详解

### 1. IRender<T> - 渲染接口

**位置**: `API/Paint/API/IRender.java`

**职责**: 定义渲染系统的基本契约

```java
public interface IRender<T extends Object> extends INBTSerializable<CompoundTag> {
    String getType();  // 获取类型标识
    
    void render(
        BlockPos blockPos,      // 方块位置
        PoseStack poseStack,    // 姿态栈（用于变换）
        int packedLight,        // 打包光照值
        int packedOverlay,      // 打包覆盖层值
        float partialTick,      // 部分刻（插值用）
        VertexConsumer vertexConsumer  // 顶点消费者
    );
}
```

**关键要点**:
- 泛型 `<T>` 表示渲染对象类型（如 `List<BakedQuad>`）
- 继承 `INBTSerializable` 支持 NBT 序列化
- 所有渲染类必须实现此接口

---

### 2. AbstractRender<T, F> - 渲染基类

**位置**: `API/Paint/API/AbstractRender.java`

**职责**: 提供通用的渲染基础设施

#### 核心数据结构

```java
public abstract class AbstractRender<T, F extends Object> implements IRender<T> {
    // 不需要持久化的数据（运行时缓存）
    protected Map<F, Integer> flags = new HashMap<>();      // 标志映射系统
    protected Map<Integer, T> objects = new HashMap<>();    // 渲染对象缓存
    protected String type;                                   // 类型标识
    protected BlockPos blockPos;                             // 方块位置
    protected Map<Integer, Vector3f> normals = new HashMap<>(); // 法线向量
    protected BlockState origin;                             // 原始方块状态
}
```

#### 关键方法

| 方法 | 作用 | 是否必须重写 |
|------|------|-------------|
| `initFlags()` | 初始化标志系统 | ✅ 是（abstract） |
| `registerFlag(F f, int flag)` | 注册标志映射 | ❌ 可选 |
| `update()` | 更新渲染状态 | ❌ 可选（已有默认实现） |
| `getFlag(F object)` | 获取标志值 | ❌ 否（final） |
| `serializeNBT()` | 序列化到 NBT | ❌ 可选（已有默认实现） |
| `deserializeNBT()` | 从 NBT 反序列化 | ❌ 可选（已有默认实现） |

#### 标志系统说明

标志系统用于将业务对象（如方向、面索引）映射为整数标识：

```java
// 示例：方向到整数的映射
registerFlag(Direction.NORTH, 1);   // NORTH → 1
registerFlag(Direction.SOUTH, 2);   // SOUTH → 2
registerFlag(Direction.WEST, 4);    // WEST → 4
// ...

// 使用时
int flag = getFlag(Direction.NORTH);  // 返回 1
```

**设计优势**:
- 解耦业务逻辑和底层标识
- 支持灵活的标志分配策略
- 便于序列化和网络传输

---

### 3. AbstractPaint - 粉刷基类

**位置**: `API/Paint/API/AbstractPaint.java`

**职责**: 专门处理方块粉刷的渲染逻辑

#### 类签名

```java
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction>
```

**泛型说明**:
- `T = List<BakedQuad>`: 渲染对象为 BakedQuad 列表（Minecraft 的面数据）
- `F = Direction`: 标志类型为方向（6个面）

#### 核心数据结构

```java
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {
    // 常量：方向标志值（位掩码风格）
    protected static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;
    
    // 不需要持久化的数据
    private final float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private final BitSet shapeFlags = new BitSet(3);
    protected Map<Integer, ModelRender.AmbientOcclusionFace> aoFaces = new HashMap<>();
    protected Map<Integer, Boolean> visibles = new HashMap<>();
    
    // 需要持久化的数据
    protected Map<Integer, BlockState> materials = new HashMap<>();  // 各方向的材质
    protected Map<Integer, float[]> uvOffsets = new HashMap<>();     // UV偏移量
}
```

#### 关键方法分类

##### 🔧 构造与初始化

```java
public AbstractPaint(BlockPos blockPos, String type) {
    super(blockPos, type);
    registerUVs();  // 初始化UV偏移
}

@Override
protected void registerFlag(Direction direction, int flag) {
    super.registerFlag(direction, flag);
    // 同时注册正负法线
    normals.put(flag, new Vector3f(direction));
    normals.put(-flag, new Vector3f(direction));
}
```

##### 🎨 粉刷操作

```java
// 粉刷指定方向的面
public final void paint(Direction f, BlockState blockState) {
    putMaterial(f, blockState);
    refresh();
}

// 设置材质
public void putMaterial(Direction f, BlockState blockState) {
    int flag = getFlag(f);
    materials.put(flag, blockState);
}

// 获取材质
public BlockState getMaterial(Direction f) {
    int flag = getFlag(f);
    return materials.get(flag);
}
```

##### 🔄 纹理操作

```java
// 循环切换UV偏移（实现纹理平铺效果）
public void cycleTextureUV(Direction direction) {
    // 计算步长并循环偏移
    // ...
}

// 循环切换方块属性（方向、轴向等）
public void cycleTextureDir(Direction direction) {
    // 智能检测并切换方块属性
    // ...
}
```

##### 👁️ 可见性与光照

```java
// 刷新可见性（视锥剔除）
@OnlyIn(Dist.CLIENT)
public void refreshVisible() {
    flags.forEach((direction, flag) -> {
        boolean shouldRender = RenderUtil.shouldRenderFace(blockPos, origin, direction);
        visibles.put(flag, shouldRender);
        visibles.put(-flag, shouldRender);
    });
}

// 刷新环境光遮蔽（AO）
@OnlyIn(Dist.CLIENT)
public void refreshAO() {
    for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
        int flag = entry.getKey();
        BlockState state = materials.get(flag);
        Direction direction = getDirection(flag);
        
        ModelRender.AmbientOcclusionFace aoFace = new ModelRender.AmbientOcclusionFace();
        aoFace.calculate(level, state, blockPos.relative(direction), direction, shape, shapeFlags, true);
        aoFaces.put(flag, aoFace);
    }
}

// 一键刷新所有缓存
public void refresh() {
    visibles.clear();
    aoFaces.clear();
    objects.clear();
    PaintRender.setChanged();  // 标记需要重绘
}
```

##### 🧱 Quad 生成

```java
// 核心方法：创建混合 Quad
public List<BakedQuad> createQuad(int flag, BlockState material) {
    Direction direction = getDirection(flag);
    
    // 1. 获取原始方块的 Quad（提供几何形状）
    List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
    
    // 2. 获取材质方块的 Quad（提供纹理）
    List<BakedQuad> materialQuads = getQuadsForDirection(material, Math.abs(getFlag(direction)));
    
    // 3. 混合两者：几何来自 origin，纹理来自 material
    List<BakedQuad> newQuads = new ArrayList<>();
    for (BakedQuad materialQuad : materialQuads) {
        // 逐顶点混合数据
        VerticeInfo[] mixedVertices = new VerticeInfo[4];
        for (int i = 0; i < 4; i++) {
            VerticeInfo originVert = originVertices.vertices.get(i);
            VerticeInfo materialVert = materialVertices.vertices.get(i);
            
            mixedVertices[i] = VerticeInfo.builder()
                .position(originVert.position)          // 几何位置
                .color(materialVert.alpha, ...)         // 颜色
                .uv(materialVert.u, materialVert.v)     // ★ 纹理坐标
                .light(originVert.light)                // 光照
                .normal(originVert.normal)              // 法线
                .build();
        }
        
        // 应用 UV 偏移
        newQuadVertices.implyUVOffest(uvOffset[0], uvOffset[1]);
        
        // 创建新 Quad
        BakedQuad newQuad = new BakedQuad(...);
        newQuads.add(newQuad);
    }
    
    return newQuads;
}

// 批量生成所有面的 Quad
public void createQuads() {
    materials.forEach((flag, material) -> {
        List<BakedQuad> quads = createQuad(flag, material);
        if (!quads.isEmpty()) {
            objects.put(flag, quads);
        }
    });
}
```

##### 🎬 渲染执行

```java
@Override
@OnlyIn(Dist.CLIENT)
public void render(BlockPos blockPos, PoseStack poseStack, int packedLight, 
                   int packedOverlay, float partialTick, VertexConsumer buffer) {
    Minecraft mc = Minecraft.getInstance();
    Level level = mc.level;
    double offset = Configs.getValue(Painter.MODID, ClientConfig.PAINT_OFFSET).get();
    
    // 懒加载：首次渲染时生成数据
    if (objects.isEmpty()) createQuads();
    if (visibles.isEmpty()) refreshVisible();
    if (aoFaces.isEmpty()) refreshAO();
    
    // 遍历所有面进行渲染
    for (Map.Entry<Integer, List<BakedQuad>> entry : objects.entrySet()) {
        List<BakedQuad> quads = entry.getValue();
        int flag = entry.getKey();
        Vector3f normal = normals.get(flag);
        
        BlockState material = materials.get(flag);
        Direction direction = getDirection(flag);
        
        // 可见性剔除
        if (!visibles.getOrDefault(flag, false)) continue;
        
        // 计算偏移位置（避免 Z-fighting）
        Vec3 vec3 = new Vec3(
            blockPos.getX() + offset * normal.getX(),
            blockPos.getY() + offset * normal.getY(),
            blockPos.getZ() + offset * normal.getZ()
        );
        
        // 渲染每个 Quad（带 AO）
        for (BakedQuad quad : quads) {
            if (aoFaces.containsKey(flag)) {
                BakedQuadRender.renderInOfferredAO(quad, material, vec3, poseStack, buffer, aoFaces.get(flag));
            }
        }
    }
}
```

##### 💾 序列化

```java
@Override
public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag tag = super.serializeNBT(provider);
    
    // 序列化 materials
    ListTag materialsList = new ListTag();
    for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
        CompoundTag entryTag = new CompoundTag();
        entryTag.putInt("flag", entry.getKey());
        DataResult<Tag> stateResult = BlockState.CODEC.encode(
            entry.getValue(),
            provider.createSerializationContext(NbtOps.INSTANCE),
            new CompoundTag()
        );
        entryTag.put("state", stateResult.getOrThrow());
        materialsList.add(entryTag);
    }
    tag.put("materials", materialsList);
    
    // 序列化 UV 偏移
    ListTag uvList = new ListTag();
    for (Map.Entry<Integer, float[]> e : uvOffsets.entrySet()) {
        CompoundTag uvTag = new CompoundTag();
        uvTag.putInt("flag", e.getKey());
        float[] arr = e.getValue();
        uvTag.putFloat("u", arr[0]);
        uvTag.putFloat("v", arr[1]);
        uvList.add(uvTag);
    }
    tag.put("uvOffsets", uvList);
    
    return tag;
}

@Override
public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
    super.deserializeNBT(provider, tag);
    
    // 反序列化 materials
    this.materials.clear();
    ListTag materialsList = tag.getList("materials", Tag.TAG_COMPOUND);
    for (int i = 0; i < materialsList.size(); i++) {
        CompoundTag entryTag = materialsList.getCompound(i);
        int flag = entryTag.getInt("flag");
        Tag stateTag = entryTag.get("state");
        DataResult<BlockState> stateResult = BlockState.CODEC.parse(
            provider.createSerializationContext(NbtOps.INSTANCE),
            stateTag
        );
        BlockState state = stateResult.getOrThrow();
        materials.put(flag, state);
    }
    
    // 反序列化 UV 偏移
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
    
    // 确保所有方向都有 UV 条目
    if (uvOffsets.isEmpty()) {
        registerUVs();
    } else {
        flags.forEach((direction, f) -> {
            uvOffsets.computeIfAbsent(f, k -> new float[]{0f, 0f});
            uvOffsets.computeIfAbsent(-f, k -> new float[]{0f, 0f});
        });
    }
    
    refresh();
}
```

---

## 🧩 具体实现示例

### 示例 1: SimpleBlockPaint - 普通方块

**位置**: `API/Paint/Imply/SimpleBlockPaint.java`

**特点**: 最简单的实现，所有6个面都可粉刷

```java
public class SimpleBlockPaint extends AbstractPaint {
    
    public SimpleBlockPaint(BlockPos blockPos) {
        super(blockPos, PaintContent.SIMPLE_BLOCK);
    }
    
    @Override
    public boolean hasNullInDirection(Direction f) {
        // 所有方向都有效，没有空面
        return false;
    }
}
```

**适用场景**: 
- 完整碰撞箱的方块（石头、木头、玻璃等）
- 无需特殊处理的常规方块

---

### 示例 2: SlabBlockPaint - 台阶方块

**位置**: `API/Paint/Imply/SlabBlockPaint.java`

**特点**: 根据台阶类型（上半/下半）禁用无效面

```java
public class SlabBlockPaint extends AbstractPaint {
    
    // 额外数据：台阶类型
    private SlabType slabType;
    
    public SlabBlockPaint(BlockPos blockPos, SlabType slabType) {
        super(blockPos, PaintContent.SLAB_BLOCK);
        this.slabType = slabType;
    }
    
    @Override
    public boolean hasNullInDirection(Direction f) {
        // 上半台阶：下面不可粉刷
        if (slabType == SlabType.TOP && f == Direction.DOWN) return true;
        
        // 下半台阶：上面不可粉刷
        if (slabType == SlabType.BOTTOM && f == Direction.UP) return true;
        
        return false;
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        // 保存台阶类型
        if (slabType != null) {
            tag.putString("slab_type", slabType.name());
        }
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        super.deserializeNBT(provider, compoundTag);
        
        // 恢复台阶类型
        if (compoundTag.contains("slab_type", CompoundTag.TAG_STRING)) {
            String typeName = compoundTag.getString("slab_type");
            try {
                slabType = SlabType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                slabType = SlabType.TOP; // fallback
            }
        } else {
            slabType = SlabType.TOP; // 兼容旧数据
        }
        
        update();
    }
}
```

**适用场景**:
- 台阶、楼梯等非完整方块
- 需要根据状态禁用某些面的情况

---

## 🔌 扩展新粉刷类型

### 步骤 1: 创建新类

继承 `AbstractPaint` 并实现必要方法：

```java
package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

public class StairBlockPaint extends AbstractPaint {
    
    // 如果需要额外数据，在这里声明
    private Half half;
    private Shape shape;
    
    public StairBlockPaint(BlockPos blockPos) {
        super(blockPos, PaintContent.STAIR_BLOCK);  // 使用新的类型标识
    }
    
    @Override
    public boolean hasNullInDirection(Direction f) {
        // 根据楼梯状态判断哪些面无效
        // 例如：某些情况下背面不可见
        return false;  // 或根据逻辑返回 true/false
    }
    
    // 如果有额外数据，需要重写序列化方法
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        
        // 保存额外数据
        if (half != null) {
            tag.putString("half", half.name());
        }
        if (shape != null) {
            tag.putString("shape", shape.name());
        }
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        
        // 恢复额外数据
        if (tag.contains("half", CompoundTag.TAG_STRING)) {
            half = Half.valueOf(tag.getString("half"));
        }
        if (tag.contains("shape", CompoundTag.TAG_STRING)) {
            shape = Shape.valueOf(tag.getString("shape"));
        }
        
        update();
    }
}
```

---

### 步骤 2: 注册类型标识

在 `PaintContent.java` 中添加新类型：

```java
public class PaintContent {
    
    public static final String SIMPLE_BLOCK = "simple_block";
    public static final String SLAB_BLOCK = "slab_block";
    public static final String STAIR_BLOCK = "stair_block";  // ← 新增
    
    private static Map<String, Function<BlockPos, AbstractRender>> RENDERS = new HashMap<>();
    
    static {
        RENDERS.put(SIMPLE_BLOCK, (pos -> new SimpleBlockPaint(pos)));
        RENDERS.put(SLAB_BLOCK, (pos -> new SlabBlockPaint(pos)));
        RENDERS.put(STAIR_BLOCK, (pos -> new StairBlockPaint(pos)));  // ← 新增
    }
    
    // ... 其他方法保持不变
}
```

---

### 步骤 3: 集成到粉刷逻辑

在 `PaintOperationHelper.java` 中添加对新类型的检测：

```java
public class PaintOperationHelper {
    
    public static void paintBlock(Level level, BlockPos blockPos, Direction direction, BlockState material) {
        PaintInfo paintInfo = level.getChunkAt(blockPos).getData(ModAttachments.PAINT_INFO);
        
        if (paintInfo.getPaints().containsKey(blockPos)) {
            // 已存在，直接粉刷
            AbstractPaint paint = paintInfo.getPaints().get(blockPos);
            paint.paint(direction, material);
        } else {
            BlockState origin = level.getBlockState(blockPos);
            
            if (!PaintValidHelper.isPaintable(origin, level, blockPos)) {
                return;
            }
            
            AbstractPaint paint;
            
            // 检测方块类型并创建对应的 Paint
            if (origin.getBlock() instanceof SlabBlock) {
                SlabType slabType = origin.getValue(SlabBlock.TYPE);
                paint = new SlabBlockPaint(blockPos, slabType);
            } else if (origin.getBlock() instanceof StairBlock) {  // ← 新增
                paint = new StairBlockPaint(blockPos);
            } else if (origin.isCollisionShapeFullBlock(level, blockPos)) {
                paint = new SimpleBlockPaint(blockPos);
            } else {
                return;  // 不支持的类型
            }
            
            paint.paint(direction, material);
            paintInfo.putPaints(level, blockPos, paint);
        }
        
        level.getChunkAt(blockPos).setUnsaved(true);
    }
}
```

---

## 🎯 高级定制

### 1. 自定义标志系统

如果需要使用非方向的标志（如多面体、自定义网格）：

```java
public class CustomPaint extends AbstractRender<List<BakedQuad>, Integer> {
    
    @Override
    public void initFlags() {
        // 自定义标志：例如 8 个角
        registerFlag(0, 1);   // 角 0
        registerFlag(1, 2);   // 角 1
        registerFlag(2, 4);   // 角 2
        // ...
    }
    
    @Override
    public void render(BlockPos blockPos, PoseStack poseStack, int packedLight, 
                      int packedOverlay, float partialTick, VertexConsumer buffer) {
        // 自定义渲染逻辑
    }
}
```

---

### 2. 自定义渲染对象类型

如果不使用 BakedQuad，可以使用其他渲染数据结构：

```java
public class MeshPaint extends AbstractRender<MeshData, Integer> {
    
    @Override
    public void initFlags() {
        // 初始化标志
    }
    
    @Override
    public void render(BlockPos blockPos, PoseStack poseStack, int packedLight, 
                      int packedOverlay, float partialTick, VertexConsumer buffer) {
        // 直接渲染 MeshData
        MeshData mesh = objects.get(someFlag);
        mesh.renderTo(buffer);
    }
}
```

---

### 3. 性能优化技巧

#### 懒加载缓存

```java
@Override
public void render(...) {
    // 只在需要时生成数据
    if (objects.isEmpty()) createQuads();
    if (visibles.isEmpty()) refreshVisible();
    if (aoFaces.isEmpty()) refreshAO();
    
    // 渲染...
}
```

#### 增量更新

```java
public void paint(Direction f, BlockState material) {
    putMaterial(f, material);
    
    // 只清除受影响的数据，而非全部
    objects.remove(getFlag(f));
    aoFaces.remove(getFlag(f));
    
    PaintRender.setChanged();
}
```

#### 对象复用

```java
// 使用对象池避免频繁创建
private static final ObjectPool<AmbientOcclusionFace> aoPool = new ObjectPool<>();

public void refreshAO() {
    for (int flag : materials.keySet()) {
        AmbientOcclusionFace aoFace = aoPool.acquire();  // 从池中获取
        aoFace.calculate(...);
        aoFaces.put(flag, aoFace);
    }
}
```

---

## 🐛 常见问题与调试

### Q1: 粉刷后不显示？

**检查清单**:
1. ✅ 调用了 `PaintRender.setChanged()` 吗？
2. ✅ `refresh()` 清除了缓存吗？
3. ✅ 可见性是否正确？（检查 `visibles` Map）
4. ✅ 区块是否正确保存？（`chunk.setUnsaved(true)`）

**调试方法**:
```java
@Override
public void render(...) {
    Painter.LOGGER.info("Rendering paint at {}, objects={}, visibles={}", 
        blockPos, objects.size(), visibles.size());
    // ...
}
```

---

### Q2: 纹理错位或拉伸？

**可能原因**:
1. UV 偏移计算错误
2. Quad 顶点顺序不正确
3. 原始方块和材质方块的 UV 空间不匹配

**解决方案**:
```java
// 检查 UV 偏移
Painter.LOGGER.info("UV Offset for {}: [{}, {}]", direction, uvOffset[0], uvOffset[1]);

// 验证 Quad 顶点
VerticesInfo info = new VerticesInfo(quad);
for (VerticeInfo vert : info.vertices) {
    Painter.LOGGER.info("Vertex: pos=({}, {}, {}), uv=({}, {})", 
        vert.position.x, vert.position.y, vert.position.z,
        vert.u, vert.v);
}
```

---

### Q3: 存档后重新加载丢失数据？

**检查清单**:
1. ✅ `serializeNBT()` 保存了所有必要数据吗？
2. ✅ `deserializeNBT()` 正确恢复了数据吗？
3. ✅ 类型标识在 `PaintContent` 中注册了吗？
4. ✅ 调用了 `super.serializeNBT/deserializeNBT()` 吗？

**调试方法**:
```java
@Override
public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag tag = super.serializeNBT(provider);
    Painter.LOGGER.info("Serializing paint: type={}, materials={}", type, materials.size());
    return tag;
}

@Override
public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
    super.deserializeNBT(provider, tag);
    Painter.LOGGER.info("Deserialized paint: type={}, materials={}", type, materials.size());
}
```

---

### Q4: 性能问题（FPS 下降）？

**优化建议**:
1. 确保使用了视锥剔除（`visibles`）
2. 避免每帧重新生成 Quad（使用 `objects` 缓存）
3. 减少不必要的 `refresh()` 调用
4. 使用 AO 缓存（`aoFaces`）

**性能监控**:
```java
private long lastRenderTime = 0;

@Override
public void render(...) {
    long start = System.nanoTime();
    
    // 渲染逻辑...
    
    long elapsed = System.nanoTime() - start;
    if (elapsed > 1_000_000) {  // 超过 1ms
        Painter.LOGGER.warn("Slow render at {}: {}ms", blockPos, elapsed / 1_000_000.0);
    }
}
```

---

## 📚 最佳实践

### 1. 数据分离原则

- **持久化数据**: 放在 `materials`、`uvOffsets` 等 Map 中
- **运行时缓存**: 放在 `objects`、`visibles`、`aoFaces` 中
- **配置数据**: 通过构造函数参数传入

### 2. 线程安全

- 所有客户端渲染方法添加 `@OnlyIn(Dist.CLIENT)`
- 避免在渲染线程修改持久化数据
- 使用 `PaintRender.setChanged()` 标记变更，下一帧统一处理

### 3. 内存管理

- 及时清除无用缓存（`refresh()` 方法）
- 避免在循环中创建临时对象
- 使用对象池复用重型对象（如 `AmbientOcclusionFace`）

### 4. 兼容性

- 始终提供默认值（fallback）
- 序列化格式变更时考虑向后兼容
- 使用类型标识字符串而非常量硬编码

### 5. 代码组织

```
YourCustomPaint.java
├── 常量定义
├── 字段声明（分持久化/非持久化）
├── 构造方法
├── 抽象方法实现（hasNullInDirection）
├── 业务方法（paint, cycleTextureUV 等）
├── 辅助方法（getQuadsForDirection 等）
├── 渲染方法（render）
└── 序列化方法（serializeNBT, deserializeNBT）
```

---

## 🔗 相关资源

- [NeoForge 文档](https://docs.neoforged.net/)
- [Minecraft Wiki - BakedModel](https://minecraft.fandom.com/wiki/BakedModel)
- [Laplace API 文档](https://github.com/SouthernWall_404/LaplaceAPI)
- [Painter 源码](https://github.com/SouthernWall_404/Painter)

---

<div align="center">

**Happy Coding! 🎨**

如有问题，请提交 [GitHub Issue](https://github.com/SouthernWall_404/Painter/issues)

</div>
