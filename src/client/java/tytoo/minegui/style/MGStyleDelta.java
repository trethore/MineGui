package tytoo.minegui.style;

import imgui.ImGuiStyle;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class MGStyleDelta {
    private final Float alpha;
    private final Float disabledAlpha;
    private final MGVec2 windowPadding;
    private final Float windowRounding;
    private final Float windowBorderSize;
    private final MGVec2 windowMinSize;
    private final MGVec2 windowTitleAlign;
    private final Integer windowMenuButtonPosition;
    private final Float childRounding;
    private final Float childBorderSize;
    private final Float popupRounding;
    private final Float popupBorderSize;
    private final MGVec2 framePadding;
    private final Float frameRounding;
    private final Float frameBorderSize;
    private final MGVec2 itemSpacing;
    private final MGVec2 itemInnerSpacing;
    private final MGVec2 cellPadding;
    private final MGVec2 touchExtraPadding;
    private final Float indentSpacing;
    private final Float columnsMinSpacing;
    private final Float scrollbarSize;
    private final Float scrollbarRounding;
    private final Float grabMinSize;
    private final Float grabRounding;
    private final Float logSliderDeadzone;
    private final Float tabRounding;
    private final Float tabBorderSize;
    private final Float tabMinWidthForCloseButton;
    private final Integer colorButtonPosition;
    private final MGVec2 buttonTextAlign;
    private final MGVec2 selectableTextAlign;
    private final MGVec2 displayWindowPadding;
    private final MGVec2 displaySafeAreaPadding;
    private final Float mouseCursorScale;
    private final Boolean antiAliasedLines;
    private final Boolean antiAliasedLinesUseTex;
    private final Boolean antiAliasedFill;
    private final Float curveTessellationTol;
    private final Float circleTessellationMaxError;
    private final MGColorPalette colorPalette;
    private final Identifier fontKey;
    private final Float fontSize;

    private MGStyleDelta(Builder builder) {
        this.alpha = builder.alpha;
        this.disabledAlpha = builder.disabledAlpha;
        this.windowPadding = builder.windowPadding;
        this.windowRounding = builder.windowRounding;
        this.windowBorderSize = builder.windowBorderSize;
        this.windowMinSize = builder.windowMinSize;
        this.windowTitleAlign = builder.windowTitleAlign;
        this.windowMenuButtonPosition = builder.windowMenuButtonPosition;
        this.childRounding = builder.childRounding;
        this.childBorderSize = builder.childBorderSize;
        this.popupRounding = builder.popupRounding;
        this.popupBorderSize = builder.popupBorderSize;
        this.framePadding = builder.framePadding;
        this.frameRounding = builder.frameRounding;
        this.frameBorderSize = builder.frameBorderSize;
        this.itemSpacing = builder.itemSpacing;
        this.itemInnerSpacing = builder.itemInnerSpacing;
        this.cellPadding = builder.cellPadding;
        this.touchExtraPadding = builder.touchExtraPadding;
        this.indentSpacing = builder.indentSpacing;
        this.columnsMinSpacing = builder.columnsMinSpacing;
        this.scrollbarSize = builder.scrollbarSize;
        this.scrollbarRounding = builder.scrollbarRounding;
        this.grabMinSize = builder.grabMinSize;
        this.grabRounding = builder.grabRounding;
        this.logSliderDeadzone = builder.logSliderDeadzone;
        this.tabRounding = builder.tabRounding;
        this.tabBorderSize = builder.tabBorderSize;
        this.tabMinWidthForCloseButton = builder.tabMinWidthForCloseButton;
        this.colorButtonPosition = builder.colorButtonPosition;
        this.buttonTextAlign = builder.buttonTextAlign;
        this.selectableTextAlign = builder.selectableTextAlign;
        this.displayWindowPadding = builder.displayWindowPadding;
        this.displaySafeAreaPadding = builder.displaySafeAreaPadding;
        this.mouseCursorScale = builder.mouseCursorScale;
        this.antiAliasedLines = builder.antiAliasedLines;
        this.antiAliasedLinesUseTex = builder.antiAliasedLinesUseTex;
        this.antiAliasedFill = builder.antiAliasedFill;
        this.curveTessellationTol = builder.curveTessellationTol;
        this.circleTessellationMaxError = builder.circleTessellationMaxError;
        this.colorPalette = builder.colorPalette;
        this.fontKey = builder.fontKey;
        this.fontSize = builder.fontSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void applyTo(ImGuiStyle style) {
        if (style == null) {
            return;
        }
        if (alpha != null) {
            style.setAlpha(alpha);
        }
        if (disabledAlpha != null) {
            style.setDisabledAlpha(disabledAlpha);
        }
        if (windowPadding != null) {
            style.setWindowPadding(windowPadding.x(), windowPadding.y());
        }
        if (windowRounding != null) {
            style.setWindowRounding(windowRounding);
        }
        if (windowBorderSize != null) {
            style.setWindowBorderSize(windowBorderSize);
        }
        if (windowMinSize != null) {
            style.setWindowMinSize(windowMinSize.x(), windowMinSize.y());
        }
        if (windowTitleAlign != null) {
            style.setWindowTitleAlign(windowTitleAlign.x(), windowTitleAlign.y());
        }
        if (windowMenuButtonPosition != null) {
            style.setWindowMenuButtonPosition(windowMenuButtonPosition);
        }
        if (childRounding != null) {
            style.setChildRounding(childRounding);
        }
        if (childBorderSize != null) {
            style.setChildBorderSize(childBorderSize);
        }
        if (popupRounding != null) {
            style.setPopupRounding(popupRounding);
        }
        if (popupBorderSize != null) {
            style.setPopupBorderSize(popupBorderSize);
        }
        if (framePadding != null) {
            style.setFramePadding(framePadding.x(), framePadding.y());
        }
        if (frameRounding != null) {
            style.setFrameRounding(frameRounding);
        }
        if (frameBorderSize != null) {
            style.setFrameBorderSize(frameBorderSize);
        }
        if (itemSpacing != null) {
            style.setItemSpacing(itemSpacing.x(), itemSpacing.y());
        }
        if (itemInnerSpacing != null) {
            style.setItemInnerSpacing(itemInnerSpacing.x(), itemInnerSpacing.y());
        }
        if (cellPadding != null) {
            style.setCellPadding(cellPadding.x(), cellPadding.y());
        }
        if (touchExtraPadding != null) {
            style.setTouchExtraPadding(touchExtraPadding.x(), touchExtraPadding.y());
        }
        if (indentSpacing != null) {
            style.setIndentSpacing(indentSpacing);
        }
        if (columnsMinSpacing != null) {
            style.setColumnsMinSpacing(columnsMinSpacing);
        }
        if (scrollbarSize != null) {
            style.setScrollbarSize(scrollbarSize);
        }
        if (scrollbarRounding != null) {
            style.setScrollbarRounding(scrollbarRounding);
        }
        if (grabMinSize != null) {
            style.setGrabMinSize(grabMinSize);
        }
        if (grabRounding != null) {
            style.setGrabRounding(grabRounding);
        }
        if (logSliderDeadzone != null) {
            style.setLogSliderDeadzone(logSliderDeadzone);
        }
        if (tabRounding != null) {
            style.setTabRounding(tabRounding);
        }
        if (tabBorderSize != null) {
            style.setTabBorderSize(tabBorderSize);
        }
        if (tabMinWidthForCloseButton != null) {
            style.setTabMinWidthForCloseButton(tabMinWidthForCloseButton);
        }
        if (colorButtonPosition != null) {
            style.setColorButtonPosition(colorButtonPosition);
        }
        if (buttonTextAlign != null) {
            style.setButtonTextAlign(buttonTextAlign.x(), buttonTextAlign.y());
        }
        if (selectableTextAlign != null) {
            style.setSelectableTextAlign(selectableTextAlign.x(), selectableTextAlign.y());
        }
        if (displayWindowPadding != null) {
            style.setDisplayWindowPadding(displayWindowPadding.x(), displayWindowPadding.y());
        }
        if (displaySafeAreaPadding != null) {
            style.setDisplaySafeAreaPadding(displaySafeAreaPadding.x(), displaySafeAreaPadding.y());
        }
        if (mouseCursorScale != null) {
            style.setMouseCursorScale(mouseCursorScale);
        }
        if (antiAliasedLines != null) {
            style.setAntiAliasedLines(antiAliasedLines);
        }
        if (antiAliasedLinesUseTex != null) {
            style.setAntiAliasedLinesUseTex(antiAliasedLinesUseTex);
        }
        if (antiAliasedFill != null) {
            style.setAntiAliasedFill(antiAliasedFill);
        }
        if (curveTessellationTol != null) {
            style.setCurveTessellationTol(curveTessellationTol);
        }
        if (circleTessellationMaxError != null) {
            style.setCircleTessellationMaxError(circleTessellationMaxError);
        }
        if (colorPalette != null) {
            colorPalette.applyTo(style);
        }
    }

    public MGStyleDescriptor resolve(MGStyleDescriptor base) {
        Objects.requireNonNull(base, "base");
        MGStyleDescriptor.Builder builder = MGStyleDescriptor.builder().fromDescriptor(base);
        if (alpha != null) {
            builder.alpha(alpha);
        }
        if (disabledAlpha != null) {
            builder.disabledAlpha(disabledAlpha);
        }
        if (windowPadding != null) {
            builder.windowPadding(windowPadding);
        }
        if (windowRounding != null) {
            builder.windowRounding(windowRounding);
        }
        if (windowBorderSize != null) {
            builder.windowBorderSize(windowBorderSize);
        }
        if (windowMinSize != null) {
            builder.windowMinSize(windowMinSize);
        }
        if (windowTitleAlign != null) {
            builder.windowTitleAlign(windowTitleAlign);
        }
        if (windowMenuButtonPosition != null) {
            builder.windowMenuButtonPosition(windowMenuButtonPosition);
        }
        if (childRounding != null) {
            builder.childRounding(childRounding);
        }
        if (childBorderSize != null) {
            builder.childBorderSize(childBorderSize);
        }
        if (popupRounding != null) {
            builder.popupRounding(popupRounding);
        }
        if (popupBorderSize != null) {
            builder.popupBorderSize(popupBorderSize);
        }
        if (framePadding != null) {
            builder.framePadding(framePadding);
        }
        if (frameRounding != null) {
            builder.frameRounding(frameRounding);
        }
        if (frameBorderSize != null) {
            builder.frameBorderSize(frameBorderSize);
        }
        if (itemSpacing != null) {
            builder.itemSpacing(itemSpacing);
        }
        if (itemInnerSpacing != null) {
            builder.itemInnerSpacing(itemInnerSpacing);
        }
        if (cellPadding != null) {
            builder.cellPadding(cellPadding);
        }
        if (touchExtraPadding != null) {
            builder.touchExtraPadding(touchExtraPadding);
        }
        if (indentSpacing != null) {
            builder.indentSpacing(indentSpacing);
        }
        if (columnsMinSpacing != null) {
            builder.columnsMinSpacing(columnsMinSpacing);
        }
        if (scrollbarSize != null) {
            builder.scrollbarSize(scrollbarSize);
        }
        if (scrollbarRounding != null) {
            builder.scrollbarRounding(scrollbarRounding);
        }
        if (grabMinSize != null) {
            builder.grabMinSize(grabMinSize);
        }
        if (grabRounding != null) {
            builder.grabRounding(grabRounding);
        }
        if (logSliderDeadzone != null) {
            builder.logSliderDeadzone(logSliderDeadzone);
        }
        if (tabRounding != null) {
            builder.tabRounding(tabRounding);
        }
        if (tabBorderSize != null) {
            builder.tabBorderSize(tabBorderSize);
        }
        if (tabMinWidthForCloseButton != null) {
            builder.tabMinWidthForCloseButton(tabMinWidthForCloseButton);
        }
        if (colorButtonPosition != null) {
            builder.colorButtonPosition(colorButtonPosition);
        }
        if (buttonTextAlign != null) {
            builder.buttonTextAlign(buttonTextAlign);
        }
        if (selectableTextAlign != null) {
            builder.selectableTextAlign(selectableTextAlign);
        }
        if (displayWindowPadding != null) {
            builder.displayWindowPadding(displayWindowPadding);
        }
        if (displaySafeAreaPadding != null) {
            builder.displaySafeAreaPadding(displaySafeAreaPadding);
        }
        if (mouseCursorScale != null) {
            builder.mouseCursorScale(mouseCursorScale);
        }
        if (antiAliasedLines != null) {
            builder.antiAliasedLines(antiAliasedLines);
        }
        if (antiAliasedLinesUseTex != null) {
            builder.antiAliasedLinesUseTex(antiAliasedLinesUseTex);
        }
        if (antiAliasedFill != null) {
            builder.antiAliasedFill(antiAliasedFill);
        }
        if (curveTessellationTol != null) {
            builder.curveTessellationTol(curveTessellationTol);
        }
        if (circleTessellationMaxError != null) {
            builder.circleTessellationMaxError(circleTessellationMaxError);
        }
        MGColorPalette palette = colorPalette != null ? base.getColorPalette().mergedWith(colorPalette) : base.getColorPalette();
        builder.colorPalette(palette);
        if (fontKey != null) {
            builder.fontKey(fontKey);
        } else {
            builder.fontKey(base.getFontKey());
        }
        if (fontSize != null) {
            builder.fontSize(fontSize);
        } else {
            builder.fontSize(base.getFontSize());
        }
        return builder.build();
    }

    public Float getAlpha() {
        return alpha;
    }

    public Float getDisabledAlpha() {
        return disabledAlpha;
    }

    public MGVec2 getWindowPadding() {
        return windowPadding;
    }

    public Float getWindowRounding() {
        return windowRounding;
    }

    public Float getWindowBorderSize() {
        return windowBorderSize;
    }

    public MGVec2 getWindowMinSize() {
        return windowMinSize;
    }

    public MGVec2 getWindowTitleAlign() {
        return windowTitleAlign;
    }

    public Integer getWindowMenuButtonPosition() {
        return windowMenuButtonPosition;
    }

    public Float getChildRounding() {
        return childRounding;
    }

    public Float getChildBorderSize() {
        return childBorderSize;
    }

    public Float getPopupRounding() {
        return popupRounding;
    }

    public Float getPopupBorderSize() {
        return popupBorderSize;
    }

    public MGVec2 getFramePadding() {
        return framePadding;
    }

    public Float getFrameRounding() {
        return frameRounding;
    }

    public Float getFrameBorderSize() {
        return frameBorderSize;
    }

    public MGVec2 getItemSpacing() {
        return itemSpacing;
    }

    public MGVec2 getItemInnerSpacing() {
        return itemInnerSpacing;
    }

    public MGVec2 getCellPadding() {
        return cellPadding;
    }

    public MGVec2 getTouchExtraPadding() {
        return touchExtraPadding;
    }

    public Float getIndentSpacing() {
        return indentSpacing;
    }

    public Float getColumnsMinSpacing() {
        return columnsMinSpacing;
    }

    public Float getScrollbarSize() {
        return scrollbarSize;
    }

    public Float getScrollbarRounding() {
        return scrollbarRounding;
    }

    public Float getGrabMinSize() {
        return grabMinSize;
    }

    public Float getGrabRounding() {
        return grabRounding;
    }

    public Float getLogSliderDeadzone() {
        return logSliderDeadzone;
    }

    public Float getTabRounding() {
        return tabRounding;
    }

    public Float getTabBorderSize() {
        return tabBorderSize;
    }

    public Float getTabMinWidthForCloseButton() {
        return tabMinWidthForCloseButton;
    }

    public Integer getColorButtonPosition() {
        return colorButtonPosition;
    }

    public MGVec2 getButtonTextAlign() {
        return buttonTextAlign;
    }

    public MGVec2 getSelectableTextAlign() {
        return selectableTextAlign;
    }

    public MGVec2 getDisplayWindowPadding() {
        return displayWindowPadding;
    }

    public MGVec2 getDisplaySafeAreaPadding() {
        return displaySafeAreaPadding;
    }

    public Float getMouseCursorScale() {
        return mouseCursorScale;
    }

    public Boolean getAntiAliasedLines() {
        return antiAliasedLines;
    }

    public Boolean getAntiAliasedLinesUseTex() {
        return antiAliasedLinesUseTex;
    }

    public Boolean getAntiAliasedFill() {
        return antiAliasedFill;
    }

    public Float getCurveTessellationTol() {
        return curveTessellationTol;
    }

    public Float getCircleTessellationMaxError() {
        return circleTessellationMaxError;
    }

    public MGColorPalette getColorPalette() {
        return colorPalette;
    }

    public Identifier getFontKey() {
        return fontKey;
    }

    public Float getFontSize() {
        return fontSize;
    }

    public static final class Builder {
        private Float alpha;
        private Float disabledAlpha;
        private MGVec2 windowPadding;
        private Float windowRounding;
        private Float windowBorderSize;
        private MGVec2 windowMinSize;
        private MGVec2 windowTitleAlign;
        private Integer windowMenuButtonPosition;
        private Float childRounding;
        private Float childBorderSize;
        private Float popupRounding;
        private Float popupBorderSize;
        private MGVec2 framePadding;
        private Float frameRounding;
        private Float frameBorderSize;
        private MGVec2 itemSpacing;
        private MGVec2 itemInnerSpacing;
        private MGVec2 cellPadding;
        private MGVec2 touchExtraPadding;
        private Float indentSpacing;
        private Float columnsMinSpacing;
        private Float scrollbarSize;
        private Float scrollbarRounding;
        private Float grabMinSize;
        private Float grabRounding;
        private Float logSliderDeadzone;
        private Float tabRounding;
        private Float tabBorderSize;
        private Float tabMinWidthForCloseButton;
        private Integer colorButtonPosition;
        private MGVec2 buttonTextAlign;
        private MGVec2 selectableTextAlign;
        private MGVec2 displayWindowPadding;
        private MGVec2 displaySafeAreaPadding;
        private Float mouseCursorScale;
        private Boolean antiAliasedLines;
        private Boolean antiAliasedLinesUseTex;
        private Boolean antiAliasedFill;
        private Float curveTessellationTol;
        private Float circleTessellationMaxError;
        private MGColorPalette colorPalette;
        private Identifier fontKey;
        private Float fontSize;

        public Builder alpha(float value) {
            this.alpha = value;
            return this;
        }

        public Builder disabledAlpha(float value) {
            this.disabledAlpha = value;
            return this;
        }

        public Builder windowPadding(float x, float y) {
            this.windowPadding = MGVec2.of(x, y);
            return this;
        }

        public Builder windowPadding(MGVec2 value) {
            this.windowPadding = Objects.requireNonNull(value, "windowPadding");
            return this;
        }

        public Builder windowRounding(float value) {
            this.windowRounding = value;
            return this;
        }

        public Builder windowBorderSize(float value) {
            this.windowBorderSize = value;
            return this;
        }

        public Builder windowMinSize(float x, float y) {
            this.windowMinSize = MGVec2.of(x, y);
            return this;
        }

        public Builder windowMinSize(MGVec2 value) {
            this.windowMinSize = Objects.requireNonNull(value, "windowMinSize");
            return this;
        }

        public Builder windowTitleAlign(float x, float y) {
            this.windowTitleAlign = MGVec2.of(x, y);
            return this;
        }

        public Builder windowTitleAlign(MGVec2 value) {
            this.windowTitleAlign = Objects.requireNonNull(value, "windowTitleAlign");
            return this;
        }

        public Builder windowMenuButtonPosition(int value) {
            this.windowMenuButtonPosition = value;
            return this;
        }

        public Builder childRounding(float value) {
            this.childRounding = value;
            return this;
        }

        public Builder childBorderSize(float value) {
            this.childBorderSize = value;
            return this;
        }

        public Builder popupRounding(float value) {
            this.popupRounding = value;
            return this;
        }

        public Builder popupBorderSize(float value) {
            this.popupBorderSize = value;
            return this;
        }

        public Builder framePadding(float x, float y) {
            this.framePadding = MGVec2.of(x, y);
            return this;
        }

        public Builder framePadding(MGVec2 value) {
            this.framePadding = Objects.requireNonNull(value, "framePadding");
            return this;
        }

        public Builder frameRounding(float value) {
            this.frameRounding = value;
            return this;
        }

        public Builder frameBorderSize(float value) {
            this.frameBorderSize = value;
            return this;
        }

        public Builder itemSpacing(float x, float y) {
            this.itemSpacing = MGVec2.of(x, y);
            return this;
        }

        public Builder itemSpacing(MGVec2 value) {
            this.itemSpacing = Objects.requireNonNull(value, "itemSpacing");
            return this;
        }

        public Builder itemInnerSpacing(float x, float y) {
            this.itemInnerSpacing = MGVec2.of(x, y);
            return this;
        }

        public Builder itemInnerSpacing(MGVec2 value) {
            this.itemInnerSpacing = Objects.requireNonNull(value, "itemInnerSpacing");
            return this;
        }

        public Builder cellPadding(float x, float y) {
            this.cellPadding = MGVec2.of(x, y);
            return this;
        }

        public Builder cellPadding(MGVec2 value) {
            this.cellPadding = Objects.requireNonNull(value, "cellPadding");
            return this;
        }

        public Builder touchExtraPadding(float x, float y) {
            this.touchExtraPadding = MGVec2.of(x, y);
            return this;
        }

        public Builder touchExtraPadding(MGVec2 value) {
            this.touchExtraPadding = Objects.requireNonNull(value, "touchExtraPadding");
            return this;
        }

        public Builder indentSpacing(float value) {
            this.indentSpacing = value;
            return this;
        }

        public Builder columnsMinSpacing(float value) {
            this.columnsMinSpacing = value;
            return this;
        }

        public Builder scrollbarSize(float value) {
            this.scrollbarSize = value;
            return this;
        }

        public Builder scrollbarRounding(float value) {
            this.scrollbarRounding = value;
            return this;
        }

        public Builder grabMinSize(float value) {
            this.grabMinSize = value;
            return this;
        }

        public Builder grabRounding(float value) {
            this.grabRounding = value;
            return this;
        }

        public Builder logSliderDeadzone(float value) {
            this.logSliderDeadzone = value;
            return this;
        }

        public Builder tabRounding(float value) {
            this.tabRounding = value;
            return this;
        }

        public Builder tabBorderSize(float value) {
            this.tabBorderSize = value;
            return this;
        }

        public Builder tabMinWidthForCloseButton(float value) {
            this.tabMinWidthForCloseButton = value;
            return this;
        }

        public Builder colorButtonPosition(int value) {
            this.colorButtonPosition = value;
            return this;
        }

        public Builder buttonTextAlign(float x, float y) {
            this.buttonTextAlign = MGVec2.of(x, y);
            return this;
        }

        public Builder buttonTextAlign(MGVec2 value) {
            this.buttonTextAlign = Objects.requireNonNull(value, "buttonTextAlign");
            return this;
        }

        public Builder selectableTextAlign(float x, float y) {
            this.selectableTextAlign = MGVec2.of(x, y);
            return this;
        }

        public Builder selectableTextAlign(MGVec2 value) {
            this.selectableTextAlign = Objects.requireNonNull(value, "selectableTextAlign");
            return this;
        }

        public Builder displayWindowPadding(float x, float y) {
            this.displayWindowPadding = MGVec2.of(x, y);
            return this;
        }

        public Builder displayWindowPadding(MGVec2 value) {
            this.displayWindowPadding = Objects.requireNonNull(value, "displayWindowPadding");
            return this;
        }

        public Builder displaySafeAreaPadding(float x, float y) {
            this.displaySafeAreaPadding = MGVec2.of(x, y);
            return this;
        }

        public Builder displaySafeAreaPadding(MGVec2 value) {
            this.displaySafeAreaPadding = Objects.requireNonNull(value, "displaySafeAreaPadding");
            return this;
        }

        public Builder mouseCursorScale(float value) {
            this.mouseCursorScale = value;
            return this;
        }

        public Builder antiAliasedLines(boolean value) {
            this.antiAliasedLines = value;
            return this;
        }

        public Builder antiAliasedLinesUseTex(boolean value) {
            this.antiAliasedLinesUseTex = value;
            return this;
        }

        public Builder antiAliasedFill(boolean value) {
            this.antiAliasedFill = value;
            return this;
        }

        public Builder curveTessellationTol(float value) {
            this.curveTessellationTol = value;
            return this;
        }

        public Builder circleTessellationMaxError(float value) {
            this.circleTessellationMaxError = value;
            return this;
        }

        public Builder colorPalette(MGColorPalette value) {
            this.colorPalette = value;
            return this;
        }

        public Builder fontKey(Identifier value) {
            this.fontKey = value;
            return this;
        }

        public Builder fontSize(Float value) {
            this.fontSize = value;
            return this;
        }

        public MGStyleDelta build() {
            MGStyleDelta delta = new MGStyleDelta(this);
            StyleValidation.validateDelta(delta);
            return delta;
        }
    }
}
