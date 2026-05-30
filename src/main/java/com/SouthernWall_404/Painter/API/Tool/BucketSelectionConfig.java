package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

/**
 * 油漆桶选区依赖配置附件
 * 用于存储每个玩家的油漆桶是否需要选区才能使用的配置状态
 * 
 * @author EzraW
 * @version 1.0
 */
public class BucketSelectionConfig implements IAttachment {

    /**
     * 默认行为：油漆桶必须依赖选区才能使用
     */
    private boolean requiresSelection = true;

    /**
     * 无参构造函数，供附件系统自动创建实例
     */
    public BucketSelectionConfig() {
    }

    /**
     * 获取当前是否需要选区
     * 
     * @return true表示需要选区，false表示不需要选区
     */
    public boolean isRequiresSelection() {
        return requiresSelection;
    }

    /**
     * 设置是否需要选区
     * 
     * @param requiresSelection true表示需要选区，false表示不需要选区
     */
    public void setRequiresSelection(boolean requiresSelection) {
        this.requiresSelection = requiresSelection;
    }

    /**
     * 切换当前的选区依赖状态
     * 
     * @return 切换后的新状态
     */
    public boolean toggleRequiresSelection() {
        this.requiresSelection = !this.requiresSelection;
        return this.requiresSelection;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("requiresSelection", requiresSelection);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.requiresSelection = tag.getBoolean("requiresSelection");
    }
}
