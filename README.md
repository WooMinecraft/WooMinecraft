# WooMinecraft - Self-hosted Minecraft Donations

[WooMinecraft](http://woominecraft.com) is a free Minecraft Donation plugin for your server that provides a self-hosted solution where you're the boss.  By leveraging a well known eCommerce plugin on the
WordPress side of things, we allow you to specify commands for product variations, general product commands, and resending of donations at any time.   
![WooMinecraft Logo](https://raw.githubusercontent.com/WooMinecraft/WooMinecraft/master/src/main/resources/wmc-logo.jpg)

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
url: "http://playground.dev"

# If you are having issues with REST, or have disabled pretty permalinks. Set this to false.
# Doing so will use the old /index.php?rest_route=/wmc/v1/server/ base
# Setting this to false will also allow you to set the restBasePath value if you have altered your
# installation in any way.
prettyPermalinks: true

# If your REST API has a custom path base, input it here. 
# NOTE: This is only loaded if prettyPermalinks is set to false.
# Known good URL bases.
# - /wp-json/wmc/v1/server/
# - /index.php?rest_route=/wmc/v1/server/
restBasePath: ""

# This must match the WordPress key in your admin panel for WooMinecraft
# This is a key that YOU set, both needing to be identical in the admin panel
# and in this config file
# For security purposes, you MUST NOT leave this empty.
key: ""

# Allowed worlds the player needs to be in to run the commands.
# Disabled by default!
#whitelist-worlds:
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

## 1.4.3
* Fix RestApi issues
* Code cleanup
* Update `/woo ping`, now has a sub command/option to ping any website
`example: /woo ping https://www.google.com/`

## 1.4.2
* Updates the helpsite command.
* Finishes the `/woo ping` command.
* Closed #238 - thanks @YouHaveTrouble

## 1.4.1-pre
* Fixes an issue with UUIDs on server-side authentication ( @jerzan )
* Adds some additional command logic and cleans things up.  ( @jerzan )

## 1.4.0-pre
* edit RestApi Url to fix alot of user issues
* extend compatibility for mc versions 1.10 - 1.18.1
* added debug command( /woo debug ) and a ping command ( /woo ping)
* added mojang auth to the plugin side, [jerzean](https://github.com/WooMinecraft/WooMinecraft/pull/256)

### 1.3.0
* Added polish translation props [YouHaveTrouble](https://github.com/WooMinecraft/WooMinecraft/pull/233)
* 1.16.x support props [jerzean](https://github.com/WooMinecraft/WooMinecraft/pull/237)
* Cleanup various readme sections.

### 1.2.1
* Disabled legacy support, "should still work on 1.12 and below(untested)"
* Update spigot api to 1.14.4+

### 1.2.0
* Tested on Spigot 1.12.2
* Cleaned up a lot of internals by simplifying a ton of requests.
* Now building with okHTTP3 library for ease of use.
* Move to REST API.
