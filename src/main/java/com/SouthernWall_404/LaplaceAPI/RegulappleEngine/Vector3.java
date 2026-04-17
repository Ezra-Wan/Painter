package com.SouthernWall_404.LaplaceAPI.RegulappleEngine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vector3 {

    private double x;
    private double y;
    private double z;

    // ==================== 构造方法 ====================

    public Vector3(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public Vector3(Vec3 vec3) {
        this(vec3.x, vec3.y, vec3.z);
    }

    public Vector3(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ==================== 静态工厂方法 ====================

    public static Vector3 of(double x, double y, double z) {
        return new Vector3(x, y, z);
    }

    public static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }

    public static Vector3 one() {
        return new Vector3(1, 1, 1);
    }

    public static Vector3 unitX() {
        return new Vector3(1, 0, 0);
    }

    public static Vector3 unitY() {
        return new Vector3(0, 1, 0);
    }

    public static Vector3 unitZ() {
        return new Vector3(0, 0, 1);
    }

    // ==================== Getter ====================

    public double get(Direction.Axis axis) {
        switch (axis) {
            case X: return x;
            case Y: return y;
            case Z: return z;
            default: return 0;
        }
    }

    public double get(Direction direction) {
        return get(direction.getAxis());
    }

    public double getX() {
        return get(Direction.Axis.X);
    }

    public double getY() {
        return get(Direction.Axis.Y);
    }

    public double getZ() {
        return get(Direction.Axis.Z);
    }

    // ==================== 不可变修改（返回新对象） ====================

    public Vector3 withX(double x) {
        return new Vector3(x, this.y, this.z);
    }

    public Vector3 withY(double y) {
        return new Vector3(this.x, y, this.z);
    }

    public Vector3 withZ(double z) {
        return new Vector3(this.x, this.y, z);
    }

    // ==================== 向量运算（返回新对象） ====================

    public Vector3 add(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 add(double x, double y, double z) {
        return new Vector3(this.x + x, this.y + y, this.z + z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3 subtract(double x, double y, double z) {
        return new Vector3(this.x - x, this.y - y, this.z - z);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3 divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Vector3(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3 negate() {
        return new Vector3(-this.x, -this.y, -this.z);
    }

    public Vector3 abs() {
        return new Vector3(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    // ==================== 标量运算 ====================

    public double dot(Vector3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector3 normalize() {
        double len = length();
        if (len == 0) {
            throw new IllegalStateException("Cannot normalize zero vector");
        }
        return new Vector3(x / len, y / len, z / len);
    }

    public List<Vector3> getOrthonormals()
    {
        List<Vector3> result=new ArrayList<>();

        Vector3 self=this;
        self=self.normalize();

        Vector3 vOrthonormal;
        if(self.y==0)
        {
            vOrthonormal=new Vector3(-self.getX(),1,-self.getZ());
        }else
        {
            vOrthonormal=new Vector3(-self.getX(),(self.getX()*self.getX()+self.getZ()*self.getZ())/self.getY(),-self.getZ());//同一竖直面向上的垂直向量

        }
        vOrthonormal=vOrthonormal.normalize();
        result.add(vOrthonormal);

        Vector3 uOrthonormal=self.cross(vOrthonormal);
        uOrthonormal=uOrthonormal.normalize();
        result.add(uOrthonormal);


        return result;
    }

    public Vector3 getU()
    {
        return getOrthonormals().get(0);
    }
    public Vector3 getV()
    {
        Vector3 self=this;
        self=self.normalize();

        Vector3 vOrthonormal;
        if(self.y==0)
        {
            vOrthonormal=new Vector3(-self.getX(),1,-self.getZ());
        }else
        {
            vOrthonormal=new Vector3(-self.getX(),(self.getX()*self.getX()+self.getZ()*self.getZ())/self.getY(),-self.getZ());//同一竖直面向上的垂直向量

        }
        return vOrthonormal.normalize();
    }


    // ==================== 距离相关 ====================

    public double distanceSquaredTo(Vector3 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceTo(Vector3 other) {
        return Math.sqrt(distanceSquaredTo(other));
    }

    // ==================== 插值 ====================

    public Vector3 lerp(Vector3 other, double t) {
        return new Vector3(
                this.x + (other.x - this.x) * t,
                this.y + (other.y - this.y) * t,
                this.z + (other.z - this.z) * t
        );
    }

    // ==================== 类型转换 ====================

    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    public Vec3i toVec3i() {
        return new Vec3i((int) x, (int) y, (int) z);
    }

    // ==================== 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 vector3 = (Vector3) o;
        return Double.compare(vector3.x, x) == 0 &&
                Double.compare(vector3.y, y) == 0 &&
                Double.compare(vector3.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3(" + x + ", " + y + ", " + z + ")";
    }
}