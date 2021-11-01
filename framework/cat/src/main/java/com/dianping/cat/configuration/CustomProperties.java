package com.dianping.cat.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CustomProperties {

    public static final String PLACEHOLDER_PREFIX = "${";

    public static final String PLACEHOLDER_SUFFIX = "}";

    public static final String NAME = "cat.project";
    public static final String SERVERS = "cat.servers";
    public static final String ENABLE = "cat.enable";
    public static final String SERVER_ID = "cat.serverId";

    public static final String CUSTOM_CONFIG_NAME = "CUSTOM_CAT_FILE";
    public static final String CUSTOM_CONFIG_FILE = "server.properties";

    private static volatile boolean init = false;

    private static boolean enable = false;
    private static String name;
    private static String servers;
    private static String serverId;

    static {
        init();
    }

    static void init() {
        if (init) {
            return;
        }
        init = true;
        com.dianping.cat.util.Properties.PropertyAccessor<String> env = com.dianping.cat.util.Properties.forString().fromEnv().fromSystem();
        String customFileName = env.getProperty(CUSTOM_CONFIG_NAME, CUSTOM_CONFIG_FILE);
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(customFileName)) {
            if (in == null) {
                return;
            }
            Properties prop = new Properties();
            prop.load(in);

            String enableStr = get(prop, ENABLE);
            if (!"true".equalsIgnoreCase(enableStr)) {
                return;
            }
            enable = true;
            name = get(prop, NAME);
            servers = get(prop, SERVERS);
            serverId = get(prop, SERVER_ID);

            String defaultCatHome = env.getProperty("CAT_HOME", null);
            if (defaultCatHome == null) {
                Path path = Paths.get(System.getProperty("user.home"), ".cat");
                String homePath = path.toAbsolutePath().toString();
                System.setProperty("CAT_HOME", homePath);
            }


        } catch (IOException ignore) {
        }
    }

    private static String get(Properties prop, String key) {
        String property = prop.getProperty(key);
        if (property == null) {
            return null;
        }
        property = property.trim();
        if (property.isEmpty()) {
            return null;
        }
        int fromIndex = property.indexOf(PLACEHOLDER_PREFIX);
        if (fromIndex < 0) {
            return property;
        }
        int toIndex = property.indexOf(PLACEHOLDER_SUFFIX);
        if (toIndex < 0) {
            return property;
        }
        String nextKey = property.substring(fromIndex + 2, toIndex);

        return prop.getProperty(nextKey);
    }

    public static boolean isEnable() {
        return enable;
    }

    public static String getName() {
        return name;
    }

    public static String getServers() {
        return servers;
    }

    public static String getServerId() {
        return serverId;
    }
}
