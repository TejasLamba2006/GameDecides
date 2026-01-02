# GameDecides

Random game-changing twists that keep your gameplay exciting! ⚡

## What is this?

I discovered this awesome plugin that adds random "twists" to your game! Every few minutes, the game throws a curveball at everyone - maybe you'll get triple loot drops, or everyone becomes invisible, or perhaps TNT starts raining from the sky! 

It's like playing Minecraft with a chaotic game master who keeps things interesting!

## Features

- **45+ Unique Twists** - From movement changes to combat modifiers to pure chaos!
- **Fully Configurable** - Enable/disable any twist, change timing, customize messages
- **1.21 Themed Twists** - Breeze attacks, vault loot, mace mechanics!
- **World Blacklist** - Disable twists in specific worlds
- **Bypass Permission** - Let certain players avoid twist effects
- **Sound & Title Support** - Dramatic announcements when twists change

## Twist Categories

### 🎮 Classic Twists
- Dream Luck, Chad Gear, Ghost Mode, Bird Mode, and more!

### 🏃 Movement Twists  
- Low Gravity, Speed Demon, Bouncy World, Dolphin Grace

### ⚔️ Combat Twists
- Glass Cannon, Vampiric, One Tap, Explosive Arrows, Fire Touch

### 🌀 1.21 Themed
- Breeze Attack, Vault Hunter, Mace Mayhem, Ominous Aura

### 💥 Chaos Twists
- Inventory Swap, Position Shuffle, Random Teleport, Hot Potato

### 🌍 Environmental
- Eternal Night, Rain of Fire, Lightning Storm, Mob Mayhem

### 🎭 Fun Twists
- Giant Mode, Baby Mode, Levitation, Freeze

## Installation

1. Download the plugin
2. Drop it in your `plugins` folder
3. Restart your server
4. Use `/gamedecides start` to begin!

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/gd start` | Start the twists | `gamedecides.admin` |
| `/gd stop` | Stop the twists | `gamedecides.admin` |
| `/gd skip` | Skip to next twist | `gamedecides.admin` |
| `/gd list` | List all twists | `gamedecides.admin` |
| `/gd info` | Show current status | `gamedecides.admin` |
| `/gd reload` | Reload config | `gamedecides.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `gamedecides.admin` | Access admin commands | OP |
| `gamedecides.bypass` | Bypass twist effects | false |

## Configuration Highlights

```yaml
# Time between twists (in seconds)
twist-interval: 300

# Enable/disable specific twists
twists:
  DREAM_LUCK:
    enabled: true
    name: "Dream Luck"
    description: "All mob drops are tripled!"
```

## Support

Need help? Join the Discord: https://discord.gg/7fQPG4Grwt

## Requirements

- Minecraft 1.21+
- Paper or Spigot
- Java 21+

## License

MIT License - Feel free to modify and share!
