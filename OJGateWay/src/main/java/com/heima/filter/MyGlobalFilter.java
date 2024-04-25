package com.heima.filter;

//import com.alibaba.cloud.commons.lang3.StringUtils;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.heima.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {
    @Value("${oj.excludepaths}")
    private String[] exps;

    @Value("${oj.notexcludepaths}")
    private String[] notexps;
    @Autowired
    private AppJwtUtil appJwtUtil;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getPath().toString();

        System.out.println(url);
        if (isEx(url) && !isnotEx(url)) {  //即使是放行的也有可能带用户信息

            System.out.println("这是直接放行的路径");
            String token = null;
            List<String> authorlization = request.getHeaders().get("authorization");

            if (authorlization != null && !authorlization.isEmpty()) {
                token = authorlization.get(0);
            }

            if (token != null) {
                Long l = null;
                try {
                    Jws<Claims> jws = AppJwtUtil.getJws(token);
                    log.info(jws.toString());
                    Claims claims = jws.getBody();

                    log.info("经过网关变成" + claims.get("id"));
                    ServerHttpRequest uid = request.mutate().headers(h -> {
                        h.add("uid", claims.get("id").toString());
                    }).build();

                    exchange.mutate().request(uid).build();

                } catch (Exception e) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            }
            return chain.filter(exchange);
        } else {


            System.out.println("这是不放行的");

            String token = null;
            List<String> authorlization = request.getHeaders().get("authorization");

            if (authorlization != null && !authorlization.isEmpty()) {
                token = authorlization.get(0);
            }

            Long l = null;
            try {

                Jws<Claims> jws = AppJwtUtil.getJws(token);
                Claims claims = jws.getBody();
                log.info("经过网关变成" + claims.get("id"));
                ServerHttpRequest uid = request.mutate().headers(h -> {
                    h.add("uid", claims.get("id").toString());
                }).build();

                exchange.mutate().request(uid).build();

            } catch (Exception e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }


            return chain.filter(exchange);
        }
    }

    private boolean isEx(String s) {
        boolean f = false;
        for (String es : exps) {
            log.info((antPathMatcher.match(es, s) == true) ? "1" : "0");
            if (antPathMatcher.match(es, s)) {
                f = true;
                break;
            }
        }
        return f;
    }

    private boolean isnotEx(String s) {
        boolean f = false;
        for (String es : notexps) {
            log.info((antPathMatcher.match(es, s) == true) ? "1" : "0");
            if (antPathMatcher.match(es, s)) {
                f = true;
                break;
            }
        }
        return f;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
