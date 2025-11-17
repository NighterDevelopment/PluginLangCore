package io.github.pluginlangcore.language;

import io.github.pluginlangcore.cache.LRUCache;
import io.github.pluginlangcore.util.ColorUtil;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Main language management system for Minecraft plugins.
 * <p>
 * This class provides comprehensive internationalization (i18n) support with features including:
 * </p>
 * <ul>
 *   <li>Multiple language file types (messages, GUI, formatting, items)</li>
 *   <li>Automatic file creation and merging with defaults</li>
 *   <li>Advanced caching system with LRU eviction</li>
 *   <li>Placeholder replacement support</li>
 *   <li>Hex color code support</li>
 *   <li>Entity and material name formatting</li>
 *   <li>Number formatting with locale-specific patterns</li>
 * </ul>
 * <p>
 * The LanguageManager supports multiple language file types through the {@link LanguageFileType} enum,
 * allowing for organized separation of different types of translatable content.
 * </p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * // Basic initialization
 * LanguageManager langManager = new LanguageManager(plugin);
 *
 * // Get a message with placeholders
 * Map<String, String> placeholders = new HashMap<>();
 * placeholders.put("player", "Steve");
 * placeholders.put("amount", "100");
 * String message = langManager.getMessage("welcome", placeholders);
 *
 * // Reload language files
 * langManager.reloadLanguages();
 * }</pre>
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanguageManager {
    private final JavaPlugin plugin;

    @Getter
    private String defaultLocale;

    private final Map<String, LocaleData> localeMap = new HashMap<>();
    private final Set<String> activeLocales = new HashSet<>();
    private final Set<LanguageFileType> activeFileTypes = new HashSet<>();
    private LocaleData cachedDefaultLocaleData;
    private static final Map<String, String> EMPTY_PLACEHOLDERS = Collections.emptyMap();

    // Enhanced cache implementation
    private final LRUCache<String, String> formattedStringCache;
    private final LRUCache<String, String[]> loreCache;
    private final LRUCache<String, List<String>> loreListCache;

    private final LRUCache<String, String> guiItemNameCache;
    private final LRUCache<String, String[]> guiItemLoreCache;
    private final LRUCache<String, List<String>> guiItemLoreListCache;

    private final LRUCache<String, String> entityNameCache;
    private final LRUCache<String, String> smallCapsCache;
    private final LRUCache<String, String> materialNameCache;

    // Cache statistics
    private final AtomicInteger cacheHits = new AtomicInteger(0);
    private final AtomicInteger cacheMisses = new AtomicInteger(0);

    // Cache configuration
    private static final int DEFAULT_STRING_CACHE_SIZE = 1000;
    private static final int DEFAULT_LORE_CACHE_SIZE = 250;
    private static final int DEFAULT_LORE_LIST_CACHE_SIZE = 250;

    /**
     * Enum representing the different language file types supported by the language manager.
     * Each file type serves a specific purpose:
     * <ul>
     *   <li><b>MESSAGES</b> - Contains player messages, notifications, and command responses</li>
     *   <li><b>GUI</b> - Contains GUI titles, item names and lore for inventory interfaces</li>
     *   <li><b>FORMATTING</b> - Contains formatting rules for numbers, entity names, etc.</li>
     *   <li><b>ITEMS</b> - Contains vanilla and custom item names and lore</li>
     * </ul>
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
     * Constructs a LanguageManager with all file types enabled.
     * This constructor:
     * <ul>
     *   <li>Reads the default locale from plugin config (defaults to "en_US")</li>
     *   <li>Enables all language file types</li>
     *   <li>Initializes all caches with default capacities</li>
     *   <li>Loads and caches the default locale</li>
     * </ul>
     *
     * @param plugin The JavaPlugin instance using this language manager
     */
    public LanguageManager(JavaPlugin plugin) {
        this(plugin, LanguageFileType.values());
    }

    /**
     * Constructs a LanguageManager with specific file types.
     * <p>
     * Use this constructor when you only need certain file types,
     * reducing memory usage and improving performance.
     * </p>
     *
     * @param plugin    The JavaPlugin instance using this language manager
     * @param fileTypes Specific file types to load (e.g., only MESSAGES and GUI)
     *
     * <pre>{@code
     * // Only load messages and GUI files
     * LanguageManager langManager = new LanguageManager(
     *     plugin,
     *     LanguageFileType.MESSAGES,
     *     LanguageFileType.GUI
     * );
     * }</pre>
     */
    public LanguageManager(JavaPlugin plugin, LanguageFileType... fileTypes) {
        this.plugin = plugin;
        this.defaultLocale = plugin.getConfig().getString("language", "en_US");
        activeFileTypes.addAll(Arrays.asList(fileTypes));

        // Initialize caches
        this.formattedStringCache = new LRUCache<>(DEFAULT_STRING_CACHE_SIZE);
        this.loreCache = new LRUCache<>(DEFAULT_LORE_CACHE_SIZE);
        this.loreListCache = new LRUCache<>(DEFAULT_LORE_LIST_CACHE_SIZE);

        this.guiItemNameCache = new LRUCache<>(DEFAULT_STRING_CACHE_SIZE);
        this.guiItemLoreCache = new LRUCache<>(DEFAULT_LORE_CACHE_SIZE);
        this.guiItemLoreListCache = new LRUCache<>(DEFAULT_LORE_LIST_CACHE_SIZE);

        this.entityNameCache = new LRUCache<>(250);
        this.smallCapsCache = new LRUCache<>(500);
        this.materialNameCache = new LRUCache<>(250);

        loadLanguages();
        cacheDefaultLocaleData();
    }

    //---------------------------------------------------
    //                 Core Methods
    //---------------------------------------------------

    /**
     * Loads language files for the default locale.
     * This method:
     * <ul>
     *   <li>Creates the language directory if it doesn't exist</li>
     *   <li>Loads all active file types for the default locale</li>
     *   <li>Merges default values with user configuration</li>
     * </ul>
     */
    public void loadLanguages() {
        loadLanguages(activeFileTypes.toArray(new LanguageFileType[0]));
    }

    /**
     * Loads specific language file types for the default locale.
     *
     * @param fileTypes The file types to load
     */
    public void loadLanguages(LanguageFileType... fileTypes) {
        File langDir = new File(plugin.getDataFolder(), "language");
        if (!langDir.exists() && !langDir.mkdirs()) {
            plugin.getLogger().severe("Failed to create language directory!");
            return;
        }

        // Clear existing locale data for the default locale to ensure a fresh load
        localeMap.remove(defaultLocale);

        // Load only the default locale
        loadLocale(defaultLocale, fileTypes);
        activeLocales.add(defaultLocale);
    }

    /**
     * Caches the default locale data for fast access.
     * <p>
     * This method stores a reference to the default locale's data,
     * which is used for all message retrieval operations to avoid
     * repeated map lookups.
     * </p>
     */
    private void cacheDefaultLocaleData() {
        cachedDefaultLocaleData = localeMap.get(defaultLocale);
        if (cachedDefaultLocaleData == null) {
            plugin.getLogger().severe("Failed to cache default locale data for " + defaultLocale);
            // Create empty configs as fallback
            cachedDefaultLocaleData = LocaleData.empty();
            localeMap.put(defaultLocale, cachedDefaultLocaleData);
        }
    }

    /**
     * Reloads all language files and clears caches.
     * <p>
     * This method:
     * <ul>
     *   <li>Clears all caches to prevent stale data</li>
     *   <li>Updates the default locale from config</li>
     *   <li>Reloads all active language files</li>
     *   <li>Re-caches the default locale</li>
     * </ul>
     * Call this method after changing language files or the default locale.
     */
    public void reloadLanguages() {
        // Clear all caches first to avoid using stale data
        clearCache();

        // Update the default locale from config
        this.defaultLocale = plugin.getConfig().getString("language", "en_US");

        // Force reload all locale files for all active locales
        for (String locale : activeLocales) {
            // Remove current locale data to ensure fresh load
            localeMap.remove(locale);
            // Force reload all file types for this locale
            for (LanguageFileType fileType : activeFileTypes) {
                YamlConfiguration config = loadOrCreateFile(locale, fileType.getFileName(), true);
                updateLocaleData(locale, fileType, config);
            }
        }

        // Load the new default locale if it's not already loaded
        if (!activeLocales.contains(this.defaultLocale)) {
            loadLocale(this.defaultLocale, activeFileTypes.toArray(new LanguageFileType[0]));
            activeLocales.add(this.defaultLocale);
        }

        // Re-cache the default locale data
        cacheDefaultLocaleData();

        plugin.getLogger().info("Successfully reloaded language files for language " + this.defaultLocale);
    }

    /**
     * Updates locale data for a specific file type.
     *
     * @param locale   The locale to update
     * @param fileType The file type to update
     * @param config   The new configuration
     */
    private void updateLocaleData(String locale, LanguageFileType fileType, YamlConfiguration config) {
        LocaleData existingData = localeMap.getOrDefault(locale, LocaleData.empty());

        switch (fileType) {
            case MESSAGES:
                localeMap.put(locale, new LocaleData(config, existingData.gui(),
                        existingData.formatting(), existingData.items()));
                break;
            case GUI:
                localeMap.put(locale, new LocaleData(existingData.messages(), config,
                        existingData.formatting(), existingData.items()));
                break;
            case FORMATTING:
                localeMap.put(locale, new LocaleData(existingData.messages(), existingData.gui(),
                        config, existingData.items()));
                break;
            case ITEMS:
                localeMap.put(locale, new LocaleData(existingData.messages(), existingData.gui(),
                        existingData.formatting(), config));
                break;
        }
    }

    /**
     * Loads or creates a language file, optionally forcing a reload.
     *
     * @param locale      The locale to load
     * @param fileName    The file name
     * @param forceReload Whether to force reload from disk
     * @return The loaded YAML configuration
     */
    private YamlConfiguration loadOrCreateFile(String locale, String fileName, boolean forceReload) {
        File file = new File(plugin.getDataFolder(), "language/" + locale + "/" + fileName);
        YamlConfiguration defaultConfig = new YamlConfiguration();
        YamlConfiguration userConfig = new YamlConfiguration();

        // Check if the default resource exists before trying to load it
        boolean defaultResourceExists = plugin.getResource("language/" + defaultLocale + "/" + fileName) != null;

        // Load default configuration from resources if it exists
        if (defaultResourceExists) {
            try (InputStream inputStream = plugin.getResource("language/" + defaultLocale + "/" + fileName)) {
                if (inputStream != null) {
                    defaultConfig.loadFromString(new String(inputStream.readAllBytes()));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load default " + fileName, e);
            }
        }

        // Only create file if it doesn't exist, the default resource exists, AND the file type is active
        boolean isActiveFileType = isFileTypeActive(fileName);
        if (!file.exists() && defaultResourceExists && isActiveFileType) {
            try (InputStream inputStream = plugin.getResource("language/" + defaultLocale + "/" + fileName)) {
                if (inputStream != null) {
                    file.getParentFile().mkdirs();
                    Files.copy(inputStream, file.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create " + fileName + " for locale " + locale, e);
                return new YamlConfiguration();
            }
        }

        // Load user configuration if file exists
        if (file.exists()) {
            try {
                if (forceReload) {
                    userConfig = YamlConfiguration.loadConfiguration(file);
                } else {
                    userConfig.load(file);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load " + fileName + " for locale " + locale + ". Using defaults.", e);
                return defaultConfig;
            }

            // Merge configurations (add missing keys from default to user config)
            boolean updated = false;
            for (String key : defaultConfig.getKeys(false)) {
                if (!userConfig.contains(key)) {
                    userConfig.set(key, defaultConfig.get(key));
                    updated = true;
                }
            }

            // Save if updated
            if (updated) {
                try {
                    userConfig.save(file);
                    plugin.getLogger().info("Updated " + fileName + " for locale " + locale);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to save updated " + fileName + " for locale " + locale, e);
                }
            }

            return userConfig;
        } else {
            return new YamlConfiguration();
        }
    }

    /**
     * Checks if a file type is active based on the file name.
     *
     * @param fileName The file name to check
     * @return true if the file type is active, false otherwise
     */
    private boolean isFileTypeActive(String fileName) {
        for (LanguageFileType fileType : activeFileTypes) {
            if (fileType.getFileName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads or creates a language file without forcing reload.
     *
     * @param locale   The locale to load
     * @param fileName The file name
     * @return The loaded YAML configuration
     */
    private YamlConfiguration loadOrCreateFile(String locale, String fileName) {
        return loadOrCreateFile(locale, fileName, false);
    }

    /**
     * Loads a specific locale with the given file types.
     *
     * @param locale    The locale to load
     * @param fileTypes The file types to load
     */
    private void loadLocale(String locale, LanguageFileType... fileTypes) {
        File localeDir = new File(plugin.getDataFolder(), "language/" + locale);
        if (!localeDir.exists() && !localeDir.mkdirs()) {
            plugin.getLogger().severe("Failed to create locale directory for " + locale);
            return;
        }

        // Create and load or update only the specified files
        YamlConfiguration messages = null;
        YamlConfiguration gui = null;
        YamlConfiguration formatting = null;
        YamlConfiguration items = null;

        for (LanguageFileType fileType : fileTypes) {
            switch (fileType) {
                case MESSAGES:
                    messages = loadOrCreateFile(locale, fileType.getFileName());
                    break;
                case GUI:
                    gui = loadOrCreateFile(locale, fileType.getFileName());
                    break;
                case FORMATTING:
                    formatting = loadOrCreateFile(locale, fileType.getFileName());
                    break;
                case ITEMS:
                    items = loadOrCreateFile(locale, fileType.getFileName());
                    break;
            }
        }

        // If a file wasn't specified, create an empty configuration
        if (messages == null) messages = new YamlConfiguration();
        if (gui == null) gui = new YamlConfiguration();
        if (formatting == null) formatting = new YamlConfiguration();
        if (items == null) items = new YamlConfiguration();

        localeMap.put(locale, new LocaleData(messages, gui, formatting, items));
    }

    //---------------------------------------------------
    //               Messages Methods
    //---------------------------------------------------

    /**
     * Gets a message with prefix and applies placeholders and colors.
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The formatted message, or null if disabled
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }

        String message = cachedDefaultLocaleData.messages().getString(key + ".message");

        if (message == null) {
            return "Missing message: " + key;
        }

        // Apply prefix
        String prefix = getPrefix();
        message = prefix + message;

        // Apply placeholders and color formatting
        return applyPlaceholdersAndColors(message, placeholders);
    }

    /**
     * Gets a message without prefix, with placeholders and colors applied.
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The formatted message without prefix, or null if disabled
     */
    public String getMessageWithoutPrefix(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }

        String message = cachedDefaultLocaleData.messages().getString(key + ".message");

        if (message == null) {
            return "Missing message: " + key;
        }

        return applyPlaceholdersAndColors(message, placeholders);
    }

    /**
     * Gets a message for console output (placeholders only, no colors).
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The message with placeholders applied, or null if disabled
     */
    public String getMessageForConsole(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }

        String message = cachedDefaultLocaleData.messages().getString(key + ".message");

        if (message == null) {
            return "Missing message: " + key;
        }

        return applyOnlyPlaceholders(message, placeholders);
    }

    /**
     * Gets the title component of a message.
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The formatted title, or null if disabled or not found
     */
    public String getTitle(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }
        return getRawMessage(key + ".title", placeholders);
    }

    /**
     * Gets the subtitle component of a message.
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The formatted subtitle, or null if disabled or not found
     */
    public String getSubtitle(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }
        return getRawMessage(key + ".subtitle", placeholders);
    }

    /**
     * Gets the action bar component of a message.
     *
     * @param key          The message key
     * @param placeholders Map of placeholders to replace
     * @return The formatted action bar text, or null if disabled or not found
     */
    public String getActionBar(String key, Map<String, String> placeholders) {
        if (!isMessageEnabled(key)) {
            return null;
        }
        return getRawMessage(key + ".action_bar", placeholders);
    }

    /**
     * Gets the sound name for a message.
     *
     * @param key The message key
     * @return The sound name, or null if disabled or not found
     */
    public String getSound(String key) {
        if (!isMessageEnabled(key)) {
            return null;
        }

        return cachedDefaultLocaleData.messages().getString(key + ".sound");
    }

    /**
     * Gets the configured message prefix.
     *
     * @return The message prefix
     */
    private String getPrefix() {
        return cachedDefaultLocaleData.messages().getString("prefix", "&7[Server] &r");
    }

    /**
     * Gets a raw message from a path without prefix.
     *
     * @param path         The configuration path
     * @param placeholders Map of placeholders to replace
     * @return The formatted message, or null if not found
     */
    String getRawMessage(String path, Map<String, String> placeholders) {
        String message = cachedDefaultLocaleData.messages().getString(path);

        if (message == null) {
            return null;
        }

        return applyPlaceholdersAndColors(message, placeholders);
    }

    /**
     * Checks if a message is enabled.
     *
     * @param key The message key
     * @return true if enabled, false otherwise
     */
    private boolean isMessageEnabled(String key) {
        return cachedDefaultLocaleData.messages().getBoolean(key + ".enabled", true);
    }

    /**
     * Checks if a message key exists.
     *
     * @param key The message key
     * @return true if the key exists, false otherwise
     */
    public boolean keyExists(String key) {
        return cachedDefaultLocaleData.messages().contains(key);
    }

    //---------------------------------------------------
    //                  GUI Methods
    //---------------------------------------------------

    /**
     * Gets a GUI title without placeholders.
     *
     * @param key The GUI title key
     * @return The formatted GUI title
     */
    public String getGuiTitle(String key) {
        return getGuiTitle(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets a GUI title with placeholders.
     *
     * @param key          The GUI title key
     * @param placeholders Map of placeholders to replace
     * @return The formatted GUI title
     */
    public String getGuiTitle(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return null;
        }

        String title = cachedDefaultLocaleData.gui().getString(key);

        if (title == null) {
            return "Missing GUI title: " + key;
        }

        return applyPlaceholdersAndColors(title, placeholders);
    }

    /**
     * Gets a GUI item name without placeholders.
     *
     * @param key The item name key
     * @return The formatted item name
     */
    public String getGuiItemName(String key) {
        return getGuiItemName(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets a GUI item name with placeholders (cached).
     *
     * @param key          The item name key
     * @param placeholders Map of placeholders to replace
     * @return The formatted item name
     */
    public String getGuiItemName(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return null;
        }

        String cacheKey = key + "|" + generateCacheKey("", placeholders);
        String cachedName = guiItemNameCache.get(cacheKey);
        if (cachedName != null) {
            cacheHits.incrementAndGet();
            return cachedName;
        }

        cacheMisses.incrementAndGet();
        String name = cachedDefaultLocaleData.gui().getString(key);

        if (name == null) {
            return "Missing item name: " + key;
        }

        String result = applyPlaceholdersAndColors(name, placeholders);
        guiItemNameCache.put(cacheKey, result);

        return result;
    }

    /**
     * Gets GUI item lore without placeholders.
     *
     * @param key The lore key
     * @return Array of formatted lore lines
     */
    public String[] getGuiItemLore(String key) {
        return getGuiItemLore(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets GUI item lore with placeholders (cached).
     *
     * @param key          The lore key
     * @param placeholders Map of placeholders to replace
     * @return Array of formatted lore lines
     */
    public String[] getGuiItemLore(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return new String[0];
        }

        String cacheKey = key + "|" + generateCacheKey("", placeholders);
        String[] cachedLore = guiItemLoreCache.get(cacheKey);
        if (cachedLore != null) {
            cacheHits.incrementAndGet();
            return cachedLore;
        }

        cacheMisses.incrementAndGet();
        List<String> loreList = cachedDefaultLocaleData.gui().getStringList(key);
        String[] result = loreList.stream()
                .map(line -> applyPlaceholdersAndColors(line, placeholders))
                .toArray(String[]::new);

        guiItemLoreCache.put(cacheKey, result);
        return result;
    }

    /**
     * Gets GUI item lore as a list without placeholders.
     *
     * @param key The lore key
     * @return List of formatted lore lines
     */
    public List<String> getGuiItemLoreAsList(String key) {
        return getGuiItemLoreAsList(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets GUI item lore as a list with placeholders (cached).
     *
     * @param key          The lore key
     * @param placeholders Map of placeholders to replace
     * @return List of formatted lore lines
     */
    public List<String> getGuiItemLoreAsList(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return Collections.emptyList();
        }

        String cacheKey = key + "|" + generateCacheKey("", placeholders);
        List<String> cachedLore = guiItemLoreListCache.get(cacheKey);
        if (cachedLore != null) {
            cacheHits.incrementAndGet();
            return cachedLore;
        }

        cacheMisses.incrementAndGet();
        List<String> loreList = cachedDefaultLocaleData.gui().getStringList(key);
        List<String> result = loreList.stream()
                .map(line -> applyPlaceholdersAndColors(line, placeholders))
                .toList();

        guiItemLoreListCache.put(cacheKey, result);
        return result;
    }

    /**
     * Gets GUI item lore with support for multi-line placeholders.
     * <p>
     * This method expands any placeholder that contains newline characters into multiple lines.
     * Useful for dynamic content that may span multiple lines.
     * </p>
     *
     * @param key          The lore key
     * @param placeholders Map of placeholders to replace (values may contain \n for multiline)
     * @return List of formatted lore lines with multiline placeholders expanded
     */
    public List<String> getGuiItemLoreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        List<String> loreList = cachedDefaultLocaleData.gui().getStringList(key);

        for (String line : loreList) {
            boolean containsMultilinePlaceholder = false;

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (line.contains(placeholder) && entry.getValue().contains("\n")) {
                    containsMultilinePlaceholder = true;
                    break;
                }
            }

            if (containsMultilinePlaceholder) {
                String processedLine = line;

                // Apply non-multiline placeholders first
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String value = entry.getValue();

                    if (!value.contains("\n")) {
                        processedLine = processedLine.replace(placeholder, value);
                    }
                }

                // Handle multiline placeholders
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String value = entry.getValue();

                    if (processedLine.contains(placeholder) && value.contains("\n")) {
                        String[] valueLines = value.split("\n");
                        String firstLine = processedLine.replace(placeholder, valueLines[0]);
                        result.add(ColorUtil.translateHexColorCodes(firstLine));

                        String lineStart = processedLine.substring(0, processedLine.indexOf(placeholder));
                        for (int i = 1; i < valueLines.length; i++) {
                            result.add(ColorUtil.translateHexColorCodes(lineStart + valueLines[i]));
                        }
                    }
                }
            } else {
                result.add(applyPlaceholdersAndColors(line, placeholders));
            }
        }

        return result;
    }

    //---------------------------------------------------
    //                  Items Methods
    //---------------------------------------------------

    /**
     * Gets a vanilla Minecraft item name with proper formatting.
     * <p>
     * First attempts to get a translated name from the items.yml file.
     * If not found, falls back to a nicely formatted version of the material name.
     * </p>
     *
     * @param material The Minecraft material
     * @return The formatted item name
     */
    public String getVanillaItemName(Material material) {
        if (material == null) {
            return "Unknown Item";
        }

        String cacheKey = "material|" + material.name();
        String cachedName = materialNameCache.get(cacheKey);
        if (cachedName != null) {
            cacheHits.incrementAndGet();
            return cachedName;
        }

        cacheMisses.incrementAndGet();
        String key = "item." + material.name() + ".name";
        String name = null;

        if (activeFileTypes.contains(LanguageFileType.ITEMS)) {
            name = cachedDefaultLocaleData.items().getString(key);
        }

        if (name == null) {
            name = formatEnumName(material.name());
        } else {
            name = applyPlaceholdersAndColors(name, null);
        }

        materialNameCache.put(cacheKey, name);
        return name;
    }

    /**
     * Gets vanilla item lore.
     *
     * @param material The Minecraft material
     * @return Array of lore lines
     */
    public String[] getVanillaItemLore(Material material) {
        if (material == null) {
            return new String[0];
        }

        String key = "item." + material.name() + ".lore";
        return getItemLore(key);
    }

    /**
     * Gets an item name without placeholders.
     *
     * @param key The item name key
     * @return The formatted item name
     */
    public String getItemName(String key) {
        return getItemName(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets an item name with placeholders.
     *
     * @param key          The item name key
     * @param placeholders Map of placeholders to replace
     * @return The formatted item name
     */
    public String getItemName(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.ITEMS)) {
            return key;
        }

        String name = cachedDefaultLocaleData.items().getString(key);
        if (name == null) {
            return key;
        }

        return applyPlaceholdersAndColors(name, placeholders);
    }

    /**
     * Gets item lore without placeholders.
     *
     * @param key The lore key
     * @return Array of lore lines
     */
    public String[] getItemLore(String key) {
        return getItemLore(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Gets item lore with placeholders (cached).
     *
     * @param key          The lore key
     * @param placeholders Map of placeholders to replace
     * @return Array of lore lines
     */
    public String[] getItemLore(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.ITEMS)) {
            return new String[0];
        }

        String cacheKey = key + "|" + generateCacheKey("", placeholders);
        String[] cachedLore = loreCache.get(cacheKey);
        if (cachedLore != null) {
            cacheHits.incrementAndGet();
            return cachedLore;
        }

        cacheMisses.incrementAndGet();
        List<String> loreList = cachedDefaultLocaleData.items().getStringList(key);
        String[] result = loreList.stream()
                .map(line -> applyPlaceholdersAndColors(line, placeholders))
                .toArray(String[]::new);

        loreCache.put(cacheKey, result);
        return result;
    }

    /**
     * Gets item lore with support for multi-line placeholders.
     *
     * @param key          The lore key
     * @param placeholders Map of placeholders to replace (values may contain \n for multiline)
     * @return List of formatted lore lines with multiline placeholders expanded
     */
    public List<String> getItemLoreWithMultilinePlaceholders(String key, Map<String, String> placeholders) {
        if (!activeFileTypes.contains(LanguageFileType.ITEMS)) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        List<String> loreList = cachedDefaultLocaleData.items().getStringList(key);

        for (String line : loreList) {
            boolean containsMultilinePlaceholder = false;

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (line.contains(placeholder) && entry.getValue().contains("\n")) {
                    containsMultilinePlaceholder = true;
                    break;
                }
            }

            if (containsMultilinePlaceholder) {
                String processedLine = line;

                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String value = entry.getValue();

                    if (!value.contains("\n")) {
                        processedLine = processedLine.replace(placeholder, value);
                    }
                }

                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String value = entry.getValue();

                    if (processedLine.contains(placeholder) && value.contains("\n")) {
                        String[] valueLines = value.split("\n");
                        String firstLine = processedLine.replace(placeholder, valueLines[0]);
                        result.add(ColorUtil.translateHexColorCodes(firstLine));

                        String lineStart = processedLine.substring(0, processedLine.indexOf(placeholder));
                        for (int i = 1; i < valueLines.length; i++) {
                            result.add(ColorUtil.translateHexColorCodes(lineStart + valueLines[i]));
                        }
                    }
                }
            } else {
                result.add(applyPlaceholdersAndColors(line, placeholders));
            }
        }

        return result;
    }

    //---------------------------------------------------
    //               Formatting Methods
    //---------------------------------------------------

    /**
     * Formats a number with locale-specific patterns.
     * <p>
     * Automatically abbreviates large numbers:
     * <ul>
     *   <li>1,000 → 1K</li>
     *   <li>1,000,000 → 1M</li>
     *   <li>1,000,000,000 → 1B</li>
     *   <li>1,000,000,000,000 → 1T</li>
     * </ul>
     *
     * @param number The number to format
     * @return The formatted number string
     */
    public String formatNumber(double number) {
        if (!activeFileTypes.contains(LanguageFileType.FORMATTING)) {
            return formatNumberDefault(number);
        }

        String format;
        double value;

        if (number >= 1_000_000_000_000L) {
            format = cachedDefaultLocaleData.formatting().getString("format_number.trillion", "{s}T");
            value = Math.round(number / 1_000_000_000_000.0 * 10) / 10.0;
        } else if (number >= 1_000_000_000L) {
            format = cachedDefaultLocaleData.formatting().getString("format_number.billion", "{s}B");
            value = Math.round(number / 1_000_000_000.0 * 10) / 10.0;
        } else if (number >= 1_000_000L) {
            format = cachedDefaultLocaleData.formatting().getString("format_number.million", "{s}M");
            value = Math.round(number / 1_000_000.0 * 10) / 10.0;
        } else if (number >= 1_000L) {
            format = cachedDefaultLocaleData.formatting().getString("format_number.thousand", "{s}K");
            value = Math.round(number / 1_000.0 * 10) / 10.0;
        } else {
            format = cachedDefaultLocaleData.formatting().getString("format_number.default", "{s}");
            value = Math.round(number * 10) / 10.0;
        }

        return format.replace("{s}", formatDecimal(value));
    }

    /**
     * Default number formatting when no formatting file is available.
     *
     * @param number The number to format
     * @return The formatted number string
     */
    private String formatNumberDefault(double number) {
        if (number >= 1_000_000_000_000L) {
            double value = Math.round(number / 1_000_000_000_000.0 * 10) / 10.0;
            return formatDecimal(value) + "T";
        } else if (number >= 1_000_000_000L) {
            double value = Math.round(number / 1_000_000_000.0 * 10) / 10.0;
            return formatDecimal(value) + "B";
        } else if (number >= 1_000_000L) {
            double value = Math.round(number / 1_000_000.0 * 10) / 10.0;
            return formatDecimal(value) + "M";
        } else if (number >= 1_000L) {
            double value = Math.round(number / 1_000.0 * 10) / 10.0;
            return formatDecimal(value) + "K";
        } else {
            double value = Math.round(number * 10) / 10.0;
            return formatDecimal(value);
        }
    }

    /**
     * Formats a decimal value, removing unnecessary decimal places.
     *
     * @param value The value to format
     * @return The formatted value as a string
     */
    private String formatDecimal(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * Gets a formatted mob/entity name (cached).
     * <p>
     * First attempts to get a translated name from formatting.yml.
     * If not found, converts the enum name to title case.
     * </p>
     *
     * @param type The entity type
     * @return The formatted mob name
     */
    public String getFormattedMobName(EntityType type) {
        if (type == null || type == EntityType.UNKNOWN) {
            return "Unknown";
        }

        String mobNameKey = type.name();
        String cacheKey = "mob_name|" + mobNameKey;
        String cachedName = entityNameCache.get(cacheKey);

        if (cachedName != null) {
            cacheHits.incrementAndGet();
            return cachedName;
        }

        cacheMisses.incrementAndGet();
        String result;

        if (activeFileTypes.contains(LanguageFileType.FORMATTING)) {
            String formattedName = cachedDefaultLocaleData.formatting().getString("mob_names." + mobNameKey);

            if (formattedName != null) {
                result = applyPlaceholdersAndColors(formattedName, null);
                entityNameCache.put(cacheKey, result);
                return result;
            }
        }

        result = formatEnumName(mobNameKey);
        entityNameCache.put(cacheKey, result);
        return result;
    }

    /**
     * Converts an enum name to a human-readable format.
     * <p>
     * Example: "CAVE_SPIDER" → "Cave Spider"
     * </p>
     *
     * @param enumName The enum constant name
     * @return The formatted name
     */
    public String formatEnumName(String enumName) {
        String[] words = enumName.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(word.charAt(0))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    //---------------------------------------------------
    //                     Utilities
    //---------------------------------------------------

    /**
     * Converts text to small caps Unicode characters (cached).
     * <p>
     * Example: "Hello World" → "ʜᴇʟʟᴏ ᴡᴏʀʟᴅ"
     * </p>
     *
     * @param text The text to convert
     * @return The small caps text
     */
    public String getSmallCaps(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cacheKey = "smallcaps|" + text;
        String cachedText = smallCapsCache.get(cacheKey);

        if (cachedText != null) {
            cacheHits.incrementAndGet();
            return cachedText;
        }

        cacheMisses.incrementAndGet();
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char lowercaseChar = Character.toLowerCase(c);
                char smallCapsChar = getSmallCapsChar(lowercaseChar);
                result.append(smallCapsChar);
            } else {
                result.append(c);
            }
        }

        String smallCapsText = result.toString();
        smallCapsCache.put(cacheKey, smallCapsText);
        return smallCapsText;
    }

    /**
     * Gets the small caps Unicode character for a lowercase letter.
     *
     * @param c The lowercase letter
     * @return The small caps equivalent
     */
    private char getSmallCapsChar(char c) {
        return switch (c) {
            case 'a' -> 'ᴀ';
            case 'b' -> 'ʙ';
            case 'c' -> 'ᴄ';
            case 'd' -> 'ᴅ';
            case 'e' -> 'ᴇ';
            case 'f' -> 'ꜰ';
            case 'g' -> 'ɢ';
            case 'h' -> 'ʜ';
            case 'i' -> 'ɪ';
            case 'j' -> 'ᴊ';
            case 'k' -> 'ᴋ';
            case 'l' -> 'ʟ';
            case 'm' -> 'ᴍ';
            case 'n' -> 'ɴ';
            case 'o' -> 'ᴏ';
            case 'p' -> 'ᴘ';
            case 'q' -> 'ǫ';
            case 'r' -> 'ʀ';
            case 's' -> 'ꜱ';
            case 't' -> 'ᴛ';
            case 'u' -> 'ᴜ';
            case 'v' -> 'ᴠ';
            case 'w' -> 'ᴡ';
            case 'x' -> 'x';
            case 'y' -> 'ʏ';
            case 'z' -> 'ᴢ';
            default -> c;
        };
    }

    /**
     * Applies placeholders and color codes to text (cached).
     *
     * @param text         The text to format
     * @param placeholders Map of placeholders to replace
     * @return The formatted text
     */
    public String applyPlaceholdersAndColors(String text, Map<String, String> placeholders) {
        if (text == null) return null;

        String cacheKey = generateCacheKey(text, placeholders);
        String cachedResult = formattedStringCache.get(cacheKey);

        if (cachedResult != null) {
            cacheHits.incrementAndGet();
            return cachedResult;
        }

        cacheMisses.incrementAndGet();
        String result = text;

        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        result = ColorUtil.translateHexColorCodes(result);
        formattedStringCache.put(cacheKey, result);
        return result;
    }

    /**
     * Gets a color code from the GUI configuration.
     *
     * @param path The configuration path
     * @return The color code string
     */
    public String getColorCode(String path) {
        if (!activeFileTypes.contains(LanguageFileType.GUI)) {
            return ChatColor.WHITE.toString();
        }

        String colorStr = cachedDefaultLocaleData.gui().getString(path);
        if (colorStr == null) {
            return ChatColor.WHITE.toString();
        }

        return applyPlaceholdersAndColors(colorStr, EMPTY_PLACEHOLDERS);
    }

    /**
     * Applies only placeholders without color codes (cached).
     *
     * @param text         The text to format
     * @param placeholders Map of placeholders to replace
     * @return The text with placeholders applied
     */
    public String applyOnlyPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null) return null;

        String cacheKey = generateCacheKey(text, placeholders);
        String cachedResult = formattedStringCache.get(cacheKey);

        if (cachedResult != null) {
            cacheHits.incrementAndGet();
            return cachedResult;
        }

        cacheMisses.incrementAndGet();
        String result = text;

        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        formattedStringCache.put(cacheKey, result);
        return result;
    }

    //---------------------------------------------------
    //                 Cache Methods
    //---------------------------------------------------

    /**
     * Clears all caches.
     * <p>
     * Call this method when reloading language files to prevent using stale data.
     * </p>
     */
    public void clearCache() {
        formattedStringCache.clear();
        loreCache.clear();
        loreListCache.clear();
        guiItemNameCache.clear();
        guiItemLoreCache.clear();
        guiItemLoreListCache.clear();
        entityNameCache.clear();
        smallCapsCache.clear();
        materialNameCache.clear();
    }

    /**
     * Generates a cache key from text and placeholders.
     *
     * @param text         The base text
     * @param placeholders The placeholders
     * @return The cache key
     */
    private String generateCacheKey(String text, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        StringBuilder keyBuilder = new StringBuilder(text);
        List<String> keys = new ArrayList<>(placeholders.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            keyBuilder.append('|').append(key).append('=').append(placeholders.get(key));
        }
        return keyBuilder.toString();
    }

    /**
     * Gets cache statistics for monitoring and debugging.
     *
     * @return Map containing cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("string_cache_size", formattedStringCache.size());
        stats.put("string_cache_capacity", formattedStringCache.capacity());
        stats.put("lore_cache_size", loreCache.size());
        stats.put("lore_cache_capacity", loreCache.capacity());
        stats.put("lore_list_cache_size", loreListCache.size());
        stats.put("lore_list_cache_capacity", loreListCache.capacity());
        stats.put("gui_name_cache_size", guiItemNameCache.size());
        stats.put("gui_name_cache_capacity", guiItemNameCache.capacity());
        stats.put("gui_lore_cache_size", guiItemLoreCache.size());
        stats.put("gui_lore_cache_capacity", guiItemLoreCache.capacity());
        stats.put("gui_lore_list_cache_size", guiItemLoreListCache.size());
        stats.put("gui_lore_list_cache_capacity", guiItemLoreListCache.capacity());
        stats.put("entity_name_cache_size", entityNameCache.size());
        stats.put("entity_name_cache_capacity", entityNameCache.capacity());
        stats.put("small_caps_cache_size", smallCapsCache.size());
        stats.put("small_caps_cache_capacity", smallCapsCache.capacity());
        stats.put("material_name_cache_size", materialNameCache.size());
        stats.put("material_name_cache_capacity", materialNameCache.capacity());
        stats.put("cache_hits", cacheHits.get());
        stats.put("cache_misses", cacheMisses.get());
        stats.put("hit_ratio", cacheHits.get() > 0 ?
                (double) cacheHits.get() / (cacheHits.get() + cacheMisses.get()) : 0);
        return stats;
    }
}