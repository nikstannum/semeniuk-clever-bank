package ru.clevertec.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.clevertec.web.factory.CommandFactory;

@WebListener
//@Log4j2
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CommandFactory factory = CommandFactory.INSTANCE;
//        log.info("initialized: {}", factory.getClass());
        // FIXME add logging factory initialized CommandFactory initialized = factory.getClass()
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        CommandFactory.INSTANCE.close();
    }
}
