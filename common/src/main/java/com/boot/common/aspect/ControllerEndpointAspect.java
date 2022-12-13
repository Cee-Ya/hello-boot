package com.boot.common.aspect;

import com.alibaba.fastjson.JSON;
import com.boot.common.annotation.ControllerEndpoint;
import com.boot.common.utils.AddressUtils;
import com.boot.common.utils.CommonUtils;
import com.boot.common.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Yarns
 */
@SuppressWarnings("Duplicates")
@Aspect
@Component
public class ControllerEndpointAspect extends AspectSupport {

    private final Logger logger = LoggerFactory.getLogger("monitor");

    @Override
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }

    /**
     * 使用注解 或者直接切controller
     */
    @Pointcut("@annotation(com.boot.common.annotation.ControllerEndpoint)")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Exception {
        Object result = null;
        Method targetMethod = resolveMethod(point);
        ControllerEndpoint annotation = targetMethod.getAnnotation(ControllerEndpoint.class);
        String operation = annotation.operation();
        long start = System.currentTimeMillis();
        String username = "匿名";
        try {
            if (StringUtils.isNotBlank(operation)) {
                String ip = CommonUtils.getHttpServletRequestIpAddress();
                logger.info("#####################log start##################");
                buildLog(point, targetMethod, ip, operation, username, start);
                result = point.proceed();
                //处理返回值
                logger.info("返回值:{}", JSON.toJSONString(result));
            }
            return result;
        } catch (Throwable throwable) {
            initChildException(throwable);
            throwable.printStackTrace();
            String exceptionMessage = annotation.exceptionMessage();
            String message = throwable.getMessage();
            String error;
            if (message != null) {
                error = CommonUtils.containChinese(message) ? exceptionMessage + "，" + message : exceptionMessage;
            } else {
                error = exceptionMessage;
            }
            logger.info("#####################logError start##################");
            logger.info("异常信息:{}", throwable.getMessage());
            logger.error("内部业务异常:{}", CommonUtils.getMessage(throwable));
            throw new Exception(error);
        } finally {
            logger.info("#####################log end##################");
        }
    }


    private void initChildException(Throwable throwable) {
        if (throwable.getCause() != null) {
            throwable = throwable.getCause();
            initChildException(throwable);
        }
    }

    /**
     * 打印日志
     *
     * @param point
     * @param method
     * @param ip
     * @param operation
     * @param username
     * @param start
     * @throws Throwable
     */
    private void buildLog(ProceedingJoinPoint point, Method method, String ip, String operation, String username, long start) throws Throwable {
        logger.info("IP:{}", ip);
        logger.info("操作人:{}", username);
        logger.info("操作时间:{}", DateUtils.getDateFormat(new Date(), DateUtils.FULL_TIME_SPLIT_PATTERN));
        logger.info("耗时:{}ms", System.currentTimeMillis() - start);
        logger.info("操作内容:{}", operation == null ? "" : operation);
        String className = point.getTarget().getClass().getName();
        String methodName = method.getName();
        logger.info("方法:{}", className + "." + methodName + "()");
        Object[] args = point.getArgs();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            StringBuilder params = new StringBuilder();
            params = handleParams(params, args, Arrays.asList(paramNames));
            StringBuilder jsonParams = new StringBuilder();
            handleJsonParams(jsonParams, args, paramNames);
            logger.info("json格式参数:{}", jsonParams);
            logger.info("对象格式参数:{}", params);
        }
        if (ip != null && !"127.0.0.1".equals(ip)) {
            logger.info("地址:{}", AddressUtils.getCityInfo(ip.trim()));
        } else {
            logger.info("地址:{}", "本地");
        }
    }
}



