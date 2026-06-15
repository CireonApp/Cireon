[![Build & Release](https://github.com/CireonApp/Cireon/actions/workflows/build-and-release.yml/badge.svg?branch=main)](https://github.com/CireonApp/Cireon/actions/workflows/build-and-release.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
# 🎬 Cireon

Cireon is a modern open-source home media server built with Spring Boot.

It is inspired by platforms like Emby and Jellyfin and gives you a clean way to organize and stream your personal media on your local network.

> [!NOTE]
> You probably should not use this for serious or production use.  
> It is a personal project and not comparable to established solutions like Jellyfin or Emby.  
> It may be less stable, less secure, and less feature complete than those platforms.  
> Cireon is experimental and focused on learning and personal use.

## ✨ Features

- 📚 Media library management for movies, TV shows, music, and more
- 👤 Multiple user profiles with access control
- 📡 Media streaming to browsers and supported devices
- ⚡ Fast and responsive web UI powered by Thymeleaf
- 🧩 Modular design for easier expansion and customization

## 🚀 Getting Started

1. Download the latest Cireon server release from the [releases](https://github.com/CireonApp/CireonBackend/releases
) page
2. Save the file to a folder on your system
3. Start the server
   - Run the executable or start the JAR using JRE 21
4. Open the web app
   - http://localhost:14567
   - Default port is 14567
   - Port can be changed in settings

## 🌐 API

- Cireon includes a built-in API for external access
- Build your own apps or integrations using it
- API documentation is available at
/api/docs via OpenAPI and Swagger UI

## 📱 Future

- Native apps for multiple platforms may come later

## License

Licensed under GPL-3.0-only.  
See [THIRD-PARTY-NOTICES.txt](./THIRD-PARTY-NOTICES.txt) for dependency licenses and attributions
