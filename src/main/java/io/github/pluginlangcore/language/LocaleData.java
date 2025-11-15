package io.github.pluginlangcore.language;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Immutable data record that holds all YAML configuration files for a specific locale.
 * <p>
 * This record encapsulates the four primary configuration files used for
 * localization and messaging:
 * <ul>
 *   <li><b>messages</b> - Player messages, system notifications, and command responses</li>
 *   <li><b>gui</b> - GUI titles, item names, and descriptions</li>
 *   <li><b>formatting</b> - Number formatting, entity names, and text transformations</li>
 *   <li><b>items</b> - Vanilla and custom item names and lore</li>
 * </ul>
 * <p>
 * Being a record, this class is immutable and provides automatic implementations
 * of equals(), hashCode(), and toString() methods.
 * <p>
 * Example usage:
 * <pre>{@code
 * YamlConfiguration messages = YamlConfiguration.loadConfiguration(new File("messages.yml"));
 * YamlConfiguration gui = YamlConfiguration.loadConfiguration(new File("gui.yml"));
 * YamlConfiguration formatting = YamlConfiguration.loadConfiguration(new File("formatting.yml"));
 * YamlConfiguration items = YamlConfiguration.loadConfiguration(new File("items.yml"));
 *
 * LocaleData localeData = new LocaleData(messages, gui, formatting, items);
 * String prefix = localeData.messages().getString("prefix");
 * }</pre>
 *
 * @param messages   Configuration containing player messages and system notifications
 * @param gui        Configuration containing GUI-related text (titles, item names, lore)
 * @param formatting Configuration containing formatting rules for numbers, names, etc.
 * @param items      Configuration containing item names and lore definitions
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public record LocaleData(
        YamlConfiguration messages,
        YamlConfiguration gui,
        YamlConfiguration formatting,
        YamlConfiguration items
) {
    /**
     * Constructs a LocaleData record with the specified configurations.
     * <p>
     * All parameters must be non-null. If any configuration file is not needed,
     * pass an empty YamlConfiguration instead of null.
     * </p>
     *
     * @param messages   Configuration containing messages (must not be null)
     * @param gui        Configuration containing GUI text (must not be null)
     * @param formatting Configuration containing formatting rules (must not be null)
     * @param items      Configuration containing item definitions (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public LocaleData {
        if (messages == null || gui == null || formatting == null || items == null) {
            throw new NullPointerException("All configuration parameters must be non-null");
        }
    }

    /**
     * Creates a LocaleData with empty configurations for all files.
     * <p>
     * Useful for creating a fallback or default instance when no configurations are available.
     * </p>
     *
     * @return A LocaleData instance with empty configurations
     */
    public static LocaleData empty() {
        return new LocaleData(
                new YamlConfiguration(),
                new YamlConfiguration(),
                new YamlConfiguration(),
                new YamlConfiguration()
        );
    }
}