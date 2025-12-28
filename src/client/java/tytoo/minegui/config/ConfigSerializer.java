package tytoo.minegui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class ConfigSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    ConfigSerializer() {
    }

    GlobalConfig readConfig(Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GlobalConfig parsed = GSON.fromJson(reader, GlobalConfig.class);
            if (parsed == null) {
                return null;
            }
            parsed.setGlobalScale(parsed.getGlobalScale());
            return parsed;
        } catch (IOException | JsonParseException e) {
            MineGuiCore.LOGGER.error("Failed to read global config from {}", path, e);
            return null;
        }
    }

    void writeConfig(Path path, GlobalConfig value, ConfigState state, ConfigPathResolver pathResolver) {
        GlobalConfig payload = ConfigState.cloneConfig(value);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            GSON.toJson(payload, writer);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to write global config to {}", path, e);
        }
    }

    GlobalConfig applyLoadProfile(GlobalConfig source, ConfigFeatureProfile profile) {
        GlobalConfig runtime = new GlobalConfig();
        if (profile.shouldLoad(ConfigFeature.CORE)) {
            runtime.setViewport(source.isViewportEnabled());
            runtime.setDockspace(source.isDockspaceEnabled());
            runtime.setGlobalScale(source.getGlobalScale());
        }
        if (profile.shouldLoad(ConfigFeature.STYLE_REFERENCES)) {
            runtime.setGlobalStyleKey(source.getGlobalStyleKey());
        } else {
            runtime.setGlobalStyleKey(null);
        }
        return runtime;
    }

    GlobalConfig mergeForSave(GlobalConfig runtime, GlobalConfig snapshot, ConfigFeatureProfile profile) {
        GlobalConfig target = snapshot != null ? ConfigState.cloneConfig(snapshot) : new GlobalConfig();
        if (profile.shouldSave(ConfigFeature.CORE)) {
            target.setViewport(runtime.isViewportEnabled());
            target.setDockspace(runtime.isDockspaceEnabled());
            target.setGlobalScale(runtime.getGlobalScale());
        }
        if (profile.shouldSave(ConfigFeature.STYLE_REFERENCES)) {
            target.setGlobalStyleKey(runtime.getGlobalStyleKey());
        }
        return target;
    }

    static void ensureDirectory(Path directory) {
        if (directory == null) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to create config directory {}", directory, e);
        }
    }

    static void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            MineGuiCore.LOGGER.error("Failed to delete config file {}", path, e);
        }
    }
}
