package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import java.awt.*;

/**
 * A sun for the Pepse simulator. Traverses the sky in an oval pattern in synchrony with the day-night cycle.
 */
public class Sun {

    /* Constants */
    private static final int SUN_DIMENSIONS = 200;
    private static final String SUN_TAG = "sun";
    private static final float A = 600;
    private static final float B = 300;
    private static final float INITIAL_SUN_ANGLE_FACTOR = 1.5f;
    private static final float FINAL_SUN_ANGLE_FACTOR = 3.5f;

    /* Public methods */

    /**
     * Creates a Sun.
     * @param gameObjects The collection of GameObjects in the current world.
     * @param layer Layer on which the Sun shall be placed.
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength Length of a complete rotation of the Sun in the sky.
     * @return The newly created Sun object.
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        GameObject sun = new GameObject(Vector2.ZERO, new Vector2(SUN_DIMENSIONS, SUN_DIMENSIONS),
                new OvalRenderable(Color.YELLOW));
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(sun, layer);
        sun.setTag(SUN_TAG);
        new Transition<Float>(sun,
                // Set Sun at [a*cos(angle), b*sin(angle)]
                angle -> sun.setCenter(new Vector2(windowDimensions.x()/2 + (A * (float) Math.cos(angle)),
                        windowDimensions.y()/2 + (B * (float) Math.sin(angle)))),
                (float)(INITIAL_SUN_ANGLE_FACTOR * Math.PI), (float)(FINAL_SUN_ANGLE_FACTOR * Math.PI),
                Transition.LINEAR_INTERPOLATOR_FLOAT, cycleLength,
                Transition.TransitionType.TRANSITION_LOOP, null);

        return sun;
    }

}
