package io.github.pluginlangcore.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling color code translations in Minecraft text.
 * <p>
 * Supports both legacy ampersand color codes (&amp;) and modern hex color codes (&#RRGGBB).
 * This class handles the conversion of these codes to their Minecraft-compatible formats.
 * </p>
 *
 * @author PluginLangCore Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ColorUtil {

    /**
     * Pattern to match hex color codes in the format &#RRGGBB.
     * Example: &#FF5733 for an orange color
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ColorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Translates hex color codes and legacy color codes in a message.
     * <p>
     * This method performs two translations:
     * <ul>
     *   <li>Converts hex color codes (&#RRGGBB) to Minecraft's hex format</li>
     *   <li>Converts ampersand color codes (&amp;a, &amp;b, etc.) to section symbols (§)</li>
     * </ul>
     * </p>
     *
     * @param message The message containing color codes to translate
     * @return The translated message with Minecraft-compatible color codes, or null if input is null
     *
     * @example
     * <pre>{@code
     * String colored = ColorUtil.translateHexColorCodes("&#FF5733Hello &aWorld");
     * // Result will have the hex color applied to "Hello" and green color applied to "World"
     * }</pre>
     */
    public static String translateHexColorCodes(String message) {
        if (message == null) {
            return null;
        }

        // Convert hex color codes (&#RRGGBB format)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);

        // Convert standard ampersand color codes (&a, &b, etc.)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Strips all color codes from a message, leaving only plain text.
     * <p>
     * This method removes all Minecraft color formatting, including:
     * <ul>
     *   <li>Legacy color codes (§a, §b, etc.)</li>
     *   <li>Hex color codes</li>
     *   <li>Format codes (§l for bold, §o for italic, etc.)</li>
     * </ul>
     * Useful for logging, console output, or length calculations.
     * </p>
     *
     * @param message The message with color codes
     * @return The message without any color codes, or null if input is null
     *
     * @example
     * <pre>{@code
     * String plain = ColorUtil.stripColors("§aGreen §bBlue &#FF5733Orange");
     * // Result: "Green Blue Orange"
     * }</pre>
     */
    public static String stripColors(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.stripColor(message);
    }

    /**
     * Checks if a string contains any color codes.
     * <p>
     * Detects both legacy color codes (§) and hex color codes (&#RRGGBB).
     * </p>
     *
     * @param message The message to check
     * @return true if the message contains color codes, false otherwise
     *
     * @example
     * <pre>{@code
     * boolean hasColors = ColorUtil.hasColors("§aHello"); // returns true
     * boolean hasColors2 = ColorUtil.hasColors("Hello"); // returns false
     * }</pre>
     */
    public static boolean hasColors(String message) {
        if (message == null) {
            return false;
        }
        return message.contains("§") || HEX_PATTERN.matcher(message).find() || message.contains("&");
    }

    /**
     * Translates only hex color codes without touching legacy color codes.
     * <p>
     * Useful when you want to handle legacy codes separately or preserve them as-is.
     * </p>
     *
     * @param message The message containing hex color codes
     * @return The message with hex codes translated, or null if input is null
     *
     * @example
     * <pre>{@code
     * String result = ColorUtil.translateHexOnly("&#FF5733Hello &aWorld");
     * // Result will have hex color for "Hello", but "&a" will remain unchanged
     * }</pre>
     */
    public static String translateHexOnly(String message) {
        if (message == null) {
            return null;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Translates only legacy ampersand color codes without touching hex codes.
     * <p>
     * Useful when you want to handle hex codes separately or preserve them as-is.
     * </p>
     *
     * @param message The message containing legacy color codes
     * @return The message with legacy codes translated, or null if input is null
     *
     * @example
     * <pre>{@code
     * String result = ColorUtil.translateLegacyOnly("&#FF5733Hello &aWorld");
     * // Result will have "&a" converted to green, but "&#FF5733" will remain unchanged
     * }</pre>
     */
    public static String translateLegacyOnly(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}