package com.SouthernWall_404.Laplace;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;

/**
 * 等待被移植到Lapalce的临时方法
 */
public class VerticesHelper {

    public static VerticesInfo texture(VerticesInfo self,VerticesInfo other)
    {
        self=self.uv(other.startVertice.uv,other.getuVec(),other.getvVec());
        self.refresh();
        return self;
    }

    public static VerticesInfo StuffedTo(VerticesInfo self,VerticesInfo other)
    {

        float uScale = other.getXLength()/1;
        float vScale = other.getYLength()/1;

        self.scaleUV(uScale,vScale);

        self.refresh();
        return self.color(other.getColors());
    }

    public static VerticesInfo offset(VerticesInfo self,float uOffset,float vOffset)
    {
        Vector2f offset = self.getuVec().multiply(uOffset)
                .add(self.getvVec().multiply(vOffset));

        self.startVertice.uv=self.startVertice.uv.add(offset);
        self.refresh();
        return self;
    }
}
