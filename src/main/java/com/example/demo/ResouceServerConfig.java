package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
public class ResouceServerConfig {

    @Bean
    public WebResponseExceptionTranslator webResponseExceptionTranslator() {
        return new DefaultWebResponseExceptionTranslator() {

            @Override
            public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
                ResponseEntity<OAuth2Exception> responseEntity = super.translate(e);
                OAuth2Exception body = responseEntity.getBody();
                // 认证失败(过期)
                if (e instanceof InsufficientAuthenticationException) {
                    body.addAdditionalInformation("code", "610");
                    body.addAdditionalInformation("msg", body.getOAuth2ErrorCode());
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setAll(responseEntity.getHeaders().toSingleValueMap());
                // do something with header or response
                return new ResponseEntity<>(body, headers, responseEntity.getStatusCode());
            }
        };
    }

    /**
     * 测试资源服务(资源在网关内)，测试使用
     */
    @Configuration
    @EnableResourceServer
    public class ResouceTestServerConfig extends
            ResourceServerConfigurerAdapter {

//        @Autowired
//        private TokenStore tokenStore;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources)
                throws Exception {
            resources.resourceId("rid")
                    //只能是基于令牌的认证方式 默认就是true
                    .stateless(true);
            // 定义异常转换类生效
            AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
            ((OAuth2AuthenticationEntryPoint) authenticationEntryPoint).setExceptionTranslator(webResponseExceptionTranslator());
            resources.authenticationEntryPoint(authenticationEntryPoint);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            //不管配置多少个资源服务器 会自动合并成一个 没有冲突的合并 有冲突的前面覆盖后面 比如这个session
            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    //下面这句其实OAUTH是根据FilterChain 里面有十几个过滤器
                    //一般有三个SecurityConfig一个 还有默认的也就是oauth/tocken oauth/check_token等等 也是一个 这个默认是默认的所有没要必要
                    //在配置什么permitAll 还有就是这个资源服务器里 不过你配置多少个资源服务器 最终会汇总到一个
                    //优先级oauthToken>资源服务器>SecurityConfig 下面这个就是匹配一下匹配到就执行这个chain匹配不到就交给下一级调用链
                    //但是假如不写这个 那他也不会到下一级 也就是说SecurityConfig的HttpSecurity
                    //基本没有调用的机会 因为默认的RequestMatch是NotOAuthRequestMatcher 最后用ReuquestMatcher调用链
                    .requestMatchers()
                    .antMatchers("/uua/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/uua/fegin/**").permitAll()
                    .antMatchers("/uua/fegin1/**")
                    //客户端也有更细粒度啊的权限控制
                    .access("#oauth2.hasScope('read') and #oauth2.clientHasRole('ROLE_ADMIN')")
                    //下面这个表示掐他请求都需要认证
                    .anyRequest().authenticated();
            // and
            // hasRole('ROLE_USER')

        }

    }
}