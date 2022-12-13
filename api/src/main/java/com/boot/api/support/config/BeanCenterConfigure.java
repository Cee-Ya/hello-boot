package com.boot.api.support.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yarns
 * @date 2022/12/13
 */
@Configuration
public class BeanCenterConfigure {


    /**
     * mybatisplus 自带分页
     * @return
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor paginationInterceptor = new MybatisPlusInterceptor();
        paginationInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        paginationInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return paginationInterceptor;
    }
}
