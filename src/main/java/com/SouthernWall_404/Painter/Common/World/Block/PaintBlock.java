package com.SouthernWall_404.Painter.Common.World.Block;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaintBlock extends Block implements EntityBlock {

    public PaintBlock() {
        super(Properties.of().strength(-1).noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaintBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity) {
        return null;
    }

    // ===== 获取伪装状态 =====
    private BlockState getOrigin(BlockGetter level, BlockPos pos) {
        BlockEntity blockEntity=level.getBlockEntity(pos);
        if (blockEntity instanceof PaintBlockEntity entity) {
            BlockState state = entity.getOrigin();
            if (state != null&&!(state.getBlock() instanceof PaintBlock)) {
                return state;
            }
        }
        return Blocks.AIR.defaultBlockState();
    }


    // ===== 渲染外观（Forge 模型数据） =====
    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                    BlockState queryState, BlockPos queryPos) {

        return getOrigin(level, pos);
    }

//    @Override
//    protected RenderShape getRenderShape(BlockState state) {
//        return RenderShape.INVISIBLE; // 关键：不渲染模型
//    }





    // ===== 委托属性 =====
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {

        BlockState blockState= getOrigin(level, pos);
        return blockState.getSoundType(level, pos, entity);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getOrigin(level, pos).getFriction(level, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState blockState=getOrigin(level, pos);
        int light = blockState.getLightEmission();
//        System.out.println("Light emission at " + pos + " = " + light);
        return light;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getOrigin(level, pos).getShape(level, pos, context);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).getLightBlock(level, pos);
    }
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // 获取被伪装的状态
        BlockState origin = getOrigin(level, pos);
        // 返回被伪装方块的遮挡形状。如果是树叶，这将是一个完整方块。
        // 这确保了你的 PaintBlock 在宏观上能像树叶一样遮挡后面的区块。
        return origin.getOcclusionShape(level, pos);
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).propagatesSkylightDown(level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        getOrigin(level, pos).tick(level, pos, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        getOrigin(level, pos).randomTick(level, pos, random);
    }


    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return true;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return getOrigin(level, pos).canHarvestBlock(level, pos, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        getOrigin(level, pos).getBlock().playerDestroy(level, player, pos, state, blockEntity, tool);

        if (level.isClientSide) return;

        // 从 blockEntity 获取伪装状态
        if (!(blockEntity instanceof PaintBlockEntity paintBE)) return;
        BlockState origin = paintBE.getOrigin();

        // 构建 LootParams.Builder
        LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, player) // 玩家也可以作为上下文参数
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity); // 某些 loot table 可能需要

        // 获取掉落物品列表
        List<ItemStack> drops = origin.getDrops(builder);

        // 生成掉落物
        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
        }
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        getOrigin(level, pos).spawnAfterBreak(level, pos, stack, dropExperience);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getOrigin(level, pos).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
                                     LivingEntity entity, int numberOfParticles) {
        return getOrigin(level, pos).addLandingEffects(level, pos, state2, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return getOrigin(level, pos).addRunningEffects(level, pos, entity);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return getOrigin(level, pos).getEnchantPowerBonus(level, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getOrigin(level, pos).canEntityDestroy(level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        BlockState copyState = getOrigin(level, pos);
        copyState.getBlock().fallOn(level, copyState, pos, entity, fallDistance);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).getDestroyProgress(player, level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        getOrigin(level, pos).onRemove(level, pos, newState, movedByPiston);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        getOrigin(level, pos).onPlace(level, pos, oldState, movedByPiston);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        getOrigin(level, pos).attack(level, pos, player);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        getOrigin(level, pos).entityInside(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        getOrigin(level, pos).getBlock().stepOn(level, pos, state, entity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        getOrigin(level, pos).getBlock().setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).getShadeBrightness(level, pos);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getOrigin(level, pos).getSignal(level, pos, direction);
    }


    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getOrigin(level, pos).getDirectSignal(level, pos, direction);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getOrigin(level, pos).getAnalogOutputSignal(level, pos);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        getOrigin(level, pos).getBlock().destroy(level, pos, state);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return getOrigin(level, pos).isFireSource(level, pos, direction);
    }

    @Override
    public boolean isConduitFrame(BlockState state, LevelReader level, BlockPos pos, BlockPos conduit) {
        return getOrigin(level, pos).isConduitFrame(level, pos, conduit);
    }

    @Override
    public boolean isPortalFrame(BlockState state, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).isPortalFrame(level, pos);
    }





    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getOrigin(level, pos).getInteractionShape(level, pos);
    }


    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return getOrigin(level, pos).getBlock().canConnectRedstone(state, level, pos, direction);
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        return getOrigin(level, pos).getMapColor(level, pos);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getOrigin(level, pos).getFireSpreadSpeed( level, pos, direction);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockState origin=getOrigin(level, pos);
        return origin.getFlammability(level, pos, direction);
    }



    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return getOrigin(level, pos).getMenuProvider( level, pos);
    }


}