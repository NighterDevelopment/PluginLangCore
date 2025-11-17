# PluginLangCore
A powerful, easy-to-use language and message management library for Minecraft plugins with automatic updates and caching. Originally extracted from SmartSpawner.

![GitHub release](https://img.shields.io/github/v/release/NighterDevelopment/PluginLangCore?style=flat-square)

## ✨ Features
- 🌍 **Multi-language** - Support multiple languages effortlessly
- 🔄 **Auto-Update** - Automatic language file version tracking and updates
- 📦 **Modular** - Use only the file types you need (messages, gui, formatting, items)
- ⚡ **High Performance** - Built-in LRU caching system
- 🎨 **Rich Formatting** - Hex colors (&a, &#RRGGBB), placeholders, titles, sounds
- 📊 **Number Formatting** - Smart abbreviations (1.5K, 2.3M, 1.2B)

## 📦 Installation
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
        <version>1.0.2</version>
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
    implementation 'com.github.NighterDevelopment:PluginLangCore:1.0.2'
}
```
## 🚀 Quick Start

### 1. Create Language File

Create `src/main/resources/language/en_US/messages.yml`:
```yaml
prefix: "&7[&aMyPlugin&7] &r"

welcome:
  enabled: true
  message: "&aWelcome {player}!"
  title: "&aWelcome!"
  subtitle: "&7{player}"
  sound: "ENTITY_PLAYER_LEVELUP"

goodbye:
  enabled: true
  message: "&cSee you later, {player}!"
```

### 2. Initialize in Your Plugin
```java
import io.github.pluginlangcore.LanguageSystem;
import io.github.pluginlangcore.LanguageSystem.LanguageFileType;
public class MyPlugin extends JavaPlugin {
    private LanguageSystem languageSystem;
    @Override
    public void onEnable() {
        // Simple initialization - specify file types only once!
        languageSystem = LanguageSystem.builder(this)
            .defaultLocale("en_US")
            .fileTypes(LanguageFileType.MESSAGES)
            .build();
    }
}
```

### 3. Send Messages
```java
// Simple message
languageSystem.getMessageService().sendMessage(player, "welcome", 
    Map.of("player", player.getName()));
// Console message
languageSystem.getMessageService().sendConsoleMessage("server_started");
```

**That's it!** 🎉
## 📚 Usage Examples

### Single Language Plugin
```java
public class MyPlugin extends JavaPlugin {
    private LanguageSystem languageSystem;
    @Override
    public void onEnable() {
        languageSystem = LanguageSystem.builder(this)
            .defaultLocale("en_US")
            .fileTypes(LanguageFileType.MESSAGES)
            .build();
    }
}
```

### Multi-Language Plugin
```java
languageSystem = LanguageSystem.builder(this)
    .supportedLanguages("en_US", "vi_VN", "de_DE")
    .fileTypes(LanguageFileType.MESSAGES)
    .build();
```

### With Multiple File Types
```java
languageSystem = LanguageSystem.builder(this)
    .defaultLocale("en_US")
    .fileTypes(
        LanguageFileType.MESSAGES,
        LanguageFileType.GUI,
        LanguageFileType.FORMATTING
    )
    .build();
```

### Disable Auto-Update
```java
languageSystem = LanguageSystem.builder(this)
    .defaultLocale("en_US")
    .fileTypes(LanguageFileType.MESSAGES)
    .autoUpdate(false)  // Manual updates only
    .build();
```

## 📖 File Types

| File Type | Purpose | Example |
|-----------|---------|---------|
| `MESSAGES` | Player messages, notifications | Welcome messages, errors |
| `GUI` | Menu titles, item names, lore | Inventory GUIs |
| `FORMATTING` | Number formats, mob names | 1.5M, "Cave Spider" |
| `ITEMS` | Custom item names and lore | Custom weapons, tools |

## 💡 API Methods
### MessageService
```java
MessageService service = languageSystem.getMessageService();
// Send message to player
service.sendMessage(player, "welcome");
service.sendMessage(player, "welcome", placeholders);
// Console message
service.sendConsoleMessage("server_started");
// Title
service.sendTitle(player, "title_key", placeholders);
// Reload
languageSystem.reload();
```

### LanguageManager
```java
LanguageManager manager = languageSystem.getLanguageManager();
// Get messages
String msg = manager.getMessage("welcome");
String msg = manager.getMessage("welcome", placeholders);
// GUI items
String name = manager.getGuiItemName("menu.main.title");
String[] lore = manager.getGuiItemLore("menu.main.item.lore");
// Format numbers
String formatted = manager.formatNumber(1500000); // "1.5M"
// Mob names
String mobName = manager.getFormattedMobName(EntityType.ZOMBIE);
```

## 📁 Resource Structure
```
src/main/resources/
└── language/
    └── en_US/
        ├── messages.yml      (Required: Player messages)
        ├── gui.yml           (Optional: GUI elements)
        ├── formatting.yml    (Optional: Number & name formats)
        └── items.yml         (Optional: Item descriptions)
```

**Pattern:** `language/{language_code}/{file_type}.yml`
**Multiple languages:**

```
src/main/resources/
└── language/
    ├── en_US/
    │   └── messages.yml
    ├── vi_VN/
    │   └── messages.yml
    └── de_DE/
        └── messages.yml
```

## 📝 Message File Format

```yaml
# Auto-managed version (do not edit)
language_version: 1.0.0

# Optional: Global prefix for all messages
prefix: "&7[&aMyPlugin&7] &r"

# Message keys
message_key:
  enabled: true                    # Enable/disable message
  message: "&aYour message here"   # Main message
  title: "&aTitle"                 # Optional: Title
  subtitle: "&7Subtitle"           # Optional: Subtitle
  action_bar: "&eAction bar"       # Optional: Action bar
  sound: "ENTITY_PLAYER_LEVELUP"   # Optional: Sound effect
  
# Simple message (no extras)
simple_message:
  enabled: true
  message: "Hello {player}!"

# Disabled message
disabled_message:
  enabled: false
  message: "This won't be sent"
```

## 🎨 Color Codes

### Legacy Colors
```yaml
message: "&aGreen &bAqua &cRed &eYellow"
```
### Hex Colors
```yaml
message: "&#FF5733Custom &#00FF00Color"
```
### Gradient (requires compatible chat plugin)
```yaml
message: "<gradient:#FF0000:#00FF00>Rainbow Text</gradient>"
```
## 🔧 Advanced Features

### Number Formatting
```java
manager.formatNumber(1000);        // "1K"
manager.formatNumber(1500000);     // "1.5M"
manager.formatNumber(1200000000);  // "1.2B"
```
Configure in `formatting.yml`:

```yaml
format_number:
  thousand: "{s}K"
  million: "{s}M"
  billion: "{s}B"
  trillion: "{s}T"
```
### Custom Mob Names

```java
String name = manager.getFormattedMobName(EntityType.CAVE_SPIDER);
```
Configure in `formatting.yml`:

```yaml
mob_names:
  CAVE_SPIDER: "&cCave Spider"
  ZOMBIE: "&2Zombie"
```

### Small Caps Text

```java
String text = manager.getSmallCaps("Hello World");
// Returns: "ʜᴇʟʟᴏ ᴡᴏʀʟᴅ"
```

### Placeholders

```java
Map<String, String> placeholders = Map.of(
    "player", player.getName(),
    "amount", "100",
    "server", "MyServer"
);
service.sendMessage(player, "message_key", placeholders);
```

In messages.yml:

```yaml
message_key:
  enabled: true
  message: "Welcome {player}! You have {amount} coins on {server}!"
```

## 🔄 Auto-Update System

The library automatically tracks language file versions and updates them when your plugin version changes:
- ✅ Preserves user customizations
- ✅ Adds new keys from defaults
- ✅ Creates backups before changes
- ✅ Only backs up when needed
- ✅ Tracks version in each file
**No configuration needed!** Just update your plugin version and default language files.

## 📚 More Examples
See [EXAMPLES.md](examples/EXAMPLES.md) for complete code examples including:
- Full plugin implementations
- GUI menu creation
- Multi-language setup
- Custom formatting
- And more!

## 🤝 Support
- **Documentation:** [JavaDocs](https://jitpack.io/com/github/NighterDevelopment/PluginLangCore/1.0.2/javadoc/)
- **Examples:** [examples/](examples/)
- **Issues:** [GitHub Issues](https://github.com/NighterDevelopment/PluginLangCore/issues)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
