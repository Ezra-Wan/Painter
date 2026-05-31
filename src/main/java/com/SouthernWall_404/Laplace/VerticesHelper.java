package com.SouthernWall_404.Laplace;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class VerticesHelper {

    //TODO 有待转移到VerticesInfo
    @OnlyIn(Dist.CLIENT)
    public static VerticesInfo localize(VerticesInfo self, ResourceLocation atlasKey)
    {
        //只在客户端运行
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            //获取sprite
            TextureAtlasSprite sprite= Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(atlasKey);

            float minU=sprite.getU0();
            float maxU=sprite.getU1();
            float minV=sprite.getV0();
            float maxV=sprite.getV1();

            //处理self
            VerticesInfo result=self.normalize();//先归一化

            //获取左下的配布，决定是uv分别为最大还是最小
            Vector2f uvVec=result.rightUp().uv.subtract(result.leftDown().uv);//获取左下到右上的uv情况

            /**
             * TODO
             *  1.按照u或v方向投影的比例， 将resulte的uVec vVec拉伸至符合Sprite的情况
             *  2.根据uvVec，可以得知当前result.leftDown选用的uv分别为最大还是最小
             *  3.根据此判定，将其uv修改为当前sprite的对应uv起点
             */

            // 1. 计算UV向量在U和V方向的分量
            float uComponent = uvVec.getX();
            float vComponent = uvVec.getY();
            
            // 2. 根据UV分量判断leftDown点的UV是最小值还是最大值
            boolean isMinU = uComponent > 0; // 如果U分量为正，则leftDown的U是最小值
            boolean isMinV = vComponent > 0; // 如果V分量为正，则leftDown的V是最小值
            
            // 3. 确定目标UV起点
            float targetU = isMinU ? minU : maxU;
            float targetV = isMinV ? minV : maxV;


            // 4. 修改leftDown点的UV
            result.startVertice.uv=Vector2f.of(targetU,targetV);//TODO 记得添加一个moveTo(Vector)方法


            //TODO 还没做缩放
            /**
             * 对于缩放，检查对于sprite的uv长度
             * 而后，与result的uVec vVec在uvVec对应方向上的投影长度进行比较，得到uScale vScale
             * 将result的uVec vVec(可以通过result.getuVec result.getvVec方法获取)缩放到应有长度
             * - 需要注意，这两个向量可能未必严格垂直或水平，存在vVec在v方向为0或uVec在u方向为0的可能，需要先确定uVec vVec的具体缩放参考方向，即对于u v哪边的投影大，按哪边的系数进行缩放
             */
            
            // 5. 处理缩放
            // 计算sprite的UV长度
            float spriteULength = maxU - minU;
            float spriteVLength = maxV - minV;

            // 获取result的uVec和vVec
            Vector2f uVec = result.getuVec();
            Vector2f vVec = result.getvVec();

            // 获取uvVec的方向信息（用于确定哪个方向是主要的）
            float uvVecUComponent = Math.abs(uvVec.getX());
            float uvVecVComponent = Math.abs(uvVec.getY());
            
            // 对于uVec：选择在U或V方向上投影更大的方向作为缩放参考
            float uVecUComponent = Math.abs(uVec.getX());
            float uVecVComponent = Math.abs(uVec.getY());
            float uScale;
            
            if (uVecUComponent >= uVecVComponent) {
                // uVec在U方向的投影更大，使用U方向进行缩放
                if (uvVecUComponent > 0.0001f) {
                    uScale = spriteULength / uvVecUComponent;
                } else {
                    uScale = 1.0f; // 避免除零，不缩放
                }
            } else {
                // uVec在V方向的投影更大，使用V方向进行缩放
                if (uvVecVComponent > 0.0001f) {
                    uScale = spriteVLength / uvVecVComponent;
                } else {
                    uScale = 1.0f; // 避免除零，不缩放
                }
            }
            
            // 对于vVec：选择在U或V方向上投影更大的方向作为缩放参考
            float vVecUComponent = Math.abs(vVec.getX());
            float vVecVComponent = Math.abs(vVec.getY());
            float vScale;
            
            if (vVecVComponent >= vVecUComponent) {
                // vVec在V方向的投影更大，使用V方向进行缩放
                if (uvVecVComponent > 0.0001f) {
                    vScale = spriteVLength / uvVecVComponent;
                } else {
                    vScale = 1.0f; // 避免除零，不缩放
                }
            } else {
                // vVec在U方向的投影更大，使用U方向进行缩放
                if (uvVecUComponent > 0.0001f) {
                    vScale = spriteULength / uvVecUComponent;
                } else {
                    vScale = 1.0f; // 避免除零，不缩放
                }
            }
            
            // 应用缩放到uVec和vVec
            result.setuVec(Vector2f.of(
                uVec.getX() * uScale,
                uVec.getY() * uScale
            ));
            
            result.setvVec(Vector2f.of(
                vVec.getX() * vScale,
                vVec.getY() * vScale
            ));

            result.refresh();//刷新以刷新数据

            return result;



        }else throw new NullPointerException("Invalid to Use VerticesHelper.localize in Dedicated Server");
    }
}
