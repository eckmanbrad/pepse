package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.ImageRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import java.awt.event.KeyEvent;

/**
 * Represents an avatar to be used as the main character in the Pepse simulator. Can run, jump and fly.
 */
public class Avatar extends GameObject {

    /* Constants */
    // Paths to images
    private static final String IDLE1_IMG_PATH = "assets/idle1.png";
    private static final String IDLE2_IMG_PATH = "assets/idle2.png";
    private static final String IDLE3_IMG_PATH = "assets/idle3.png";
    private static final String WALKING1_IMG_PATH = "assets/walking1.png";
    private static final String WALKING2_IMG_PATH = "assets/walking2.png";
    private static final String WALKING3_IMG_PATH = "assets/walking3.png";
    private static final String JUMPING1_IMG_PATH = "assets/jumping1.png";
    // Attributes
    public static final float HEIGHT = 60;
    private static final float WIDTH = 30;
    private static final float SPEED = 300;
    private static final float GRAVITY = 500;
    private static final float FULL_ENERGY = 100;
    private static final float ENERGY_UNIT = 0.5f;
    private static final double TIME_BETWEEN_CLIPS = 0.2;


    /* Private members */
    private static UserInputListener inputListener;
    private static ImageReader imageReader;
    // Animations to be used to render the Avatar within the Pepse world
    private static AnimationRenderable idle;
    private static AnimationRenderable walkingRight;
    private static AnimationRenderable jumping;
    private float energy;

    /* Public methods */
    /**
     * Construct a new Avatar instance.
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        super(topLeftCorner, dimensions, renderable);
        this.energy = FULL_ENERGY;
    }

    /**
     * Creates an avatar within the Pepse simulator.
     * @param gameObjects The collection of GameObjects in the current world.
     * @param layer The layer in the simulator in which to place the avatar.
     * @param topLeftCorner The coordinates at which the avatar shall be placed within the simulator.
     * @param inputListener Contains a single method: isKeyPressed, which returns whether a given key is
     *                      currently pressed by the user or not.
     * @param imageReader Contains a single method: readImage, which reads an image from disk.
     * @return The Avatar created.
     */
    public static Avatar create(GameObjectCollection gameObjects, int layer,
                                Vector2 topLeftCorner, UserInputListener inputListener,
                                ImageReader imageReader) {
        Avatar.inputListener = inputListener;
        Avatar.imageReader = imageReader;
        initAnimations();
        Avatar avatar = new Avatar(topLeftCorner, new Vector2(WIDTH, HEIGHT), idle);
        gameObjects.addGameObject(avatar, layer);
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        avatar.transform().setAccelerationY(GRAVITY);  // forces Avatar to abide by gravity

        return avatar;
    }

    /**
     * Updates the game and it's objects. Called once per frame.
     * @param deltaTime The time, in seconds, that passed since the last invocation of this method.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Reset the avatar to idle position by default
        this.renderer().setRenderable(idle);
        this.renderer().setRenderableAngle(0);
        float yVelocity = getVelocity().y();
        // Gain energy while on the ground
        if (yVelocity == 0 && energy < FULL_ENERGY) {
            energy += ENERGY_UNIT;
        }
        // Halt lateral movement
        setVelocity(new Vector2(0, yVelocity));

        // Move the avatar based on user input
        // Fly
        if (inputListener.isKeyPressed((KeyEvent.VK_SPACE)) &&
                inputListener.isKeyPressed((KeyEvent.VK_SHIFT)) && energy > 0) {
            this.renderer().setRenderable(jumping);
            this.renderer().setRenderableAngle(300f);
            setVelocity(new Vector2(0, -SPEED));
            energy -= ENERGY_UNIT;
        }
        // Walk left
        if(inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            this.renderer().setRenderable(walkingRight);
            this.renderer().setIsFlippedHorizontally(true);
            setVelocity(new Vector2(-SPEED, yVelocity));
        }
        // Walk right
        if(inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            this.renderer().setRenderable(walkingRight);
            this.renderer().setIsFlippedHorizontally(false);
            setVelocity(new Vector2(SPEED, yVelocity));
        }
        // Jump
        if (inputListener.isKeyPressed((KeyEvent.VK_SPACE)) && yVelocity == 0) {
            setVelocity(new Vector2(0, -SPEED));
        }
    }

    /* Initializes animations to be used to render the Avatar within the Pepse world */
    private static void initAnimations() {
        // Read images into imageRenderables
        ImageRenderable idle1 = imageReader.readImage(IDLE1_IMG_PATH, true);
        ImageRenderable idle2 = imageReader.readImage(IDLE2_IMG_PATH, true);
        ImageRenderable idle3 = imageReader.readImage(IDLE3_IMG_PATH, true);
        ImageRenderable walking1 = imageReader.readImage(WALKING1_IMG_PATH, true);
        ImageRenderable walking2 = imageReader.readImage(WALKING2_IMG_PATH, true);
        ImageRenderable walking3 = imageReader.readImage(WALKING3_IMG_PATH, true);
        ImageRenderable jumping1 = imageReader.readImage(JUMPING1_IMG_PATH, true);
        // Create AnimationRenderables
        idle = new AnimationRenderable( new ImageRenderable[]{ idle1, idle2, idle3 }, TIME_BETWEEN_CLIPS);
        walkingRight = new AnimationRenderable(new ImageRenderable[]{walking1, walking2, walking3,
                walking2}, TIME_BETWEEN_CLIPS);
        jumping = new AnimationRenderable( new ImageRenderable[] { jumping1, idle1} , TIME_BETWEEN_CLIPS);
    }

}

