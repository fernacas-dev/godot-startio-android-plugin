```markdown
# Godot Start.io Ads Integration

![Godot 3.6](https://img.shields.io/badge/Godot-3.6-%23478cbf) 
![Android](https://img.shields.io/badge/Platform-Android-green)
![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)

Complete solution for integrating Start.io ads in Godot 3.6 Android games, featuring:

1. **StartIOAd Singleton** - GDScript interface for ads management
2. **Android Plugin** - Native bridge to Start.io SDK

## Features

- 🎯 **Full Ad Support**
  - Banner Ads (Top/Bottom positioning)
  - Interstitial Ads with auto-reload
  - Rewarded Video Ads
- 🔧 **Advanced Configuration**
  - Test mode support
  - Error recovery system
  - Cross-scene persistence
- 📡 **Real-time Callbacks**
  - 7 detailed signals for ad events
  - Comprehensive error reporting

## Installation

### 1. Singleton Script
1. Copy `addons/startioad/StartIOAd.gd` to your project's `addons/` folder
2. Enable in **Project Settings → AutoLoad**:
   - Path: `res://addons/startioad/StartIOAd.gd`
   - Name: `StartIOAd`

### 2. Android Plugin
1. Add to `android/plugins/StartIOAd/`:
   - `StartIOAd.aar` (native library)
   - `StartIOAd.gdap` (plugin config)
2. Configure Android export:
   ```ini
   [config]
   name="StartIOAd"
   binary_type="android"
   binary="android/plugins/StartIOAd/StartIOAd.aar"

   [dependencies]
   remote=["com.startapp:inapp-sdk:5.1.0"]
   ```

## Configuration

### Android Manifest
Add these permissions in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

### Build Configuration
Update `build.gradle`:
```groovy
android {
    compileSdkVersion 33
    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 33
    }
}
```

## Usage

### Initialization
```gdscript
func _ready():
    if StartIOAd.get_instance().is_ready():
        StartIOAd.get_instance().configure("YOUR_APP_ID", true) # Test mode
        StartIOAd.get_instance().load_interstitial()
```

### Basic Operations
```gdscript
# Show banner
StartIOAd.get_instance().show_banner("bottom")

# Show interstitial
if StartIOAd.get_instance().is_interstitial_loaded():
    StartIOAd.get_instance().show_interstitial()

# Show rewarded ad
StartIOAd.get_instance().show_rewarded()
```

### Signal Handling
```gdscript
func _on_reward_granted():
    print("Reward granted!")
    Global.player_coins += 50

func _connect_signals():
    StartIOAd.get_instance().connect("reward_granted", self, "_on_reward_granted")
```

## Android Specific Setup

### Requirements
- Minimum SDK: 21 (Android 5.0 Lollipop)
- Target SDK: 33 (Android 13)
- Start.io Developer Account

### Test Mode
Always enable during development:
```gdscript
StartIOAd.get_instance().configure("TEST_APP_ID", true)
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Ads not loading | Check internet connection & app ID |
| `Plugin not initialized` | Verify AutoLoad configuration |
| Black screen after ad | Ensure `pause_mode` is set correctly |
| Reward not granted | Implement signal listeners properly |
| Android build errors | Verify gradle dependencies |

## License

MIT License - See [LICENSE](LICENSE) file for details.  
**Note:** Start.io SDK has its own license terms - [Review Here](https://www.start.io/policies/terms-of-use/)

---

📚 [Start.io Documentation](https://support.start.io/) | 
🐛 [Report Issues](https://github.com/fernacas-dev/godot-startio-android-plugin/issues) | 
📦 [Example Project](StartIOAdDemo/)
```