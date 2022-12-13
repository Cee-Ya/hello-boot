package com.boot.common.context;

import com.boot.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.*;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Properties;

/**
 * 应用启动类
 */
public class FrameworkApplication extends SpringApplication {

    private static ConfigurableApplicationContext context;
    private static FrameworkApplication application;
    private ConfigurableEnvironment environment;
    private final String appName;
    private final String profile;


    public ConfigurableEnvironment getEnvironment() {
        return this.environment;
    }

    public FrameworkApplication(String appName, String profile, Class<?>... primarySources) {
        super(primarySources);
        this.appName = appName;
        this.profile = profile;
    }

    public String getAppName() {
        return this.appName;
    }


    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    public static FrameworkApplication getApplication() {
        return application;
    }

    public static String getMode() {
        FrameworkApplication frameworkApplication = getApplication();
        if (null == frameworkApplication) {
            return FrameworkContext.getAppMode(null);
        }
        return getMode(frameworkApplication.getEnvironment());
    }

    public static String getMode(ConfigurableEnvironment environment) {
        // 获取配置的环境变量
        String[] activeProfiles = environment.getActiveProfiles();
        return FrameworkContext.getAppMode(activeProfiles);
    }

    /**
     * 是否开发环境
     *
     * @return boolean
     */
    public static boolean isDev() {
        return isMode(Constants.MODE_DEV);
    }

    /**
     * 是否测试环境
     *
     * @return boolean
     */
    public static boolean isTest() {
        return isMode(Constants.MODE_TEST);
    }

    /**
     * 是否正式环境
     *
     * @return boolean
     */
    public static boolean isProd() {
        return isMode(Constants.MODE_PROD);
    }


    /**
     * 是否运行与某个环境
     *
     * @param mode 环境，Constants.MODE_xx
     * @return boolean
     */
    public static boolean isMode(String mode) {
        if (StringUtils.isEmpty(mode)) {
            return false;
        }
        return mode.equals(FrameworkApplication.getMode());
    }

    /**
     * 是否运行与某几个环境钟的一个
     *
     * @param modes 环境，Constants.MODE_xx
     * @return boolean
     */
    public static boolean isModes(String... modes) {
        String profile = FrameworkApplication.getMode();
        return Arrays.asList(modes).contains(profile);
    }

    private void setBasicConfig() {
        if (null == this.environment) {
            return;
        }
        MutablePropertySources propertySources = this.environment.getPropertySources();
        final String basicName = "basicProperties";
        PropertySource<?> basic = propertySources.get(basicName);
        if (null == basic) {
            Properties props = new Properties();
            props.setProperty("spring.application.name", this.appName);
            props.setProperty("spring.profiles.active", this.profile);
            props.setProperty("spring.messages.encoding", "UTF-8");
            //优雅停机
            props.setProperty("server.shutdown", "graceful");
            props.setProperty("spring.lifecycle.timeout-per-shutdown-phase", "20s");
            //开启健康检测
            props.setProperty("management.endpoint.health.probes.enabled", "true");
            //Nacos配置中心
//            props.setProperty("nacos.config.auto-refresh", "true");
//            props.setProperty("nacos.config.group", "DEFAULT_GROUP");
//            final String applicationConfig = "application";
//            List<String> dataIds = new ArrayList<>();
//            dataIds.add(this.appName.concat("_").concat(this.profile));
//            dataIds.add(applicationConfig.concat("_").concat(this.profile));
//            dataIds.add(this.appName);
//            dataIds.add(applicationConfig);
//            props.setProperty("nacos.config.data-ids", String.join(",", dataIds));
//            props.setProperty("nacos.config.type", "yaml");
            basic = new PropertiesPropertySource(basicName, props);
        } else {
            propertySources.remove(basicName);
        }
        propertySources.addLast(basic);
    }

    public static ConfigurableApplicationContext run(String appName, Class<?> source, String... args) {
        Assert.hasText(appName, "[appName]不能为空");
        FrameworkContext.init(appName);
        LauncherManager manager = LauncherManager.getInstance();
        manager.preLoad();

        // 读取环境变量，使用spring boot的规则
        ConfigurableEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new SimpleCommandLinePropertySource(args));
        propertySources.addLast(new MapPropertySource(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, environment.getSystemProperties()));
        propertySources.addLast(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, environment.getSystemEnvironment()));

        String profile = getMode(environment);
        String startJarPath = FrameworkApplication.class.getResource("/").getPath().split("!")[0];
        System.out.printf("##环境变量:[%s]##\n##jar地址:[%s]##%n", profile, startJarPath);
        application = new FrameworkApplication(appName, profile, source);
        application.addListeners((ApplicationListener<ApplicationFailedEvent>) applicationEvent -> {
            manager.onDestroy();
        });
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) applicationEvent -> {
            application.setBasicConfig();
        });
        context = application.run(args);
        manager.onLoad();
        return context;
    }

    @Override
    protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
        environment.setDefaultProfiles(Constants.MODE_DEV);
        super.configureEnvironment(environment, args);
        this.setBasicConfig();
        this.environment = environment;
    }

    @Override
    protected ConfigurableApplicationContext createApplicationContext() {
        return super.createApplicationContext();
    }

}