# TelegramRemoteSpigot
# EN
This is a Spigot plugin for Minecraft servers that enables remote management and notifications via Telegram. The bot interacts with the server console, provides administrative functions, and sends notifications based on specific events.

## Installation
1. **Telegram bot setup**
   - Create a bot on [BotFather](https://t.me/BotFather).
   - Obtain the bot token.

2. **Plugin installation**
   - Download the latest plugin version from the [releases page](https://github.com/drazz0m/TelegramRemote/releases).
   - Place the downloaded JAR file into the `plugins` folder of your Spigot server.

3. **Configuration**
   - Edit the `config.yml` file in the `plugins/TelegramRemote` folder.
   - Set the bot token and its username in the `token` property.
   - Set your ChatID obtained through [getmyid_bot](https://t.me/getmyid_bot).
   - Configure RCON for console and command operations in the bot:
      ```yaml
      rcon:
        enable: true
        rcon_host: 0.0.0.0
        rcon_port: 25575
        rcon_pass: qwerty
      ```
   - Configure other parameters as needed.

## Usage
- Start your Spigot server.
- Begin a private chat with the bot.
- Use `/start` to initiate communication with the bot.

## Administrative Functions
- **Bot settings**
  - Manage language, notifications, enable/disable features, and add administrators.
- **Console mode**
  - Enter console mode to send commands to the server.
- **Notifications**
  - Configure various notification parameters.
- **Administrator Panel**
  - Access administrative functions to manage the server.

## Telegram Notifications
Receive notifications about:
- Changes in operator status.
- Server startup.
- Server updates.

## Author
- drazz

## Issues and Contributions
Report issues and bugs.

## License
This project is licensed under the MIT License.

