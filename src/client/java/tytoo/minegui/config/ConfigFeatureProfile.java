package tytoo.minegui.config;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ConfigFeatureProfile {
    private final EnumSet<ConfigFeature> loadFeatures;
    private final EnumSet<ConfigFeature> saveFeatures;

    private ConfigFeatureProfile(EnumSet<ConfigFeature> loadFeatures, EnumSet<ConfigFeature> saveFeatures) {
        this.loadFeatures = loadFeatures;
        this.saveFeatures = saveFeatures;
    }

    public static ConfigFeatureProfile all() {
        EnumSet<ConfigFeature> features = EnumSet.allOf(ConfigFeature.class);
        return new ConfigFeatureProfile(EnumSet.copyOf(features), EnumSet.copyOf(features));
    }

    public static ConfigFeatureProfile none() {
        return new ConfigFeatureProfile(EnumSet.noneOf(ConfigFeature.class), EnumSet.noneOf(ConfigFeature.class));
    }

    public static ConfigFeatureProfile of(Set<ConfigFeature> loadFeatures, Set<ConfigFeature> saveFeatures) {
        Objects.requireNonNull(loadFeatures, "loadFeatures");
        Objects.requireNonNull(saveFeatures, "saveFeatures");
        return new ConfigFeatureProfile(copy(loadFeatures), copy(saveFeatures));
    }

    private static EnumSet<ConfigFeature> copy(Set<ConfigFeature> features) {
        if (features.isEmpty()) {
            return EnumSet.noneOf(ConfigFeature.class);
        }
        return EnumSet.copyOf(features);
    }

    public Set<ConfigFeature> loadFeatures() {
        return Collections.unmodifiableSet(loadFeatures);
    }

    public Set<ConfigFeature> saveFeatures() {
        return Collections.unmodifiableSet(saveFeatures);
    }

    public boolean shouldLoad(ConfigFeature feature) {
        return loadFeatures.contains(feature);
    }

    public boolean shouldSave(ConfigFeature feature) {
        return saveFeatures.contains(feature);
    }

    public ConfigFeatureProfile withLoadFeatures(Set<ConfigFeature> features) {
        Objects.requireNonNull(features, "features");
        return new ConfigFeatureProfile(copy(features), EnumSet.copyOf(saveFeatures));
    }

    public ConfigFeatureProfile withSaveFeatures(Set<ConfigFeature> features) {
        Objects.requireNonNull(features, "features");
        return new ConfigFeatureProfile(EnumSet.copyOf(loadFeatures), copy(features));
    }

    public ConfigFeatureProfile withFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> loadCopy = EnumSet.copyOf(loadFeatures);
        EnumSet<ConfigFeature> saveCopy = EnumSet.copyOf(saveFeatures);
        loadCopy.add(feature);
        saveCopy.add(feature);
        return new ConfigFeatureProfile(loadCopy, saveCopy);
    }

    public ConfigFeatureProfile withLoadFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> loadCopy = EnumSet.copyOf(loadFeatures);
        loadCopy.add(feature);
        return new ConfigFeatureProfile(loadCopy, EnumSet.copyOf(saveFeatures));
    }

    public ConfigFeatureProfile withSaveFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> saveCopy = EnumSet.copyOf(saveFeatures);
        saveCopy.add(feature);
        return new ConfigFeatureProfile(EnumSet.copyOf(loadFeatures), saveCopy);
    }

    public ConfigFeatureProfile withoutFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> loadCopy = EnumSet.copyOf(loadFeatures);
        EnumSet<ConfigFeature> saveCopy = EnumSet.copyOf(saveFeatures);
        loadCopy.remove(feature);
        saveCopy.remove(feature);
        return new ConfigFeatureProfile(loadCopy, saveCopy);
    }

    public ConfigFeatureProfile withoutLoadFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> loadCopy = EnumSet.copyOf(loadFeatures);
        loadCopy.remove(feature);
        return new ConfigFeatureProfile(loadCopy, EnumSet.copyOf(saveFeatures));
    }

    public ConfigFeatureProfile withoutSaveFeature(ConfigFeature feature) {
        Objects.requireNonNull(feature, "feature");
        EnumSet<ConfigFeature> saveCopy = EnumSet.copyOf(saveFeatures);
        saveCopy.remove(feature);
        return new ConfigFeatureProfile(EnumSet.copyOf(loadFeatures), saveCopy);
    }

    public ConfigFeatureProfile merge(ConfigFeatureProfile other) {
        Objects.requireNonNull(other, "other");
        EnumSet<ConfigFeature> loadCopy = EnumSet.copyOf(loadFeatures);
        EnumSet<ConfigFeature> saveCopy = EnumSet.copyOf(saveFeatures);
        loadCopy.addAll(other.loadFeatures);
        saveCopy.addAll(other.saveFeatures);
        return new ConfigFeatureProfile(loadCopy, saveCopy);
    }
}
