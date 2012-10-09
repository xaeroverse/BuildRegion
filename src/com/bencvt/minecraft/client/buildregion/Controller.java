package com.bencvt.minecraft.client.buildregion;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.PlayerControllerHooks;

import com.bencvt.minecraft.client.buildregion.region.Direction3D;
import com.bencvt.minecraft.client.buildregion.region.PlaneRegion;
import com.bencvt.minecraft.client.buildregion.ui.InputManager;
import com.bencvt.minecraft.client.buildregion.ui.MessageManager;
import com.bencvt.minecraft.client.buildregion.ui.ShapeManager;

/**
 * Primary class that ties everything together.
 * 
 * @author bencvt
 */
public class Controller {
    /**
     * Maximum number of blocks the player can be away from the region before
     * it's automatically cancelled.
     */
    public final int MAX_DISTANCE = 50;

    private final Minecraft minecraft;
    private final InputManager inputManager;
    private final MessageManager messageManager;
    private final ShapeManager shapeManager;
    private PlaneRegion planeRegion;
    private BuildMode buildMode;

    public Controller(Minecraft minecraft) {
        Listener listener = new Listener(this);
        PlayerControllerHooks.register(listener);
        this.minecraft = minecraft;
        inputManager = new InputManager(this, minecraft);
        messageManager = new MessageManager(minecraft);
        shapeManager = new ShapeManager(new LibShapeDraw().addEventListener(listener));
        buildMode = BuildMode.defaultMode();
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public void cmdClear(boolean silent) {
        unlockRegion(silent);
    }

    public void cmdSet() {
        Direction3D dir = getFacingDirection();
        if (dir == null) {
            return;
        }

        // Remember stuff about existing region, if any.
        boolean redefineRegion = planeRegion != null;
        boolean shiftRegion = redefineRegion && dir.axis == planeRegion.getAxis();

        // Define new region.
        Vector3 pos = new Vector3(
                minecraft.thePlayer.posX,
                minecraft.thePlayer.posY,
                minecraft.thePlayer.posZ);
        planeRegion = new PlaneRegion(dir.axis, pos);
        planeRegion.addCoord(dir.axisDirection * 2);

        // Update UI.
        if (shiftRegion) {
            shapeManager.animateShift(planeRegion.getCoord());
        } else {
            if (redefineRegion) {
                shapeManager.animateFadeOut();
            }
            shapeManager.animateFadeIn(dir.axis, pos, planeRegion.getCoord());
        }
        messageManager.info("build region locked to " + planeRegion + "\n");
    }

    public void cmdShift(int amount) {
        if (planeRegion == null) {
            cmdSet();
            return;
        }
        Direction3D dir = getFacingDirection();
        if (dir == null) {
            return;
        }
        if (dir.axis != planeRegion.getAxis()) {
            cmdSet();
            return;
        }

        // Update region.
        planeRegion.addCoord(amount * dir.axisDirection);

        // Update UI.
        shapeManager.animateShift(planeRegion.getCoord());
        messageManager.info("build region shifted to " + planeRegion + "\n");
    }

    public void cmdMode() {
        buildMode = buildMode.nextMode();
        shapeManager.animateGridColor(buildMode.gridColor);
        messageManager.info("build region mode: " + buildMode.toString().toLowerCase());
    }

    private Direction3D getFacingDirection() {
        Direction3D dir = Direction3D.fromYawPitch(
                minecraft.thePlayer.rotationYaw,
                minecraft.thePlayer.rotationPitch);
        if (dir == null) {
            messageManager.error("ambiguous direction\nface north, south, east, west, up, or down");
        }
        return dir;
    }

    private void unlockRegion(boolean silent) {
        if (planeRegion == null) {
            return;
        }
        planeRegion = null;
        shapeManager.animateFadeOut();
        if (!silent) {
            messageManager.info("build region unlocked\n");
        }
    }

    public void render() {
        messageManager.render();
    }

    public void updatePlayerPosition(ReadonlyVector3 playerCoords) {
        shapeManager.updateProjection(playerCoords);
        if (planeRegion != null && planeRegion.distance(playerCoords) > MAX_DISTANCE) {
            // Player is too far away from the region; disable it.
            unlockRegion(true);
            messageManager.error("build region unlocked\nbecause you are beyond " +
                    MAX_DISTANCE + " blocks away");
        }
    }

    public boolean canBuild(double x, double y, double z) {
        if (planeRegion == null) {
            return true;
        } else if (buildMode == BuildMode.INSIDE) {
            return planeRegion.isInsideRegion(x, y, z);
        } else if (buildMode == BuildMode.OUTSIDE) {
            return !planeRegion.isInsideRegion(x, y, z);
        } else {
            return true;
        }
    }

    public void disallowedClick() {
        messageManager.info("misclick blocked by build region\n");
    }
}
