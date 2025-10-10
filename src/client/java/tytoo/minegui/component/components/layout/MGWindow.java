package tytoo.minegui.component.components.layout;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;
import tytoo.minegui.component.MGComponent;
import tytoo.minegui.component.behavior.FocusMode;
import tytoo.minegui.component.behavior.VisibilityMode;
import tytoo.minegui.manager.UIManager;
import tytoo.minegui.utils.McUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public abstract class MGWindow extends MGComponent<MGWindow> {
    private final ImBoolean isFocused;
    private final ImBoolean visible;
    private final List<MGWindow> subWindows = new ArrayList<>();
    private final EnumSet<MGWindowOption> windowOptions = EnumSet.noneOf(MGWindowOption.class);
    @Nullable
    protected MGWindow parentWindow;
    private boolean shouldFocus = false;
    private String title;
    private Integer initX = 100;
    private Integer initY = 100;
    private Integer initWidth = 400;
    private Integer initHeight = 300;
    private boolean boundsInitialized = false;
    private int currentX = 100;
    private int currentY = 100;
    private int currentWidth = 400;
    private int currentHeight = 300;
    private VisibilityMode visibilityMode = VisibilityMode.FOLLOW_PARENT;
    private FocusMode focusMode = FocusMode.FOLLOW_PARENT;

    protected MGWindow(String title) {
        this(title, true);
    }

    protected MGWindow(String title, boolean autoRegister) {
        this.title = title;
        this.visible = new ImBoolean(true);
        this.isFocused = new ImBoolean(false);
        if (autoRegister && isTopLevel()) {
            UIManager.getInstance().autoRegister(this);
        }
    }

    public static <W extends MGWindow> W create(Supplier<W> factory) {
        W window = factory.get();
        window.initialize();
        return window;
    }

    protected void initialize() {
        build();
    }

    protected void build() {

    }

    @Override
    public void render() {
        if (!isVisible()) {
            return;
        }

        if (shouldFocus && !isFocused()) {
            ImGui.setNextWindowFocus();
            shouldFocus = false;
        }

        int flags = 0;
        boolean topLevel = getParent() == null;
        boolean disablesCloseButton = false;
        boolean allowChildMove = false;
        boolean allowChildResize = false;
        for (MGWindowOption option : windowOptions) {
            Integer imGuiFlag = option.getImGuiFlag();
            if (imGuiFlag != null) {
                flags |= imGuiFlag;
            }
            if (option.disablesCloseButton()) {
                disablesCloseButton = true;
            }
            if (option.allowsChildMove()) {
                allowChildMove = true;
            }
            if (option.allowsChildResize()) {
                allowChildResize = true;
            }
        }
        if (!topLevel) {
            if (!allowChildMove) {
                flags |= ImGuiWindowFlags.NoMove;
            }
            if (!allowChildResize) {
                flags |= ImGuiWindowFlags.NoResize;
            }
        }

        if (topLevel && !boundsInitialized && initX != null && initY != null && initWidth != null && initHeight != null) {
            ImGui.setNextWindowPos(initX, initY);
            ImGui.setNextWindowSize(initWidth, initHeight);
            boundsInitialized = true;
        }

        boolean opened = disablesCloseButton ? ImGui.begin(title, flags) : ImGui.begin(title, visible, flags);
        isFocused.set(ImGui.isWindowFocused());
        if (opened) {
            super.render();
        }
        float px = ImGui.getWindowPosX();
        float py = ImGui.getWindowPosY();
        float pw = ImGui.getWindowWidth();
        float ph = ImGui.getWindowHeight();
        currentX = Math.round(px);
        currentY = Math.round(py);
        currentWidth = Math.round(pw);
        currentHeight = Math.max(1, Math.round(ph));
        ImGui.end();

        for (MGWindow subWindow : subWindows) {
            subWindow.render();
        }
    }


    public void open() {
        open(false);
    }

    public void openAndUnLockCursor() {
        open(true);
    }

    public void close() {
        close(false);
    }

    public void closeAndLockCursor() {
        close(true);
    }

    private void open(boolean unlockCursor) {
        if (unlockCursor) {
            McUtils.getMc().ifPresent(mc -> mc.mouse.unlockCursor());
        }
        shouldFocus = true;
        setVisible(true);
    }

    private void close(boolean lockCursor) {
        setVisible(false);
        if (lockCursor) {
            McUtils.getMc().ifPresent(mc -> mc.mouse.lockCursor());
        }
    }

    public void toggleDependingOnScreen() {
        McUtils.getMc().ifPresent(mc -> {
            if (mc.currentScreen == null) {
                toggleWithCursorLock();
            } else {
                toggle();
            }
        });
    }


    public void toggleWithCursorLock() {
        if (isVisible()) {
            closeAndLockCursor();
        } else {
            openAndUnLockCursor();
        }
    }

    public void toggle() {
        if (isVisible()) {
            close();
        } else {
            open();
        }
    }

    public String getTitle() {
        return title;
    }

    public String setTitle(String title) {
        return this.title = title;
    }

    public boolean isVisible() {
        return this.visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
        if (!visible) {
            this.isFocused.set(false);
            this.shouldFocus = false;
        }
        propagateVisibilityToChildren(visible);
    }

    private void propagateVisibilityToChildren(boolean parentVisible) {
        for (MGWindow subWindow : subWindows) {
            switch (subWindow.visibilityMode) {
                case FOLLOW_PARENT -> subWindow.setVisible(parentVisible);
                case INVERSE -> subWindow.setVisible(!parentVisible);
                case INDEPENDENT -> {
                }
            }
        }
    }

    public boolean isFocused() {
        return isFocused.get();
    }

    public void setFocused(boolean focused) {
        this.shouldFocus = focused;
    }

    public void setInitialBounds(int x, int y, int width, int height) {
        this.initX = x;
        this.initY = y;
        this.initWidth = width;
        this.initHeight = height;
        this.boundsInitialized = false;
    }

    public int getX() {
        return currentX;
    }

    public int getY() {
        return currentY;
    }

    public int getWidth() {
        return currentWidth;
    }

    public int getHeight() {
        return currentHeight;
    }

    public boolean isTopLevel() {
        return getParent() == null && parentWindow == null;
    }

    public <W extends MGWindow> W addSubWindow(W subWindow) {
        subWindows.add(subWindow);
        subWindow.parentWindow = this;
        return subWindow;
    }

    public MGWindow removeSubWindow(MGWindow subWindow) {
        subWindows.remove(subWindow);
        if (subWindow.parentWindow == this) {
            subWindow.parentWindow = null;
        }
        return this;
    }

    public List<MGWindow> getSubWindows() {
        return new ArrayList<>(subWindows);
    }

    @Nullable
    public MGWindow getParentWindow() {
        return parentWindow;
    }

    public MGWindow visibilityMode(VisibilityMode mode) {
        this.visibilityMode = mode;
        return this;
    }

    public VisibilityMode getVisibilityMode() {
        return visibilityMode;
    }

    public MGWindow focusMode(FocusMode mode) {
        this.focusMode = mode;
        return this;
    }

    public FocusMode getFocusMode() {
        return focusMode;
    }

    public MGWindow initialBounds(int x, int y, int width, int height) {
        setInitialBounds(x, y, width, height);
        return this;
    }

    public MGWindow windowOptions(MGWindowOption... options) {
        windowOptions.clear();
        return addWindowOptions(options);
    }

    public MGWindow windowOptions(Set<MGWindowOption> options) {
        windowOptions.clear();
        if (options != null) {
            windowOptions.addAll(options);
        }
        return this;
    }

    public MGWindow addWindowOption(MGWindowOption option) {
        if (option != null) {
            windowOptions.add(option);
        }
        return this;
    }

    public MGWindow addWindowOptions(MGWindowOption... options) {
        if (options != null) {
            for (MGWindowOption option : options) {
                if (option != null) {
                    windowOptions.add(option);
                }
            }
        }
        return this;
    }

    public MGWindow removeWindowOption(MGWindowOption option) {
        if (option != null) {
            windowOptions.remove(option);
        }
        return this;
    }

    public MGWindow clearWindowOptions() {
        windowOptions.clear();
        return this;
    }

    public EnumSet<MGWindowOption> getWindowOptions() {
        return windowOptions.isEmpty() ? EnumSet.noneOf(MGWindowOption.class) : EnumSet.copyOf(windowOptions);
    }
}
