package io.github.pluginlangcore;

import io.github.pluginlangcore.language.LanguageManager;
import io.github.pluginlangcore.language.MessageService;
import io.github.pluginlangcore.updater.LanguageUpdater;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Unified language system that manages both {@link LanguageUpdater} and {@link LanguageManager}.
 * <p>
 * This class simplifies the initialization process by allowing you to specify
 * language file types only once, which will be applied to both the updater and manager.
 * </p>
 * <p>
 * <b>Basic Usage:</b>
 * <pre>{@code
 * // Simple setup with default settings
 * LanguageSystem langSystem = LanguageSystem.builder(plugin)
 *     .defaultLocale("en_US")
 *     .fileTypes(LanguageFileType.MESSAGES)
 *     .build();
 *
 * // Get the message service
 * MessageService messageService = langSystem.getMessageService();
 * messageService.sendMessage(player, "welcome");
 * }</pre>
 * <p>
 * <b>Multi-language Setup:</b>
 * <pre>{@code
 * LanguageSystem langSystem = LanguageSystem.builder(plugin)
 *     .supportedLanguages("en_US", "vi_VN", "de_DE")
 *     .fileTypes(
 *         LanguageFileType.MESSAGES,
 *         LanguageFileType.GUI
 *     )
 *     .build();
 * }</pre>
 * <p>
 * <b>Advanced Setup:</b>
 * <pre>{@code
 * LanguageSystem langSystem = LanguageSystem.builder(plugin)
 *     .supportedLanguages("en_US", "vi_VN")
 *     .fileTypes(LanguageFileType.values()) // All file types
 *     .autoUpdate(true) // Enable automatic updates
 *     .build();
 *
 * // Access components directly if needed
 * LanguageManager manager = langSystem.getLanguageManager();
 * MessageService service = langSystem.getMessageService();
 * }</pre>
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanguageSystem {
    @Getter
    private final LanguageManager languageManager;

    @Getter
    private final MessageService messageService;

    private final LanguageUpdater languageUpdater;

    /**
     * File type enum that can be used with the builder.
     */
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

        /**
         * Gets the file name for this language file type.
         *
         * @return The file name (e.g., "messages.yml")
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Converts this enum to LanguageManager.LanguageFileType
         */
        LanguageManager.LanguageFileType toManagerType() {
            return LanguageManager.LanguageFileType.valueOf(this.name());
        }

        /**
         * Converts this enum to LanguageUpdater.LanguageFileType
         */
        LanguageUpdater.LanguageFileType toUpdaterType() {
            return LanguageUpdater.LanguageFileType.valueOf(this.name());
        }
    }

    private LanguageSystem(JavaPlugin plugin, List<String> supportedLanguages,
                          LanguageFileType[] fileTypes, boolean autoUpdate) {
        // Convert to manager and updater types
        LanguageManager.LanguageFileType[] managerTypes = Arrays.stream(fileTypes)
                .map(LanguageFileType::toManagerType)
                .toArray(LanguageManager.LanguageFileType[]::new);

        LanguageUpdater.LanguageFileType[] updaterTypes = Arrays.stream(fileTypes)
                .map(LanguageFileType::toUpdaterType)
                .toArray(LanguageUpdater.LanguageFileType[]::new);

        // Initialize updater if auto-update is enabled
        if (autoUpdate && !supportedLanguages.isEmpty()) {
            this.languageUpdater = new LanguageUpdater(plugin, supportedLanguages, updaterTypes);
        } else {
            this.languageUpdater = null;
        }

        // Initialize language manager
        this.languageManager = new LanguageManager(plugin, managerTypes);

        // Initialize message service
        this.messageService = new MessageService(plugin, languageManager);
    }

    /**
     * Creates a new builder for configuring the language system.
     *
     * @param plugin The JavaPlugin instance
     * @return A new Builder instance
     */
    public static Builder builder(JavaPlugin plugin) {
        return new Builder(plugin);
    }

    /**
     * Reloads all language files and clears caches.
     */
    public void reload() {
        languageManager.reloadLanguages();
        messageService.clearKeyExistsCache();
    }

    /**
     * Builder class for creating a LanguageSystem instance.
     */
    public static class Builder {
        private final JavaPlugin plugin;
        private List<String> supportedLanguages;
        private LanguageFileType[] fileTypes;
        private boolean autoUpdate = true;

        private Builder(JavaPlugin plugin) {
            this.plugin = plugin;
            // Default to the locale from config
            String defaultLocale = plugin.getConfig().getString("language", "en_US");
            this.supportedLanguages = List.of(defaultLocale);
            // Default to all file types
            this.fileTypes = LanguageFileType.values();
        }

        /**
         * Sets the default locale (single language).
         * <p>
         * This is a convenience method equivalent to {@code supportedLanguages(locale)}.
         * </p>
         *
         * @param locale The language code (e.g., "en_US")
         * @return This builder instance
         */
        public Builder defaultLocale(String locale) {
            this.supportedLanguages = List.of(locale);
            return this;
        }

        /**
         * Sets the supported languages.
         *
         * @param languages Language codes (e.g., "en_US", "vi_VN")
         * @return This builder instance
         */
        public Builder supportedLanguages(String... languages) {
            this.supportedLanguages = Arrays.asList(languages);
            return this;
        }

        /**
         * Sets the supported languages.
         *
         * @param languages List of language codes
         * @return This builder instance
         */
        public Builder supportedLanguages(List<String> languages) {
            this.supportedLanguages = languages;
            return this;
        }

        /**
         * Sets which language file types to use.
         * <p>
         * These file types will be applied to both the updater and manager,
         * ensuring consistency.
         * </p>
         *
         * @param types The file types to enable
         * @return This builder instance
         */
        public Builder fileTypes(LanguageFileType... types) {
            this.fileTypes = types;
            return this;
        }

        /**
         * Sets whether to automatically update language files on version changes.
         * <p>
         * Default is {@code true}. Set to {@code false} if you want to manage
         * language file updates manually.
         * </p>
         *
         * @param autoUpdate Whether to enable automatic updates
         * @return This builder instance
         */
        public Builder autoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        /**
         * Builds and initializes the LanguageSystem.
         *
         * @return A fully initialized LanguageSystem instance
         */
        public LanguageSystem build() {
            if (fileTypes == null || fileTypes.length == 0) {
                throw new IllegalStateException("At least one file type must be specified");
            }
            return new LanguageSystem(plugin, supportedLanguages, fileTypes, autoUpdate);
        }
    }
}

