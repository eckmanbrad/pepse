package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.GameObjectPhysics;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Responsible for creating trees in the Pepse simulator.
 */
public class Tree {

    /* Constants */
    private static final int MAX_TREE_HEIGHT = 13;
    private static final int MIN_TREE_HEIGHT = 6;
    private static final int TREES_DENSITY_IN_WORLD = 8;
    private static final int LEAVES_DENSITY = 5;
    private static final int FADEOUT_TIME = 5;
    private static final int MAX_LEAF_LIFETIME = 50;
    private static final int MAX_LEAF_FADE_IN_TIME = 15;
    private static final int LEAF_PROPERTIES_TRANSITION_TIME = 25;
    private static final int MIN_LEAF_FADE_IN_TIME = 5;
    private static final int MIN_LEAF_LIFETIME = 3;
    private static final float TRUNK_TO_TREE_RATIO = (float) 2 / 3;
    private static final float LEAF_GRAVITY = 50;
    private static final float LEAF_WIND_SENSITIVITY = 50f;
    private static final float LEAF_HORIZONTAL_MOVEMENT_TRANSITION_TIME = 1;
    private static final float LEAF_SIZE_DEVIATION = 0.9f;
    private static final float LEAF_SHAKE = 15f;
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final Color LEAVES_COLOR = new Color(50, 200, 30);
    private static final String LEAF_TAG = "leaf";
    private static final String TRUNK_TAG = "trunk";


    /* Private members */
    private final GameObjectCollection gameObjects;
    private final int trunkLayer;
    private final int leafLayer;
    private final long seed;
    private final Function<Float, Float> getTerrainHeightAtX;
    private HashMap<Integer, ArrayList<GameObject>> cache;

    /* Public methods */
    /**
     * Constructor.
     * @param gameObjects The collection of GameObjects in the current world.
     * @param leafLayer Layer on which the leaves of the trees should be placed.
     * @param seed Seed for all random generation.
     * @param getTerrainHeightAtX Function to compute the terrain height at a given x.
     */
    public Tree(GameObjectCollection gameObjects, int leafLayer, long seed,
                Function<Float, Float> getTerrainHeightAtX) {
        this.gameObjects = gameObjects;
        this.trunkLayer = leafLayer - 1;  // save trunks and leaves on different layers
        this.leafLayer = leafLayer;
        this.getTerrainHeightAtX = getTerrainHeightAtX;
        this.seed = seed;
    }

    /**
     * Creates trees on the terrain in the given range.
     * @param minX The left-most x value of the range.
     * @param maxX The right-most x value of the range.
     */
    public void createInRange(int minX, int maxX) {
        int middleX = (((minX - maxX) / 2) / Block.SIZE) * Block.SIZE;
        for (int x = minX; x < maxX; x += Block.SIZE) {
            // Create a new Random object at every x seeded by hash function to ensure a consistent world
            Random treeRandom = new Random(Objects.hash(x, seed));
            // Prevent tree generation where an avatar may be created
            if (x == middleX - Block.SIZE || x == middleX || x == middleX + Block.SIZE) { continue; }
            if (treeRandom.nextInt(TREES_DENSITY_IN_WORLD) == 0) {
                createTree(x, treeRandom);
            }
        }
    }

    /**
     * Setter for the cache.
     * @param cache Hash table mapping x values to a list of all gameObjects created at x
     */
    public void setCache(HashMap<Integer, ArrayList<GameObject>> cache) {
        this.cache = cache;
    }

    /* Private methods */

    /* Creates a single tree at x */
    private void createTree(int x, Random treeRandom) {
        // Calculate random height, then create trunk and proportional leaves
        int height = MIN_TREE_HEIGHT + treeRandom.nextInt(MAX_TREE_HEIGHT);
        createLeaves(x, (int) Math.floor(0.5f * height), height, treeRandom);
        createTrunk(x, (int)Math.floor(TRUNK_TO_TREE_RATIO * height));
    }

    /* Creates a tree trunk */
    private void createTrunk(int x, int trunkHeight) {
        Vector2 heightVector = new Vector2(0, Block.SIZE);
        Vector2 currentBlockVector = new Vector2(x, getTerrainHeightAtX.apply((float)x) - Block.SIZE);
        for (int y = 0; y < trunkHeight; y++) {
            Block trunkBlock = new Block(currentBlockVector,
                    new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_COLOR)));
            trunkBlock.setTag(TRUNK_TAG);
            // Record all created Blocks in the cache
            if (!(cache.containsKey(x))) {
                cache.put(x, new ArrayList<GameObject>());
            }
            cache.get(x).add(trunkBlock);
            gameObjects.addGameObject(trunkBlock, trunkLayer);
            trunkBlock.physics().preventIntersectionsFromDirection(Vector2.ZERO);
            trunkBlock.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
            currentBlockVector = currentBlockVector.subtract(heightVector);
        }
    }

    /* Creates the leaves for a tree */
    private void createLeaves(int x, int bushHeight, int treeHeight, Random treeRandom) {
        // Init variables for creating symmetrical-looking trees
        float initialX = x + getBushOffset(bushHeight) - ((float) Math.floor(0.5f * bushHeight) * Block.SIZE);
        float initialY = getTerrainHeightAtX.apply((float)x) - (treeHeight * Block.SIZE);
        Vector2 widthVector = new Vector2(Block.SIZE, 0);
        for (int i = 0; i < bushHeight; i++) {
            Vector2 currentBlockVector = new Vector2(initialX, initialY + (i*Block.SIZE));
            for (int j = 0; j < bushHeight; j++) {
                if (treeRandom.nextInt(LEAVES_DENSITY) != 0) {
                    createLeaf(x, currentBlockVector, treeRandom);
                }
                currentBlockVector = currentBlockVector.add(widthVector);
            }
        }
    }

    /* Creates a single leaf */
    private void createLeaf(int x, Vector2 currentBlockVector, Random treeRandom) {
        Leaf leaf = new Leaf(currentBlockVector,
                new RectangleRenderable(ColorSupplier.approximateColor(LEAVES_COLOR)));
        // Record all created Blocks in the cache
        if (!(cache.containsKey(x))) {
            cache.put(x, new ArrayList<GameObject>());
        }
        cache.get(x).add(leaf);
        leaf.physics().setMass(0);
        leaf.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        gameObjects.addGameObject(leaf, leafLayer);
        leaf.setTag(LEAF_TAG);
        // Create leaf dynamics
        createLeafAngleTransition(leaf, treeRandom);
        createLeafSizeTransition(leaf, treeRandom);
        createLeafLifeCycle(leaf, treeRandom);
    }

    /* Creates the complete life cycle of a leaf. The life cycle of a leaf is:
    * Appear on tree -> fall with lateral movement -> fade out -> repeat */
    private void createLeafLifeCycle(Leaf leaf, Random treeRandom) {
        // Init variables
        int lifetime = treeRandom.nextInt(MAX_LEAF_LIFETIME) + MIN_LEAF_LIFETIME;
        int fadeInTime = treeRandom.nextInt(MAX_LEAF_FADE_IN_TIME) + MIN_LEAF_FADE_IN_TIME;
        Vector2 originalLeafCenter = leaf.getCenter();
        // Create Runnable containing information for a complete leaf life cycle
        Runnable leafFallRunnable = () -> {
            // Starts to fall
            leaf.transform().setAccelerationY(LEAF_GRAVITY);
            // Lateral movement upon falling
            Transition<Float> horizontalTransition = new Transition<>(leaf, leaf.transform()::setVelocityX,
                    -LEAF_WIND_SENSITIVITY, LEAF_WIND_SENSITIVITY, Transition.CUBIC_INTERPOLATOR_FLOAT,
                    LEAF_HORIZONTAL_MOVEMENT_TRANSITION_TIME,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
            leaf.setTransition(horizontalTransition);
            // Fade out
            leaf.renderer().fadeOut(FADEOUT_TIME, () -> new ScheduledTask(leaf, fadeInTime, false,
                    () ->
                    {   // Reestablish the leaf with a new life cycle
                        leaf.setCenter(originalLeafCenter);
                        leaf.transform().setVelocity(Vector2.ZERO);
                        leaf.transform().setAccelerationY(0);
                        leaf.renderer().fadeIn(0);
                        createLeafLifeCycle(leaf, treeRandom);
                    }
            ));
        };
        // Start to fall once leaf has lived its lifetime on the tree
        new ScheduledTask(leaf, lifetime, false, leafFallRunnable);
    }

    /* Makes minor changes in leaf size to create the effect of a real leaf */
    private void createLeafSizeTransition(Leaf leaf, Random treeRandom) {
        Runnable leafSizeTransitionRunnable = () ->
                new Transition<>(leaf, leaf::setDimensions, new Vector2(LEAF_SIZE_DEVIATION * Block.SIZE,
                        Block.SIZE), new Vector2(Block.SIZE,LEAF_SIZE_DEVIATION * Block.SIZE),
                        Transition.LINEAR_INTERPOLATOR_VECTOR, 1,
                        Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        new ScheduledTask(leaf,(float) treeRandom.nextInt(LEAF_PROPERTIES_TRANSITION_TIME) /
                        LEAF_PROPERTIES_TRANSITION_TIME, true,leafSizeTransitionRunnable);
    }

    /* Rotates the leaf in place to create the effect of a real leaf */
    private void createLeafAngleTransition(Leaf leaf, Random treeRandom) {
        Runnable leafAngleTransitionRunnable = () ->
                new Transition<>(leaf, leaf.renderer()::setRenderableAngle, -LEAF_SHAKE, LEAF_SHAKE,
                        Transition.LINEAR_INTERPOLATOR_FLOAT, 2,
                        Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        new ScheduledTask(leaf,(float) treeRandom.nextInt(LEAF_PROPERTIES_TRANSITION_TIME) /
                        LEAF_PROPERTIES_TRANSITION_TIME,false, leafAngleTransitionRunnable);
    }

    /* Computes the offset for the bushes. Used to ensure symmetric-looking trees */
    private float getBushOffset(int bushHeight) {
        if (bushHeight % 2 == 0) {
            return Block.SIZE * 0.5f;
        }
        return 0;
    }

}
