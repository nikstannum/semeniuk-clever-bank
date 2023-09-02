package ru.clevertec.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.log4j.Log4j2;
import ru.clevertec.factory.BeanFactory;

@WebListener
@Log4j2
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BeanFactory factory = BeanFactory.INSTANCE;
        log.info("initialized: {}", factory.getClass());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BeanFactory.INSTANCE.close();
    }
}
