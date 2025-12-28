# MineGui src/client review

## Readiness verdict
Not production-ready yet for documentation or release due to configuration options that do not take effect and some default behaviors that add runtime weight.

## Findings
- [High] MineGuiInitializationOptions config controls are not applied anywhere (featureProfile, configPathStrategy, loadGlobalConfig, ignoreGlobalConfig). Options imply configurable loading/saving behavior, but GlobalConfigService is never updated, so callers cannot actually opt out or change strategy. `src/client/java/tytoo/minegui/MineGuiInitializationOptions.java:16`.
- [Medium] MineGuiCore.init dereferences options before null validation, leading to a NullPointerException on invalid input instead of a clear error. `src/client/java/tytoo/minegui/MineGuiCore.java:41`.
- [Medium] Fonts are preloaded at init, and multiple default fonts are registered unconditionally, increasing memory footprint even if unused (not lightweight by default). `src/client/java/tytoo/minegui/imgui/ImGuiContextManager.java:85`, `src/client/java/tytoo/minegui/style/Fonts.java:47`.
- [Low] ImGui image cache is unbounded; new ResourceId entries persist until a global invalidation, which can grow memory for dynamic/unique textures. `src/client/java/tytoo/minegui/util/ImGuiImageUtils.java:21`, `src/client/java/tytoo/minegui/util/ImGuiImageUtils.java:106`.

## Plan
1. Wire MineGuiInitializationOptions into ConfigRegistry/GlobalConfigService so configPathStrategy, featureProfile, loadGlobalConfig, and ignoreGlobalConfig actually affect loading/saving.
2. Decide on font loading policy (lazy-load or make default font registration optional) to keep the default footprint small.
3. Add a cache policy or explicit API/docs for ImGuiImageUtils cache lifetimes; consider size limits or explicit per-texture eviction.
4. Document core extension points (setup, views/render callbacks, style/FontLibrary, cursor policies) and lifecycle constraints (viewport/dockspace flags are set at ImGui init).
