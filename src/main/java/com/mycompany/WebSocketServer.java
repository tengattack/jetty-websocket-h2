package com.mycompany;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        // 创建 Jetty Server
        Server server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(8443);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.p12");
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // 设置 HTTP/1.1 和 HTTP/2 连接工厂
        HttpConnectionFactory http1 = new HttpConnectionFactory(httpsConfig);
        HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(httpsConfig);

        // 设置服务端 ALPN 连接工厂
        // NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        // WebSocket upgrade comes as an HTTP/1.1 request, with the exception of RFC8441 where a single HTTP/2 stream is
        // upgraded to websocket and this is not widely supported yet.
        // https://github.com/jetty/jetty.project/issues/7740
        // alpn.setDefaultProtocol(http2.getProtocol());
        alpn.setDefaultProtocol(http1.getProtocol());

        // 设置 SSL 连接工厂
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        // 创建服务端连接器
        ServerConnector serverConnector = new ServerConnector(server, ssl, alpn, http2, http1);
        serverConnector.setPort(8443);

        server.addConnector(serverConnector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        JettyWebSocketServletContainerInitializer.configure(context, null);
        context.setContextPath("/");
        server.setHandler(context);

        // Add websocket servlet
        ServletHolder wsHolder = new ServletHolder("echo", new EchoSocketServlet());
        context.addServlet(wsHolder, "/echo");

        try {
            // 开始运行服务
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
