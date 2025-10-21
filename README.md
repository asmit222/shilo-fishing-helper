# 🎣 Shilo Fishing Helper

A RuneLite plugin that enhances your AFK fishing experience in **Shilo Village**.  
It provides a visual **path overlay**, **idle detection screen tint**, and an optional **path to the deposit box** when your inventory is full — making repetitive fishing more relaxing and efficient.

---

## 🧭 Features

### ✅ Pathing Overlay
- Draws a smooth **cyan path** from your player to the nearest fishing spot.
- Avoids obstacles and blocked tiles automatically.
- Color and opacity are fully customizable in the config panel.

### 🟡 Deposit Box Path
- When your **inventory is full**, the plugin automatically shows a **gold path** leading to the Shilo Village **deposit box** (world location: `2852, 2952, 0`).
- The deposit box is highlighted while your inventory is full.
- Can be toggled on/off in the config menu.

### 🔴 Idle Screen Overlay
- When you stop fishing or become idle, your screen is tinted with a semi-transparent red overlay.
- The idle overlay color is customizable.
- Helps prevent AFK logout and missed catches.

### 🧮 Inventory Counter
- Displays how many **free inventory slots** you have left, shown next to your character.

---

## ⚙️ Configuration Options

| Setting | Description | Default |
|----------|--------------|----------|
| **Show Fishing Path** | Toggles the main cyan fishing path overlay | ✅ On |
| **Path Color** | Custom color (with opacity) for the path tiles | Cyan |
| **Show Path to Deposit Box** | Draws a path to the deposit box when inventory is full | ✅ On |
| **Show Free Inventory Count** | Displays free inventory slots above player | ✅ On |
| **Show Color Overlay When Idle** | Turns screen red when idle | ✅ On |
| **Idle Screen Color** | Custom color for idle overlay | Red |

---

## 📍 Location

This plugin is designed **specifically for Shilo Village**.  
It detects fishing spots automatically and generates valid walking paths that respect the river and other impassable tiles.

---

## 💡 Tips

- The path color includes its opacity, so there’s no separate transparency slider.
- If you don’t see the deposit box path, ensure:
  - You have “Show Path to Deposit Box” enabled in the config.
  - Your inventory is completely full.
- Reset the plugin’s configuration if new defaults (like enabled toggles) don’t appear after an update.

---

## 🧑‍💻 Author

**Author:** [AustinFroob] 
**Display Name:** *Shilo Fishing Helper*  
**Description:** Provides pathing, idle overlay, and QoL tools for AFK fishing in Shilo Village.  



