package com.jm.filter;

import io.netty.util.internal.StringUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter  implements GlobalFilter, Ordered {

    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     * 全局拦截
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取用户令牌信息，如果没有令牌，则拦截
        // 1、参数获取令牌
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //boolean true : 令牌在头文件中 false：令牌不在头文件中
        boolean hasToken = true;

        // 2、头文件中获取
        if(StringUtils.isEmpty(token)){
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
        // 3、Cookie中
        if(StringUtils.isEmpty(token)){
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if(httpCookie != null){
                token = httpCookie.getValue();
            }
        }

        //如果没有令牌，返回无效响应
        if(StringUtils.isEmpty(token)){
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 响应空数据
            return response.setComplete();
        }

        // 令牌设置是否为空，如果不为空，将令牌放到头文件中 放行
        if(StringUtils.isEmpty(token)){
            // 设置没有权限的状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 响应清空数据
            return response.setComplete();
        }else{
            if(!hasToken){
                // 判断令牌是否有bearer前缀, 如果没有则添加前缀 bearer
                if(!token.startsWith("bearer ")&&!token.startsWith("Bearer ")){
                    token="bearer "+token;
                }
                // 将令牌封装到头文件中
                request.mutate().header(AUTHORIZE_TOKEN, token);
            }

        }

        //有效则放行
        return chain.filter(exchange);

        /*try {
            //JwtUtil.parseJWT(token);


        } catch (Exception e) {
            //无效拦截
            //设置没有权限的状态码 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 响应空数据
            return response.setComplete();
        }*/


    }

    @Override
    public int getOrder() {
        return 0;
    }
}
