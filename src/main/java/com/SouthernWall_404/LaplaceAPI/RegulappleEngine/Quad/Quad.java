package com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class Quad {

    private List<Vector3f> vertexes;//顶点信息，左上 左下 右下 右上\

    private int color;
    private int[] vertexIndex=new int[]{0,1,2,1,2,3};

    private Quad(List<Vector3f> vertexes,int color)
    {
        this.vertexes=vertexes;
        this.color=color;

    }
    public static Builder builder()
    {
        return new Builder();
    }


    public void render(Matrix4f matrix4f, VertexConsumer buffer)
    {
        for(int index:vertexIndex)
        {
            Vector3f vertex=vertexes.get(index);
            buffer.addVertex(matrix4f,vertex.getX(),vertex.getY(),vertex.getZ()).setColor(color);
        }
    }




    public static class Builder {

        private List<Vector3f> vertexes=new ArrayList<>();
        private int color;


        private Builder()
        {

        }

        public Builder addVertex(Vector3f vector3f)
        {
            vertexes.add(vector3f);
            return  this;
        }

        public Builder addVertex(List<Vector3f> vertexes)
        {
            this.vertexes.addAll(vertexes);
            return  this;
        }

        public Quad.Builder setColor(int color) {
            this.color = color;
            return this;
        }
        public Quad.Builder setColor(int r, int g, int b, int a){
            return setColor(FastColor.ARGB32.color(a, r, g, b));
        }

        public Quad build()
        {
            return new Quad(vertexes,color);
        }

    }
}
