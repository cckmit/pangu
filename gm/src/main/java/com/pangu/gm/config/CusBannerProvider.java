package com.pangu.gm.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.shell.support.util.FileUtils;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CusBannerProvider implements BannerProvider {
    @Override
    public String getBanner() {
        ClassPathResource resource = new ClassPathResource("banner.txt");
        try {
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
            return FileUtils.readBanner(reader) +
                    getVersion() + OsUtils.LINE_SEPARATOR +
                    OsUtils.LINE_SEPARATOR;
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to " + getProviderName() + ". For assistance press or type \"hint\" then hit ENTER.";
    }

    @Override
    public String getProviderName() {
        return "PanGu shell";
    }
}
