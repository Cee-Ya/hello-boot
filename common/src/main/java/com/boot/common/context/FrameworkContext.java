package com.boot.common.context;

import com.boot.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class FrameworkContext {
    private final String appName;
    private String appMode;
    private static FrameworkContext instance;

    public static void init(String appName) {
        instance = new FrameworkContext(appName);
    }

    public static String getAppName() {
        return null == instance ? Constants.EMPTY_STR : instance.appName;
    }

    public static String getAppMode() {
        return null == instance ? Constants.MODE_DEV : instance.appMode;
    }

    private static String getActiveProfile(String[] activeProfiles) {
        if (null == activeProfiles || 0 == activeProfiles.length) {
            return Constants.MODE_DEV;
        }
        // 判断环境:dev、test、prod
        List<String> profiles = Arrays.asList(activeProfiles);
        // 交集
        profiles.retainAll(Constants.MODE_LIST);
        // 当前使用
        List<String> activeProfileList = new ArrayList<>(profiles);
        if (activeProfileList.isEmpty()) {
            // 默认dev开发
            return Constants.MODE_DEV;
        } else if (activeProfileList.size() == 1) {
            return activeProfileList.get(0);
        } else {
            // 同时存在dev、test、prod环境时
            throw new RuntimeException("同时存在环境变量:[" + String.join(",", activeProfileList) + "]");
        }
    }

    public static String getAppMode(String[] activeProfiles) {
        String profile = getActiveProfile(activeProfiles);
        if (null != instance) {
            instance.appMode = profile;
        }
        return profile;
    }

    /**
     * 是否开发环境
     *
     * @return boolean
     */
    public static boolean isDev() {
        if (instance == null || StringUtils.isEmpty(instance.appMode)) {
            return true;
        }
        if (!Constants.validMode(instance.appMode)) {
            return true;
        }
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
        if (instance == null || StringUtils.isEmpty(mode)) {
            return false;
        }
        return mode.equals(instance.appMode);
    }

    /**
     * 是否运行与某几个环境钟的一个
     *
     * @param modes 环境，Constants.MODE_xx
     * @return boolean
     */
    public static boolean isModes(String... modes) {
        if (instance == null) {
            return false;
        }
        return Arrays.asList(modes).contains(instance.appMode);
    }
}
