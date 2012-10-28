package com.bencvt.minecraft.buildregion.ui;

import libshapedraw.primitive.Color;
import net.minecraft.client.Minecraft;

import com.bencvt.minecraft.buildregion.region.Units;

public class GuiInputDouble extends GuiLabeledControl {
    public static final int PAD_TOP = 2;
    public static final int PAD_BOTTOM = 1;

    public static final int XBEGIN_TEXT   = 0;
    public static final int XEND_TEXT     = XBEGIN_TEXT + 44; // == fontRenderer.getStringWidth("-99999.5")
    public static final int XBEGIN_MINUS  = XEND_TEXT + 2;
    public static final int XEND_MINUS    = XBEGIN_MINUS + 11;
    public static final int XBEGIN_PLUS   = XEND_MINUS + 2;
    public static final int XEND_PLUS     = XBEGIN_PLUS + 11;
    public static final int XBEGIN_SLIDER = XEND_PLUS + 2;
    public static final int MIN_SLIDER_WIDTH = 100;
    public static final int R_XBEGIN_GROUP   = GuiInputDoubleGroup.WIDTH + 3;
    public static final int R_XEND_GROUP     = R_XBEGIN_GROUP + 11;
    public static final int R_XBEGIN_SLIDER  = R_XEND_GROUP + 2;

    public static final int BUTTON_DISABLED_ARGB = Color.BLACK.copy().scaleAlpha(0.25).getARGB();
    public static final int BUTTON_ENABLED_ARGB = Color.LIGHT_GRAY.copy().scaleAlpha(3.0/8.0).getARGB();
    public static final int BUTTON_MOUSEOVER_ARGB = Color.LIGHT_GRAY.copy().scaleAlpha(5.0/8.0).getARGB();
    public static final int BUTTON_LOCKED_ARGB = Color.DODGER_BLUE.getARGB();
    public static final int BUTTON_MOUSEOVERLOCKED_ARGB = Color.DODGER_BLUE.copy().blend(Color.LIGHT_GRAY, 0.25).getARGB();

    public static final int SLIDER_LINE0_ARGB = Color.GRAY.copy().scaleAlpha(0.5).getARGB();
    public static final int SLIDER_LINE1_ARGB = Color.DARK_GRAY.copy().scaleAlpha(0.5).getARGB();
    public static final int SLIDER_HALF_WIDTH = 2;

    private double value;
    private final Units units;
    private final boolean positive;
    private final GuiInputDoubleGroup group;
    private boolean dragging;
    private double dragStartValue;

    public GuiInputDouble(GuiBaseScreen parent, String text, Units units, boolean positive, GuiInputDoubleGroup group) {
        super(parent, text);
        this.units = units;
        this.positive = positive;
        this.group = group == null ? null : group.register(this);
        setControlWidth(XBEGIN_SLIDER + MIN_SLIDER_WIDTH + R_XBEGIN_SLIDER); // also sets width
        height = PAD_TOP + parent.getFontRenderer().FONT_HEIGHT + PAD_BOTTOM;
    }

    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        setValueFromGroup(value);
        if (group != null) {
            group.setValue(this.value);
        }
    }
    protected void setValueFromGroup(double value) {
        if (positive) {
            this.value = units.clampAtom(value);
        } else {
            this.value = units.clamp(value);
        }
    }
    private void setValueFromSlider(int xMouse) {
        final int xMinSlider = getControlXOffset() + XBEGIN_SLIDER;
        final int xMaxSlider = xPosition + width - R_XBEGIN_SLIDER;
        final int xMidSlider = xMinSlider + (xMaxSlider - xMinSlider)/2;
        // TODO: scaling factor
        setValue(dragStartValue + units.atom*(xMouse - xMidSlider));
    }

    private boolean isMinusButtonEnabled() {
        return !positive || value > units.atom;
    }

    private boolean isPlusButtonEnabled() {
        return true;
    }

    @Override
    protected int getLabelYOffset() {
        return PAD_TOP;
    }

    @Override
    protected void drawControl(int xMouse, int yMouse) {
        final int xControlBegin = getControlXOffset();
        final int xControlEnd = xPosition + width;

        // Value as text.
        String valueString = units.d2s(getValue());
        parent.getFontRenderer().drawString(
                valueString,
                xControlBegin + XEND_TEXT - XBEGIN_TEXT - parent.getFontRenderer().getStringWidth(valueString),
                yPosition + PAD_TOP,
                CONTROL_ENABLED_ARGB);

        // Minus button.
        drawSubButton(xMouse, yMouse, "-",
                isMinusButtonEnabled(), false,
                xControlBegin + XBEGIN_MINUS,
                xControlBegin + XEND_MINUS, xControlBegin + XEND_MINUS);

        // Plus button.
        drawSubButton(xMouse, yMouse, "+",
                isPlusButtonEnabled(), false,
                xControlBegin + XBEGIN_PLUS,
                xControlBegin + XEND_PLUS, xControlBegin + XEND_PLUS);

        // Slider.
        final int xMinSlider = xControlBegin + XBEGIN_SLIDER;
        final int xMaxSlider = xControlEnd - R_XBEGIN_SLIDER;
        final int xSlider;
        if (dragging) {
            setValueFromSlider(xMouse);
            parent.rapidUpdate(this);
            xSlider = Math.min(Math.max(xMinSlider + SLIDER_HALF_WIDTH, xMouse), xMaxSlider - SLIDER_HALF_WIDTH);
        } else {
            xSlider = xMinSlider + (xMaxSlider - xMinSlider)/2;
        }
        int ySlider = yPosition + (height/2);
        drawRect(xMinSlider, ySlider - 1, xMaxSlider, ySlider,     SLIDER_LINE0_ARGB);
        drawRect(xMinSlider, ySlider,     xMaxSlider, ySlider + 1, SLIDER_LINE1_ARGB);
        drawRect(
                xSlider - SLIDER_HALF_WIDTH,
                yPosition,
                xSlider + SLIDER_HALF_WIDTH,
                yPosition + height - 1,
                Color.PINK.getARGB());

        // Group button.
        if (group != null) {
            drawSubButton(xMouse, yMouse, "=",
                    true, group.isLocked(),
                    xControlEnd - R_XEND_GROUP,
                    xControlEnd - R_XBEGIN_GROUP, xControlEnd);
        }
    }

    private void drawSubButton(int xMouse, int yMouse, String text, boolean enabled, boolean locked, int xBegin, int xEnd, int xEndMouse) {
        final int buttonARGB;
        if (!enabled) {
            buttonARGB = BUTTON_DISABLED_ARGB;
        } else {
            if (xMouse >= xBegin && xMouse <= xEndMouse &&
                    yMouse >= yPosition && yMouse <= yPosition + height) {
                buttonARGB = locked ? BUTTON_MOUSEOVERLOCKED_ARGB : BUTTON_MOUSEOVER_ARGB;
            } else {
                buttonARGB = locked ? BUTTON_LOCKED_ARGB : BUTTON_ENABLED_ARGB;
            }
        }
        drawRect(xBegin, yPosition, xEnd, yPosition + height - 1, buttonARGB);
        parent.getFontRenderer().drawString(
                text,
                xBegin + (xEnd - xBegin - parent.getFontRenderer().getStringWidth(text))/2 + 1,
                yPosition + PAD_TOP,
                enabled ? CONTROL_ENABLED_ARGB : CONTROL_DISABLED_ARGB);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int xMouse, int yMouse) {
        if (!super.mousePressed(minecraft, xMouse, yMouse)) {
            return false;
        }
        final int xL = xMouse - getControlXOffset();
        final int xR = xPosition + width - xMouse;
        if (xL >= XBEGIN_TEXT && xL <= XEND_TEXT) {
            System.out.println("you clicked text");//TODO: turn into text input box
            return true;
        } else if (isMinusButtonEnabled() && xL >= XBEGIN_MINUS && xL <= XEND_MINUS) {
            setValue(getValue() - units.atom);
            return true;
        } else if (isPlusButtonEnabled() && xL >= XBEGIN_PLUS && xL <= XEND_PLUS) {
            setValue(getValue() + units.atom);
            return true;
        } else if (xL >= XBEGIN_SLIDER && xR >= R_XBEGIN_SLIDER) {
            dragging = true;
            dragStartValue = getValue();
            setValueFromSlider(xMouse);
            return true;
        } else if (group != null && xR <= R_XEND_GROUP) {
            if (group.isLocked()) {
                group.unlock();
            } else {
                group.lock(value);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mouseReleased(int xMouse, int yMouse) {
        dragging = false;
    }
}
