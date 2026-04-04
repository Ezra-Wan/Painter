package com.SouthernWall_404.Painter.API.Tool.Wall.Filters;

import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class SelectFilter extends EmptyFilter {

    public SelectFilter() {
    }

    @Override
    public boolean check(BlockPos pos, Player player, Direction face) {

        SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
        if(selectedZone!=null)
        {
            if(selectedZone.isSelecting()) {//如果存在选区,且在同一平面

                if(!selectedZone.isInSurface(pos,face))
                {
                    return false;
                }
                if (selectedZone.contains(pos))
                {
                    return super.check(pos,player,face);
                }else {
                    return false;
                }
            }else{
                return super.check(pos,player,face);
            }
        }

        return false;
    }
}
