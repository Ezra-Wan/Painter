package com.SouthernWall_404.Laplace;

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

        float uScale = other.getXLength()/self.getXLength();
        float vScale = other.getYLength()/self.getYLength();

        self.scaleUV(uScale,vScale);

        self.refresh();
        return self.color(other.getColors());
    }
}
