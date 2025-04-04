<html>
<body>
<!--StartFragment--><html><head></head><body><h1>RoleAuth 1.1-Beta - Advanced Minecraft BungeeCord Authentication System</h1>
<p><img src="https://img.shields.io/badge/License-Apache2.0-green" alt="License"></p>
<h2>Overview</h2>
<p>RoleAuth is a powerful authentication plugin for BungeeCord Minecraft servers that offers secure user authentication with premium account verification using Mojang API. The plugin provides an extensive set of commands for both users and administrators, with data securely stored in an SQLite database.</p>
<h2>What's New in Version 1.1-Beta</h2>
<ul>
<li><strong>Code Optimization</strong>: Complete code restructuring with improved package organization</li>
<li><strong>Multi-Module Support</strong>: New modular architecture for better feature management</li>
<li><strong>Spigot Support</strong>: Added Spigot support to handle block move listeners (required)</li>
<li><strong>Improved Encryption System</strong>: Now properly supports BCrypt and Argon2 (previous bugs fixed)</li>
<li><strong>Enhanced Performance</strong>: General plugin optimization</li>
</ul>
<h2>Coming Soon</h2>
<ul>
<li>Velocity Support</li>
<li>Custom API</li>
<li>NMS Support for multiple versions</li>
<li>New commands</li>
<li>Complete source code optimization with thread and workload implementation</li>
</ul>
<h2>Features</h2>
<ul>
<li><strong>Dual Authentication Mode</strong>: Support for both premium (Mojang) and cracked (non-premium) accounts</li>
<li><strong>Premium Account Verification</strong>: Automatic validation through Mojang API</li>
<li><strong>Secure Password Storage</strong>: Uses industry-standard encryption algorithms (Argon2 or BCrypt)</li>
<li><strong>SQLite Database</strong>: Custom API implementation with automatic library management</li>
<li><strong>Comprehensive Command Set</strong>: For both users and administrators</li>
<li><strong>Permission-Based Access</strong>: Granular control over plugin functionality</li>
</ul>
<h2>Commands</h2>
<h3>User Commands</h3>

Command | Description
-- | --
/login <password> | Login to your account
/register <password> <password> | Register a new account
/changepassword <old> <new> | Change your password
/premium | Enable premium auto-login
/cracked | Disable premium auto-login
/roleauth status | Show your account status
/roleauth version | Show plugin version
/roleauth reload | Reload configuration (requires permission)


<h2>Installation</h2>
<ol>
<li>Download the latest version of RoleAuth from the releases page</li>
<li>Place the RoleAuth Bungee JAR file in your BungeeCord plugins folder</li>
<li>Place the RoleAuth Spigot JAR file in your Server Spigot (Lobby / Hub) plugins folder</li>
<li>Restart your BungeeCord & Spigot server</li>
<li>Configure the plugin by editing the config.yml file in the plugins/RoleAuth directory from Bungee Server!</li>
</ol>
<h2>Configuration</h2>
<p>The plugin creates a default configuration that can be adjusted to your needs. The SQLite database will be automatically generated on first run.</p>
<h2>Dependencies</h2>
<ul>
<li>BungeeCord</li>
<li>Spigot (for block move listeners)</li>
<li>SQLite (automatically included)</li>
</ul>
<h2>Support</h2>
<p>If you encounter any issues or have suggestions for improvements, please open an issue on the GitHub repository.</p>
<h2>License</h2>
<p>This project is licensed under the Apache 2.0 License - see the LICENSE file for details.</p></body></html><!--EndFragment-->
</body>
</html>
