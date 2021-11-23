package com.pangu.gm.config;

import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CusPromptProvider implements PromptProvider {
    @Override
    public String getPrompt() {
        return "gm>";
    }

    @Override
    public String getProviderName() {
        return "default prompt provider";
    }
}
