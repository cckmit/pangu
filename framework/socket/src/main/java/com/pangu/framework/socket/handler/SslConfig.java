package com.pangu.framework.socket.handler;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SslConfig {
    // 是否启用
    private final boolean enabled;

    //证书密码
    private final String password;

    //证书类型
    private final String storeType;

    // 证书路径
    private final String storePath;

    //SSL协议
    private final String protocol = "TLSv1.2";

    public SslConfig(boolean enabled, String password, String storeType, String storePath) {
        this.enabled = enabled;
        this.password = password;
        this.storeType = storeType;
        this.storePath = storePath;
    }

    public SslConfig(boolean enabled) {
        this(enabled, null, null, null);
    }

    public SslContext createForServer() {
        if (!enabled) {
            return null;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            InputStream ksInputStream = new FileInputStream(storePath);
            keyStore.load(ksInputStream, password.toCharArray());
            String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(defaultAlgorithm);
            kmf.init(keyStore, password.toCharArray());

            return SslContextBuilder.forServer(kmf)
                    .sslProvider(SslProvider.OPENSSL).protocols(protocol)
                    .build();
        } catch (Exception e) {
            throw new Error("Failed to initialize the SSLContext", e);
        }
    }

    public SslContext createForClient() {
        if (!enabled) {
            return null;
        }
        try {
            return SslContextBuilder.forClient()
                    .sslProvider(SslProvider.OPENSSL).protocols(protocol)
                    .trustManager(TrustManagers.TRUST_ALL_CERTS)
                    .build();
        } catch (Exception e) {
            throw new Error("Failed to initialize the SSLContext", e);
        }
    }

    public static SslConfig server(boolean enabled, String password, String storeType, String storePath) {
        return new SslConfig(enabled, password, storeType, storePath);
    }

    public static SslConfig client(boolean enabled) {
        return new SslConfig(enabled);
    }

    public static class TrustManagers {
        public static TrustManager TRUST_ALL_CERTS = new X509ExtendedTrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {
            }

            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
            }

            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {
            }
        };
    }
}
