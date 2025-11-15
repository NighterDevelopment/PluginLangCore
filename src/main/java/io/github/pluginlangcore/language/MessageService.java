package io.github.pluginlangcore.language;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service class for sending formatted messages to players and console.
 * <p>
 * This service integrates with {@link LanguageManager} to provide a high-level
 * API for sending messages with features including:
 * <ul>
 *   <li>Automatic message validation</li>
 *   <li>Title and subtitle support</li>
 *   <li>Action bar messages</li>
 *   <li>Sound effects</li>
 *   <li>Console logging with color stripping</li>
 *   <li>Placeholder replacement</li>
 * </ul>
 * </p>
 * <p>
 * The service caches key existence checks to improve performance and provides
 * methods for both player and console message delivery.
 * </p>
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 *
 * @example
 * <pre>{@code
 * LanguageManager langManager = new LanguageManager(plugin);
 * MessageService messageService = new MessageService(plugin, langManager);
 *
 * // Send a simple message
 * messageService.sendMessage(player, "welcome");
 *
 * // Send a message with placeholders
 * Map<String, String> placeholders = new HashMap<>();
 * placeholders.put("player", player.getName());
 * messageService.sendMessage(player, "player_join", placeholders);
 *
 * // Send a console message
 * messageService.sendConsoleMessage("server_started");
 * }</pre>
 */
@RequiredArgsConstructor
public class MessageService {
    private final JavaPlugin plugin;
    private final LanguageManager languageManager;

    /**
     * Static empty map to avoid creating new HashMap instances.
     */
    private static final Map<String, String> EMPTY_PLACEHOLDERS = Collections.emptyMap();

    /**
     * Cache for key existence checks to reduce repeated lookups.
     */
    private final Map<String, Boolean> keyExistsCache = new ConcurrentHashMap<>(128);

    // Patterns for color code stripping - precompiled for better performance
    private static final Pattern COLOR_CODES = Pattern.compile("§[0-9a-fA-FxX]|§[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]|§[klmnorKLMNOR]");
    private static final Pattern HEX_CODES = Pattern.compile("&#[0-9a-fA-F]{6}");
    private static final Pattern AMPERSAND_CODES = Pattern.compile("&[0-9a-fA-FxXklmnorKLMNOR]");

    /**
     * Sends a message to a CommandSender with no placeholders.
     * <p>
     * This is a convenience method that uses an empty placeholder map.
     * </p>
     *
     * @param sender The command sender to receive the message
     * @param key    The message key from the language files
     */
    public void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Sends a message to a Player with no placeholders.
     * <p>
     * This is a convenience method that uses an empty placeholder map.
     * Automatically handles player-specific features like titles and sounds.
     * </p>
     *
     * @param player The player to receive the message
     * @param key    The message key from the language files
     */
    public void sendMessage(Player player, String key) {
        sendMessage(player, key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Sends a message to a Player with placeholders.
     * <p>
     * Automatically handles player-specific features:
     * <ul>
     *   <li>Chat messages</li>
     *   <li>Titles and subtitles</li>
     *   <li>Action bar messages</li>
     *   <li>Sound effects</li>
     * </ul>
     * </p>
     *
     * @param player       The player to receive the message
     * @param key          The message key from the language files
     * @param placeholders Map of placeholders to replace in the message (e.g., {player} → "Steve")
     */
    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        sendMessage((CommandSender) player, key, placeholders);
    }

    /**
     * Sends a message to a CommandSender with placeholders.
     * <p>
     * This method validates the message key exists, retrieves the message from
     * the language manager, and sends it to the recipient. If the recipient is
     * a player, additional features like titles and sounds are also processed.
     * </p>
     *
     * @param sender       The command sender to receive the message
     * @param key          The message key from the language files
     * @param placeholders Map of placeholders to replace in the message
     *
     * @example
     * <pre>{@code
     * Map<String, String> placeholders = new HashMap<>();
     * placeholders.put("amount", "100");
     * placeholders.put("item", "Diamond");
     * messageService.sendMessage(sender, "item_received", placeholders);
     * }</pre>
     */
    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        // Validate the message key exists (using cache to avoid lookups)
        if (!checkKeyExists(key)) {
            plugin.getLogger().warning("Message key not found: " + key);
            sender.sendMessage("§cMissing message key: " + key);
            return;
        }

        // Get and send the chat message if it exists
        String message = languageManager.getMessage(key, placeholders);
        if (message != null && !message.startsWith("Missing message:")) {
            sender.sendMessage(message);
        }

        // Process player-specific features
        if (sender instanceof Player player) {
            sendPlayerSpecificContent(player, key, placeholders);
        }
    }

    /**
     * Check if a key exists, using cache for efficiency.
     * <p>
     * This method caches the result of key existence checks to avoid
     * repeated lookups in the language manager.
     * </p>
     *
     * @param key The message key to check
     * @return true if the key exists, false otherwise
     */
    private boolean checkKeyExists(String key) {
        return keyExistsCache.computeIfAbsent(key, languageManager::keyExists);
    }

    /**
     * Clear the key existence cache.
     * <p>
     * Call this method when reloading language files to ensure the cache
     * reflects the current state of language files.
     * </p>
     */
    public void clearKeyExistsCache() {
        keyExistsCache.clear();
    }

    /**
     * Sends a message to the console with no placeholders.
     * <p>
     * This is a convenience method that uses an empty placeholder map.
     * Color codes are automatically stripped for console output.
     * </p>
     *
     * @param key The message key from the language files
     */
    public void sendConsoleMessage(String key) {
        sendConsoleMessage(key, EMPTY_PLACEHOLDERS);
    }

    /**
     * Sends a message to the console with placeholders.
     * <p>
     * This method retrieves the message, applies placeholders, strips all
     * color codes, and logs it to the console. Useful for server logging
     * and administrative notifications.
     * </p>
     *
     * @param key          The message key from the language files
     * @param placeholders Map of placeholders to replace in the message
     *
     * @example
     * <pre>{@code
     * Map<String, String> placeholders = new HashMap<>();
     * placeholders.put("player", "Steve");
     * placeholders.put("reason", "Flying");
     * messageService.sendConsoleMessage("player_kicked", placeholders);
     * }</pre>
     */
    public void sendConsoleMessage(String key, Map<String, String> placeholders) {
        // Validate the message key exists
        if (!languageManager.keyExists(key)) {
            plugin.getLogger().warning("Message key not found: " + key);
            return;
        }

        // Get the message without prefix
        String message = languageManager.getMessageForConsole(key, placeholders);

        if (message != null && !message.startsWith("Missing message:")) {
            // Strip all color codes for console
            String consoleMessage = stripAllColorCodes(message);
            plugin.getLogger().info(consoleMessage);
        } else {
            // Log a warning if we still couldn't get the message
            plugin.getLogger().warning("Failed to retrieve message for key: " + key);
        }
    }

    /**
     * Strips all types of color codes from a message for console output.
     * <p>
     * Handles multiple color code formats:
     * <ul>
     *   <li>Minecraft color codes (§a, §1, §f, etc.)</li>
     *   <li>Hex codes (&#RRGGBB)</li>
     *   <li>Ampersand codes (&amp;a, &amp;b, etc.)</li>
     *   <li>Format codes (§l for bold, §o for italic, etc.)</li>
     * </ul>
     * </p>
     *
     * @param message The message with color codes
     * @return The message without any color codes, or empty string if input is null
     */
    private String stripAllColorCodes(String message) {
        if (message == null) return "";

        // Process each pattern in sequence
        String result = COLOR_CODES.matcher(message).replaceAll("");
        result = HEX_CODES.matcher(result).replaceAll("");
        result = AMPERSAND_CODES.matcher(result).replaceAll("");

        return result;
    }

    /**
     * Handles player-specific message components (title, subtitle, action bar, sound).
     * <p>
     * This method is automatically called when sending messages to players.
     * It processes additional message features that are only available for players:
     * <ul>
     *   <li>Titles and subtitles (displayed at the center of the screen)</li>
     *   <li>Action bar messages (displayed above the hotbar)</li>
     *   <li>Sound effects (played at the player's location)</li>
     * </ul>
     * </p>
     *
     * @param player       The player to receive the content
     * @param key          The message key from the language files
     * @param placeholders Map of placeholders to replace in the content
     */
    private void sendPlayerSpecificContent(Player player, String key, Map<String, String> placeholders) {
        // Title and subtitle
        String title = languageManager.getTitle(key, placeholders);
        String subtitle = languageManager.getSubtitle(key, placeholders);
        if (title != null || subtitle != null) {
            player.sendTitle(
                    title != null ? title : "",
                    subtitle != null ? subtitle : "",
                    10, 70, 20
            );
        }

        // Action bar
        String actionBar = languageManager.getActionBar(key, placeholders);
        if (actionBar != null) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(actionBar)
            );
        }

        // Sound
        String soundName = languageManager.getSound(key);
        if (soundName != null) {
            try {
                player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid sound name for key " + key + ": " + soundName);
            }
        }
    }
}