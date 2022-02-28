package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Terrain {

    /* Constants */
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final float DEFAULT_GROUND_HEIGHT_AT_X0 = (float) 2/3;
    private static final int TERRAIN_DEPTH = 20;
    private static final String TOP_TERRAIN_TAG = "top-terrain";
    private static final String BOTTOM_TERRAIN_TAG = "bottom-terrain";


    /* Private members */
    private final GameObjectCollection gameObjects;
    private final int bottomGroundLayer;
    private final float groundHeightAtx0;
    private final float a;
    private final float b;
    private final float c;
    private HashMap<Integer, ArrayList<GameObject>> cache;

    public Terrain(GameObjectCollection gameObjects, int groundLayer, Vector2 windowDimensions, int seed) {
        this.gameObjects = gameObjects;
        this.bottomGroundLayer = groundLayer;
        this.groundHeightAtx0 = DEFAULT_GROUND_HEIGHT_AT_X0 * windowDimensions.y();
        // Initialize terrain function random parameters
        Random randIntGenerator = new Random(seed);
        // Legal range is floats between -10f and 10f
        this.a = (float) (randIntGenerator.nextInt(200) - 100) / 10;
        this.b = (float) (randIntGenerator.nextInt(200) - 100) / 10;
        this.c = (float) (randIntGenerator.nextInt(200) - 100) / 10;
    }

    /**
     * Creates terrain in the given range.
     * @param minX The left-most x value of the range.
     * @param maxX The right-most x value of the range.
     */
    public void createInRange(int minX, int maxX) {
        for (int x = minX; x < maxX; x += Block.SIZE) {
            float smallestY = groundHeightAt(x);
            for (float y = smallestY; y < smallestY + (TERRAIN_DEPTH * Block.SIZE); y += Block.SIZE) {
                Block block = new Block(new Vector2(x,y),
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
                // Record all created Blocks in the cache
                if (!(cache.containsKey(x))) {
                    cache.put(x, new ArrayList<GameObject>());
                }
                cache.get(x).add(block);
                // Differentiate between top terrain Blocks and less substantial Blocks of terrain
                int layer = bottomGroundLayer;
                String tag = BOTTOM_TERRAIN_TAG;
                if (y < smallestY + 2*Block.SIZE) {
                    layer -= 1;
                    tag = TOP_TERRAIN_TAG;
                }
                block.setTag(tag);
                gameObjects.addGameObject(block, layer);
                block.physics().preventIntersectionsFromDirection(Vector2.ZERO);
                block.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
            }
        }
    }

    /**
     * Calculates the terrain height at the given x. Uses Fourier transformation with irrational
     * parameters to ensure a random-like topology for the terrain.
     * @param x Value to compute height at.
     * @return Terrain height at the given x.
     */
    public float groundHeightAt(float x) {
        float functionVal =
                (float) (10 * (a * Math.sin(0.1 * 0.04 * x) +
                               b * Math.sin(0.1 * Math.E * 0.04 * x) +
                               c * (Math.sin(0.1 * Math.PI * 0.04 * x))));

        return functionVal - (functionVal%Block.SIZE) + groundHeightAtx0;
    }

    /**
     * Sets the Cache.
     */
    public void setCache(HashMap<Integer, ArrayList<GameObject>> cache) {
        this.cache = cache;
    }
}
