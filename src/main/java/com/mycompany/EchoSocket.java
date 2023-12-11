package com.mycompany;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class EchoSocket implements WebSocketListener
{
    private static final Logger LOG = Log.getLogger(EchoSocket.class);
    private Session outbound;

    public void onWebSocketClose(int statusCode, String reason)
    {
        this.outbound = null;
        LOG.info("WebSocket Close: {} - {}",statusCode,reason);
    }

    public void onWebSocketConnect(Session session)
    {
        this.outbound = session;
        String httpVersion = session.getUpgradeRequest().getHttpVersion();
        LOG.info("WebSocket Connect: {}, http version: {}", session, httpVersion);
        this.outbound.getRemote().sendString("You are now connected to " + this.getClass().getName()
            + ", http version: " + httpVersion, null);
    }

    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error",cause);
    }

    public void onWebSocketText(String message)
    {
        if ((outbound != null) && (outbound.isOpen()))
        {
            LOG.info("Echoing back text message [{}]",message);
            outbound.getRemote().sendString(message,null);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2)
    {
        /* ignore */
    }
}