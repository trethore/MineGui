package tytoo.minegui.style;

import imgui.ImGuiStyle;
import lombok.Getter;
import net.minecraft.util.Identifier;

import java.util.Objects;

@Getter
public final class MGStyleDescriptor {
    private final float alpha;
    private final float disabledAlpha;
    private final MGVec2 windowPadding;
    private final float windowRounding;
    private final float windowBorderSize;
    private final MGVec2 windowMinSize;
    private final MGVec2 windowTitleAlign;
    private final int windowMenuButtonPosition;
    private final float childRounding;
    private final float childBorderSize;
    private final float popupRounding;
    private final float popupBorderSize;
    private final MGVec2 framePadding;
    private final float frameRounding;
    private final float frameBorderSize;
    private final MGVec2 itemSpacing;
    private final MGVec2 itemInnerSpacing;
    private final MGVec2 cellPadding;
    private final MGVec2 touchExtraPadding;
    private final float indentSpacing;
    private final float columnsMinSpacing;
    private final float scrollbarSize;
    private final float scrollbarRounding;
    private final float grabMinSize;
    private final float grabRounding;
    private final float logSliderDeadzone;
    private final float tabRounding;
    private final float tabBorderSize;
    private final float tabMinWidthForCloseButton;
    private final int colorButtonPosition;
    private final MGVec2 buttonTextAlign;
    private final MGVec2 selectableTextAlign;
    private final MGVec2 displayWindowPadding;
    private final MGVec2 displaySafeAreaPadding;
    private final float mouseCursorScale;
    private final boolean antiAliasedLines;
    private final boolean antiAliasedLinesUseTex;
    private final boolean antiAliasedFill;
    private final float curveTessellationTol;
    private final float circleTessellationMaxError;
    private final MGColorPalette colorPalette;
    private final Identifier fontKey;
    private final Float fontSize;

    private MGStyleDescriptor(Builder builder) {
        this.alpha = builder.alpha;
        this.disabledAlpha = builder.disabledAlpha;
        this.windowPadding = Objects.requireNonNull(builder.windowPadding, "windowPadding");
        this.windowRounding = builder.windowRounding;
        this.windowBorderSize = builder.windowBorderSize;
        this.windowMinSize = Objects.requireNonNull(builder.windowMinSize, "windowMinSize");
        this.windowTitleAlign = Objects.requireNonNull(builder.windowTitleAlign, "windowTitleAlign");
        this.windowMenuButtonPosition = builder.windowMenuButtonPosition;
        this.childRounding = builder.childRounding;
        this.childBorderSize = builder.childBorderSize;
        this.popupRounding = builder.popupRounding;
        this.popupBorderSize = builder.popupBorderSize;
        this.framePadding = Objects.requireNonNull(builder.framePadding, "framePadding");
        this.frameRounding = builder.frameRounding;
        this.frameBorderSize = builder.frameBorderSize;
        this.itemSpacing = Objects.requireNonNull(builder.itemSpacing, "itemSpacing");
        this.itemInnerSpacing = Objects.requireNonNull(builder.itemInnerSpacing, "itemInnerSpacing");
        this.cellPadding = Objects.requireNonNull(builder.cellPadding, "cellPadding");
        this.touchExtraPadding = Objects.requireNonNull(builder.touchExtraPadding, "touchExtraPadding");
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
        this.buttonTextAlign = Objects.requireNonNull(builder.buttonTextAlign, "buttonTextAlign");
        this.selectableTextAlign = Objects.requireNonNull(builder.selectableTextAlign, "selectableTextAlign");
        this.displayWindowPadding = Objects.requireNonNull(builder.displayWindowPadding, "displayWindowPadding");
        this.displaySafeAreaPadding = Objects.requireNonNull(builder.displaySafeAreaPadding, "displaySafeAreaPadding");
        this.mouseCursorScale = builder.mouseCursorScale;
        this.antiAliasedLines = builder.antiAliasedLines;
        this.antiAliasedLinesUseTex = builder.antiAliasedLinesUseTex;
        this.antiAliasedFill = builder.antiAliasedFill;
        this.curveTessellationTol = builder.curveTessellationTol;
        this.circleTessellationMaxError = builder.circleTessellationMaxError;
        this.colorPalette = builder.colorPalette != null ? builder.colorPalette : MGColorPalette.empty();
        this.fontKey = builder.fontKey;
        this.fontSize = builder.fontSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MGStyleDescriptor capture(ImGuiStyle style) {
        return capture(style, MGColorPalette.fromStyle(style), null, null);
    }

    public static MGStyleDescriptor capture(ImGuiStyle style, MGColorPalette palette, Identifier fontKey, Float fontSize) {
        return builder()
                .fromStyle(style)
                .colorPalette(palette)
                .fontKey(fontKey)
                .fontSize(fontSize)
                .build();
    }

    public void applyTo(ImGuiStyle style) {
        if (style == null) {
            return;
        }
        style.setAlpha(alpha);
        style.setDisabledAlpha(disabledAlpha);
        style.setWindowPadding(windowPadding.x(), windowPadding.y());
        style.setWindowRounding(windowRounding);
        style.setWindowBorderSize(windowBorderSize);
        style.setWindowMinSize(windowMinSize.x(), windowMinSize.y());
        style.setWindowTitleAlign(windowTitleAlign.x(), windowTitleAlign.y());
        style.setWindowMenuButtonPosition(windowMenuButtonPosition);
        style.setChildRounding(childRounding);
        style.setChildBorderSize(childBorderSize);
        style.setPopupRounding(popupRounding);
        style.setPopupBorderSize(popupBorderSize);
        style.setFramePadding(framePadding.x(), framePadding.y());
        style.setFrameRounding(frameRounding);
        style.setFrameBorderSize(frameBorderSize);
        style.setItemSpacing(itemSpacing.x(), itemSpacing.y());
        style.setItemInnerSpacing(itemInnerSpacing.x(), itemInnerSpacing.y());
        style.setCellPadding(cellPadding.x(), cellPadding.y());
        style.setTouchExtraPadding(touchExtraPadding.x(), touchExtraPadding.y());
        style.setIndentSpacing(indentSpacing);
        style.setColumnsMinSpacing(columnsMinSpacing);
        style.setScrollbarSize(scrollbarSize);
        style.setScrollbarRounding(scrollbarRounding);
        style.setGrabMinSize(grabMinSize);
        style.setGrabRounding(grabRounding);
        style.setLogSliderDeadzone(logSliderDeadzone);
        style.setTabRounding(tabRounding);
        style.setTabBorderSize(tabBorderSize);
        style.setTabMinWidthForCloseButton(tabMinWidthForCloseButton);
        style.setColorButtonPosition(colorButtonPosition);
        style.setButtonTextAlign(buttonTextAlign.x(), buttonTextAlign.y());
        style.setSelectableTextAlign(selectableTextAlign.x(), selectableTextAlign.y());
        style.setDisplayWindowPadding(displayWindowPadding.x(), displayWindowPadding.y());
        style.setDisplaySafeAreaPadding(displaySafeAreaPadding.x(), displaySafeAreaPadding.y());
        style.setMouseCursorScale(mouseCursorScale);
        style.setAntiAliasedLines(antiAliasedLines);
        style.setAntiAliasedLinesUseTex(antiAliasedLinesUseTex);
        style.setAntiAliasedFill(antiAliasedFill);
        style.setCurveTessellationTol(curveTessellationTol);
        style.setCircleTessellationMaxError(circleTessellationMaxError);
        colorPalette.applyTo(style);
    }

    public MGStyleDescriptor withDelta(MGStyleDelta delta) {
        if (delta == null) {
            return this;
        }
        return delta.resolve(this);
    }

    public static final class Builder {
        private float alpha;
        private float disabledAlpha;
        private MGVec2 windowPadding = MGVec2.of(0.0f, 0.0f);
        private float windowRounding;
        private float windowBorderSize;
        private MGVec2 windowMinSize = MGVec2.of(0.0f, 0.0f);
        private MGVec2 windowTitleAlign = MGVec2.of(0.5f, 0.5f);
        private int windowMenuButtonPosition;
        private float childRounding;
        private float childBorderSize;
        private float popupRounding;
        private float popupBorderSize;
        private MGVec2 framePadding = MGVec2.of(0.0f, 0.0f);
        private float frameRounding;
        private float frameBorderSize;
        private MGVec2 itemSpacing = MGVec2.of(0.0f, 0.0f);
        private MGVec2 itemInnerSpacing = MGVec2.of(0.0f, 0.0f);
        private MGVec2 cellPadding = MGVec2.of(0.0f, 0.0f);
        private MGVec2 touchExtraPadding = MGVec2.of(0.0f, 0.0f);
        private float indentSpacing;
        private float columnsMinSpacing;
        private float scrollbarSize;
        private float scrollbarRounding;
        private float grabMinSize;
        private float grabRounding;
        private float logSliderDeadzone;
        private float tabRounding;
        private float tabBorderSize;
        private float tabMinWidthForCloseButton;
        private int colorButtonPosition;
        private MGVec2 buttonTextAlign = MGVec2.of(0.5f, 0.5f);
        private MGVec2 selectableTextAlign = MGVec2.of(0.0f, 0.0f);
        private MGVec2 displayWindowPadding = MGVec2.of(0.0f, 0.0f);
        private MGVec2 displaySafeAreaPadding = MGVec2.of(0.0f, 0.0f);
        private float mouseCursorScale = 1.0f;
        private boolean antiAliasedLines = true;
        private boolean antiAliasedLinesUseTex = true;
        private boolean antiAliasedFill = true;
        private float curveTessellationTol = 1.25f;
        private float circleTessellationMaxError = 0.3f;
        private MGColorPalette colorPalette = MGColorPalette.empty();
        private Identifier fontKey;
        private Float fontSize;

        public Builder fromStyle(ImGuiStyle style) {
            if (style == null) {
                return this;
            }
            alpha(style.getAlpha());
            disabledAlpha(style.getDisabledAlpha());
            windowPadding(style.getWindowPaddingX(), style.getWindowPaddingY());
            windowRounding(style.getWindowRounding());
            windowBorderSize(style.getWindowBorderSize());
            windowMinSize(style.getWindowMinSizeX(), style.getWindowMinSizeY());
            windowTitleAlign(style.getWindowTitleAlignX(), style.getWindowTitleAlignY());
            windowMenuButtonPosition(style.getWindowMenuButtonPosition());
            childRounding(style.getChildRounding());
            childBorderSize(style.getChildBorderSize());
            popupRounding(style.getPopupRounding());
            popupBorderSize(style.getPopupBorderSize());
            framePadding(style.getFramePaddingX(), style.getFramePaddingY());
            frameRounding(style.getFrameRounding());
            frameBorderSize(style.getFrameBorderSize());
            itemSpacing(style.getItemSpacingX(), style.getItemSpacingY());
            itemInnerSpacing(style.getItemInnerSpacingX(), style.getItemInnerSpacingY());
            cellPadding(style.getCellPaddingX(), style.getCellPaddingY());
            touchExtraPadding(style.getTouchExtraPaddingX(), style.getTouchExtraPaddingY());
            indentSpacing(style.getIndentSpacing());
            columnsMinSpacing(style.getColumnsMinSpacing());
            scrollbarSize(style.getScrollbarSize());
            scrollbarRounding(style.getScrollbarRounding());
            grabMinSize(style.getGrabMinSize());
            grabRounding(style.getGrabRounding());
            logSliderDeadzone(style.getLogSliderDeadzone());
            tabRounding(style.getTabRounding());
            tabBorderSize(style.getTabBorderSize());
            tabMinWidthForCloseButton(style.getTabMinWidthForCloseButton());
            colorButtonPosition(style.getColorButtonPosition());
            buttonTextAlign(style.getButtonTextAlignX(), style.getButtonTextAlignY());
            selectableTextAlign(style.getSelectableTextAlignX(), style.getSelectableTextAlignY());
            displayWindowPadding(style.getDisplayWindowPaddingX(), style.getDisplayWindowPaddingY());
            displaySafeAreaPadding(style.getDisplaySafeAreaPaddingX(), style.getDisplaySafeAreaPaddingY());
            mouseCursorScale(style.getMouseCursorScale());
            antiAliasedLines(style.getAntiAliasedLines());
            antiAliasedLinesUseTex(style.getAntiAliasedLinesUseTex());
            antiAliasedFill(style.getAntiAliasedFill());
            curveTessellationTol(style.getCurveTessellationTol());
            circleTessellationMaxError(style.getCircleTessellationMaxError());
            return this;
        }

        public Builder fromDescriptor(MGStyleDescriptor descriptor) {
            if (descriptor == null) {
                return this;
            }
            alpha(descriptor.getAlpha());
            disabledAlpha(descriptor.getDisabledAlpha());
            windowPadding(descriptor.getWindowPadding());
            windowRounding(descriptor.getWindowRounding());
            windowBorderSize(descriptor.getWindowBorderSize());
            windowMinSize(descriptor.getWindowMinSize());
            windowTitleAlign(descriptor.getWindowTitleAlign());
            windowMenuButtonPosition(descriptor.getWindowMenuButtonPosition());
            childRounding(descriptor.getChildRounding());
            childBorderSize(descriptor.getChildBorderSize());
            popupRounding(descriptor.getPopupRounding());
            popupBorderSize(descriptor.getPopupBorderSize());
            framePadding(descriptor.getFramePadding());
            frameRounding(descriptor.getFrameRounding());
            frameBorderSize(descriptor.getFrameBorderSize());
            itemSpacing(descriptor.getItemSpacing());
            itemInnerSpacing(descriptor.getItemInnerSpacing());
            cellPadding(descriptor.getCellPadding());
            touchExtraPadding(descriptor.getTouchExtraPadding());
            indentSpacing(descriptor.getIndentSpacing());
            columnsMinSpacing(descriptor.getColumnsMinSpacing());
            scrollbarSize(descriptor.getScrollbarSize());
            scrollbarRounding(descriptor.getScrollbarRounding());
            grabMinSize(descriptor.getGrabMinSize());
            grabRounding(descriptor.getGrabRounding());
            logSliderDeadzone(descriptor.getLogSliderDeadzone());
            tabRounding(descriptor.getTabRounding());
            tabBorderSize(descriptor.getTabBorderSize());
            tabMinWidthForCloseButton(descriptor.getTabMinWidthForCloseButton());
            colorButtonPosition(descriptor.getColorButtonPosition());
            buttonTextAlign(descriptor.getButtonTextAlign());
            selectableTextAlign(descriptor.getSelectableTextAlign());
            displayWindowPadding(descriptor.getDisplayWindowPadding());
            displaySafeAreaPadding(descriptor.getDisplaySafeAreaPadding());
            mouseCursorScale(descriptor.getMouseCursorScale());
            antiAliasedLines(descriptor.isAntiAliasedLines());
            antiAliasedLinesUseTex(descriptor.isAntiAliasedLinesUseTex());
            antiAliasedFill(descriptor.isAntiAliasedFill());
            curveTessellationTol(descriptor.getCurveTessellationTol());
            circleTessellationMaxError(descriptor.getCircleTessellationMaxError());
            colorPalette(descriptor.getColorPalette());
            fontKey(descriptor.getFontKey());
            fontSize(descriptor.getFontSize());
            return this;
        }

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
            this.colorPalette = value != null ? value : MGColorPalette.empty();
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

        public MGStyleDescriptor build() {
            MGStyleDescriptor descriptor = new MGStyleDescriptor(this);
            StyleValidation.validateDescriptor(descriptor);
            return descriptor;
        }
    }
}
