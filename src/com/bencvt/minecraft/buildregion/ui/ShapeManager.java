package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.LibShapeDraw;
import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

import com.bencvt.minecraft.buildregion.Controller;
import com.bencvt.minecraft.buildregion.region.Axis;
import com.bencvt.minecraft.buildregion.region.RegionBase;
import com.bencvt.minecraft.buildregion.region.RegionCuboid;
import com.bencvt.minecraft.buildregion.region.RegionCylinder;
import com.bencvt.minecraft.buildregion.region.RegionPlane;
import com.bencvt.minecraft.buildregion.region.RegionSphere;

/**
 * Manage LibShapeDraw Shape objects and animations... i.e., the in-world GUI.
 * 
 * @author bencvt
 */
public class ShapeManager {
    private final Controller controller;
    private final LibShapeDraw libShapeDraw;
    private RenderBase mainShape;
    private RenderBase prevShape;
    private boolean guiScreenActive;

    public ShapeManager(Controller controller, LibShapeDraw libShapeDraw) {
        this.controller = controller;
        this.libShapeDraw = libShapeDraw;
    }

    public void reset() {
        if (mainShape == null) {
            libShapeDraw.getShapes().remove(mainShape);
        }
        mainShape = null;
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = null;
    }

    public void updateRegion(RegionBase region) {
        if (region == null) {
            removeShape();
            return;
        }
        if (mainShape != null && mainShape.updateIfPossible(region)) {
            return;
        }
        removeShape();
        mainShape = createShape(region);
        mainShape.setRenderOriginMarkerNow(guiScreenActive);
        libShapeDraw.addShape(mainShape);
        mainShape.fadeIn();
    }

    private void removeShape() {
        if (mainShape == null) {
            return;
        }
        if (prevShape != null) {
            libShapeDraw.getShapes().remove(prevShape);
        }
        prevShape = mainShape;
        mainShape = null;
        prevShape.fadeOut();
    }

    private RenderBase createShape(RegionBase region) {
        switch (region.getRegionType()) {
        case NONE:
            break;
        case PLANE:
            return new RenderPlane(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionPlane) region);
        /* TODO:
        case CUBOID:
            return new RenderCuboid(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionCuboid) region);
        case CYLINDER:
            return new RenderCylinder(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionCylinder) region);
        */
        case SPHERE:
            return new RenderSphere(
                    controller.getBuildMode().getColorVisible(),
                    controller.getBuildMode().getColorHidden(),
                    (RegionSphere) region);
        }
        if(true)return new RenderPlane(controller.getBuildMode().getColorVisible(),controller.getBuildMode().getColorHidden(),new RegionPlane(Vector3.ZEROS, Axis.Z));//XXX
        throw new IllegalStateException();
    }

    public void updateObserverPosition(ReadonlyVector3 playerCoords) {
        if (mainShape != null) {
            mainShape.updateObserverPosition(playerCoords);
        }
    }

    public void setGuiScreenActive(boolean guiScreenActive) {
        this.guiScreenActive = guiScreenActive;
        if (mainShape != null) {
            mainShape.setRenderOriginMarkerNow(guiScreenActive);
        }
        if (prevShape != null) {
            prevShape.setRenderOriginMarkerNow(guiScreenActive);
        }
    }
}