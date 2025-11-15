/**
 * PluginLangCore - A comprehensive language and message management library for Minecraft plugins.
 *
 * <h2>Overview</h2>
 * <p>
 * PluginLangCore provides a complete solution for internationalization (i18n) and message management
 * in Minecraft plugins. It features advanced caching, hex color support, placeholder replacement,
 * and support for multiple language file types.
 * </p>
 *
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link io.github.pluginlangcore.language.LanguageManager} - Core language management system</li>
 *   <li>{@link io.github.pluginlangcore.language.MessageService} - High-level message sending service</li>
 *   <li>{@link io.github.pluginlangcore.updater.LanguageUpdater} - Automatic language file updater</li>
 *   <li>{@link io.github.pluginlangcore.cache.LRUCache} - Thread-safe LRU cache implementation</li>
 *   <li>{@link io.github.pluginlangcore.util.ColorUtil} - Color code translation utilities</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * public class MyPlugin extends JavaPlugin {
 *     private LanguageManager languageManager;
 *     private MessageService messageService;
 *
 *     @Override
 *     public void onEnable() {
 *         languageManager = new LanguageManager(this);
 *         messageService = new MessageService(this, languageManager);
 *     }
 * }
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Multiple language file types (messages, GUI, formatting, items)</li>
 *   <li>Automatic language file updates with version tracking</li>
 *   <li>Advanced LRU caching with statistics</li>
 *   <li>Hex color code support (&#RRGGBB)</li>
 *   <li>Placeholder replacement</li>
 *   <li>Title, subtitle, and action bar support</li>
 *   <li>Sound effect integration</li>
 *   <li>Number formatting with abbreviations</li>
 *   <li>Entity and material name formatting</li>
 *   <li>Small caps text conversion</li>
 * </ul>
 *
 * @since 1.0.0
 * @version 1.0.0
 */
package io.github.pluginlangcore;