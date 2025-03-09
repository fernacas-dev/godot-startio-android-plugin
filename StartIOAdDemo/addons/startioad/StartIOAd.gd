# StartIOAd.gd
extends Node

# Signals
signal banner_loaded
signal banner_failed(error)
signal interstitial_loaded
signal interstitial_failed(error)
signal interstitial_closed
signal reward_granted
signal reward_failed(error)

var _plugin = null
var _configured = false
var _config = {
	"app_id": "",
	"test_mode": false
}

# Singleton implementation
var instance = null

func _init():
	instance = self
	name = "StartIOAd"  # Important for autoload

func get_instance():
	return instance

# Public API
func configure(app_id, test_mode = true):
	if not _configured:
		push_error("StartIOAd: Plugin not initialized")
		return
	
	_config.app_id = app_id
	_config.test_mode = test_mode
	if _plugin:
		_plugin.call("configureAds", app_id, test_mode)
	print("StartIOAd: Configured with App ID: ", app_id)

func show_banner(position = "bottom"):
	if _plugin:
		_plugin.call("showBannerAd", position)
		print("StartIOAd: Showing banner at: ", position)

func hide_banner():
	if _plugin:
		_plugin.call("hideBannerAd")
		print("StartIOAd: Hiding banner")

func load_interstitial():
	if _plugin:
		_plugin.call("loadInterstitialAd")
		print("StartIOAd: Loading interstitial")

func show_interstitial():
	if _plugin:
		_plugin.call("showInterstitialAd")
		print("StartIOAd: Showing interstitial")

func show_rewarded():
	if _plugin:
		_plugin.call("showRewardedAd")
		print("StartIOAd: Showing rewarded ad")

func is_ready():
	return _configured

func _ready():
	if OS.get_name() == "Android" and Engine.has_singleton("StartIOAd"):
		_initialize_plugin()
	else:
		push_warning("StartIOAd: Android only plugin")
		_disable_ads()
	
	pause_mode = Node.PAUSE_MODE_PROCESS  # Persist between scenes

func _initialize_plugin():
	_plugin = Engine.get_singleton("StartIOAd")
	if _plugin:
		_connect_signals()
		_configured = true
		print("StartIOAd: Native plugin initialized")
	else:
		push_error("StartIOAd: Failed to initialize native plugin")

func _connect_signals():
	_plugin.connect("onBannerAdLoaded", self, "_on_banner_loaded")
	_plugin.connect("onBannerAdFailed", self, "_on_banner_failed")
	_plugin.connect("onInterstitialAdLoaded", self, "_on_interstitial_loaded")
	_plugin.connect("onInterstitialAdFailed", self, "_on_interstitial_failed")
	_plugin.connect("onInterstitialAdClosed", self, "_on_interstitial_closed")
	_plugin.connect("onRewardedAdCompleted", self, "_on_reward_completed")
	_plugin.connect("onRewardedAdFailed", self, "_on_reward_failed")

func _disable_ads():
	# Call this in platform-specific error handling
	pass

# Signal handlers
func _on_banner_loaded():
	emit_signal("banner_loaded")

func _on_banner_failed(error):
	emit_signal("banner_failed", error)

func _on_interstitial_loaded():
	emit_signal("interstitial_loaded")

func _on_interstitial_failed(error):
	emit_signal("interstitial_failed", error)

func _on_interstitial_closed():
	emit_signal("interstitial_closed")

func _on_reward_completed():
	emit_signal("reward_granted")

func _on_reward_failed(error):
	emit_signal("reward_failed", error)
