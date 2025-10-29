package tytoo.minegui.imgui.dock;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.*;

public final class DockspaceRenderState {
    private static final String DEFAULT_WINDOW_TITLE = "Dockspace Host";
    private static final String DEFAULT_DOCKSPACE_ID = "MineGuiDockspace";
    private static final int DEFAULT_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
            ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoNavInputs |
            ImGuiWindowFlags.NoDocking;
    private static final int DEFAULT_DOCKSPACE_FLAGS = ImGuiDockNodeFlags.PassthruCentralNode | ImGuiDockNodeFlags.NoDockingInCentralNode;
    private final List<Runnable> beforeWindowTasks = new ArrayList<>();
    private final List<Runnable> beforeDockspaceTasks = new ArrayList<>();
    private final List<Runnable> afterDockspaceTasks = new ArrayList<>();
    private String windowTitle = DEFAULT_WINDOW_TITLE;
    private String dockspaceId = DEFAULT_DOCKSPACE_ID;
    private int windowFlags = DEFAULT_WINDOW_FLAGS;
    private int dockspaceFlags = DEFAULT_DOCKSPACE_FLAGS;
    private boolean overrideWindowPadding = true;
    private float windowPaddingX = 0.0f;
    private float windowPaddingY = 0.0f;
    private boolean overrideWindowBorderSize = true;
    private float windowBorderSize = 0.0f;
    private boolean applyWindowPos = true;
    private boolean applyWindowSize = true;
    private float windowPosX;
    private float windowPosY;
    private int windowPosCondition = ImGuiCond.None;
    private float windowWidth;
    private float windowHeight;
    private int windowSizeCondition = ImGuiCond.None;
    private float defaultWindowPosX;
    private float defaultWindowPosY;
    private float defaultWindowWidth;
    private float defaultWindowHeight;
    private float dockspaceWidth = 0.0f;
    private float dockspaceHeight = 0.0f;
    private boolean dockspaceEnabled = true;

    private DockspaceRenderState() {
    }

    public static DockspaceRenderState createDefault(int windowX, int windowY, int windowWidth, int windowHeight) {
        DockspaceRenderState state = new DockspaceRenderState();
        state.windowPosX = windowX;
        state.windowPosY = windowY;
        state.windowWidth = Math.max(windowWidth, 0);
        state.windowHeight = Math.max(windowHeight, 0);
        state.defaultWindowPosX = state.windowPosX;
        state.defaultWindowPosY = state.windowPosY;
        state.defaultWindowWidth = state.windowWidth;
        state.defaultWindowHeight = state.windowHeight;
        return state;
    }

    private static void addTask(List<Runnable> tasks, Runnable task) {
        Objects.requireNonNull(task, "task");
        tasks.add(task);
    }

    private static float sanitizeFinite(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        return value;
    }

    private static float sanitizeDimension(float value) {
        if (!Float.isFinite(value) || value < 0.0f) {
            return 0.0f;
        }
        return value;
    }

    private static float sanitizeNonNegative(float value) {
        if (!Float.isFinite(value) || value < 0.0f) {
            return 0.0f;
        }
        return value;
    }

    public String windowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String title) {
        if (title == null || title.isBlank()) {
            windowTitle = DEFAULT_WINDOW_TITLE;
            return;
        }
        windowTitle = title;
    }

    public String dockspaceId() {
        return dockspaceId;
    }

    public void setDockspaceId(String id) {
        if (id == null || id.isBlank()) {
            dockspaceId = DEFAULT_DOCKSPACE_ID;
            return;
        }
        dockspaceId = id;
    }

    public int windowFlags() {
        return windowFlags;
    }

    public void setWindowFlags(int flags) {
        windowFlags = flags;
    }

    public void addWindowFlags(int flags) {
        windowFlags |= flags;
    }

    public void removeWindowFlags(int flags) {
        windowFlags &= ~flags;
    }

    public int dockspaceFlags() {
        return dockspaceFlags;
    }

    public void setDockspaceFlags(int flags) {
        dockspaceFlags = flags;
    }

    public void addDockspaceFlags(int flags) {
        dockspaceFlags |= flags;
    }

    public void removeDockspaceFlags(int flags) {
        dockspaceFlags &= ~flags;
    }

    public boolean overridesWindowPadding() {
        return overrideWindowPadding;
    }

    public void setWindowPadding(float x, float y) {
        if (!Float.isFinite(x) || x < 0.0f || !Float.isFinite(y) || y < 0.0f) {
            return;
        }
        overrideWindowPadding = true;
        windowPaddingX = x;
        windowPaddingY = y;
    }

    public void disableWindowPaddingOverride() {
        overrideWindowPadding = false;
    }

    public float windowPaddingX() {
        return windowPaddingX;
    }

    public float windowPaddingY() {
        return windowPaddingY;
    }

    public boolean overridesWindowBorderSize() {
        return overrideWindowBorderSize;
    }

    public void setWindowBorderSize(float borderSize) {
        if (!Float.isFinite(borderSize) || borderSize < 0.0f) {
            return;
        }
        overrideWindowBorderSize = true;
        windowBorderSize = borderSize;
    }

    public void disableWindowBorderOverride() {
        overrideWindowBorderSize = false;
    }

    public float windowBorderSize() {
        return windowBorderSize;
    }

    public boolean isWindowPosApplied() {
        return applyWindowPos;
    }

    public void setWindowPos(float x, float y) {
        setWindowPos(x, y, ImGuiCond.None);
    }

    public void setWindowPos(float x, float y, int condition) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }
        applyWindowPos = true;
        windowPosX = x;
        windowPosY = y;
        windowPosCondition = condition;
    }

    public void disableWindowPos() {
        applyWindowPos = false;
    }

    public float windowPosX() {
        return windowPosX;
    }

    public float windowPosY() {
        return windowPosY;
    }

    public float defaultWindowPosX() {
        return defaultWindowPosX;
    }

    public float defaultWindowPosY() {
        return defaultWindowPosY;
    }

    public boolean isWindowSizeApplied() {
        return applyWindowSize;
    }

    public void setWindowSize(float width, float height) {
        setWindowSize(width, height, ImGuiCond.None);
    }

    public void setWindowSize(float width, float height, int condition) {
        if (!Float.isFinite(width) || width < 0.0f || !Float.isFinite(height) || height < 0.0f) {
            return;
        }
        applyWindowSize = true;
        windowWidth = width;
        windowHeight = height;
        windowSizeCondition = condition;
    }

    public void disableWindowSize() {
        applyWindowSize = false;
    }

    public float windowWidth() {
        return windowWidth;
    }

    public float windowHeight() {
        return windowHeight;
    }

    public float defaultWindowWidth() {
        return defaultWindowWidth;
    }

    public float defaultWindowHeight() {
        return defaultWindowHeight;
    }

    public void setWindowRect(float x, float y, float width, float height) {
        setWindowPos(x, y);
        setWindowSize(width, height);
    }

    public void setWindowRect(float x, float y, float width, float height, int posCondition, int sizeCondition) {
        setWindowPos(x, y, posCondition);
        setWindowSize(width, height, sizeCondition);
    }

    public void useDefaultWindowRect() {
        applyWindowPos = true;
        applyWindowSize = true;
        windowPosX = defaultWindowPosX;
        windowPosY = defaultWindowPosY;
        windowWidth = defaultWindowWidth;
        windowHeight = defaultWindowHeight;
        windowPosCondition = ImGuiCond.None;
        windowSizeCondition = ImGuiCond.None;
    }

    public float dockspaceWidth() {
        return dockspaceWidth;
    }

    public float dockspaceHeight() {
        return dockspaceHeight;
    }

    public void setDockspaceSize(float width, float height) {
        if (Float.isFinite(width) && width >= 0.0f) {
            dockspaceWidth = width;
        }
        if (Float.isFinite(height) && height >= 0.0f) {
            dockspaceHeight = height;
        }
    }

    public boolean isDockspaceEnabled() {
        return dockspaceEnabled;
    }

    public void setDockspaceEnabled(boolean enabled) {
        dockspaceEnabled = enabled;
    }

    public void disableDockspace() {
        dockspaceEnabled = false;
    }

    public void beforeWindow(Runnable task) {
        addTask(beforeWindowTasks, task);
    }

    public void beforeDockspace(Runnable task) {
        addTask(beforeDockspaceTasks, task);
    }

    public void afterDockspace(Runnable task) {
        addTask(afterDockspaceTasks, task);
    }

    public Collection<Runnable> beforeWindowTasks() {
        return Collections.unmodifiableList(beforeWindowTasks);
    }

    public Collection<Runnable> beforeDockspaceTasks() {
        return Collections.unmodifiableList(beforeDockspaceTasks);
    }

    public Collection<Runnable> afterDockspaceTasks() {
        return Collections.unmodifiableList(afterDockspaceTasks);
    }

    public void applyPlacement() {
        if (applyWindowPos) {
            ImGui.setNextWindowPos(windowPosX, windowPosY, windowPosCondition);
        }
        if (applyWindowSize) {
            ImGui.setNextWindowSize(windowWidth, windowHeight, windowSizeCondition);
        }
    }

    public int applyStyleOverrides() {
        int pushes = 0;
        if (overrideWindowPadding) {
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, windowPaddingX, windowPaddingY);
            pushes++;
        }
        if (overrideWindowBorderSize) {
            ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, windowBorderSize);
            pushes++;
        }
        return pushes;
    }

    public void normalize() {
        if (windowTitle == null || windowTitle.isBlank()) {
            windowTitle = DEFAULT_WINDOW_TITLE;
        }
        if (dockspaceId == null || dockspaceId.isBlank()) {
            dockspaceId = DEFAULT_DOCKSPACE_ID;
        }
        if ((dockspaceFlags & ImGuiDockNodeFlags.PassthruCentralNode) != 0) {
            addWindowFlags(ImGuiWindowFlags.NoBackground);
        }
        windowPosX = sanitizeFinite(windowPosX);
        windowPosY = sanitizeFinite(windowPosY);
        windowWidth = sanitizeDimension(windowWidth);
        windowHeight = sanitizeDimension(windowHeight);
        dockspaceWidth = sanitizeDimension(dockspaceWidth);
        dockspaceHeight = sanitizeDimension(dockspaceHeight);
        defaultWindowPosX = sanitizeFinite(defaultWindowPosX);
        defaultWindowPosY = sanitizeFinite(defaultWindowPosY);
        defaultWindowWidth = sanitizeDimension(defaultWindowWidth);
        defaultWindowHeight = sanitizeDimension(defaultWindowHeight);
        if (overrideWindowPadding) {
            windowPaddingX = sanitizeNonNegative(windowPaddingX);
            windowPaddingY = sanitizeNonNegative(windowPaddingY);
        }
        if (overrideWindowBorderSize) {
            windowBorderSize = sanitizeNonNegative(windowBorderSize);
        }
    }
}
