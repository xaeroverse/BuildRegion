package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * A geometric region in space.
 * 
 * @author bencvt
 */
public abstract class RegionBase {
    //XXX: public static final RegionBase DEFAULT_REGION = new RegionPlane(Vector3.ZEROS, Axis.X);
    public static final RegionBase DEFAULT_REGION = new RegionCuboid(new Vector3(0, 10, 0), new Vector3(5, 13, 12));

    private final Vector3 origin;

    protected RegionBase(ReadonlyVector3 origin) {
        this.origin = origin.copy();
        onOriginUpdate();
    }

    // ========
    // Methods for child classes to override (don't forget toString!)
    // ========

    public abstract RegionType getRegionType();

    /**
     * Deep copy this region but use a different origin. The axis parameter
     * may be used too, though it isn't applicable to all region types.
     */
    public abstract RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis);

    /**
     * Called whenever one of the origin's components is modified; this is the
     * hook to force origin coordinates to be a whole (or half) number.
     */
    protected abstract void onOriginUpdate();

    /**
     * @return true if it makes sense to shift the region along the specified
     *         axis. Will only ever be false if the region is infinite in one
     *         or more directions.
     */
    public boolean canShiftAlongAxis(Axis axis) {
        return axis != null;
    }

    /**
     * @return true if the position (x,y,z) is inside this region
     */
    public abstract boolean isInsideRegion(double x, double y, double z);

    public abstract double size();

    /**
     * Get the axis-aligned minimum bounding box around this region.
     * @return true if bounds were set, false if this region is infinite
     */
    public abstract boolean getAABB(Vector3 lower, Vector3 upper);

    // ========
    // Accessors and mutators
    // ========

    public final boolean isRegionType(Class<? extends RegionBase> type) {
        return this != DEFAULT_REGION && type.isInstance(this);
    }

    public final ReadonlyVector3 getOriginReadonly() {
        return origin;
    }

    protected final Vector3 getOrigin() {
        return origin;
    }

    public final double getOriginCoord(Axis axis) {
        if (axis == Axis.X) {
            return getOrigin().getX();
        } else if (axis == Axis.Y) {
            return getOrigin().getY();
        } else if (axis == Axis.Z) {
            return getOrigin().getZ();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public final void setOriginCoord(Axis axis, double value) {
        if (axis == Axis.X) {
            getOrigin().setX(value);
        } else if (axis == Axis.Y) {
            getOrigin().setY(value);
        } else if (axis == Axis.Z) {
            getOrigin().setZ(value);
        } else {
            throw new IllegalArgumentException();
        }
        onOriginUpdate();
    }

    public final void shiftOriginCoord(Axis axis, double amount) {
        setOriginCoord(axis, getOriginCoord(axis) + amount);
    }

    // ========
    // Utility methods
    // ========

    protected static final void enforceHalfUnits(Vector3 v) {
        v.scale(2.0).floor().scale(0.5);
    }

    protected static final double enforceHalfUnits(double d) {
        return enforceWholeUnits(d * 2.0) * 0.5;
    }

    protected static final void enforceWholeUnits(Vector3 v) {
        v.floor();
    }

    protected static final double enforceWholeUnits(double d) {
        return Math.floor(d);
    }

    protected static final boolean isWholeUnit(double d) {
        return d == (int) d;
    }

    protected static final String strXYZ(Vector3 v) {
        return "x=" + strFloorIfPossible(v.getX()) +
                ", y=" + strFloorIfPossible(v.getY()) +
                ", z=" + strFloorIfPossible(v.getZ());
    }

    protected static final String strFloorIfPossible(double d) {
        int i = (int) Math.floor(d);
        return d == i ? Integer.toString(i) : Double.toString(d);
    }
}
