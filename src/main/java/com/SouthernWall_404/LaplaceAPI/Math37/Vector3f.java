package com.SouthernWall_404.LaplaceAPI.Math37;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vector3f {

    private float x;
    private float y;
    private float z;

    // ==================== 构造方法 ====================

    public Vector3f(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public Vector3f(BlockPos pos,float[] list)
    {
        this(pos.getX()+list[0], pos.getY()+list[1], pos.getZ()+list[2]);
    }

    public Vector3f(float[] list)
    {
        this(list[0],list[1],list[2]);
    }

    public Vector3f(Direction direction)
    {
        this(direction.getNormal());
    }

    public Vector3f(Vec3 vec3) {
        this((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public Vector3f(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3f(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ==================== 静态工厂方法 ====================

    public static Vector3f of(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    public static Vector3f zero() {
        return new Vector3f(0, 0, 0);
    }

    public static Vector3f one() {
        return new Vector3f(1, 1, 1);
    }

    public static Vector3f unitX() {
        return new Vector3f(1, 0, 0);
    }

    public static Vector3f unitY() {
        return new Vector3f(0, 1, 0);
    }

    public static Vector3f unitZ() {
        return new Vector3f(0, 0, 1);
    }

    // ==================== Getter ====================

    public float get(Direction.Axis axis) {
        switch (axis) {
            case X: return x;
            case Y: return y;
            case Z: return z;
            default: return 0;
        }
    }

    public float get(Direction direction) {
        return get(direction.getAxis());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    // ==================== 不可变修改（返回新对象） ====================

    public Vector3f withX(float x) {
        return new Vector3f(x, this.y, this.z);
    }

    public Vector3f withY(float y) {
        return new Vector3f(this.x, y, this.z);
    }

    public Vector3f withZ(float z) {
        return new Vector3f(this.x, this.y, z);
    }

    // ==================== 向量运算（返回新对象） ====================

    public Vector3f add(Vector3f other,float multiply)
    {
        return add(other.multiply(multiply));
    }
    public Vector3f add(Vector3f other) {
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f add(float x, float y, float z) {
        return new Vector3f(this.x + x, this.y + y, this.z + z);
    }

    public Vector3f subtract(Vector3f other) {
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3f subtract(float x, float y, float z) {
        return new Vector3f(this.x - x, this.y - y, this.z - z);
    }

    public Vector3f multiply(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3f divide(float scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3f negate() {
        return new Vector3f(-this.x, -this.y, -this.z);
    }

    public Vector3f abs() {
        return new Vector3f(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    // ==================== 标量运算 ====================

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public Vector3f normalize() {
        float len = length();
        if (len == 0) {
            throw new IllegalStateException("Cannot normalize zero vector");
        }
        return new Vector3f(x / len, y / len, z / len);
    }

    public List<Vector3f> getUV() {
        List<Vector3f> result = new ArrayList<>();

        Vector3f self = this.normalize();

        Vector3f vOrthonormal=getV();
        vOrthonormal = vOrthonormal.normalize();
        result.add(vOrthonormal);

        Vector3f uOrthonormal = self.cross(vOrthonormal).normalize();
        result.add(uOrthonormal);

        return result;
    }

    public Vector3f getU() {
        return getUV().get(1);
    }

    public Vector3f getV() {
        Vector3f self = this.normalize();

        Vector3f vOrthonormal;
        if (self.y == 0) {
            vOrthonormal = new Vector3f(0, 1, 0);
        }else  if (self.x==0&&self.z==0)
        {
            vOrthonormal = new Vector3f(1, 0, 0);
        }
        else {
            vOrthonormal = new Vector3f(
                    -self.x,
                    (self.x * self.x + self.z * self.z) / self.y,
                    -self.z
            );
        }
        return vOrthonormal.normalize();
    }

    // ==================== 距离相关 ====================

    public float distanceSquaredTo(Vector3f other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distanceTo(Vector3f other) {
        return (float) Math.sqrt(distanceSquaredTo(other));
    }

    // ==================== 插值 ====================

    public Vector3f lerp(Vector3f other, double t) {
        return new Vector3f(
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
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Vec3i toVec3i() {
        return new Vec3i((int) x, (int) y, (int) z);
    }

    // ==================== 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3f vector3f = (Vector3f) o;
        return Float.compare(vector3f.x, x) == 0 &&
                Float.compare(vector3f.y, y) == 0 &&
                Float.compare(vector3f.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3f(" + x + ", " + y + ", " + z + ")";
    }
}