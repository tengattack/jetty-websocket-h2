package com.mycompany;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

public class EchoSocketServlet extends JettyWebSocketServlet
{
    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.register(EchoSocket.class);
    }
}