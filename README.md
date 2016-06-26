# WooMinecraft - Self-hosted Minecraft Donations

WooMinecraft is a bukkit/spigot plugin for your Minecraft server that provides a self-hosted donation solution.  By leveraging a well known eCommerce plugin on the
WordPress side of things, we allow you to specify commands for product variations, general product commands, and resending of donations at any time.   
![WooMinecraft Logo](https://raw.githubusercontent.com/WooMinecraft/WooMinecraft/dev-nightly/src/main/resources/wmc-logo.jpg)

## Upgrade Notice
Woominecraft 1.0.6 is ONLY compatible with v1.0.5+ of the WordPress plugin. You MUST be using that version or higher, or it will not work.  

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

## Current Bukkit Build

Currently supports Bukkit/Spigot 1.8.x

## New Tasks
- [x] Update to Spigot/Bukkit 1.8.x , #1
- [ ] Add Donation Signs , #2
- [ ] Add Donation GUI's , #3
- [x] Reorganize Plugin , #4
- [ ] Create Auto-Updater , #5
- [ ] Create Metrics , #6
- [x] Fully Test and Make Sure it works
- [x] Check if Below Tasks Were Completed , #8

## Tasks
- [x] Create Wiki , #1
- [x] Wiki: Bukkit Installation & Config , #2
