
# Dislink 🗣️🔗
### A Minecraft-Discord Account Linker for Spigot 1.20+

---

At it's core, Dislink is a Minecraft-Discord account linker, with the added functionality of whitelisting your server
for users that link. Linking can be locked behind specific discord roles to ensure that only the right people have
access to the minecraft server, as well as offering support for multiple discord servers to be linked from at once.

It's built to be highly customizable to adapt to server needs, as well as offering a small API to help with
integrating metadata from the linking into other plugins.

### Chat Integration 

The plugin offers a portion of the config dedicated to adding prefixes depending on where the player runs /link. While
the plugin does not offer a built-in way to display this, it offers enough API hooks to integrate the prefixes into
existing chat formatters.

For a reference (& working) implementation, 
see [VaultChatFormatter for Dislink.](https://github.com/squeeglii/VaultChatFormatter-Dislink)

### Discord Bot Integration

The plugin requires a Discord Bot Token to be provided within the config in order to offer the discord-side of
the linking process. This requires you to make your own Bot application with permission to create commands.

See the following resources on how to create a bot & add it to servers:
 - https://discordjs.guide/preparations/setting-up-a-bot-application.html
 - https://discordjs.guide/preparations/adding-your-bot-to-servers.html

Don't forget to add the token to the config!

### Database Support

This plugin only offers support for SQL-like databases currently. A local SQLite fallback will be added soon for ease of
use, however there's no estimate on when it will be added.

