# PluginLangCore - Usage Examples

This directory contains example implementations of PluginLangCore in Minecraft plugins.

## Example 1: Basic Plugin Setup with Auto-Update

```java
package com.example.myplugin;

import io.github.pluginlangcore.language.LanguageManager;
import io.github.pluginlangcore.language.MessageService;
import io.github.pluginlangcore.updater.LanguageUpdater;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class MyPlugin extends JavaPlugin {
    
    private LanguageManager languageManager;
    private MessageService messageService;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize automatic language file updater (run before LanguageManager)
        new LanguageUpdater(this, Arrays.asList("en_US", "vi_VN", "de_DE"));
        
        // Initialize language system
        languageManager = new LanguageManager(this);
        messageService = new MessageService(this, languageManager);
        
        // Register commands and listeners
        getCommand("myplugin").setExecutor(new MyCommand(messageService));
        getServer().getPluginManager().registerEvents(new MyListener(messageService), this);
        
        // Log startup message
        messageService.sendConsoleMessage("plugin_enabled");
    }
    
    @Override
    public void onDisable() {
        messageService.sendConsoleMessage("plugin_disabled");
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public MessageService getMessageService() {
        return messageService;
    }
}
```

## Example 2: Command with Messages

```java
package com.example.myplugin.commands;

import io.github.pluginlangcore.language.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RewardCommand implements CommandExecutor {
    
    private final MessageService messageService;
    
    public RewardCommand(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messageService.sendMessage(sender, "player_only");
            return true;
        }
        
        if (!player.hasPermission("myplugin.reward")) {
            messageService.sendMessage(player, "no_permission");
            return true;
        }
        
        if (args.length < 1) {
            messageService.sendMessage(player, "usage_reward");
            return true;
        }
        
        try {
            int amount = Integer.parseInt(args[0]);
            
            // Give reward
            giveReward(player, amount);
            
            // Send message with placeholders
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amount));
            placeholders.put("player", player.getName());
            
            messageService.sendMessage(player, "reward_received", placeholders);
            
        } catch (NumberFormatException e) {
            messageService.sendMessage(player, "invalid_number");
        }
        
        return true;
    }
    
    private void giveReward(Player player, int amount) {
        // Your reward logic here
    }
}
```

## Example 3: Custom GUI with Language Support

```java
package com.example.myplugin.gui;

import io.github.pluginlangcore.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsGUI {
    
    private final LanguageManager languageManager;
    
    public SettingsGUI(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }
    
    public void openGUI(Player player, String currentSetting) {
        // Get GUI title from language file
        String title = languageManager.getGuiTitle("menu.settings.title");
        
        // Create inventory
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        // Create placeholders for items
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("setting", currentSetting);
        placeholders.put("player", player.getName());
        
        // Settings item
        ItemStack settingsItem = createGuiItem(
            Material.COMPARATOR,
            "menu.settings.item.toggle.name",
            "menu.settings.item.toggle.lore",
            placeholders
        );
        inv.setItem(13, settingsItem);
        
        // Close item
        ItemStack closeItem = createGuiItem(
            Material.BARRIER,
            "menu.settings.item.close.name",
            "menu.settings.item.close.lore",
            null
        );
        inv.setItem(26, closeItem);
        
        // Open inventory
        player.openInventory(inv);
    }
    
    private ItemStack createGuiItem(Material material, String nameKey, String loreKey, 
                                    Map<String, String> placeholders) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Get name from language file
            String name = languageManager.getGuiItemName(nameKey, placeholders);
            meta.setDisplayName(name);
            
            // Get lore from language file
            List<String> lore = languageManager.getGuiItemLoreAsList(loreKey, placeholders);
            meta.setLore(lore);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
```

## Example 4: Event Listener with Messages

```java
package com.example.myplugin.listeners;

import io.github.pluginlangcore.language.MessageService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class JoinQuitListener implements Listener {
    
    private final MessageService messageService;
    
    public JoinQuitListener(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Create placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("players_online", String.valueOf(player.getServer().getOnlinePlayers().size()));
        
        // Send join message to player (with title, subtitle, sound, etc.)
        messageService.sendMessage(player, "player_join", placeholders);
        
        // Broadcast to all players
        for (Player online : player.getServer().getOnlinePlayers()) {
            messageService.sendMessage(online, "player_join_broadcast", placeholders);
        }
        
        // Console log
        messageService.sendConsoleMessage("player_join_log", placeholders);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        
        messageService.sendConsoleMessage("player_quit_log", placeholders);
    }
}
```

## Example 5: Reload Command

```java
package com.example.myplugin.commands;

import com.example.myplugin.MyPlugin;
import io.github.pluginlangcore.language.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    
    private final MyPlugin plugin;
    private final MessageService messageService;
    
    public ReloadCommand(MyPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myplugin.reload")) {
            messageService.sendMessage(sender, "no_permission");
            return true;
        }
        
        try {
            // Reload config
            plugin.reloadConfig();
            
            // Reload language files
            plugin.getLanguageManager().reloadLanguages();
            
            // Clear message service cache
            messageService.clearKeyExistsCache();
            
            // Send success message
            messageService.sendMessage(sender, "reload_success");
            
        } catch (Exception e) {
            messageService.sendMessage(sender, "reload_failed");
            e.printStackTrace();
        }
        
        return true;
    }
}
```

## Example 6: Number Formatting

```java
package com.example.myplugin.utils;

import io.github.pluginlangcore.language.LanguageManager;

public class MoneyFormatter {
    
    private final LanguageManager languageManager;
    
    public MoneyFormatter(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }
    
    public String formatMoney(double amount) {
        // Uses locale-specific formatting from formatting.yml
        // 1000 -> 1K
        // 1000000 -> 1M
        // etc.
        return languageManager.formatNumber(amount);
    }
    
    public String formatWithCurrency(double amount) {
        String formatted = languageManager.formatNumber(amount);
        return "$" + formatted;
    }
}
```

## Example 7: Entity Name Display

```java
package com.example.myplugin.utils;

import io.github.pluginlangcore.language.LanguageManager;
import org.bukkit.entity.EntityType;

public class EntityDisplayUtil {
    
    private final LanguageManager languageManager;
    
    public EntityDisplayUtil(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }
    
    public String getEntityDisplayName(EntityType type) {
        // Gets formatted name from formatting.yml or creates a nice default
        // CAVE_SPIDER -> "Cave Spider" or custom translation
        return languageManager.getFormattedMobName(type);
    }
    
    public String getEntityWithSmallCaps(EntityType type) {
        String name = languageManager.getFormattedMobName(type);
        // Converts to small caps: "Cave Spider" -> "ᴄᴀᴠᴇ ꜱᴘɪᴅᴇʀ"
        return languageManager.getSmallCaps(name);
    }
}
```

## Example Language Files

### language/en_US/messages.yml
```yaml
prefix: "&7[&bMyPlugin&7] &r"

plugin_enabled:
  enabled: true
  message: "Plugin has been enabled!"

plugin_disabled:
  enabled: true
  message: "Plugin has been disabled!"

player_only:
  enabled: true
  message: "&cThis command can only be used by players!"

no_permission:
  enabled: true
  message: "&cYou don't have permission to do that!"

player_join:
  enabled: true
  message: "&aWelcome to the server, {player}!"
  title: "&aWelcome!"
  subtitle: "&7{player}"
  action_bar: "&a&lWelcome to the server!"
  sound: "ENTITY_PLAYER_LEVELUP"

player_join_broadcast:
  enabled: true
  message: "&a{player} has joined the server! &7({players_online} online)"

reward_received:
  enabled: true
  message: "&aYou received a reward of &e{amount} coins&a!"
  title: "&aReward!"
  subtitle: "&e+{amount} coins"
  sound: "ENTITY_EXPERIENCE_ORB_PICKUP"

reload_success:
  enabled: true
  message: "&aConfiguration reloaded successfully!"

reload_failed:
  enabled: true
  message: "&cFailed to reload configuration!"
```

### language/en_US/gui.yml
```yaml
menu:
  settings:
    title: "&8Settings Menu"
    item:
      toggle:
        name: "&eToggle Setting"
        lore:
          - "&7Current: &f{setting}"
          - ""
          - "&eClick to toggle"
      close:
        name: "&cClose"
        lore:
          - "&7Click to close this menu"
```

### language/en_US/formatting.yml
```yaml
format_number:
  thousand: "{s}K"
  million: "{s}M"
  billion: "{s}B"
  trillion: "{s}T"
  default: "{s}"

mob_names:
  ZOMBIE: "&2Zombie"
  SKELETON: "&7Skeleton"
  CREEPER: "&aCreeper"
  CAVE_SPIDER: "&cCave Spider"
```

## Example 8: Using LanguageUpdater for Automatic Updates

```java
package com.example.myplugin.updater;

import io.github.pluginlangcore.updater.LanguageUpdater;
import io.github.pluginlangcore.updater.LanguageUpdater.LanguageFileType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class LanguageUpdateExample {
    
    private final JavaPlugin plugin;
    
    public LanguageUpdateExample(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Example 1: Update all file types for all languages
     */
    public void updateAllLanguages() {
        // This will check and update messages.yml, gui.yml, formatting.yml, and items.yml
        // for all specified languages
        new LanguageUpdater(this.plugin, Arrays.asList("en_US", "vi_VN", "de_DE", "es_ES"));
    }
    
    /**
     * Example 2: Update only specific file types
     */
    public void updateSpecificFileTypes() {
        // Only update messages.yml and gui.yml
        new LanguageUpdater(
            this.plugin,
            Arrays.asList("en_US", "vi_VN"),
            LanguageFileType.MESSAGES,
            LanguageFileType.GUI
        );
    }
    
    /**
     * Example 3: Conditional update based on configuration
     */
    public void conditionalUpdate() {
        // Check if auto-update is enabled in config
        if (plugin.getConfig().getBoolean("language.auto-update", true)) {
            List<String> enabledLanguages = plugin.getConfig().getStringList("language.enabled-locales");
            
            if (enabledLanguages.isEmpty()) {
                // Default to English if none specified
                enabledLanguages = Arrays.asList("en_US");
            }
            
            new LanguageUpdater(this.plugin, enabledLanguages);
            plugin.getLogger().info("Language files updated for: " + String.join(", ", enabledLanguages));
        }
    }
    
    /**
     * Example 4: Update on reload command
     */
    public void handleReloadCommand() {
        try {
            // Force re-check and update language files
            LanguageUpdater updater = new LanguageUpdater(
                this.plugin,
                Arrays.asList("en_US", "vi_VN")
            );
            
            plugin.getLogger().info("Language files have been checked and updated if necessary.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update language files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### How LanguageUpdater Works

1. **Version Tracking**: Each language file gets a `language_version` key
2. **Version Comparison**: Compares file version with plugin version
3. **Backup Creation**: Creates backup only if meaningful changes detected
4. **User Preservation**: Keeps all user customizations
5. **Key Merging**: Adds new keys from defaults while keeping user values

### Example Language File After Update

```yaml
# Language file version - Do not modify this value
language_version: 1.5.7

prefix: "&7[&aMyPlugin&7] &r"

welcome:
  enabled: true
  message: "Welcome {player}!"  # User's custom value preserved
  
# New key added by updater
new_feature:
  enabled: true
  message: "Check out our new feature!"  # Added from defaults
```

### Config.yml Setup for Language Updater

```yaml
language:
  # Enable automatic language file updates
  auto-update: true
  
  # List of language locales to maintain
  enabled-locales:
    - "en_US"
    - "vi_VN"
    - "de_DE"
```

## Tips

1. **Always provide default language files** in your plugin's resources
2. **Use consistent key naming** (e.g., `category.subcategory.item`)
3. **Cache LanguageManager and MessageService** instances
4. **Clear caches** when reloading
5. **Use placeholders** instead of string concatenation
6. **Test with different locales** to ensure proper fallback behavior
7. **Run LanguageUpdater before LanguageManager** for clean initialization
8. **Include language_version in your default files** (updater will add if missing)