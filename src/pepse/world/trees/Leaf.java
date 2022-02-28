package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Block;

public class Leaf extends Block {

    /* Private members */
    private Transition transition;  // set after construction using a setter; turns off on collision

    /**
     * Construct a new Block instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Leaf(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, renderable);
    }

    /**
     * Called upon colliding with another GameObject.
     *
     * @param other     The other GameObject with which the Block has collided with.
     * @param collision Various information regarding the collision.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        // Cancel lateral movement of leaves
        this.removeComponent(transition);
        this.transform().setVelocity(Vector2.ZERO);
    }


    /* Setter for the strategy.
    This Transition will be that responsible for the lateral movement of leaves */
    public void setTransition(Transition transition) {
        this.transition = transition;
    }

}
