# RoleAuth - Advanced Minecraft BungeeCord Authentication System

![License](https://img.shields.io/badge/License-MIT-green)

## Overview

RoleAuth is a powerful authentication plugin for BungeeCord Minecraft servers that offers secure user authentication with premium account verification using Mojang API. The plugin provides an extensive set of commands for both users and administrators, with data securely stored in an SQLite database.

## Features

- **Dual Authentication Mode**: Support for both premium (Mojang) and cracked (non-premium) accounts
- **Premium Account Verification**: Automatic validation through Mojang API
- **Secure Password Storage**: Uses industry-standard encryption algorithms (Argon2 or BCrypt)
- **SQLite Database**: Custom API implementation with automatic library management
- **Comprehensive Command Set**: For both users and administrators
- **Permission-Based Access**: Granular control over plugin functionality

## Commands

### User Commands

| Command | Description |
|---------|-------------|
| `/login <password>` | Login to your account |
| `/register <password> <password>` | Register a new account |
| `/changepassword <old> <new>` | Change your password |
| `/premium` | Enable premium auto-login |
| `/cracked` | Disable premium auto-login |
| `/roleauth status` | Show your account status |
| `/roleauth version` | Show plugin version |
| `/roleauth reload` | Reload configuration (requires permission) |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/roleauthadmin forcelogin <player>` | Force login a player |
| `/roleauthadmin forceregister <player> <password>` | Force register a player |
| `/roleauthadmin reset <player>` | Reset player credentials |
| `/roleauthadmin setpremium <player>` | Set player as premium |
| `/roleauthadmin setcracked <player>` | Set player as cracked |
| `/roleauthadmin info <player>` | Show player account info |

## Installation

1. Download the latest version of RoleAuth from the releases page
2. Place the JAR file in your BungeeCord plugins folder
3. Restart your BungeeCord server
4. Configure the plugin by editing the config.yml file in the plugins/RoleAuth directory

## Configuration

The plugin creates a default configuration that can be adjusted to your needs. The SQLite database will be automatically generated on first run.

## Upcoming Features

- Improved event listeners
- Velocity Support
- MultiModule Plugin
- Custom API
- Additional commands and events
- Support for multiple database types
- Enhanced data encryption options beyond Argon2 and BCrypt
- Improved premium user verification system
- Overall plugin optimization
- Optimized workload/thread system for premium and cracked user tasks
- Enhanced permission system with direct configuration file editing

## Dependencies

- BungeeCord
- SQLite (automatically included)

## Support

If you encounter any issues or have suggestions for improvements, please open an issue on the GitHub repository.

## License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.
