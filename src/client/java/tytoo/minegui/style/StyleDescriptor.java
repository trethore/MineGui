package tytoo.minegui.style;

import imgui.ImGuiStyle;
import tytoo.minegui.util.ResourceId;

public final class StyleDescriptor {
    private final StylePropertyValues values;

    StyleDescriptor(StylePropertyValues values) {
        this.values = values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static StyleDescriptor capture(ImGuiStyle style) {
        return capture(style, ColorPalette.fromStyle(style), null, null);
    }

    public static StyleDescriptor capture(ImGuiStyle style, ColorPalette palette, ResourceId fontKey, Float fontSize) {
        StylePropertyValues values = StylePropertyValues.captureFrom(style, palette, fontKey, fontSize);
        return new StyleDescriptor(values);
    }

    public void applyTo(ImGuiStyle style) {
        values.applyTo(style);
    }

    public StyleDescriptor withDelta(StyleDelta delta) {
        if (delta == null) {
            return this;
        }
        return delta.resolve(this);
    }

    public <T> T get(StyleProperty property) {
        return values.get(property);
    }

    public float getAlpha() {
        Float val = values.get(StyleProperty.ALPHA);
        return val != null ? val : 1.0f;
    }

    public float getDisabledAlpha() {
        Float val = values.get(StyleProperty.DISABLED_ALPHA);
        return val != null ? val : 0.6f;
    }

    public Vec2 getWindowPadding() {
        Vec2 val = values.get(StyleProperty.WINDOW_PADDING);
        return val != null ? val : Vec2.of(0f, 0f);
    }

    public float getWindowRounding() {
        Float val = values.get(StyleProperty.WINDOW_ROUNDING);
        return val != null ? val : 0f;
    }

    public float getWindowBorderSize() {
        Float val = values.get(StyleProperty.WINDOW_BORDER_SIZE);
        return val != null ? val : 1f;
    }

    public Vec2 getWindowMinSize() {
        Vec2 val = values.get(StyleProperty.WINDOW_MIN_SIZE);
        return val != null ? val : Vec2.of(32f, 32f);
    }

    public Vec2 getWindowTitleAlign() {
        Vec2 val = values.get(StyleProperty.WINDOW_TITLE_ALIGN);
        return val != null ? val : Vec2.of(0f, 0.5f);
    }

    public int getWindowMenuButtonPosition() {
        Integer val = values.get(StyleProperty.WINDOW_MENU_BUTTON_POSITION);
        return val != null ? val : 0;
    }

    public float getChildRounding() {
        Float val = values.get(StyleProperty.CHILD_ROUNDING);
        return val != null ? val : 0f;
    }

    public float getChildBorderSize() {
        Float val = values.get(StyleProperty.CHILD_BORDER_SIZE);
        return val != null ? val : 1f;
    }

    public float getPopupRounding() {
        Float val = values.get(StyleProperty.POPUP_ROUNDING);
        return val != null ? val : 0f;
    }

    public float getPopupBorderSize() {
        Float val = values.get(StyleProperty.POPUP_BORDER_SIZE);
        return val != null ? val : 1f;
    }

    public Vec2 getFramePadding() {
        Vec2 val = values.get(StyleProperty.FRAME_PADDING);
        return val != null ? val : Vec2.of(4f, 3f);
    }

    public float getFrameRounding() {
        Float val = values.get(StyleProperty.FRAME_ROUNDING);
        return val != null ? val : 0f;
    }

    public float getFrameBorderSize() {
        Float val = values.get(StyleProperty.FRAME_BORDER_SIZE);
        return val != null ? val : 0f;
    }

    public Vec2 getItemSpacing() {
        Vec2 val = values.get(StyleProperty.ITEM_SPACING);
        return val != null ? val : Vec2.of(8f, 4f);
    }

    public Vec2 getItemInnerSpacing() {
        Vec2 val = values.get(StyleProperty.ITEM_INNER_SPACING);
        return val != null ? val : Vec2.of(4f, 4f);
    }

    public Vec2 getCellPadding() {
        Vec2 val = values.get(StyleProperty.CELL_PADDING);
        return val != null ? val : Vec2.of(4f, 2f);
    }

    public Vec2 getTouchExtraPadding() {
        Vec2 val = values.get(StyleProperty.TOUCH_EXTRA_PADDING);
        return val != null ? val : Vec2.of(0f, 0f);
    }

    public float getIndentSpacing() {
        Float val = values.get(StyleProperty.INDENT_SPACING);
        return val != null ? val : 21f;
    }

    public float getColumnsMinSpacing() {
        Float val = values.get(StyleProperty.COLUMNS_MIN_SPACING);
        return val != null ? val : 6f;
    }

    public float getScrollbarSize() {
        Float val = values.get(StyleProperty.SCROLLBAR_SIZE);
        return val != null ? val : 14f;
    }

    public float getScrollbarRounding() {
        Float val = values.get(StyleProperty.SCROLLBAR_ROUNDING);
        return val != null ? val : 9f;
    }

    public float getGrabMinSize() {
        Float val = values.get(StyleProperty.GRAB_MIN_SIZE);
        return val != null ? val : 10f;
    }

    public float getGrabRounding() {
        Float val = values.get(StyleProperty.GRAB_ROUNDING);
        return val != null ? val : 0f;
    }

    public float getLogSliderDeadzone() {
        Float val = values.get(StyleProperty.LOG_SLIDER_DEADZONE);
        return val != null ? val : 4f;
    }

    public float getTabRounding() {
        Float val = values.get(StyleProperty.TAB_ROUNDING);
        return val != null ? val : 4f;
    }

    public float getTabBorderSize() {
        Float val = values.get(StyleProperty.TAB_BORDER_SIZE);
        return val != null ? val : 0f;
    }

    public float getTabMinWidthForCloseButton() {
        Float val = values.get(StyleProperty.TAB_MIN_WIDTH_FOR_CLOSE_BUTTON);
        return val != null ? val : 0f;
    }

    public int getColorButtonPosition() {
        Integer val = values.get(StyleProperty.COLOR_BUTTON_POSITION);
        return val != null ? val : 1;
    }

    public Vec2 getButtonTextAlign() {
        Vec2 val = values.get(StyleProperty.BUTTON_TEXT_ALIGN);
        return val != null ? val : Vec2.of(0.5f, 0.5f);
    }

    public Vec2 getSelectableTextAlign() {
        Vec2 val = values.get(StyleProperty.SELECTABLE_TEXT_ALIGN);
        return val != null ? val : Vec2.of(0f, 0f);
    }

    public Vec2 getDisplayWindowPadding() {
        Vec2 val = values.get(StyleProperty.DISPLAY_WINDOW_PADDING);
        return val != null ? val : Vec2.of(19f, 19f);
    }

    public Vec2 getDisplaySafeAreaPadding() {
        Vec2 val = values.get(StyleProperty.DISPLAY_SAFE_AREA_PADDING);
        return val != null ? val : Vec2.of(3f, 3f);
    }

    public float getMouseCursorScale() {
        Float val = values.get(StyleProperty.MOUSE_CURSOR_SCALE);
        return val != null ? val : 1f;
    }

    public boolean isAntiAliasedLines() {
        Boolean val = values.get(StyleProperty.ANTI_ALIASED_LINES);
        return val != null ? val : true;
    }

    public boolean isAntiAliasedLinesUseTex() {
        Boolean val = values.get(StyleProperty.ANTI_ALIASED_LINES_USE_TEX);
        return val != null ? val : true;
    }

    public boolean isAntiAliasedFill() {
        Boolean val = values.get(StyleProperty.ANTI_ALIASED_FILL);
        return val != null ? val : true;
    }

    public float getCurveTessellationTol() {
        Float val = values.get(StyleProperty.CURVE_TESSELLATION_TOL);
        return val != null ? val : 1.25f;
    }

    public float getCircleTessellationMaxError() {
        Float val = values.get(StyleProperty.CIRCLE_TESSELLATION_MAX_ERROR);
        return val != null ? val : 0.3f;
    }

    public ColorPalette getColorPalette() {
        ColorPalette palette = values.colorPalette();
        return palette != null ? palette : ColorPalette.empty();
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
            initDefaults();
        }

        private void initDefaults() {
            delegate.alpha(1.0f);
            delegate.disabledAlpha(0.6f);
            delegate.windowPadding(8f, 8f);
            delegate.windowRounding(0f);
            delegate.windowBorderSize(1f);
            delegate.windowMinSize(32f, 32f);
            delegate.windowTitleAlign(0f, 0.5f);
            delegate.windowMenuButtonPosition(0);
            delegate.childRounding(0f);
            delegate.childBorderSize(1f);
            delegate.popupRounding(0f);
            delegate.popupBorderSize(1f);
            delegate.framePadding(4f, 3f);
            delegate.frameRounding(0f);
            delegate.frameBorderSize(0f);
            delegate.itemSpacing(8f, 4f);
            delegate.itemInnerSpacing(4f, 4f);
            delegate.cellPadding(4f, 2f);
            delegate.touchExtraPadding(0f, 0f);
            delegate.indentSpacing(21f);
            delegate.columnsMinSpacing(6f);
            delegate.scrollbarSize(14f);
            delegate.scrollbarRounding(9f);
            delegate.grabMinSize(10f);
            delegate.grabRounding(0f);
            delegate.logSliderDeadzone(4f);
            delegate.tabRounding(4f);
            delegate.tabBorderSize(0f);
            delegate.tabMinWidthForCloseButton(0f);
            delegate.colorButtonPosition(1);
            delegate.buttonTextAlign(0.5f, 0.5f);
            delegate.selectableTextAlign(0f, 0f);
            delegate.displayWindowPadding(19f, 19f);
            delegate.displaySafeAreaPadding(3f, 3f);
            delegate.mouseCursorScale(1f);
            delegate.antiAliasedLines(true);
            delegate.antiAliasedLinesUseTex(true);
            delegate.antiAliasedFill(true);
            delegate.curveTessellationTol(1.25f);
            delegate.circleTessellationMaxError(0.3f);
            delegate.colorPalette(ColorPalette.empty());
        }

        public Builder fromStyle(ImGuiStyle style) {
            if (style != null) {
                delegate.fromStyle(style);
            }
            return this;
        }

        public Builder fromDescriptor(StyleDescriptor descriptor) {
            if (descriptor != null) {
                delegate.fromValues(descriptor.values);
            }
            return this;
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
            delegate.colorPalette(value != null ? value : ColorPalette.empty());
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

        public StyleDescriptor build() {
            StylePropertyValues values = delegate.build();
            values.validate("StyleDescriptor");
            return new StyleDescriptor(values);
        }
    }
}
