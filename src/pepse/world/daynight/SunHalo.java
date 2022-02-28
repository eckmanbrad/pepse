package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import java.awt.*;

/**
 * Halo around the Sun in the sky of the Pepse simulator.
 */
public class SunHalo {

    /* Constants */
    private static final float SUN_HALO_DIMENSIONS = 300f;
    private static final String SUN_HALO_TAG = "sunHalo";

    /* Public methods */

    /**
     * Creates SunHalo object.
     * @param gameObjects The collection of GameObjects in the current world.
     * @param sun Sun around which the SunHalo should be created
     * @param color Color of the halo.
     * @param layer Layer on which the SanHalo object should be placed.
     * @return The newly created SunHalo.
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer, GameObject sun,
                                    Color color) {
        GameObject sunHalo = new GameObject(Vector2.ZERO, new Vector2(SUN_HALO_DIMENSIONS,
                SUN_HALO_DIMENSIONS), new OvalRenderable(color));
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        // Follow sun's cycle
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));
        gameObjects.addGameObject(sunHalo, layer);
        sunHalo.setTag(SUN_HALO_TAG);

        return sunHalo;
    }
}
