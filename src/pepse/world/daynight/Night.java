package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Responsible for the day-and-night cycles in the Pepse simulator.
 */
public class Night {

    /* Constants */
    private static final float NOON_OPACITY = 0f;
    private static final float MIDNIGHT_OPACITY = 0.5f;
    private static final String NIGHT_TAG = "night";

    /* Public methods */

    /**
     * Creates the Night object.
     * @param gameObjects The collection of GameObjects in the current world.
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength Length of a complete day/night cycle
     * @param layer Layer on which the night object should be placed.
     * @return The newly created Night object.
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions,
                new RectangleRenderable(Color.BLACK));
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(night, layer);
        night.setTag(NIGHT_TAG);
        new Transition<Float>(night, night.renderer()::setOpaqueness, NOON_OPACITY, MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT, cycleLength/2,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);

        return night;
    }


}
