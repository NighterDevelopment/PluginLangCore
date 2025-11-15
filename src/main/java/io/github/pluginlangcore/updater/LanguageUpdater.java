package io.github.pluginlangcore.updater;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Automatic language file updater for Minecraft plugins.
 * <p>
 * This class handles automatic updates of language files when a plugin version changes.
 * It preserves user customizations while adding new keys and updating the version number.
 * Key features include:
 * <ul>
 *   <li>Version-based update detection</li>
 *   <li>Automatic backup creation before updates</li>
 *   <li>Preservation of user customizations</li>
 *   <li>Support for multiple language file types</li>
 *   <li>Support for multiple languages</li>
 *   <li>Smart detection of meaningful changes</li>
 * </ul>
 * <p>
 * The updater adds a {@code language_version} key to each language file to track versions.
 * When the plugin version increases, it automatically merges new keys while preserving
 * user modifications.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     @Override
 *     public void onEnable() {
 *         // Initialize language updater with all file types
 *         new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN"));
 *
 *         // Or with specific file types
 *         new LanguageUpdater(
 *             this,
 *             Arrays.asList("en_US", "de_DE"),
 *             LanguageFileType.MESSAGES,
 *             LanguageFileType.GUI
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanguageUpdater {
    private final String currentVersion;
    private final JavaPlugin plugin;
    private final List<String> supportedLanguages;
    private static final String LANGUAGE_VERSION_KEY = "language_version";

    // Track which file types to update
    private final Set<LanguageFileType> activeFileTypes = new HashSet<>();

    /**
     * Enum representing different language file types.
     * <p>
     * Each file type corresponds to a specific YAML file used for different
     * aspects of plugin localization.
     * </p>
     */
    @Getter
    public enum LanguageFileType {
        /**
         * Messages file containing player notifications and system messages.
         */
        MESSAGES("messages.yml"),

        /**
         * GUI file containing inventory titles and item descriptions.
         */
        GUI("gui.yml"),

        /**
         * Formatting file containing number and name formatting rules.
         */
        FORMATTING("formatting.yml"),

        /**
         * Items file containing item names and lore.
         */
        ITEMS("items.yml");

        private final String fileName;

        LanguageFileType(String fileName) {
            this.fileName = fileName;
        }
    }

    /**
     * Constructs a LanguageUpdater with all file types enabled.
     * <p>
     * This constructor automatically checks and updates all language files
     * for all supported languages immediately upon construction.
     * </p>
     *
     * @param plugin             The JavaPlugin instance
     * @param supportedLanguages List of language codes to support (e.g., "en_US", "vi_VN")
     *
     * <pre>{@code
     * // Update all file types for English and Vietnamese
     * new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN"));
     * }</pre>
     */
    public LanguageUpdater(JavaPlugin plugin, List<String> supportedLanguages) {
        this(plugin, supportedLanguages, LanguageFileType.values());
    }

    /**
     * Constructs a LanguageUpdater with specific file types.
     * <p>
     * Use this constructor when you only want to update certain file types,
     * which can improve performance and reduce unnecessary file operations.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Only update messages and GUI files
     * new LanguageUpdater(
     *     this,
     *     Arrays.asList("en_US", "de_DE"),
     *     LanguageFileType.MESSAGES,
     *     LanguageFileType.GUI
     * );
     * }</pre>
     *
     * @param plugin             The JavaPlugin instance
     * @param supportedLanguages List of language codes to support
     * @param fileTypes          Specific file types to update
     */
    public LanguageUpdater(JavaPlugin plugin, List<String> supportedLanguages, LanguageFileType... fileTypes) {
        this.plugin = plugin;
        this.supportedLanguages = supportedLanguages != null ? supportedLanguages : new ArrayList<>();
        this.currentVersion = plugin.getDescription().getVersion();
        activeFileTypes.addAll(Arrays.asList(fileTypes));
        checkAndUpdateLanguageFiles();
    }

    /**
     * Checks and updates all language files for all supported languages.
     * This method:
     * <ul>
     *   <li>Creates language directories if they don't exist</li>
     *   <li>Checks version of each language file</li>
     *   <li>Updates files that are outdated</li>
     *   <li>Preserves user customizations</li>
     *   <li>Creates backups when necessary</li>
     * </ul>
     * This method is called automatically in the constructor but can be called
     * manually to force a re-check and update.
     */
    public void checkAndUpdateLanguageFiles() {
        for (String language : supportedLanguages) {
            File langDir = new File(plugin.getDataFolder(), "language/" + language);

            // Create language directory if it doesn't exist
            if (!langDir.exists()) {
                langDir.mkdirs();
            }

            // Check and update each language file type
            for (LanguageFileType fileType : activeFileTypes) {
                File languageFile = new File(langDir, fileType.getFileName());
                updateLanguageFile(language, languageFile, fileType);
            }
        }
    }

    /**
     * Updates a specific language file.
     * <p>
     * This method handles the entire update process:
     * <ol>
     *   <li>Checks if file exists, creates if needed</li>
     *   <li>Compares version numbers</li>
     *   <li>Creates backup if changes detected</li>
     *   <li>Merges new keys with user values</li>
     *   <li>Saves updated file</li>
     * </ol>
     *
     * @param language     The language code (e.g., "en_US")
     * @param languageFile The file to update
     * @param fileType     The type of language file
     */
    private void updateLanguageFile(String language, File languageFile, LanguageFileType fileType) {
        try {
            // Create parent directory if it doesn't exist
            if (!languageFile.getParentFile().exists()) {
                languageFile.getParentFile().mkdirs();
            }

            // Create the file if it doesn't exist
            if (!languageFile.exists()) {
                createDefaultLanguageFileWithHeader(language, languageFile, fileType);
                plugin.getLogger().info("Created new " + fileType.getFileName() + " for " + language);
                return;
            }

            FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(languageFile);
            String configVersionStr = currentConfig.getString(LANGUAGE_VERSION_KEY, "0.0.0");
            Version configVersion = new Version(configVersionStr);
            Version pluginVersion = new Version(currentVersion);

            if (configVersion.compareTo(pluginVersion) >= 0) {
                return; // No update needed
            }

            if (!configVersionStr.equals("0.0.0")) {
                plugin.getLogger().info("Updating " + language + " " + fileType.getFileName() +
                        " from version " + configVersionStr + " to " + currentVersion);
            }

            // Store user's current values
            Map<String, Object> userValues = flattenConfig(currentConfig);

            // Create temp file with new default config
            File tempFile = new File(plugin.getDataFolder(),
                    "language/" + language + "/" + fileType.getFileName().replace(".yml", "_new.yml"));
            createDefaultLanguageFileWithHeader(language, tempFile, fileType);

            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(tempFile);
            newConfig.set(LANGUAGE_VERSION_KEY, currentVersion);

            // Check if there are actual differences before creating backup
            boolean configDiffers = hasConfigDifferences(userValues, newConfig);

            if (configDiffers) {
                File backupFile = new File(plugin.getDataFolder(),
                        "language/" + language + "/" + fileType.getFileName().replace(".yml", "_backup_" + configVersionStr + ".yml"));
                Files.copy(languageFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info(language + " " + fileType.getFileName() + " backup created at " + backupFile.getName());
            }

            // Apply user values and save
            applyUserValues(newConfig, userValues);
            newConfig.save(languageFile);
            tempFile.delete();

            plugin.getLogger().info("Successfully updated " + language + " " + fileType.getFileName() + " to version " + currentVersion);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update " + language + " " + fileType.getFileName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a default language file with a version header.
     * <p>
     * This method attempts to load the default file from the plugin's resources.
     * If the resource doesn't exist, it creates an empty file with just the version number.
     * </p>
     *
     * @param language        The language code
     * @param destinationFile The file to create
     * @param fileType        The type of language file
     */
    private void createDefaultLanguageFileWithHeader(String language, File destinationFile, LanguageFileType fileType) {
        try (InputStream in = plugin.getResource("language/" + language + "/" + fileType.getFileName())) {
            if (in != null) {
                List<String> defaultLines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines()
                        .toList();

                List<String> newLines = new ArrayList<>();
                newLines.add("# Language file version - Do not modify this value");
                newLines.add(LANGUAGE_VERSION_KEY + ": " + currentVersion);
                newLines.add("");
                newLines.addAll(defaultLines);

                destinationFile.getParentFile().mkdirs();
                Files.write(destinationFile.toPath(), newLines, StandardCharsets.UTF_8);
            } else {
                plugin.getLogger().warning("Default " + fileType.getFileName() + " for " + language +
                        " not found in the plugin's resources.");

                // Create empty file with just version
                destinationFile.getParentFile().mkdirs();

                // Create basic YAML with just the version
                YamlConfiguration emptyConfig = new YamlConfiguration();
                emptyConfig.set(LANGUAGE_VERSION_KEY, currentVersion);
                emptyConfig.set("_note", "This is an empty " + fileType.getFileName() +
                        " created because no default was found in the plugin resources.");
                emptyConfig.save(destinationFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default language file " + fileType.getFileName() +
                    " for " + language + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determines if there are actual differences between old and new configs.
     * <p>
     * This method compares the configurations to detect:
     * <ul>
     *   <li>Removed keys</li>
     *   <li>Changed default values</li>
     *   <li>New keys</li>
     * </ul>
     * If no meaningful changes are detected, backup creation is skipped.
     * </p>
     *
     * @param userValues Map of user's current configuration values
     * @param newConfig  The new default configuration
     * @return true if there are meaningful differences, false otherwise
     */
    private boolean hasConfigDifferences(Map<String, Object> userValues, FileConfiguration newConfig) {
        // Get all paths from new config (excluding version key)
        Map<String, Object> newConfigMap = flattenConfig(newConfig);

        // Check for removed or changed keys
        for (Map.Entry<String, Object> entry : userValues.entrySet()) {
            String path = entry.getKey();
            Object oldValue = entry.getValue();

            // Skip version key
            if (path.equals(LANGUAGE_VERSION_KEY)) continue;

            // Check if path no longer exists
            if (!newConfig.contains(path)) {
                return true; // Found a removed path
            }

            // Check if default value changed
            Object newDefaultValue = newConfig.get(path);
            if (newDefaultValue != null && !newDefaultValue.equals(oldValue)) {
                return true; // Default value changed
            }
        }

        // Check for new keys
        for (String path : newConfigMap.keySet()) {
            if (!path.equals(LANGUAGE_VERSION_KEY) && !userValues.containsKey(path)) {
                return true; // Found a new path
            }
        }

        return false; // No significant differences
    }

    /**
     * Flattens a configuration section into a map of path to value.
     * <p>
     * This method converts a nested YAML structure into a flat map where
     * keys are dot-separated paths and values are the leaf values.
     * <p>
     * Example usage:
     * <pre>{@code
     * // YAML:
     * // player:
     * //   name: "Steve"
     * //   level: 5
     *
     * // Result:
     * // {
     * //   "player.name": "Steve",
     * //   "player.level": 5
     * // }
     * }</pre>
     *
     * @param config The configuration section to flatten
     * @return A map of path to value
     */
    private Map<String, Object> flattenConfig(ConfigurationSection config) {
        Map<String, Object> result = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                result.put(key, config.get(key));
            }
        }
        return result;
    }

    /**
     * Applies user values to the new configuration.
     * <p>
     * This method preserves user customizations by applying their values
     * to the new configuration. If a key no longer exists in the new config,
     * a warning is logged but the update continues.
     * </p>
     *
     * @param newConfig  The new configuration to update
     * @param userValues Map of user's custom values to apply
     */
    private void applyUserValues(FileConfiguration newConfig, Map<String, Object> userValues) {
        for (Map.Entry<String, Object> entry : userValues.entrySet()) {
            String path = entry.getKey();
            Object value = entry.getValue();

            // Don't override version key
            if (path.equals(LANGUAGE_VERSION_KEY)) continue;

            if (newConfig.contains(path)) {
                newConfig.set(path, value);
            } else {
                plugin.getLogger().fine("Config path '" + path + "' from old config no longer exists in new config");
            }
        }
    }

    /**
     * Gets the list of supported languages.
     *
     * @return Unmodifiable list of language codes
     */
    public List<String> getSupportedLanguages() {
        return Collections.unmodifiableList(supportedLanguages);
    }

    /**
     * Gets the set of active file types being managed.
     *
     * @return Unmodifiable set of active file types
     */
    public Set<LanguageFileType> getActiveFileTypes() {
        return Collections.unmodifiableSet(activeFileTypes);
    }

    /**
     * Gets the current plugin version.
     *
     * @return The current plugin version string
     */
    public String getCurrentVersion() {
        return currentVersion;
    }
}