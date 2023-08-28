package ru.clevertec.service.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.clevertec.service.AccountService;

@RequiredArgsConstructor
public class AccrualServiceImpl implements Runnable, Closeable {
    private final AccountService accountService;
    private final Long periodicity;
    @Setter
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void run() {
        accountService.accrueInterest();
        scheduledExecutorService.schedule(this, periodicity, TimeUnit.SECONDS);
    }

    @Override
    public void close() throws IOException {
        scheduledExecutorService.shutdown();
    }
}
