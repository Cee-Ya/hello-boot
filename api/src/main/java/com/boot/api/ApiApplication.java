package com.boot.api;

import com.boot.api.support.constant.Constants;
import com.boot.common.context.FrameworkApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Yarns
 * @date 2022/12/13
 */
@SpringBootApplication
@MapperScan(Constants.basePackageName)
public class ApiApplication {

    public static void main(String[] args) {
        FrameworkApplication.run(Constants.applicationName,ApiApplication.class,args);
    }

}
