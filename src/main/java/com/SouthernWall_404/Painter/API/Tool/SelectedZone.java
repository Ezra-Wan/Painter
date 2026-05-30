package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import com.SouthernWall_404.Painter.API.Tool.Wall.SelectedQuad;
import com.SouthernWall_404.Painter.Client.SelectedZoneRender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

/**
 * 数据交互类，会与各种外部方法交互
 */
public class SelectedZone implements IAttachment {

    private List<SelectedQuad> quads=new ArrayList<>();//建立的所有选区
    private Map<Direction, Set<BlockPos>> contains=new HashMap<>();//所有被选中的位置

    private BlockPos cachedA;//缓存的选区A位置
    private Direction cachedFace;//缓存选区的朝向


    //========业务方法========
    public void setA(BlockPos a, Direction face)
    {
        cachedA=a;
        cachedFace=face;
    }

    /**
     * 设置选区B点
     * @param b B点位置
     * @return 是否成功创建选区（两点在同一平面）
     */
    public boolean setB(BlockPos b)
    {
        if(SelectedQuad.isSameSurface(cachedA,b,cachedFace)){//确保两个点在同一平面
            addQuad(b);//添加选区
            return true;//成功创建选区
        }else {
            //两点不在同一平面，清除缓存
            cachedA = null;
            cachedFace = null;
            return false;//创建失败
        }
    }

    public boolean isCreating()
    {
        return cachedA!=null;
    }

    public void addQuad(BlockPos blockPos,Direction face)
    {
        addQuad(blockPos,blockPos,face);
    }
    public void addQuad(BlockPos b)
    {
        addQuad(cachedA,b,cachedFace);
        cachedA=null;
        cachedFace=null;
    }
    public void addQuad(BlockPos a,BlockPos b, Direction face)
    {
        addQuad(new SelectedQuad(a, b, face));
    }
    public void addQuad(SelectedQuad quad)
    {
        quads.add(quad);
        update(quad);
    }

    public boolean contains(Direction face,BlockPos pos)
    {
        return contains.getOrDefault(face,new HashSet<>()).contains(pos);
    }

    public boolean contains(BlockPos pos)
    {
        return quads.stream().anyMatch(quad->quad.getSelectedPos().contains(pos));
    }


    public void removeQuad(Direction face,BlockPos pos)
    {
        quads.reversed().forEach(quad->{
            if(quad.face==face&&quad.getSelectedPos().contains(pos))
                quads.remove(quad);
        });
        update();
    }
    public boolean isSelecting()
    {
        return !quads.isEmpty();
    }


    public void update()
    {
        contains.clear();
        quads.forEach(quad->{
            contains.getOrDefault(quad.face,new HashSet<>()).addAll(quad.getSelectedPos());//添加选区

        });
        SelectedZoneRender.setChanged();
    }

    public void update(SelectedQuad quad)
    {
        Set<BlockPos> set=contains.getOrDefault(quad.face,new HashSet<>());
        set.addAll(quad.getSelectedPos());
        contains.put(quad.face,set);
        SelectedZoneRender.setChanged();
    }



    /**
     * 清除所有选区数据
     */
    public void clear() {
        quads.clear();         // 清空选区列表
        contains.clear();      // 清空位置映射
        cachedA = null;        // 清除缓存A点
        cachedFace = null;     // 清除缓存朝向
        SelectedZoneRender.setChanged(); // 通知渲染系统更新
    }

    public Map<Direction, Set<BlockPos>> getContains() {
        return contains;
    }

    public List<SelectedQuad> getQuads() {
        return quads;
    }

    //缓存数据，不需要序列化

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }
}
