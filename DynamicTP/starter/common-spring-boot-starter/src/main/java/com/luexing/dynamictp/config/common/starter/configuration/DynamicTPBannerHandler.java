/*
 * 掠星 DynamicTP 动态线程池
 */
package com.luexing.dynamictp.config.common.starter.configuration;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.info.BuildProperties;

/**
 * dynamicTP Banner 打印组件
 * <p>
 */
@Slf4j
public class DynamicTPBannerHandler implements InitializingBean {

    private static final String DYNAMIC_THREAD_POOL = " :: 掠星 luexing :: ";
    private static final int STRAP_LINE_SIZE = 50;
    private final String version;

    public DynamicTPBannerHandler(BuildProperties buildProperties) {
        this.version = buildProperties != null ? buildProperties.getVersion() : "";
    }

    @Override
    public void afterPropertiesSet() {
        String banner = """
                                    _______ __                        __
                .-----.-----.-----.|_     _|  |--.----.-----.---.-.--|  |
                |  _  |     |  -__|  |   | |     |   _|  -__|  _  |  _  |
                |_____|__|__|_____|  |___| |__|__|__| |_____|___._|_____|
                
                """;
        String bannerVersion = StrUtil.isNotEmpty(version) ? " (v" + version + ")" : "no version.";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE - (bannerVersion.length() + DYNAMIC_THREAD_POOL.length())) {
            padding.append(" ");
        }
        System.out.println(AnsiOutput.toString(banner, AnsiColor.GREEN, DYNAMIC_THREAD_POOL, AnsiColor.DEFAULT,
                padding.toString(), AnsiStyle.FAINT, bannerVersion, "\n"));
    }
}
