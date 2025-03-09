extends Node

onready var status_label = $CanvasLayer/StatusLabel

const APP_ID = "..." # Your APP_ID
const IS_DEVELOPMENT_MODE = true

# Singleton reference
var start_io_ad = null

func _ready():
	# Get singleton instance
	start_io_ad = StartIOAd.get_instance()
	
	if start_io_ad and start_io_ad.is_ready():
		initialize_ad_plugin()
	else:
		handle_unsupported_platform()

func initialize_ad_plugin():
	start_io_ad.configure(APP_ID, IS_DEVELOPMENT_MODE)
	connect_plugin_signals()
	update_status("Plugin initialized\nApp ID: %s" % APP_ID)
	
	# Preload initial ads
	if start_io_ad.is_ready():
		start_io_ad.load_interstitial()

func connect_plugin_signals():
	if start_io_ad:
		start_io_ad.connect("banner_loaded", self, "_on_banner_loaded")
		start_io_ad.connect("banner_failed", self, "_on_banner_failed")
		start_io_ad.connect("interstitial_loaded", self, "_on_interstitial_loaded")
		start_io_ad.connect("interstitial_failed", self, "_on_interstitial_failed")
		start_io_ad.connect("interstitial_closed", self, "_on_interstitial_closed")
		start_io_ad.connect("reward_granted", self, "_on_reward_completed")
		start_io_ad.connect("reward_failed", self, "_on_reward_failed")

# UI Handler Methods
func _on_show_banner_pressed():
	if start_io_ad and start_io_ad.is_ready():
		update_status("Loading banner...")
		start_io_ad.show_banner("bottom")

func _on_hide_banner_pressed():
	if start_io_ad and start_io_ad.is_ready():
		start_io_ad.hide_banner()
		update_status("Banner hidden")

func _on_show_interstitial_pressed():
	if start_io_ad and start_io_ad.is_ready():
		update_status("Requesting interstitial...")
		start_io_ad.show_interstitial()

func _on_show_rewarded_pressed():
	if start_io_ad and start_io_ad.is_ready():
		update_status("Loading rewarded ad...")
		start_io_ad.show_rewarded()

# Signal Handlers
func _on_banner_loaded():
	update_status("Banner loaded ✓")

func _on_banner_failed(error):
	update_status("Banner error: " + error)
	handle_ad_failure(error)

func _on_interstitial_loaded():
	update_status("Interstitial ready ✓")

func _on_interstitial_failed(error):
	update_status("Interstitial error: " + error)
	handle_ad_failure(error)

func _on_interstitial_closed():
	update_status("Interstitial closed")
	if start_io_ad and start_io_ad.is_ready():
		start_io_ad.load_interstitial()

func _on_reward_completed():
	update_status("Reward granted!")
	# Add your reward logic here
	# Example: PlayerData.add_coins(50)

func _on_reward_failed(error):
	update_status("Reward error: " + error)
	handle_ad_failure(error)

# Helper Methods
func update_status(message: String):
	if status_label:
		status_label.text = message
	print("[STATUS] ", message)

func handle_unsupported_platform():
	update_status("Android only feature\nCheck export settings")
	disable_ad_buttons(true)

func handle_ad_failure(error: String):
	if start_io_ad and start_io_ad.is_ready():
		if "Interstitial" in error:
			update_status("Reloading interstitial...")
			start_io_ad.load_interstitial()
		elif "Banner" in error:
			update_status("Retrying banner...")
			start_io_ad.show_banner("bottom")
		else:
			update_status("Reinitializing plugin...")
			start_io_ad.configure(APP_ID, IS_DEVELOPMENT_MODE)

func disable_ad_buttons(disabled: bool):
	var button_names = [
		"ShowBanner",
		"HideBanner",
		"ShowInterstitial",
		"ShowRewarded"
	]
	
	for btn_name in button_names:
		var btn = get_node("/CanvasLayer/" + btn_name)
		if btn:
			btn.disabled = disabled
			btn.visible = not disabled
