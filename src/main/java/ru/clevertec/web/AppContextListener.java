package ru.clevertec.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.clevertec.service.impl.AccrualServiceImpl;
import ru.clevertec.factory.BeanFactory;

@WebListener
//@Log4j2
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        BeanFactory factory = BeanFactory.INSTANCE;
//        log.info("initialized: {}", factory.getClass());
        // FIXME add logging factory initialized BeanFactory initialized = factory.getClass()
        AccrualServiceImpl accrualService = (AccrualServiceImpl) factory.getBean("accrualService");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        accrualService.setScheduledExecutorService(scheduler);
        scheduler.schedule(accrualService, 0, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BeanFactory.INSTANCE.close();
    }
}
