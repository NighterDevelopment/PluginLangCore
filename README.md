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
        <groupId>io.github.pluginlangcore</groupId>
        <artifactId>pluginlangcore</artifactId>
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
    implementation 'io.github.pluginlangcore:pluginlangcore:1.0.0'
}
```

## Quick Start

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
        new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN"));
        
        // Initialize with all file types
        languageManager = new LanguageManager(this);
        messageService = new MessageService(this, languageManager);
        
        // Or initialize with specific file types
        languageManager = new LanguageManager(this, 
            LanguageFileType.MESSAGES, 
            LanguageFileType.GUI
        );
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

PluginLangCore supports four types of language files:

### 1. messages.yml
Player messages, notifications, and command responses.

```yaml
prefix: "&7[&aMyPlugin&7] &r"

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
```

### 2. gui.yml
GUI titles, item names, and descriptions.

```yaml
menu:
  main:
    title: "&8Main Menu"
    item:
      settings:
        name: "&eSettings"
        lore:
          - "&7Click to open settings"
          - "&7Current: {setting}"
```

### 3. formatting.yml
Number formatting and entity names.

```yaml
format_number:
  thousand: "{s}K"
  million: "{s}M"
  billion: "{s}B"
  trillion: "{s}T"
  default: "{s}"

mob_names:
  CAVE_SPIDER: "&cCave Spider"
  ZOMBIE: "&2Zombie"
  SKELETON: "&7Skeleton"
```

### 4. items.yml
Vanilla and custom item names and lore.

```yaml
item:
  DIAMOND_SWORD:
    name: "&bDiamond Sword"
    lore:
      - "&7A powerful weapon"
      - "&7Durability: {durability}"
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

1. **Always use placeholders** instead of string concatenation
2. **Cache MessageService and LanguageManager** instances
3. **Clear caches** when reloading language files
4. **Use appropriate file types** to reduce memory usage
5. **Provide default language files** in your plugin resources
6. **Use meaningful message keys** that describe the content

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

## Examples

See the [examples directory](examples/) for complete plugin examples.

## License

This library is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

Originally extracted and refactored from the SmartSpawner plugin.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request