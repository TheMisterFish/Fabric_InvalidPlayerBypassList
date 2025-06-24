# InvalidPlayerBypassList

## Installing

Download or build the `.jar` file and place it in the `/mods/` folder of your **Minecraft Fabric server** directory.

> ⚠️ This mod is **server-side only** and will not run on the client.

## Dependencies and Compatibility

This mod is built for **Minecraft version 1.21** and uses the following dependencies:

* Fabric Loader `0.16.14` or higher
* Fabric API `0.102.0+1.21` or higher

## Versions

* **Current Version (1.21)**: [Download v1.0.0-mc.1.21](https://github.com/TheMisterFish/InvalidPlayerBypassList/releases/tag/v1.0.0-mc.1.21)

    * Available on Modrinth: [InvalidPlayerBypassList - Modrinth](https://modrinth.com/mod/invalidplayerbypasslist)

## Download

You can download this mod from the [GitHub Releases](https://github.com/TheMisterFish/InvalidPlayerBypassList/releases) or [Modrinth](https://modrinth.com/mod/invalidplayerbypasslist).

## Goal

**InvalidPlayerBypassList** allows specific players to join a Minecraft server even when the server is in **online mode**, regardless of Mojang authentication.
This is particularly useful for private testing, LAN setups, or fallback scenarios where certain usernames (and optionally IPs) should be allowed to bypass authentication checks.

This mod works **only** if the server is in `online-mode=true`, and it will intercept login attempts and allow access if the player's name and IP match entries in a configurable bypass list.

## How It Works

When a player tries to join the server, the mod checks if they are present in the bypass list. This list is defined by pairs of:

* **Username**
* **Optional IP address**

If IP enforcement is enabled via config, the player must match both the name and IP. Otherwise, just the name is sufficient if `"none"` is specified.

## Commands

You can manage the bypass list directly using Minecraft server commands:

### `/bypasslist add <player> [ip]`

* Adds a player (with optional IP) to the bypass list.
* If IP checking is enabled, the IP must be provided.

### `/bypasslist remove <player> [ip]`

* Removes a specific player from the bypass list.
* If the IP is not provided, all entries for the player will be removed.

### `/bypasslist list`

* Lists all current entries in the bypass list with their associated IPs.

### `/bypasslist on`

* Enables the bypass list at runtime.

### `/bypasslist off`

* Disables the bypass list at runtime.

> All commands require a permission level of `2` or higher (e.g., server operators).

## Config

Configuration can be found under `/config/invalidplayerbypasslist.properties` and includes the following settings:

| Property             | Description                                                                | Type    | Default |
| -------------------- | -------------------------------------------------------------------------- | ------- | ------- |
| `ip-required`        | If true, players must match both username and IP to bypass authentication. | boolean | true    |
| `enforce-bypasslist` | If true, the bypass list is active on server startup.                      | boolean | true    |

## Use Cases

This mod is ideal for:

* Whitelisted development environments
* Testing servers with known usernames/IPs
* Offline access fallback while still in `online-mode`
* Controlled security bypass for known players

## Reporting Issues

If you find a bug or want to request a feature:

1. Go to the [Issues](https://github.com/TheMisterFish/InvalidPlayerBypassList/issues) tab of the repository.
2. Click **New Issue**.
3. Use the appropriate template (Bug Report or Feature Request).
4. Fill in all relevant details.
5. Submit the issue.

## Setup

For mod development or building from source, please refer to the [Fabric wiki setup guide](https://fabricmc.net/wiki/tutorial:setup) for your IDE (IntelliJ, Eclipse, or VSCode).

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it into your own projects.