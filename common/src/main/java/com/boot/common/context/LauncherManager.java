package com.boot.common.context;

import com.boot.common.constant.Constants;
import com.boot.common.utils.ReflectUtils;

import java.util.HashSet;
import java.util.Set;

public class LauncherManager {

    private final Set<Launcher> launcherList;
    private static volatile LauncherManager instance;

    public static synchronized LauncherManager getInstance() {
        if (null == instance) {
            instance = new LauncherManager();
        }
        return instance;
    }

    private LauncherManager() {
        launcherList = new HashSet<>();
        Set<Class<?>> classSet = ReflectUtils.findClasses(Constants.BASE_PACKAGES, c -> ReflectUtils.isImplClass(Launcher.class, c));
        for (Class<?> clazz : classSet) {
            try {
                launcherList.add((Launcher) clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addLauncher(Launcher launcher) {
        this.launcherList.add(launcher);
    }

    public void preLoad() {
        for (Launcher launcher : this.launcherList) {
            launcher.preLoad();
        }
    }

    public void onLoad() {
        for (Launcher launcher : this.launcherList) {
            launcher.onLoad();
        }
    }

    public void onDestroy() {
        for (Launcher launcher : this.launcherList) {
            launcher.onDestroy();
        }
    }

}