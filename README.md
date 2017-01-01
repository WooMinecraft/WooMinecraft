# WooMinecraft - Self-hosted Minecraft Donations

[WooMinecraft](http://woominecraft.com) is a free Minecraft Donation plugin for your server that provides a self-hosted solution where you're the boss.  By leveraging a well known eCommerce plugin on the
WordPress side of things, we allow you to specify commands for product variations, general product commands, and resending of donations at any time.   
![WooMinecraft Logo](https://raw.githubusercontent.com/WooMinecraft/WooMinecraft/dev-nightly/src/main/resources/wmc-logo.jpg)

## Upgrade Notice
Woominecraft 1.0.6 is ONLY compatible with v1.0.5+ of the WordPress plugin. You MUST be using that version or higher, or it will not work.  

## Supported Bukkit Versions
* 1.7
* 1.8
* 1.9
* 1.10
* 1.11

## Config
Your config should look like the below section.
```
# Set this to the desired language file you wish to load.
#
# If your l10n is not available but one is that you know how to speak,consider
# contributing to this plugin at https://github.com/WooMinecraft/WooMinecraft/
lang: "en"

# This is how often, in seconds, the server will contact your WordPress installation
# to see if there are donations that need made.
update_interval: 1500

# You must set this to your WordPress site URL.  If you installed WordPress in a
# subdirectory, it should point there.
url: "http://example.com"

# This must match the WordPress key in your admin panel for WooMinecraft
# For security purposes, you MUST NOT leave this empty.
key: ""

# Allowed words the player need to be on to run the commands.
# Disabled by default!
# whitelist-worlds:
#  - world

# Set to true in order to toggle debug information
debug: false
```

## How does it work?
This bridges the gap between PHP, and Java by leveraging both the bukkit/spigot API ( java ) and the WordPress API with WooCommerce support ( php ). It stores commands
per order, per player, per command ( yes you read that right ) in the WordPress database.  This plugin, either when an op requests it, or on a timer, sends a request to
the WordPress server to check for donations for all online players.

If online players have commands waiting to be processed, then all necessary commands are ran.  There is NO LIMIT to the type of commands you can set, `give`, `i`, `tp`, etc... all commands are ran
by the console sender, and not a player.

## Contributions

As with all Github projects, we encourage our users to contribute, even if it's just a small as [opening an issue](https://github.com/WooMinecraft/WooMinecraft).  Every little bit helps, especially with pre-releases
that may be unstable.

Please review the [Contributors Guidelines](https://github.com/WooMinecraft/WooMinecraft/blob/master/CONTRIBUTING.md) for the best way to contribute. If you'd like to see our list of contributors, [check that out on github here](https://github.com/WooMinecraft/WooMinecraft/graphs/contributors).

## Mojang Guidelines
Since this plugin is GPL and entirely opensource, we cannot be sure how you will use this. However, when providing 'donation' options, you are still considered a 
`commercial` entity and therefore are bound to the [Mojang Commercial Usage Guidelines](https://account.mojang.com/terms#commercial)

### WordPress Plugin
You'll need the WordPress plugin for this MC Plugin to work - you can [get it here](https://github.com/WooMinecraft/woominecraft-wp).

## Changelog

### 1.1.0
* Added - Redirect Exceptions for sending/receiving data from the server. You will now get an exception if your host is redirecting the requests in most cases.
* Added - Debug logging specifically for HTTP requests. Just set `debug: true` in your config.
* Added - Exception handling for sending order updates to server. Will now throw exceptions if plugin receives invalid data.
* Added - World white-listing, props [FabioZumbi12](https://github.com/WooMinecraft/WooMinecraft/pull/117) - disabled by default
* Added - Clarification around server key, props [spannerman79](https://github.com/WooMinecraft/WooMinecraft/pull/119)
* Updated - HTTP Requests now use `CloseableHttpClient` and `CloseableHttpResponse` so connections will now close, not sure if they weren't before.

### 1.0.10
* Updated public suffix list, required by HTTP client

### 1.0.9
* Change key sent to server, fixes WooCommerce compatibility.

### 1.0.8
* Better error handling from WordPress
* Make use of the `debug: true` flag in the config.
* Code cleanup, removed unused libs, removed commented code.
* Added config validation for users coming from older versions - will now throw exceptions if your config is not setup properly and will stop the check.
* Fixed player online check, props [@FailPlayDE](https://github.com/FailPlayDE) - [#108](https://github.com/WooMinecraft/WooMinecraft/pull/108)
* **REMOVED** Reload & Register commands - more problems than their worth.

### 1.0.7
* Prints stacktraces on JSON error to log.
* Updated Readme.md file to reflect supported bukkit versions.

### 1.0.6
* Refactored all HTTP connections to work on a single thread
* Cleaned up a TON of code
* Removed internal JSON library, used maven deps instead
* Fixed [#88](https://github.com/WooMinecraft/WooMinecraft/issues/88), [#85](https://github.com/WooMinecraft/WooMinecraft/issues/85), [#48](https://github.com/WooMinecraft/WooMinecraft/issues/48), [#60](https://github.com/WooMinecraft/WooMinecraft/issues/60)

### 1.0.5
* Added debug option for more straight forward debug options.

### 1.0.4
* Too much to detail

### 1.0.0
* First official release