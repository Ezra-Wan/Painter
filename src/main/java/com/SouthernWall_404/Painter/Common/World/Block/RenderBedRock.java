package com.SouthernWall_404.Painter.Common.World.Block;

import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.RenderBedRockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import org.jetbrains.annotations.Nullable;

public class RenderBedRock extends Block implements EntityBlock {

    public RenderBedRock() {
        super(Properties.of().strength(-1));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RenderBedRockEntity(pos, state);
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
    private BlockState getCopyState(BlockGetter level, BlockPos pos) {
        BlockEntity blockEntity=level.getBlockEntity(pos);
        if (blockEntity instanceof RenderBedRockEntity entity) {
            BlockState state = entity.getCopyState();
            if (state != null) {
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

//            if (isIgnoredConnectivitySide(level, state, side, pos, queryPos))
//                return state;

            ModelDataManager modelDataManager = level.getModelDataManager();
            if (modelDataManager == null)
                return getCopyState(level,pos);
            return CopycatModel.getMaterial(modelDataManager.getAt(pos));

    }

    // ===== 委托属性 =====
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {

        BlockState blockState=getCopyState(level, pos);
        return blockState.getSoundType(level, pos, entity);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getCopyState(level, pos).getFriction(level, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState blockState=getCopyState(level, pos);
        return blockState.getLightEmission();
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return getCopyState(level, pos).canHarvestBlock(level, pos, player);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getCopyState(level, pos).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
                                     LivingEntity entity, int numberOfParticles) {
        return getCopyState(level, pos).addLandingEffects(level, pos, state2, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return getCopyState(level, pos).addRunningEffects(level, pos, entity);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return getCopyState(level, pos).getEnchantPowerBonus(level, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getCopyState(level, pos).canEntityDestroy(level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        BlockState copyState = getCopyState(level, pos);
        copyState.getBlock().fallOn(level, copyState, pos, entity, fallDistance);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return getCopyState(level, pos).getDestroyProgress(player, level, pos);
    }
}