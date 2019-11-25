package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.Arrays;


//@Configuration
//@EnableAuthorizationServer
public class AuthorizationServer extends
        AuthorizationServerConfigurerAdapter {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private UserDetailsService userDetailsService;


	@Autowired
	private ClientDetailsService clientDetailsService;

	@Autowired
	private AuthenticationManager authenticationManager;

//    @Bean
//    public ClientDetailsService clientDetailsService(DataSource dataSource) {
//        ClientDetailsService clientDetailsService = new CustomJdbcClientDetailsService(dataSource);
//        ((CustomJdbcClientDetailsService) clientDetailsService).setPasswordEncoder(passwordEncoder());
//        return clientDetailsService;
//    }


    //客户端详情信息
    @Override
	public void configure(ClientDetailsServiceConfigurer clients)
			throws Exception {
		clients.inMemory()
				.withClient("password1")
				.authorizedGrantTypes("password","refresh_token")
				.accessTokenValiditySeconds(1800)
				.authorities("ROLE_ADMIN")
				.resourceIds("rid")
				.scopes("read");


	}
  


    

    //用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)。
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.tokenStore(new RedisTokenStore(redisConnectionFactory))
				.authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService);
	}
	
    //用来配置令牌端点(Token Endpoint)的安全约束
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security)
			throws Exception {
		security
				.tokenKeyAccess("permitAll()")
				.checkTokenAccess("permitAll()")
				.allowFormAuthenticationForClients()//允许表单认证
		;
	}

	
}
