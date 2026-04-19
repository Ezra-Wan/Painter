package com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.QuadRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Line {

    private final Vector3f start;
    private final Vector3f end;
    private final int color;
    private final float uLength;
    private final float vLength;
    private final Set<Quad> quads =new HashSet<>();


    private Line(Vector3f start, Vector3f end, int color, float uLength, float vLength) {
        this.start = start;
        this.end = end;

        this.color = color;
        this.uLength = uLength;
        this.vLength = vLength;

        update();
    }


    public void update()
    {
        List<Vector3f> vertexes = getVertexes();
        for(int[] faceVertex:vertexIndexes)
        {
            Quad.Builder builder=Quad.builder();
            for(int index:faceVertex)
            {
                Vector3f vertex=vertexes.get(index-1);

                builder.addVertex(vertex);

            }
            Quad quad=builder.setColor(color).build();
            quads.add(quad);

        }
    }

    public void render(Vec3 camPos, VertexConsumer buffer, PoseStack poseStack) {

        for (Quad quad : quads)
        {
            QuadRender.renderQuad(quad,start,camPos,poseStack,buffer);

        }

    }

    public static final int[][] vertexIndexes=new int[][]{
            {4,8,5,1},//正面
            {1,5,6,2},//右面
            {2,6,7,3},//后面
            {3,7,8,4},//左面
            {3,4,1,2},//上面
            {7,8,5,6}//下面

    };

    public List<Vector3f> getVertexes()
    {
        Vector3f line=end.subtract(start);
        Vector3f vVec=line.getV();
        Vector3f uVec=line.getU();

        float centerU=uLength/2;
        float centerV=vLength/2;

        List<Vector3f> vertexes=new ArrayList<>();
        /**
         * 按卦限排
         */

        vertexes.add(line.add(vVec.multiply(centerV)).add(uVec.multiply(centerU)));
        vertexes.add(line.add(vVec.multiply(-centerV)).add(uVec.multiply(centerU)));
        vertexes.add(line.add(vVec.multiply(-centerV)).add(uVec.multiply(-centerU)));
        vertexes.add(line.add(vVec.multiply(centerV)).add(uVec.multiply(-centerU)));

        vertexes.add(Vector3f.zero().add(vVec.multiply(centerV)).add(uVec.multiply(centerU)));
        vertexes.add(Vector3f.zero().add(vVec.multiply(-centerV)).add(uVec.multiply(centerU)));
        vertexes.add(Vector3f.zero().add(vVec.multiply(-centerV)).add(uVec.multiply(-centerU)));
        vertexes.add(Vector3f.zero().add(vVec.multiply(centerV)).add(uVec.multiply(-centerU)));

        return vertexes;

    }

    public static Builder builder(Vector3f start,Vector3f end)
    {
        return new Builder(start,end);
    }




    public static class Builder
    {

        Vector3f start =Vector3f.zero();
        Vector3f end =Vector3f.zero();

        int color=0x00000000;
        float u =0.1f;
        float v =0.1f;

        public Builder(Vector3f start, Vector3f end)
        {
            this.start = start;
            this.end = end;
        }

        public Line build()
        {
            return new Line(start, end,color, u, v);
        }

        public Builder setColor(int color){
            this.color=color;
            return this;
        }

        public Builder setColor(int r,int g,int b,int a){
            return setColor(FastColor.ARGB32.color(a, r, g, b));
        }

        public Builder setColor(int r,int g,int b)
        {
            return setColor(r,g,b,1);
        }

        public Builder setWidth(float width)
        {
            return setUV(width,width);
        }

        public Builder setUV(float u,float v)
        {
            this.u =u;
            this.v =v;

            return this;
        }

        public Builder setU(float u)
        {
            return setUV(u, this.v);
        }
        public Builder setV(float v)
        {
            return setUV(this.u, v);
        }

    }
}
