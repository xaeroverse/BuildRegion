package com.bencvt.minecraft.buildregion.region;

import libshapedraw.primitive.ReadonlyVector3;
import libshapedraw.primitive.Vector3;

/**
 * Represent a cylinder -- actually an elliptic cylinder, or cylindroid --
 * specified by an origin 3-tuple, a radii 2-tuple, an axis, and a height.
 * The components of each tuple are reduced to half units; the height is
 * reduced to whole units. The one component of the origin matching the axis
 * is also reduced to whole units. Radii are non-negative and >=0.5. Height
 * is non negative and >=1.0.
 * 
 * @author bencvt
 */
public class RegionCylinder extends RegionBase {
    private final Vector3 halfHeightAndRadii;

    protected RegionCylinder(ReadonlyVector3 origin, Axis axis, double height, double radiusA, double radiusB) {
        super(origin, axis);
        halfHeightAndRadii = new Vector3();
        setHeight(height);
        setRadiusA(radiusA);
        setRadiusB(radiusB);
    }

    @Override
    public RegionType getRegionType() {
        return RegionType.CYLINDER;
    }

    @Override
    public RegionBase copyUsing(ReadonlyVector3 newOrigin, Axis newAxis) {
        if (getAxis() == newAxis) {
            return new RegionCylinder(newOrigin, newAxis, getHeight(), getRadiusA(), getRadiusB());
        } else {
            return new RegionCylinder(newOrigin, newAxis, getHeight(), getRadiusB(), getRadiusA());
        }
    }

    @Override
    protected void onOriginUpdate() {
        Units.HALF.clamp(getOriginMutable());
        getAxis().setVectorComponent(getOriginMutable(),
                Units.WHOLE.clamp(getAxis().getVectorComponent(getOriginReadonly())));
    }

    @Override
    public boolean isInsideRegion(double x, double y, double z) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getSize() {
        return Math.PI * 2.0 * halfHeightAndRadii.getX() * halfHeightAndRadii.getY() * halfHeightAndRadii.getZ();
    }

    @Override
    public boolean getAABB(Vector3 lower, Vector3 upper) {
        lower.set(getOriginReadonly()).subtract(halfHeightAndRadii);
        upper.set(getOriginReadonly()).add(halfHeightAndRadii);
        return true;
    }

    @Override
    public Units getUnits(Axis axis) {
        return axis == this.getAxis() ? Units.WHOLE : Units.HALF;
    }

    @Override
    public boolean expand(Axis axis, double amount) {
        if (axis == this.getAxis()) {
            amount *= 0.5;
            // TODO: adjust origin too
        }
        double prev = axis.getVectorComponent(halfHeightAndRadii);
        Units.HALF.clampAtom(axis.addVectorComponent(halfHeightAndRadii, amount));
        return axis.getVectorComponent(halfHeightAndRadii) != prev;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        double radiusA = getRadiusA();
        double radiusB = getRadiusB();
        if (radiusA == radiusB) {
            b.append(i18n("enum.regiontype.cylinder.locked")).append(" @ ");
            b.append(Units.HALF.v2s(getOriginReadonly())).append('\n');
            b.append(i18n("radius")).append(' ').append(i18n(getRadiusAxisA()));
            b.append('/').append(i18n(getRadiusAxisB()));
            b.append('=');
            b.append(Units.HALF.d2s(radiusA));
        } else {
            b.append(i18n("enum.regiontype.cylinder.unlocked")).append(" @ ");
            b.append(Units.HALF.v2s(getOriginReadonly())).append('\n');
            b.append(i18n("radius")).append(' ').append(i18n(getRadiusAxisA()));
            b.append('=');
            b.append(Units.HALF.d2s(radiusA));
            b.append(", ").append(i18n(getRadiusAxisB()));
            b.append('=');
            b.append(Units.HALF.d2s(radiusB));
        }
        b.append(' ').append(i18n("height")).append(' ').append(i18n(getAxis()));
        b.append('=');
        b.append(Units.WHOLE.d2s(getHeight()));
        return b.toString();
    }

    @Override
    public void setAxis(Axis axis) {
        super.setAxis(axis);
        onOriginUpdate();
    }

    public double getHeight() {
        return getAxis().getVectorComponent(halfHeightAndRadii) * 2.0;
    }
    public void setHeight(double height) {
        getAxis().setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(height * 0.5));
    }

    public double getRadiusA() {
        return getAxis().next().getVectorComponent(halfHeightAndRadii);
    }
    public void setRadiusA(double radiusA) {
        getAxis().next().setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(radiusA));
    }

    public double getRadiusB() {
        return getAxis().next().next().getVectorComponent(halfHeightAndRadii);
    }
    public void setRadiusB(double radiusB) {
        getAxis().next().next().setVectorComponent(halfHeightAndRadii, Units.HALF.clampAtom(radiusB));
    }

    public Axis getRadiusAxisA() {
        return getAxis().next();
    }

    public Axis getRadiusAxisB() {
        return getAxis().next().next();
    }

    public ReadonlyVector3 getHalfHeightAndRadiiReadonly() {
        return halfHeightAndRadii;
    }
}
