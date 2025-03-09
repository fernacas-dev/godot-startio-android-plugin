
```markdown
# Godot Start.io Ads Integration

![Godot Version](https://img.shields.io/badge/Godot-3.6-%23478cbf)
[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Professional Start.io ads implementation for Godot 3.6 with proper Android plugin structure.

## Project Structure

your_project/
├── addons/
│   └── startioad/
│       └── StartIOAd.gd        # Singleton script
├── android/
│   └── plugins/
│       └── StartIOAd/          # Android plugin files
│           ├── StartIOAd.aar
│           └── StartIOAd.gdap
├── scenes/
├── scripts/
└── project.godot


## Installation

### 1. Install Plugin Files

1. Copy the `release/addons/*` folder to your project's `addons/*` directory.
2. Copy the `release/android/plugins/*` folder to your project's root `android/plugins/*` directory.

### 2. Enable Android Plugin

1. Go to **Project → Export → Android**.
2. In the **Plugins** section, enable "StartIOAd".
3. Verify the plugin configuration:

# android/plugins/StartIOAd.gdap
[config]
name="StartIOAd"
binary_type="local"
binary="StartIOAd.aar"

[dependencies]
remote=["com.startapp:inapp-sdk:5.1.0"]


### 3. Configure AutoLoad

1. Go to **Project Settings → AutoLoad**.
2. Add `addons/startioad/StartIOAd.gd`.
3. Set the name to `StartIOAd`.

## Android Configuration

### Required Permissions

```xml
<!-- android/build/AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

### Gradle Setup

```groovy
// android/build.gradle
android {
    compileSdkVersion 33
    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 33
    }
}
```

## Usage Example

```gdscript
# Initialize plugin
func _ready():
    if StartIOAd.is_ready():
        StartIOAd.configure("YOUR_APP_ID", true) # Test mode
        StartIOAd.load_interstitial()

# Show banner
func _on_show_banner_pressed():
    StartIOAd.show_banner("bottom")

# Handle rewards
func _on_reward_granted():
    Global.player_coins += 50
    update_ui()
```


## Key Features

- ✅ **Proper Android Plugin Structure**
- ✅ **Singleton Interface**
- ✅ **Automatic Ad Recycling**
- ✅ **Detailed Error Reporting**

## Troubleshooting


| Issue                | Solution                                              |
|----------------------|-------------------------------------------------------|
| Plugin not detected  | Verify that the `.gdap` file is in `android/plugins/` |
| Missing dependencies | Check Gradle sync in Android Studio                   |
| Class not registered | Perform a clean rebuild of the Android export         |
| Signal errors        | Confirm the AutoLoad initialization order             |

## License

MIT License - See [LICENSE](LICENSE) for details.  
Contains Start.io SDK ([Terms of Use](https://www.start.io/policies/terms-of-use/)).

---

[View Documentation](https://support.start.io/) | [Report Issues](https://github.com/fernacas-dev/godot-startio-android-plugin/issues) | [Example Project](StartIOAdDemo/)
```