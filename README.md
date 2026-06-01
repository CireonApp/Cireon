# Cireon

**Cireon** is a modern, open-source home media server built with Spring Boot. Inspired by solutions like Emby and Jellyfin, Cireon lets you organize, stream, and manage your personal media library from any device on your network.

## Features

- **Media Library Management:** Organize movies, TV shows, music, and more with rich metadata.
- **User Profiles:** Create and manage multiple user profiles with customizable access.
- **Streaming:** Stream your media to web browsers and compatible devices.
- **Modern UI:** Fast, responsive interface powered by Thymeleaf.
- **Extensible:** Built with modularity in mind for easy customization and expansion.

## Getting Started

1. **Download the latest Cireon server release** from [the releases page](https://github.com/CireonApp/CireonBackend/releases).
2. **Save the file** to your preferred directory (will create more files in the directory so create an empty one).
3. **Start the server:**
   You can run the software if you downloaded an executable or run the jar file using JRE 21.
5. **Access the server:**
   - Open [http://localhost:50262](http://localhost:50262) in your browser.
   - The default port is 50262 but you can change it in the settings.
   - The app comes with a web app in mind. But native apps for different platform might come in the future.

## API

- The app is built with an API so you can reach it without using the web app.  
  You can make your own apps using the api.
- The API documentation is available at `/api/docs` endpoint. Created with OpenAPI and Swagger UI

## License

This project is licensed under the GPL-3.0-only license.  
For more information about third-party dependencies, see [THIRD-PARTY-NOTICES.txt](./THIRD-PARTY-NOTICES.txt)
