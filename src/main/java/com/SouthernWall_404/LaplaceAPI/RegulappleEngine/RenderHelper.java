package com.SouthernWall_404.LaplaceAPI.RegulappleEngine;

import net.minecraft.core.Direction;

public class RenderHelper {


    public static float[][] getSimpleQuadVertex(Direction face)
    {

        float yMin=0;
        float yMax=1;
        float[][] positions;

        switch (face) {
            case DOWN -> positions =new float[][]{
                    {0, yMin, 1}, {0, yMin, 0}, {1, yMin, 0}, {1, yMin, 1}
            };
            case UP -> positions = new float[][]{
                    {0, yMax, 0}, {0, yMax, 1}, {1, yMax, 1}, {1, yMax, 0}
            };
            case NORTH -> positions = new float[][]{
                    {1, yMax, 0},{1, yMin, 0},{0, yMin, 0},{0, yMax, 0}
            };
            case SOUTH -> positions = new float[][]{
                    {0, yMax, 1}, {0, yMin, 1}, {1, yMin, 1}, {1, yMax, 1}
            };
            case WEST -> positions = new float[][]{
                    {0, yMax, 0}, {0, yMin, 0}, {0, yMin, 1}, {0, yMax, 1}

            };
            case EAST -> positions = new float[][]{
                    {1, yMax, 1}, {1, yMin, 1}, {1, yMin, 0}, {1, yMax, 0}
            };
            default -> throw new IllegalArgumentException("Invalid direction: " + face);
        }

        return positions;
    }
}
