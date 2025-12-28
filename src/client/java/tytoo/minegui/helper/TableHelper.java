package tytoo.minegui.helper;

import imgui.ImGui;

public final class TableHelper {

    private TableHelper() {
    }

    public static void row(Object... columns) {
        ImGui.tableNextRow();
        for (int i = 0; i < columns.length; i++) {
            ImGui.tableSetColumnIndex(i);
            ImGui.text(String.valueOf(columns[i]));
        }
    }

    public static void rowWrapped(Object... columns) {
        ImGui.tableNextRow();
        for (int i = 0; i < columns.length; i++) {
            ImGui.tableSetColumnIndex(i);
            ImGui.textWrapped(String.valueOf(columns[i]));
        }
    }
}
