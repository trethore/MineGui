package tytoo.minegui.view.persistence;

import tytoo.minegui.MineGuiCore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

public final class DefaultViewPersistenceAdapter implements ViewPersistenceAdapter {
    private final Path viewsDirectory;
    private final Path stylesDirectory;
    private final Path sharedLayoutDirectory;

    public DefaultViewPersistenceAdapter(Path rootDirectory) {
        Objects.requireNonNull(rootDirectory, "rootDirectory");
        Path base = rootDirectory;
        if (base.getFileName() != null && "views".equals(base.getFileName().toString())) {
            Path parent = base.getParent();
            if (parent != null) {
                base = parent;
            }
        }
        this.viewsDirectory = base.resolve("views");
        this.stylesDirectory = base.resolve("styles");
        this.sharedLayoutDirectory = viewsDirectory;
    }

    @Override
    public Optional<String> loadSharedLayout(String namespace) {
        Path path = sharedLayoutPath(namespace);
        return readString(path);
    }

    @Override
    public void saveSharedLayout(String namespace, String payload) {
        Path path = sharedLayoutPath(namespace);
        write(path, payload);
    }

    @Override
    public Optional<String> loadLayout(ViewPersistenceRequest request) {
        Path path = viewLayoutPath(request);
        return readString(path);
    }

    @Override
    public void saveLayout(ViewPersistenceRequest request, String payload) {
        Path path = viewLayoutPath(request);
        write(path, payload);
    }

    @Override
    public Optional<String> loadStyle(ViewPersistenceRequest request) {
        Path path = viewStylePath(request);
        return readString(path);
    }

    @Override
    public void saveStyle(ViewPersistenceRequest request, ViewStyleSnapshot snapshot) {
        Path path = viewStylePath(request);
        if (snapshot.deleted()) {
            delete(path);
            return;
        }
        write(path, snapshot.snapshotJson());
    }

    private Path sharedLayoutPath(String namespace) {
        // We ignore the namespace argument for the path structure, 
        // assuming this adapter is already scoped to the correct namespace root.
        return sharedLayoutDirectory.resolve("_shared.ini");
    }

    private Path viewLayoutPath(ViewPersistenceRequest request) {
        // We ignore request.namespace() for the directory structure.
        return viewsDirectory.resolve(request.slug() + ".ini");
    }

    private Path viewStylePath(ViewPersistenceRequest request) {
        // We ignore request.namespace() for the directory structure.
        return stylesDirectory.resolve(request.slug() + ".json");
    }

    private Optional<String> readString(Path path) {
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to read persistence file {}", path, e);
            return Optional.empty();
        }
    }

    private void write(Path path, String payload) {
        if (path == null || payload == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to write persistence file {}", path, e);
        }
    }

    private void delete(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            MineGuiCore.LOGGER.warn("Failed to delete persistence file {}", path, e);
        }
    }
}
