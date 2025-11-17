# PluginLangCore Examples

Complete, ready-to-use examples for PluginLangCore.

> 💡 **Quick Start:** See [README.md](../README.md) for installation and basic usage.

## 📋 Table of Contents

1. [Simple Plugin](#1-simple-plugin)
2. [Multi-Language Plugin](#2-multi-language-plugin)
3. [Plugin with GUI](#3-plugin-with-gui)
4. [Complete Plugin Example](#4-complete-plugin-example)

---

## 1. Simple Plugin

Basic single-language plugin with messages only.

### Plugin Code

```java
package com.example.simpleplugin;

import io.github.pluginlangcore.LanguageSystem;
import io.github.pluginlangcore.LanguageSystem.LanguageFileType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class SimplePlugin extends JavaPlugin {
    private LanguageSystem languageSystem;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Initialize language system
        languageSystem = LanguageSystem.builder(this)
            .defaultLocale("en_US")
            .fileTypes(LanguageFileType.MESSAGES)
            .build();
        
        languageSystem.getMessageService()
            .sendConsoleMessage("plugin_enabled");
    }
    
    @Override
    public void onDisable() {
        languageSystem.getMessageService()
            .sendConsoleMessage("plugin_disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("hello")) {
            languageSystem.getMessageService().sendMessage(
                player, 
                "welcome", 
                Map.of("player", player.getName())
            );
            return true;
        }
        
        return false;
    }
}
```

### Resources Structure

```
src/main/resources/
├── plugin.yml
├── config.yml
└── language/
    └── en_US/
        └── messages.yml
```

### messages.yml

```yaml
prefix: "&7[&aSimplePlugin&7] &r"

plugin_enabled:
  enabled: true
  message: "&aPlugin has been enabled!"

plugin_disabled:
  enabled: true
  message: "&cPlugin has been disabled!"

welcome:
  enabled: true
  message: "&aWelcome to the server, {player}!"
  title: "&aWelcome!"
  subtitle: "&7{player}"
  sound: "ENTITY_PLAYER_LEVELUP"
```

### plugin.yml

```yaml
name: SimplePlugin
version: 1.0.0
main: com.example.simpleplugin.SimplePlugin
api-version: 1.19

commands:
  hello:
    description: Say hello
    usage: /hello
```

---

## 2. Multi-Language Plugin

Plugin supporting multiple languages.

### Plugin Code

```java
package com.example.multilang;

import io.github.pluginlangcore.LanguageSystem;
import io.github.pluginlangcore.LanguageSystem.LanguageFileType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class MultiLangPlugin extends JavaPlugin implements Listener {
    private LanguageSystem languageSystem;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Support multiple languages
        languageSystem = LanguageSystem.builder(this)
            .supportedLanguages("en_US", "vi_VN", "de_DE")
            .fileTypes(LanguageFileType.MESSAGES)
            .autoUpdate(true)
            .build();
        
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        languageSystem.getMessageService().sendMessage(
            player,
            "player_join",
            Map.of(
                "player", player.getName(),
                "online", String.valueOf(getServer().getOnlinePlayers().size())
            )
        );
    }
}
```

### Resources Structure

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

### messages.yml (en_US)

```yaml
prefix: "&7[&aMultiLang&7] &r"

player_join:
  enabled: true
  message: "&aWelcome {player}! &7({online} online)"
  title: "&aWelcome!"
  subtitle: "&7{player}"
  sound: "ENTITY_PLAYER_LEVELUP"
```

### messages.yml (vi_VN)

```yaml
prefix: "&7[&aMultiLang&7] &r"

player_join:
  enabled: true
  message: "&aChào mừng {player}! &7({online} đang online)"
  title: "&aChào mừng!"
  subtitle: "&7{player}"
  sound: "ENTITY_PLAYER_LEVELUP"
```

### messages.yml (de_DE)

```yaml
prefix: "&7[&aMultiLang&7] &r"

player_join:
  enabled: true
  message: "&aWillkommen {player}! &7({online} online)"
  title: "&aWillkommen!"
  subtitle: "&7{player}"
  sound: "ENTITY_PLAYER_LEVELUP"
```

---

## 3. Plugin with GUI

Plugin with GUI menus using messages and gui file types.

### Plugin Code

```java
package com.example.guiplugin;

import io.github.pluginlangcore.LanguageSystem;
import io.github.pluginlangcore.LanguageSystem.LanguageFileType;
import io.github.pluginlangcore.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class GUIPlugin extends JavaPlugin {
    private LanguageSystem languageSystem;
    private LanguageManager languageManager;
    
    @Override
    public void onEnable() {
        // Enable both MESSAGES and GUI file types
        languageSystem = LanguageSystem.builder(this)
            .defaultLocale("en_US")
            .fileTypes(
                LanguageFileType.MESSAGES,
                LanguageFileType.GUI
            )
            .build();
        
        languageManager = languageSystem.getLanguageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("menu")) {
            openMainMenu(player);
            return true;
        }
        
        return false;
    }
    
    private void openMainMenu(Player player) {
        // Get title from gui.yml
        String title = languageManager.getGuiItemName("menu.main.title");
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        // Settings item
        inv.setItem(11, createMenuItem(
            Material.COMPARATOR,
            "menu.main.item.settings.name",
            "menu.main.item.settings.lore"
        ));
        
        // Shop item
        inv.setItem(13, createMenuItem(
            Material.EMERALD,
            "menu.main.item.shop.name",
            "menu.main.item.shop.lore"
        ));
        
        // Close item
        inv.setItem(15, createMenuItem(
            Material.BARRIER,
            "menu.main.item.close.name",
            "menu.main.item.close.lore"
        ));
        
        player.openInventory(inv);
    }
    
    private ItemStack createMenuItem(Material material, String nameKey, String loreKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Get name and lore from language files
        String name = languageManager.getGuiItemName(nameKey);
        String[] lore = languageManager.getGuiItemLore(loreKey);
        
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        
        return item;
    }
}
```

### Resources Structure

```
src/main/resources/
└── language/
    └── en_US/
        ├── messages.yml
        └── gui.yml
```

### gui.yml

```yaml
menu:
  main:
    title: "&8Main Menu"
    item:
      settings:
        name: "&eSettings"
        lore:
          - "&7Click to configure"
          - "&7your preferences"
          - ""
          - "&eLeft-click to open"
      
      shop:
        name: "&aShop"
        lore:
          - "&7Buy items and upgrades"
          - "&7Balance: &e{balance}"
          - ""
          - "&aClick to browse"
      
      close:
        name: "&cClose"
        lore:
          - "&7Close this menu"
```

---

## 4. Complete Plugin Example

Full-featured plugin with all file types.

### Plugin Code

```java
package com.example.fullplugin;

import io.github.pluginlangcore.LanguageSystem;
import io.github.pluginlangcore.LanguageSystem.LanguageFileType;
import io.github.pluginlangcore.language.LanguageManager;
import io.github.pluginlangcore.language.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class FullPlugin extends JavaPlugin implements Listener {
    private LanguageSystem languageSystem;
    private MessageService messageService;
    private LanguageManager languageManager;
    
    // Player stats (example)
    private final Map<String, Integer> playerKills = new HashMap<>();
    private final Map<String, Long> playerMoney = new HashMap<>();
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Initialize with all file types
        languageSystem = LanguageSystem.builder(this)
            .supportedLanguages("en_US", "vi_VN")
            .fileTypes(
                LanguageFileType.MESSAGES,
                LanguageFileType.GUI,
                LanguageFileType.FORMATTING,
                LanguageFileType.ITEMS
            )
            .autoUpdate(true)
            .build();
        
        messageService = languageSystem.getMessageService();
        languageManager = languageSystem.getLanguageManager();
        
        getServer().getPluginManager().registerEvents(this, this);
        
        messageService.sendConsoleMessage("plugin_enabled");
    }
    
    @Override
    public void onDisable() {
        messageService.sendConsoleMessage("plugin_disabled");
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player player) {
            EntityType type = event.getEntityType();
            String playerName = player.getName();
            
            // Update stats
            int kills = playerKills.getOrDefault(playerName, 0) + 1;
            playerKills.put(playerName, kills);
            
            long reward = 100;
            long money = playerMoney.getOrDefault(playerName, 0L) + reward;
            playerMoney.put(playerName, money);
            
            // Get formatted mob name
            String mobName = languageManager.getFormattedMobName(type);
            
            // Send message
            Map<String, String> placeholders = Map.of(
                "mob", mobName,
                "reward", String.valueOf(reward)
            );
            
            messageService.sendMessage(player, "mob_killed", placeholders);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("stats")) {
            showStats(player);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("reload")) {
            if (!player.hasPermission("fullplugin.reload")) {
                messageService.sendMessage(player, "no_permission");
                return true;
            }
            
            reloadConfig();
            languageSystem.reload();
            
            messageService.sendMessage(player, "plugin_reloaded");
            return true;
        }
        
        return false;
    }
    
    private void showStats(Player player) {
        String playerName = player.getName();
        int kills = playerKills.getOrDefault(playerName, 0);
        long money = playerMoney.getOrDefault(playerName, 0L);
        
        // Format money with abbreviations (1.5K, 2.3M, etc.)
        String formattedMoney = languageManager.formatNumber(money);
        
        Map<String, String> placeholders = Map.of(
            "player", playerName,
            "kills", String.valueOf(kills),
            "money", formattedMoney
        );
        
        messageService.sendMessage(player, "stats_display", placeholders);
    }
}
```

### Resources Structure

```
src/main/resources/
├── plugin.yml
├── config.yml
└── language/
    └── en_US/
        ├── messages.yml
        ├── gui.yml
        ├── formatting.yml
        └── items.yml
```

### messages.yml

```yaml
prefix: "&7[&aFullPlugin&7] &r"

plugin_enabled:
  enabled: true
  message: "&aPlugin enabled successfully!"

plugin_disabled:
  enabled: true
  message: "&cPlugin disabled!"

plugin_reloaded:
  enabled: true
  message: "&aPlugin reloaded successfully!"

no_permission:
  enabled: true
  message: "&cYou don't have permission!"

mob_killed:
  enabled: true
  message: "&aYou killed a {mob} and earned &e{reward} coins!"
  action_bar: "&a+{reward} coins"
  sound: "ENTITY_EXPERIENCE_ORB_PICKUP"

stats_display:
  enabled: true
  message: |
    &7&m------------------
    &e&lYour Stats
    &7&m------------------
    &7Player: &a{player}
    &7Kills: &e{kills}
    &7Money: &e{money}
    &7&m------------------
```

### formatting.yml

```yaml
# Number formatting
format_number:
  thousand: "{s}K"
  million: "{s}M"
  billion: "{s}B"
  trillion: "{s}T"
  default: "{s}"

# Mob names
mob_names:
  ZOMBIE: "&2Zombie"
  SKELETON: "&7Skeleton"
  CREEPER: "&aCreeper"
  SPIDER: "&cSpider"
  CAVE_SPIDER: "&4Cave Spider"
  ENDERMAN: "&5Enderman"
  ENDER_DRAGON: "&5&lEnder Dragon"
  WITHER: "&8&lWither"
```

---

## 🎯 Quick Reference

### Initialize Language System

```java
// Single language, messages only
languageSystem = LanguageSystem.builder(this)
    .defaultLocale("en_US")
    .fileTypes(LanguageFileType.MESSAGES)
    .build();

// Multi-language
languageSystem = LanguageSystem.builder(this)
    .supportedLanguages("en_US", "vi_VN", "de_DE")
    .fileTypes(LanguageFileType.MESSAGES)
    .build();

// Multiple file types
languageSystem = LanguageSystem.builder(this)
    .defaultLocale("en_US")
    .fileTypes(
        LanguageFileType.MESSAGES,
        LanguageFileType.GUI
    )
    .build();

// All file types
languageSystem = LanguageSystem.builder(this)
    .defaultLocale("en_US")
    .fileTypes(LanguageFileType.values())
    .build();
```

### Send Messages

```java
MessageService service = languageSystem.getMessageService();

// Simple
service.sendMessage(player, "message_key");

// With placeholders
service.sendMessage(player, "message_key", Map.of("key", "value"));

// Console
service.sendConsoleMessage("message_key");
```

### Get Language Manager

```java
LanguageManager manager = languageSystem.getLanguageManager();

// Messages
String msg = manager.getMessage("key");
String msg = manager.getMessage("key", placeholders);

// GUI
String name = manager.getGuiItemName("menu.item.name");
String[] lore = manager.getGuiItemLore("menu.item.lore");

// Format numbers
String formatted = manager.formatNumber(1500000); // "1.5M"

// Mob names
String name = manager.getFormattedMobName(EntityType.ZOMBIE);
```

### Reload

```java
languageSystem.reload();
```

---

## 💡 Tips

1. **Use only the file types you need** - Don't enable all file types if you only use messages
2. **Match your resources** - Only enable file types that you have resources for
3. **Use placeholders** - Make messages dynamic with `{placeholder}` syntax
4. **Test with multiple languages** - Ensure all translations are complete
5. **Use auto-update** - Keep it enabled (default) for automatic version updates

---

For more information, see the [README.md](../README.md)

