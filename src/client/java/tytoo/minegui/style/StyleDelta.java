package tytoo.minegui.style;

import imgui.ImGuiStyle;
import tytoo.minegui.util.ResourceId;

import java.util.Objects;

@SuppressWarnings("unused")
public final class StyleDelta {
    private final StylePropertyValues values;

    private StyleDelta(StylePropertyValues values) {
        this.values = values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void applyTo(ImGuiStyle style) {
        values.applyTo(style);
    }

    public StyleDescriptor resolve(StyleDescriptor base) {
        Objects.requireNonNull(base, "base");
        StylePropertyValues merged = base.values().mergeWith(values);
        return new StyleDescriptor(merged);
    }

    public <T> T get(StyleProperty property) {
        return values.get(property);
    }

    public Float getAlpha() {
        return values.get(StyleProperty.ALPHA);
    }

    public Float getDisabledAlpha() {
        return values.get(StyleProperty.DISABLED_ALPHA);
    }

    public Vec2 getWindowPadding() {
        return values.get(StyleProperty.WINDOW_PADDING);
    }

    public Float getWindowRounding() {
        return values.get(StyleProperty.WINDOW_ROUNDING);
    }

    public Float getWindowBorderSize() {
        return values.get(StyleProperty.WINDOW_BORDER_SIZE);
    }

    public Vec2 getWindowMinSize() {
        return values.get(StyleProperty.WINDOW_MIN_SIZE);
    }

    public Vec2 getWindowTitleAlign() {
        return values.get(StyleProperty.WINDOW_TITLE_ALIGN);
    }

    public Integer getWindowMenuButtonPosition() {
        return values.get(StyleProperty.WINDOW_MENU_BUTTON_POSITION);
    }

    public Float getChildRounding() {
        return values.get(StyleProperty.CHILD_ROUNDING);
    }

    public Float getChildBorderSize() {
        return values.get(StyleProperty.CHILD_BORDER_SIZE);
    }

    public Float getPopupRounding() {
        return values.get(StyleProperty.POPUP_ROUNDING);
    }

    public Float getPopupBorderSize() {
        return values.get(StyleProperty.POPUP_BORDER_SIZE);
    }

    public Vec2 getFramePadding() {
        return values.get(StyleProperty.FRAME_PADDING);
    }

    public Float getFrameRounding() {
        return values.get(StyleProperty.FRAME_ROUNDING);
    }

    public Float getFrameBorderSize() {
        return values.get(StyleProperty.FRAME_BORDER_SIZE);
    }

    public Vec2 getItemSpacing() {
        return values.get(StyleProperty.ITEM_SPACING);
    }

    public Vec2 getItemInnerSpacing() {
        return values.get(StyleProperty.ITEM_INNER_SPACING);
    }

    public Vec2 getCellPadding() {
        return values.get(StyleProperty.CELL_PADDING);
    }

    public Vec2 getTouchExtraPadding() {
        return values.get(StyleProperty.TOUCH_EXTRA_PADDING);
    }

    public Float getIndentSpacing() {
        return values.get(StyleProperty.INDENT_SPACING);
    }

    public Float getColumnsMinSpacing() {
        return values.get(StyleProperty.COLUMNS_MIN_SPACING);
    }

    public Float getScrollbarSize() {
        return values.get(StyleProperty.SCROLLBAR_SIZE);
    }

    public Float getScrollbarRounding() {
        return values.get(StyleProperty.SCROLLBAR_ROUNDING);
    }

    public Float getGrabMinSize() {
        return values.get(StyleProperty.GRAB_MIN_SIZE);
    }

    public Float getGrabRounding() {
        return values.get(StyleProperty.GRAB_ROUNDING);
    }

    public Float getLogSliderDeadzone() {
        return values.get(StyleProperty.LOG_SLIDER_DEADZONE);
    }

    public Float getTabRounding() {
        return values.get(StyleProperty.TAB_ROUNDING);
    }

    public Float getTabBorderSize() {
        return values.get(StyleProperty.TAB_BORDER_SIZE);
    }

    public Float getTabMinWidthForCloseButton() {
        return values.get(StyleProperty.TAB_MIN_WIDTH_FOR_CLOSE_BUTTON);
    }

    public Integer getColorButtonPosition() {
        return values.get(StyleProperty.COLOR_BUTTON_POSITION);
    }

    public Vec2 getButtonTextAlign() {
        return values.get(StyleProperty.BUTTON_TEXT_ALIGN);
    }

    public Vec2 getSelectableTextAlign() {
        return values.get(StyleProperty.SELECTABLE_TEXT_ALIGN);
    }

    public Vec2 getDisplayWindowPadding() {
        return values.get(StyleProperty.DISPLAY_WINDOW_PADDING);
    }

    public Vec2 getDisplaySafeAreaPadding() {
        return values.get(StyleProperty.DISPLAY_SAFE_AREA_PADDING);
    }

    public Float getMouseCursorScale() {
        return values.get(StyleProperty.MOUSE_CURSOR_SCALE);
    }

    public Boolean getAntiAliasedLines() {
        return values.get(StyleProperty.ANTI_ALIASED_LINES);
    }

    public Boolean getAntiAliasedLinesUseTex() {
        return values.get(StyleProperty.ANTI_ALIASED_LINES_USE_TEX);
    }

    public Boolean getAntiAliasedFill() {
        return values.get(StyleProperty.ANTI_ALIASED_FILL);
    }

    public Float getCurveTessellationTol() {
        return values.get(StyleProperty.CURVE_TESSELLATION_TOL);
    }

    public Float getCircleTessellationMaxError() {
        return values.get(StyleProperty.CIRCLE_TESSELLATION_MAX_ERROR);
    }

    public ColorPalette getColorPalette() {
        return values.colorPalette();
    }

    public ResourceId getFontKey() {
        return values.fontKey();
    }

    public Float getFontSize() {
        return values.fontSize();
    }

    StylePropertyValues values() {
        return values;
    }

    public static final class Builder {
        private final StylePropertyValues.Builder delegate = StylePropertyValues.builder();

        private Builder() {
        }

        public Builder alpha(float value) {
            delegate.alpha(value);
            return this;
        }

        public Builder disabledAlpha(float value) {
            delegate.disabledAlpha(value);
            return this;
        }

        public Builder windowPadding(float x, float y) {
            delegate.windowPadding(x, y);
            return this;
        }

        public Builder windowPadding(Vec2 value) {
            delegate.windowPadding(value);
            return this;
        }

        public Builder windowRounding(float value) {
            delegate.windowRounding(value);
            return this;
        }

        public Builder windowBorderSize(float value) {
            delegate.windowBorderSize(value);
            return this;
        }

        public Builder windowMinSize(float x, float y) {
            delegate.windowMinSize(x, y);
            return this;
        }

        public Builder windowMinSize(Vec2 value) {
            delegate.windowMinSize(value);
            return this;
        }

        public Builder windowTitleAlign(float x, float y) {
            delegate.windowTitleAlign(x, y);
            return this;
        }

        public Builder windowTitleAlign(Vec2 value) {
            delegate.windowTitleAlign(value);
            return this;
        }

        public Builder windowMenuButtonPosition(int value) {
            delegate.windowMenuButtonPosition(value);
            return this;
        }

        public Builder childRounding(float value) {
            delegate.childRounding(value);
            return this;
        }

        public Builder childBorderSize(float value) {
            delegate.childBorderSize(value);
            return this;
        }

        public Builder popupRounding(float value) {
            delegate.popupRounding(value);
            return this;
        }

        public Builder popupBorderSize(float value) {
            delegate.popupBorderSize(value);
            return this;
        }

        public Builder framePadding(float x, float y) {
            delegate.framePadding(x, y);
            return this;
        }

        public Builder framePadding(Vec2 value) {
            delegate.framePadding(value);
            return this;
        }

        public Builder frameRounding(float value) {
            delegate.frameRounding(value);
            return this;
        }

        public Builder frameBorderSize(float value) {
            delegate.frameBorderSize(value);
            return this;
        }

        public Builder itemSpacing(float x, float y) {
            delegate.itemSpacing(x, y);
            return this;
        }

        public Builder itemSpacing(Vec2 value) {
            delegate.itemSpacing(value);
            return this;
        }

        public Builder itemInnerSpacing(float x, float y) {
            delegate.itemInnerSpacing(x, y);
            return this;
        }

        public Builder itemInnerSpacing(Vec2 value) {
            delegate.itemInnerSpacing(value);
            return this;
        }

        public Builder cellPadding(float x, float y) {
            delegate.cellPadding(x, y);
            return this;
        }

        public Builder cellPadding(Vec2 value) {
            delegate.cellPadding(value);
            return this;
        }

        public Builder touchExtraPadding(float x, float y) {
            delegate.touchExtraPadding(x, y);
            return this;
        }

        public Builder touchExtraPadding(Vec2 value) {
            delegate.touchExtraPadding(value);
            return this;
        }

        public Builder indentSpacing(float value) {
            delegate.indentSpacing(value);
            return this;
        }

        public Builder columnsMinSpacing(float value) {
            delegate.columnsMinSpacing(value);
            return this;
        }

        public Builder scrollbarSize(float value) {
            delegate.scrollbarSize(value);
            return this;
        }

        public Builder scrollbarRounding(float value) {
            delegate.scrollbarRounding(value);
            return this;
        }

        public Builder grabMinSize(float value) {
            delegate.grabMinSize(value);
            return this;
        }

        public Builder grabRounding(float value) {
            delegate.grabRounding(value);
            return this;
        }

        public Builder logSliderDeadzone(float value) {
            delegate.logSliderDeadzone(value);
            return this;
        }

        public Builder tabRounding(float value) {
            delegate.tabRounding(value);
            return this;
        }

        public Builder tabBorderSize(float value) {
            delegate.tabBorderSize(value);
            return this;
        }

        public Builder tabMinWidthForCloseButton(float value) {
            delegate.tabMinWidthForCloseButton(value);
            return this;
        }

        public Builder colorButtonPosition(int value) {
            delegate.colorButtonPosition(value);
            return this;
        }

        public Builder buttonTextAlign(float x, float y) {
            delegate.buttonTextAlign(x, y);
            return this;
        }

        public Builder buttonTextAlign(Vec2 value) {
            delegate.buttonTextAlign(value);
            return this;
        }

        public Builder selectableTextAlign(float x, float y) {
            delegate.selectableTextAlign(x, y);
            return this;
        }

        public Builder selectableTextAlign(Vec2 value) {
            delegate.selectableTextAlign(value);
            return this;
        }

        public Builder displayWindowPadding(float x, float y) {
            delegate.displayWindowPadding(x, y);
            return this;
        }

        public Builder displayWindowPadding(Vec2 value) {
            delegate.displayWindowPadding(value);
            return this;
        }

        public Builder displaySafeAreaPadding(float x, float y) {
            delegate.displaySafeAreaPadding(x, y);
            return this;
        }

        public Builder displaySafeAreaPadding(Vec2 value) {
            delegate.displaySafeAreaPadding(value);
            return this;
        }

        public Builder mouseCursorScale(float value) {
            delegate.mouseCursorScale(value);
            return this;
        }

        public Builder antiAliasedLines(boolean value) {
            delegate.antiAliasedLines(value);
            return this;
        }

        public Builder antiAliasedLinesUseTex(boolean value) {
            delegate.antiAliasedLinesUseTex(value);
            return this;
        }

        public Builder antiAliasedFill(boolean value) {
            delegate.antiAliasedFill(value);
            return this;
        }

        public Builder curveTessellationTol(float value) {
            delegate.curveTessellationTol(value);
            return this;
        }

        public Builder circleTessellationMaxError(float value) {
            delegate.circleTessellationMaxError(value);
            return this;
        }

        public Builder colorPalette(ColorPalette value) {
            delegate.colorPalette(value);
            return this;
        }

        public Builder fontKey(ResourceId value) {
            delegate.fontKey(value);
            return this;
        }

        public Builder fontSize(Float value) {
            delegate.fontSize(value);
            return this;
        }

        public StyleDelta build() {
            StylePropertyValues values = delegate.build();
            values.validate("StyleDelta");
            return new StyleDelta(values);
        }
    }
}
