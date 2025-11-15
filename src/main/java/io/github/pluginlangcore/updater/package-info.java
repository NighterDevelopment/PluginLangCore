/**
 * Automatic language file update utilities.
 * <p>
 * This package provides tools for automatically updating language files when
 * a plugin version changes. It includes:
 * <ul>
 *   <li>Version comparison and tracking</li>
 *   <li>Automatic file backup creation</li>
 *   <li>User customization preservation</li>
 *   <li>Smart change detection</li>
 * </ul>
 * </p>
 * <p>
 * The updater system works by adding a version key to each language file and
 * comparing it against the plugin version. When an update is needed, it:
 * <ol>
 *   <li>Creates a backup of the old file (if changes detected)</li>
 *   <li>Loads the new default configuration</li>
 *   <li>Applies user customizations to the new configuration</li>
 *   <li>Saves the merged result</li>
 * </ol>
 * </p>
 *
 * @see io.github.pluginlangcore.updater.LanguageUpdater
 * @see io.github.pluginlangcore.updater.Version
 * @since 1.0.0
 */
package io.github.pluginlangcore.updater;