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
    //建立的所有选区
    private List<SelectedQuad> quads=new ArrayList<>();
    //所有被选中的位置
    private Map<Direction, Set<BlockPos>> contains=new HashMap<>();
    //缓存的选区A位置
    private BlockPos cachedA;
    //缓存选区的朝向
    private Direction cachedFace;


    //========业务方法========
    public void setA(BlockPos a, Direction face) {
        cachedA=a;
        cachedFace=face;
    }

    public boolean setB(BlockPos b) {
        if(SelectedQuad.isSameSurface(cachedA,b,cachedFace)){//确保两个点在同一平面
            addQuad(b);//添加选区并清除缓存
            return true;//成功创建选区
        }else {
            //两点不在同一平面，不清除A点缓存，只返回false
            //用户可以继续尝试选择正确的B点
            return false;//创建失败
        }
    }

    public boolean isCreating() {
        return cachedA!=null;//判断是否正在创建选区
    }

    public void addQuad(BlockPos blockPos,Direction face) {
        addQuad(blockPos,blockPos,face);
    }

    public void addQuad(BlockPos b) {
        addQuad(cachedA,b,cachedFace);
        cachedA=null;
        cachedFace=null;
    }

    public void addQuad(BlockPos a,BlockPos b, Direction face) {
        addQuad(new SelectedQuad(a, b, face));
    }

    public void addQuad(SelectedQuad quad) {
        quads.add(quad);
        update(quad);
    }

    public boolean contains(Direction face,BlockPos pos) {
        return contains.getOrDefault(face,new HashSet<>()).contains(pos);
    }

    public boolean contains(BlockPos pos) {
        return quads.stream().anyMatch(quad->quad.getSelectedPos().contains(pos));
    }


    public void removeQuad(Direction face,BlockPos pos) {
        quads.reversed().forEach(quad->{
            if(quad.face==face&&quad.getSelectedPos().contains(pos))
                quads.remove(quad);
        });
        update();
    }
    public boolean isSelecting() {
        return !quads.isEmpty();
    }


    public void update() {
        contains.clear();
        quads.forEach(quad->{
            contains.getOrDefault(quad.face,new HashSet<>()).addAll(quad.getSelectedPos());//添加选区

        });
        SelectedZoneRender.setChanged();
    }

    public void update(SelectedQuad quad) {
        Set<BlockPos> set=contains.getOrDefault(quad.face,new HashSet<>());
        set.addAll(quad.getSelectedPos());
        contains.put(quad.face,set);
        SelectedZoneRender.setChanged();
    }



    //TODO 建立面时记得加合法判定
    public void clear() {
        quads.clear();
        contains.clear();
        cachedA=null;
        cachedFace=null;
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
