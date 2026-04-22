package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserConverter;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class Databases implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public static ObjectRepository<User> userRepository;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ServerApplication.LOGGER.log(java.util.logging.Level.INFO, "Initializing databases...");

        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath("database.db")
                .build();

        Nitrite userDB = Nitrite.builder()
                .loadModule(storeModule)
                .registerEntityConverter(new UserConverter())
                .openOrCreate();

        userRepository = userDB.getRepository(User.class,"users");

    }
}
