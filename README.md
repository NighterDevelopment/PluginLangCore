# PluginLangCore

A comprehensive, high-performance language and message management library for Minecraft plugins.

## Features

- 🌍 **Multi-language Support** - Easy internationalization with multiple file types
- 🔄 **Automatic Updates** - Version-tracked language file updates with backup
- ⚡ **High Performance** - Advanced LRU caching system with cache statistics
- 🎨 **Rich Formatting** - Hex color codes, legacy colors, and placeholder support
- 📦 **Modular Design** - Use only the file types you need
- 🔧 **Easy Integration** - Simple API with comprehensive JavaDocs
- 🎯 **Player Features** - Titles, subtitles, action bars, and sounds
- 📊 **Number Formatting** - Locale-specific number abbreviations (K, M, B, T)
- 🎭 **Entity & Item Names** - Automatic formatting for Minecraft entities and materials

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.NighterDevelopment</groupId>
        <artifactId>PluginLangCore</artifactId>
        <version>1.0.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.NighterDevelopment:PluginLangCore:1.0.0'
}
```

## Quick Start

### Resource Directory Structure

**Important:** You need to create the language files in your plugin's resources directory. The library will copy these default files to the plugin's data folder on first run.

#### Recommended Structure

```
src/main/resources/
├── plugin.yml
├── config.yml
└── language/
    ├── en_US/
    │   ├── messages.yml      (Player messages, notifications)
    │   ├── gui.yml           (GUI titles, item names, lore)
    │   ├── formatting.yml    (Number & entity name formatting)
    │   └── items.yml         (Vanilla & custom item names)
    ├── vi_VN/
    │   ├── messages.yml
    │   ├── gui.yml
    │   ├── formatting.yml
    │   └── items.yml
    └── de_DE/
        ├── messages.yml
        └── gui.yml
```

**Pattern:** `language/{language_code}/{file_type}.yml`

#### Minimal Setup (Messages Only)

If you only need player messages, create just what you need:

```
src/main/resources/
├── plugin.yml
├── config.yml
└── language/
    └── en_US/
        └── messages.yml
```

Then in your code, specify only the file types you're using:

```java
// Only enable MESSAGES file type
new LanguageUpdater(this, List.of("en_US"),
    LanguageUpdater.LanguageFileType.MESSAGES
);

languageManager = new LanguageManager(this,
    LanguageManager.LanguageFileType.MESSAGES
);
```

> **💡 Tip:** Only create and enable the file types you actually use. This prevents empty files from being created and reduces warnings.

### Basic Setup

```java
import io.github.pluginlangcore.language.LanguageManager;
import io.github.pluginlangcore.language.MessageService;
import io.github.pluginlangcore.updater.LanguageUpdater;

public class MyPlugin extends JavaPlugin {
    private LanguageManager languageManager;
    private MessageService messageService;
    
    @Override
    public void onEnable() {
        // Initialize automatic language file updater (optional but recommended)
        // IMPORTANT: Specify the same file types in both LanguageUpdater and LanguageManager
        new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN"),
            LanguageManager.LanguageFileType.MESSAGES,
            LanguageManager.LanguageFileType.GUI
        );
        
        // Initialize with specific file types (recommended)
        languageManager = new LanguageManager(this, 
            LanguageManager.LanguageFileType.MESSAGES, 
            LanguageManager.LanguageFileType.GUI
        );
        
        messageService = new MessageService(this, languageManager);
        
        // Or initialize with all file types (if you use all)
        languageManager = new LanguageManager(this);
        messageService = new MessageService(this, languageManager);
    }
}
```

### Automatic Language File Updates

The library includes an automatic updater that keeps language files synchronized with your plugin version:

```java
// Update all file types for multiple languages
new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN", "de_DE"));

// Update only specific file types
new LanguageUpdater(
    this,
    Arrays.asList("en_US", "de_DE"),
    LanguageFileType.MESSAGES,
    LanguageFileType.GUI
);
```

**Features:**
- Automatic version tracking in language files
- Preserves user customizations during updates
- Creates backups before modifying files
- Only backs up when meaningful changes detected
- Adds new keys while keeping user values

### Sending Messages

```java
// Simple message without placeholders
messageService.sendMessage(player, "welcome");

// Message with placeholders
Map<String, String> placeholders = new HashMap<>();
placeholders.put("player", player.getName());
placeholders.put("amount", "100");
messageService.sendMessage(player, "reward_received", placeholders);

// Console message
messageService.sendConsoleMessage("server_started");
```

### Using LanguageManager Directly

```java
// Get a formatted message
Map<String, String> placeholders = Map.of("player", "Steve");
String message = languageManager.getMessage("welcome", placeholders);

// Get GUI item name
String itemName = languageManager.getGuiItemName("menu.main.item.settings");

// Get item lore
String[] lore = languageManager.getGuiItemLore("menu.main.item.settings.lore");

// Format numbers
String formatted = languageManager.formatNumber(1500000); // Returns "1.5M"

// Get mob names
String mobName = languageManager.getFormattedMobName(EntityType.CAVE_SPIDER);

// Small caps text
String smallCaps = languageManager.getSmallCaps("Hello World"); // Returns "ʜᴇʟʟᴏ ᴡᴏʀʟᴅ"
```

## Language File Structure

PluginLangCore supports four types of language files. Each file should be placed in your plugin's resources directory following the pattern: `language/{language_code}/{file_type}.yml`

### 1. messages.yml
**Location:** `src/main/resources/language/en_US/messages.yml`  
**Purpose:** Player messages, notifications, and command responses.

```yaml
# Language file version - Automatically managed by LanguageUpdater
language_version: 1.0.0

# Message prefix (applied to all messages by default)
prefix: "&7[&aMyPlugin&7] &r"

# Example messages
welcome:
  enabled: true
  message: "Welcome to the server, {player}!"
  title: "&aWelcome!"
  subtitle: "&7{player}"
  action_bar: "&aWelcome {player}!"
  sound: "ENTITY_PLAYER_LEVELUP"

player_join:
  enabled: true
  message: "&a{player} has joined the server!"

no_permission:
  enabled: true
  message: "&cYou don't have permission to do that!"

reward_received:
  enabled: true
  message: "&aYou received {amount} coins!"
  title: "&aReward!"
  subtitle: "&e+{amount} coins"
  sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
```

### 2. gui.yml
**Location:** `src/main/resources/language/en_US/gui.yml`  
**Purpose:** GUI titles, item names, and descriptions.

```yaml
language_version: 1.0.0

menu:
  main:
    title: "&8Main Menu"
    item:
      settings:
        name: "&eSettings"
        lore:
          - "&7Click to open settings"
          - "&7Current: {setting}"
      
      close:
        name: "&cClose"
        lore:
          - "&7Click to close this menu"
  
  shop:
    title: "&6Shop Menu"
    item:
      diamond:
        name: "&bDiamond"
        lore:
          - "&7Price: &e{price}"
          - ""
          - "&eClick to purchase"
```

### 3. formatting.yml
**Location:** `src/main/resources/language/en_US/formatting.yml`  
**Purpose:** Number formatting and entity names.

```yaml
language_version: 1.0.0

# Number abbreviation formats
format_number:
  thousand: "{s}K"    # 1000 -> 1K
  million: "{s}M"     # 1000000 -> 1M
  billion: "{s}B"     # 1000000000 -> 1B
  trillion: "{s}T"    # 1000000000000 -> 1T
  default: "{s}"      # Below 1000

# Custom mob name translations
mob_names:
  CAVE_SPIDER: "&cCave Spider"
  ZOMBIE: "&2Zombie"
  SKELETON: "&7Skeleton"
  CREEPER: "&aCreeper"
  ENDER_DRAGON: "&5&lEnder Dragon"
```

### 4. items.yml
**Location:** `src/main/resources/language/en_US/items.yml`  
**Purpose:** Vanilla and custom item names and lore.

```yaml
language_version: 1.0.0

# Vanilla item overrides
item:
  DIAMOND_SWORD:
    name: "&bDiamond Sword"
    lore:
      - "&7A powerful weapon"
      - "&7Durability: {durability}"
  
  GOLDEN_APPLE:
    name: "&6Golden Apple"
    lore:
      - "&7Heals and grants buffs"
      - "&7Rarity: &6Rare"

# Custom items
custom:
  magic_wand:
    name: "&5&lMagic Wand"
    lore:
      - "&7A mystical wand"
      - "&7Level: {level}"
      - ""
      - "&eRight-click to use"
```

### Example: Multi-language Support

**English (en_US):**
```
src/main/resources/language/en_US/messages.yml
```
```yaml
language_version: 1.0.0
prefix: "&7[&aPlugin&7] &r"

welcome:
  enabled: true
  message: "Welcome, {player}!"
```

**Vietnamese (vi_VN):**
```
src/main/resources/language/vi_VN/messages.yml
```
```yaml
language_version: 1.0.0
prefix: "&7[&aPlugin&7] &r"

welcome:
  enabled: true
  message: "Chào mừng, {player}!"
```

**German (de_DE):**
```
src/main/resources/language/de_DE/messages.yml
```
```yaml
language_version: 1.0.0
prefix: "&7[&aPlugin&7] &r"

welcome:
  enabled: true
  message: "Willkommen, {player}!"
```

## Advanced Features

### Hex Color Support

```yaml
message: "&#FF5733This text is orange!"
mixed: "&aGreen &#FF5733Orange &bBlue"
```

### Multi-line Placeholders

```java
Map<String, String> placeholders = new HashMap<>();
placeholders.put("description", "Line 1\nLine 2\nLine 3");

List<String> lore = languageManager.getGuiItemLoreWithMultilinePlaceholders(
    "item.detailed.lore", 
    placeholders
);
```

### Cache Statistics

```java
Map<String, Object> stats = languageManager.getCacheStats();
System.out.println("Cache hit ratio: " + stats.get("hit_ratio"));
System.out.println("Total hits: " + stats.get("cache_hits"));
System.out.println("Total misses: " + stats.get("cache_misses"));
```

### Reloading Language Files

```java
// Reload all language files and clear caches
languageManager.reloadLanguages();
messageService.clearKeyExistsCache();
```

### Custom Cache Sizes

```java
// The library uses default cache sizes, but you can modify them
// by accessing the cache objects directly if needed
languageManager.clearCache(); // Clear all caches manually
```

## Color Utility

The library includes a powerful color utility:

```java
import io.github.pluginlangcore.util.ColorUtil;

// Translate color codes
String colored = ColorUtil.translateHexColorCodes("&#FF5733Hello &aWorld");

// Strip colors
String plain = ColorUtil.stripColors("§aGreen §bBlue");

// Check for colors
boolean hasColors = ColorUtil.hasColors("§aHello");

// Translate only hex codes
String hexOnly = ColorUtil.translateHexOnly("&#FF5733Hello &aWorld");
```

## Language File Types

```java
// Use specific file types to reduce memory usage
LanguageManager langManager = new LanguageManager(plugin,
    LanguageFileType.MESSAGES,  // Player messages
    LanguageFileType.GUI,       // GUI text
    LanguageFileType.FORMATTING,// Number and name formatting
    LanguageFileType.ITEMS      // Item names and lore
);
```

## Configuration

In your `config.yml`:

```yaml
# Default language locale
language: "en_US"
```

The library will automatically:
- Create the language directory structure
- Copy default language files (if provided in your plugin resources)
- Merge new keys from defaults into existing files
- Load only the configured locale

## Best Practices

### 1. Resource Structure
- ✅ **DO:** Place language files in `src/main/resources/language/{language_code}/`
- ✅ **DO:** Follow the pattern: `language/{language_code}/{file_type}.yml`
- ✅ **DO:** Only create file types you actually use
- ✅ **DO:** Always provide at least one default language (e.g., en_US)
- ❌ **DON'T:** Create language files directly in the plugin data folder

### 2. File Type Selection
- ✅ **DO:** Specify only the file types you need in both `LanguageUpdater` and `LanguageManager`
- ✅ **DO:** Match file types between `LanguageUpdater` and `LanguageManager` constructors
- ❌ **DON'T:** Use all file types if you only need messages

**Example:**
```java
// Good - Only uses what's needed
new LanguageUpdater(this, List.of("en_US"),
    LanguageFileType.MESSAGES
);
languageManager = new LanguageManager(this,
    LanguageFileType.MESSAGES
);

// Bad - Creates unnecessary files
new LanguageUpdater(this, List.of("en_US")); // All types
languageManager = new LanguageManager(this,
    LanguageFileType.MESSAGES  // Only messages
);
```

### 3. Code Practices
- ✅ **DO:** Always use placeholders instead of string concatenation
- ✅ **DO:** Cache `MessageService` and `LanguageManager` instances
- ✅ **DO:** Clear caches when reloading language files
- ✅ **DO:** Use meaningful message keys that describe the content
- ✅ **DO:** Run `LanguageUpdater` before initializing `LanguageManager`

### 4. Message Keys
- ✅ **DO:** Use hierarchical keys: `category.subcategory.item`
- ✅ **DO:** Use descriptive names: `player_join_broadcast`, `no_permission`
- ❌ **DON'T:** Use vague keys: `msg1`, `text2`, `error`

### 5. Performance
- ✅ **DO:** Take advantage of the built-in LRU cache
- ✅ **DO:** Use cache statistics to monitor performance
- ❌ **DON'T:** Repeatedly call `getMessage()` for the same key in loops

## Performance

- **LRU Caching**: Automatic cache management with configurable sizes
- **Lazy Loading**: Only the default locale is loaded initially
- **Minimal Object Creation**: Reuses empty maps and optimizes allocations
- **Pre-compiled Patterns**: Color code patterns are compiled once
- **Cache Statistics**: Monitor performance with built-in statistics

## Thread Safety

- **LanguageManager**: Thread-safe for reading operations
- **MessageService**: Thread-safe for all operations
- **LRUCache**: Fully synchronized for concurrent access

## Requirements

- **Minecraft**: 1.21+
- **Java**: 21+
- **Server**: Paper or compatible forks

## Troubleshooting

### Issue: "Default {file}.yml not found in the plugin's resources"

**Cause:** The language file doesn't exist in your plugin's resources.

**Solution:**
1. Create the file in `src/main/resources/language/{language_code}/{file}.yml`
2. Or disable that file type if you don't need it:
```java
// Only enable file types you have created
new LanguageUpdater(this, List.of("en_US"),
    LanguageFileType.MESSAGES  // Only enable MESSAGES
);
```

### Issue: Empty language files being created

**Cause:** File type is enabled but not provided in resources.

**Solution:** Only enable file types you have in resources:
```java
// Match file types with what you have in resources
new LanguageUpdater(this, List.of("en_US"),
    LanguageFileType.MESSAGES,
    LanguageFileType.GUI
);

languageManager = new LanguageManager(this,
    LanguageFileType.MESSAGES,
    LanguageFileType.GUI
);
```

### Issue: "Missing message: {key}"

**Cause:** Message key doesn't exist in your language file.

**Solution:**
1. Add the key to your `messages.yml`:
```yaml
your_key:
  enabled: true
  message: "Your message here"
```
2. Reload the plugin or restart the server

### Issue: Placeholders not replaced

**Cause:** Incorrect placeholder syntax.

**Solution:** Use curly braces, not other brackets:
```yaml
# Correct
message: "Welcome, {player}!"

# Wrong
message: "Welcome, %player%!"
message: "Welcome, [player]!"
```

### Issue: Colors not working

**Cause:** Using wrong color code format.

**Solution:** 
```yaml
# Legacy colors - Use &
message: "&aGreen &cRed"

# Hex colors - Use &#
message: "&#FF5733Orange &#00FF00Green"

# Mixed
message: "&aLegacy green &#FF5733Hex orange"
```

## Examples

See the [examples directory](examples/) for complete plugin examples.

## License

This library is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

Originally extracted and refactored from the SmartSpawner plugin.

## Quick Reference

### Resource Directory Setup

```
YourPlugin/
└── src/main/resources/
    ├── plugin.yml
    ├── config.yml
    └── language/
        └── en_US/              ← Your language code
            ├── messages.yml     ← Player messages
            ├── gui.yml          ← GUI items (optional)
            ├── formatting.yml   ← Number/entity formatting (optional)
            └── items.yml        ← Item names (optional)
```

### Minimal messages.yml Template

```yaml
language_version: 1.0.0
prefix: "&7[&aYourPlugin&7] &r"

welcome:
  enabled: true
  message: "Welcome, {player}!"
```

### Common Language Codes

- `en_US` - English (United States)
- `en_GB` - English (United Kingdom)
- `vi_VN` - Vietnamese (Vietnam)
- `de_DE` - German (Germany)
- `fr_FR` - French (France)
- `es_ES` - Spanish (Spain)
- `zh_CN` - Chinese (Simplified)
- `ja_JP` - Japanese (Japan)
- `ko_KR` - Korean (Korea)
- `pt_BR` - Portuguese (Brazil)
- `ru_RU` - Russian (Russia)
- `it_IT` - Italian (Italy)

### File Type Reference

| File Type | File Name | Purpose | Common Use Cases |
|-----------|-----------|---------|------------------|
| `MESSAGES` | `messages.yml` | Player messages, notifications, commands | Always needed for user communication |
| `GUI` | `gui.yml` | Inventory menus, item names/lore | Needed if you have GUI menus |
| `FORMATTING` | `formatting.yml` | Number format, entity names | Needed for stats, leaderboards, mob names |
| `ITEMS` | `items.yml` | Item names and descriptions | Needed for custom items |

### Code Template

```java
public class YourPlugin extends JavaPlugin {
    private LanguageManager languageManager;
    private MessageService messageService;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Step 1: Update language files (optional but recommended)
        new LanguageUpdater(this, List.of("en_US"),
            LanguageUpdater.LanguageFileType.MESSAGES
        );
        
        // Step 2: Initialize language manager (must match file types above)
        languageManager = new LanguageManager(this,
            LanguageManager.LanguageFileType.MESSAGES
        );
        
        // Step 3: Initialize message service
        messageService = new MessageService(this, languageManager);
        
        // Step 4: Use it!
        messageService.sendConsoleMessage("plugin_enabled");
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request