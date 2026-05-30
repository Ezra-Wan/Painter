package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import com.SouthernWall_404.Painter.Common.Config.ModConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

/**
 * 油漆桶选区模式配置附件（玩家级别）
 * 用于临时切换油漆桶的选区限制模式
 */
public class BucketSelectionConfig implements IAttachment {
    
    // null = 使用默认值，true = 严格模式，false = 自由模式
    private Boolean requiresSelection = null;
    
    public BucketSelectionConfig() {
    }
    
    /**
     * 获取当前是否需要选区
     * @return true=需要选区（严格模式），false=不需要选区（自由模式）
     */
    public boolean isRequiresSelection() {
        return requiresSelection != null 
            ? requiresSelection 
            : ModConfig.getBucketRequiresSelectionDefault();
    }
    
    /**
     * 设置是否需要选区
     * @param value true=严格模式，false=自由模式
     */
    public void setRequiresSelection(boolean value) {
        this.requiresSelection = value;
    }
    
    /**
     * 切换模式并返回新状态
     * @return 切换后的状态
     */
    public boolean toggle() {
        if (this.requiresSelection == null) {
            // 第一次切换：从默认值切换到相反值
            this.requiresSelection = !ModConfig.getBucketRequiresSelectionDefault();
        } else {
            // 后续切换：直接取反
            this.requiresSelection = !this.requiresSelection;
        }
        return this.requiresSelection;
    }
    
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        // 不序列化，会话级配置
        return new CompoundTag();
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        // 不反序列化，重置为默认状态
        this.requiresSelection = null;
    }
}
