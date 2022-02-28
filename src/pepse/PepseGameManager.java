package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;
import java.awt.*;

/**
 * A generic game simulator.
 */
public class PepseGameManager extends GameManager {

    /* Constants */

    // Layers
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int SUN_LAYER = SKY_LAYER + 1;
    private static final int SUN_HALO_LAYER = SUN_LAYER + 1;
    private static final int BOTTOM_TERRAIN_LAYER = Layer.STATIC_OBJECTS;
    private static final int TOP_TERRAIN_LAYER = BOTTOM_TERRAIN_LAYER - 1;
    private static final int LEAF_LAYER = BOTTOM_TERRAIN_LAYER + 2;
    private static final int TRUNK_LAYER = LEAF_LAYER - 1;
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    private static final int AVATAR_LAYER = Layer.DEFAULT;
    // Tags
    private static final String TOP_TERRAIN_TAG = "top-terrain";
    private static final String BOTTOM_TERRAIN_TAG = "bottom-terrain";
    private static final String LEAF_TAG = "leaf";
    private static final String TRUNK_TAG = "trunk";
    // Other
    private static final int DAY_CYCLE_LENGTH = 30;
    private static final Color SUN_HALO_COLOR = new Color(255, 255, 0, 20);
    private static final int RANGE_BUFFER = -90;
    // private static final long INITIAL_SEED = 6;  // init this.seed with this constant to test consistency

    /* Private members */

    // GameManager-related members
    private Vector2 windowDimensions;
    private Terrain terrain;
    private UserInputListener inputListener;
    private WindowController windowController;
    private ImageReader imageReader;
    // Pepse-related members
    private Tree trees;  // a Tree object
    private Function<Float, Float> getTerrainHeightAtX;  // function to get terrain height at a given x
    private int leftRange;  // left-most x value at which objects have been created
    private int rightRange;  // right-most x value at which objects have been created
    private final int seed = new Random().nextInt();  // seed for all randomness
    private float avatarAtX;  // last location of the Avatar
    // Hash table mapping x values to a list of all gameObjects created at x
    private final HashMap<Integer, ArrayList<GameObject>> cache = new HashMap<>();


    /* Public methods */  // (Main located below)
    /**
     * Initializes the simulator.
     * @param imageReader Contains a single method: readImage, which reads an image from disk.
     * @param soundReader Contains a single method: readSound, which reads a wav file from disk.
     * @param inputListener Contains a single method: isKeyPressed, which returns whether a given key is
     *                      currently pressed by the user or not.
     * @param windowController Contains an array of helpful, self explanatory methods concerning the window.
     */
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        // Set private members of the game manager
        this.imageReader = imageReader;
        this.windowDimensions = windowController.getWindowDimensions();
        this.inputListener = inputListener;
        this.windowController = windowController;
        this.avatarAtX = windowDimensions.x() / 2;
        // Create the various objects that the simulator consists of
        computeRanges();
        createSky();
        createTerrain();
        createNight();
        createSunHalo(createSun());
        createTrees();
        createAvatar();
    }

    /**
     * Updates the game and it's objects. Called once per frame.
     * @param deltaTime The time, in seconds, that passed since the last invocation of this method.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        // Dynamically update the world as the avatar progresses through it
        float actualRightBorder = camera().screenToWorldCoords(windowDimensions).x();
        float actualLeftBorder = camera().screenToWorldCoords(windowDimensions).x() - windowDimensions.x();
        while (actualRightBorder - rightRange > RANGE_BUFFER) {
            // moved right
            generateWorld(rightRange, rightRange + Block.SIZE);
            collectGarbage(leftRange);
            this.leftRange += Block.SIZE;
            this.rightRange += Block.SIZE;
        }
        while (leftRange - actualLeftBorder > RANGE_BUFFER) {
            // moved left
            generateWorld(leftRange - Block.SIZE, leftRange);
            collectGarbage(rightRange - Block.SIZE);
            this.leftRange -= Block.SIZE;
            this.rightRange -= Block.SIZE;
        }
    }

    /* Private methods */

    /* Computes the initial left and right ranges in which to create objects. The difference will be a
    multiple of Block.SIZE */
    private void computeRanges() {
        int left = RANGE_BUFFER;
        int right = (int) windowDimensions.x() - RANGE_BUFFER;
        if ((right - left) % Block.SIZE != 0) {
            right += Block.SIZE - ((right - left) % Block.SIZE);
        }
        this.leftRange = left;
        this.rightRange = right;
    }

    /* Creates a Sky */
    private void createSky() {
        GameObject sky = Sky.create(gameObjects(), windowDimensions, SKY_LAYER);
    }

    /* Creates the initial terrain upon booting */
    private void createTerrain() {
        this.terrain = new Terrain(gameObjects(), BOTTOM_TERRAIN_LAYER, windowDimensions, seed);
        terrain.setCache(cache);
        terrain.createInRange(leftRange, rightRange);
        // Save terrain calculation function as private member, to be used when needed
        this.getTerrainHeightAtX = terrain::groundHeightAt;
    }

    /* Creates the Night */
    private void createNight() {
        GameObject night = Night.create(gameObjects(), NIGHT_LAYER, windowDimensions, DAY_CYCLE_LENGTH);
    }

    /* Creates the Sun */
    private GameObject createSun() {
        return Sun.create(gameObjects(), SUN_LAYER, windowDimensions, DAY_CYCLE_LENGTH);
    }

    /* Creates the SunHalo */
    private void createSunHalo(GameObject sun) {
        GameObject sunHalo = SunHalo.create(gameObjects(), SUN_HALO_LAYER, sun, SUN_HALO_COLOR);
    }

    /* Creates the initial trees upon booting */
    private void createTrees() {
        Tree trees = new Tree(gameObjects(), LEAF_LAYER, seed, terrain::groundHeightAt);
        this.trees = trees;
        trees.setCache(cache);
        trees.createInRange(leftRange, rightRange);
        // Leaves should collide with the terrain
        gameObjects().layers().shouldLayersCollide(LEAF_LAYER, TOP_TERRAIN_LAYER, true);
    }

    /* Creates the Avatar to be used in the simulator */
    private void createAvatar() {
        GameObject avatar = Avatar.create(gameObjects(), AVATAR_LAYER,
                new Vector2(avatarAtX,
                        getTerrainHeightAtX.apply(avatarAtX) - Avatar.HEIGHT),
                inputListener, imageReader);
        this.setCamera(new Camera(avatar, Vector2.ZERO, windowController.getWindowDimensions(),
                windowController.getWindowDimensions()));
        // The Avatar should collide with tree trunks and top layer of terrain
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TRUNK_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TOP_TERRAIN_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, BOTTOM_TERRAIN_LAYER, false);

    }

    /* Generates a new segment of the world in a given range */
    private void generateWorld(int minX, int maxX) {
        if (((maxX - minX) % Block.SIZE) != 0) {
            maxX += Block.SIZE - ((maxX - minX) % Block.SIZE);
        }
        terrain.createInRange(minX, maxX);
        trees.createInRange(minX, maxX);
    }

    /* Removes all GameObjects at a given x */
    private void collectGarbage(int x) {
        for (GameObject gameObject: cache.get(x)) {
            switch (gameObject.getTag()) {
                case TOP_TERRAIN_TAG:
                    gameObjects().removeGameObject(gameObject, TOP_TERRAIN_LAYER);
                    break;
                case BOTTOM_TERRAIN_TAG:
                    gameObjects().removeGameObject(gameObject, BOTTOM_TERRAIN_LAYER);
                    break;
                case LEAF_TAG:
                    gameObjects().removeGameObject(gameObject, LEAF_LAYER);
                    break;
                case TRUNK_TAG:
                    gameObjects().removeGameObject(gameObject, TRUNK_LAYER);
                    break;
            }
        }
        cache.remove(x);
    }

    /**
     * Main method for the Pepse simulator.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}