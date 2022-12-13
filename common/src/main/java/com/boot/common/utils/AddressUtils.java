package com.boot.common.utils;

import com.boot.common.constant.Constants;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @Author Yarns
 * @Date 19:50
 * @Version 1.0
 **/
@Slf4j
public class AddressUtils {
    public static String getCityInfo(String ip) {
        try {
            String dbPath = AddressUtils.class.getResource("/ip2region/ip2region.db").getPath();
            File file = new File(dbPath);
            if (!file.exists()) {
                String tmpDir = System.getProperties().getProperty(Constants.JAVA_TEMP_DIR);
                dbPath = tmpDir + "ip.db";
                file = new File(dbPath);
                InputStream resourceAsStream = AddressUtils.class.getClassLoader().getResourceAsStream("classpath:ip2region/ip2region.db");
                if (resourceAsStream != null) {
                    FileUtils.copyInputStreamToFile(resourceAsStream, file);
                }
            }
            DbConfig config = new DbConfig();
            @Cleanup
            DbSearcher searcher = new DbSearcher(config, file.getPath());
            Method method = searcher.getClass().getMethod("btreeSearch", String.class);
            DataBlock dataBlock = (DataBlock) method.invoke(searcher, ip);
            log.info(dataBlock.getRegion());
            return dataBlock.getRegion();
        } catch (Exception e) {
            log.warn("获取地址信息异常,{}", e.getMessage());
            return StringUtils.EMPTY;
        }
    }
}
