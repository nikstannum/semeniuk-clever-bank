package ru.clevertec.logging;

import java.util.Arrays;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Log4j2
public class Logger {

    @Around("@annotation(Loggable)")
    public Object logMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info(String.format("Method: %s with args: %s was called", methodName, Arrays.toString(args)));
        Object result = joinPoint.proceed();
        log.info(String.format("Method: %s returned: %s", methodName, result));
        return result;
    }
}
