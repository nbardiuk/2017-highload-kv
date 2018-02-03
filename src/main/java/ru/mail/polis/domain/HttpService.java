package ru.mail.polis.domain;

import com.sun.net.httpserver.HttpServer;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpService implements KVService {

    private final HttpServer server;

    public HttpService(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/v0/status", http -> {
            byte[] body = "ONLINE".getBytes(UTF_8);
            http.sendResponseHeaders(200, body.length);
            http.getResponseBody().write(body);
            http.close();
        });
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
