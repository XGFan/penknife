package com.test4x.penknife;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Data
public class Request {
    private String url;
    private String uri;
    private String protocol;
    private String method;

    private HashMap<String, String> headers;


    private byte[] body;
    private Map<String, List<String>> parameters = new HashMap<>();
    private Map<String, List<String>> queryParameters;
    private Map<String, FileItem> fileItems = new HashMap<>();

    private HttpMethod httpMethod;

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    public Request(FullHttpRequest fullHttpRequest) {
        this.url = fullHttpRequest.uri();
        int pathEndPos = this.url.indexOf('?');
        this.uri = pathEndPos < 0 ? this.url : this.url.substring(0, pathEndPos);
        this.protocol = fullHttpRequest.protocolVersion().text();
        this.method = fullHttpRequest.method().name();

        // headers
        HttpHeaders httpHeaders = fullHttpRequest.headers();
        if (httpHeaders.size() > 0) {
            this.headers = new HashMap<>(httpHeaders.size());
            httpHeaders.forEach((header) -> headers.put(header.getKey(), header.getValue()));
        } else {
            this.headers = new HashMap<>();
        }

        // body content
        this.body = new byte[fullHttpRequest.content().readableBytes()];
        fullHttpRequest.content().copy().readBytes(this.body);

        //query parameters
        this.queryParameters = new QueryStringDecoder(url, CharsetUtil.UTF_8).parameters();

        //body
        if (!fullHttpRequest.method().equals(HttpMethod.GET)) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
            decoder.getBodyHttpDatas().forEach(this::parseData);
        }
    }


    public Map<String, Cookie> cookie() {
        String cookie = header("Cookie");
        cookie = cookie.length() > 0 ? cookie : header("Cookie".toLowerCase());
        if (cookie != null && !cookie.isEmpty()) {
            return ServerCookieDecoder.LAX.decode(cookie).stream().collect(Collectors.toMap(Cookie::name, Function.identity()));
        } else {
            return Collections.emptyMap();
        }
    }

    public String header(String name) {
        return headers.getOrDefault(name, "");
    }

    //todo
    private void parseData(InterfaceHttpData data) {
        try {
            switch (data.getHttpDataType()) {
                case Attribute:
                    Attribute attribute = (Attribute) data;
                    String name = attribute.getName();
                    String value = attribute.getValue();

                    List<String> values;
                    if (this.parameters.containsKey(name)) {
                        values = this.parameters.get(name);
                        values.add(value);
                    } else {
                        values = new ArrayList<>();
                        values.add(value);
                        this.parameters.put(name, values);
                    }

                    break;
                case FileUpload:
                    FileUpload fileUpload = (FileUpload) data;
                    parseFileUpload(fileUpload);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            log.error("Parse request parameter error", e);
        } finally {
            data.release();
        }
    }

    //todo
    private void parseFileUpload(FileUpload fileUpload) throws IOException {
        if (fileUpload.isCompleted()) {
            String contentType = URLConnection.guessContentTypeFromName(fileUpload.getFilename());
            if (fileUpload.isInMemory()) {
                FileItem fileItem = new FileItem(fileUpload.getName(), fileUpload.getFilename(),
                        contentType, fileUpload.length());

                ByteBuf byteBuf = fileUpload.getByteBuf();
                fileItem.setData(ByteBufUtil.getBytes(byteBuf));
                fileItems.put(fileItem.getName(), fileItem);
            } else {
                FileItem fileItem = new FileItem(fileUpload.getName(), fileUpload.getFilename(),
                        contentType, fileUpload.length());
                byte[] bytes = Files.readAllBytes(fileUpload.getFile().toPath());
                fileItem.setData(bytes);
                fileItems.put(fileItem.getName(), fileItem);
            }
        }
    }


    @Data
    public class FileItem {

        private String name;
        private String fileName;
        private String contentType;
        private long length;
        private byte[] data;

        public FileItem(String name, String fileName, String contentType, long length) {
            this.name = name;
            this.fileName = fileName;
            this.contentType = contentType;
            this.length = length;
        }

        @Override
        public String toString() {
            long kb = length / 1024;
            return "FileItem(" +
                    "name='" + name + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", size=" + (kb < 1 ? 1 : kb) + "KB)";
        }
    }
}
