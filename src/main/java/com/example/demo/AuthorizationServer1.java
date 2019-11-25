package com.example.demo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


@Configuration
@EnableAuthorizationServer
public class AuthorizationServer1 extends
        AuthorizationServerConfigurerAdapter {

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private AuthenticationManager authenticationManager;


    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()
                .withClient("password1")
                .authorizedGrantTypes("password","refresh_token")
                .accessTokenValiditySeconds(1800)
        //这个其实就是权限更粗一点的 例如ROLE_CMS_NEWS,ROLE_APP_INFO,ROLE_KVCONFIG,ROLE_NEWS_COMMENT这几个微服务的权限 由自已灵活使用 前面几个是微服务名
                .authorities("ROLE_ADMIN")
             //这个也就是权限更高一点的 针对于资源服务器 需要和资源服务器一一对应 都设置成一样就行
                .resourceIds("rid")
              //这个也是针对于资源服务器的
                .scopes("read");


    }
  


    

    //用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)。
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    	//这个会自动把认证信息 存入存入到redis 只要你配了数据源 在掉接口的时候/oauth/token
		//比如access_token key名就为 access:e18e1f70-d89d-4c70-b6bf-1f3a7036b310 在用户认证的时候可以直接拿key 看存不存在
		endpoints.tokenStore(new RedisTokenStore(redisConnectionFactory))
				//这个啊 就是让他支持密码模式 否则会报 不支持 密码模式
				.authenticationManager(authenticationManager)
				//这个可以用来验证用户名 如果你自已定义的的userDetailsService 没有也得加上 默认的
				//否则会报错Handling error: IllegalStateException, UserDetailsService is required.
				.userDetailsService(userDetailsService);
	}
	
    //用来配置令牌端点(Token Endpoint)的安全约束
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security)
			throws Exception {
    	//允许check_token 这个接口是用来解token 的 可以看到token 存放具体什么信息
		security.checkTokenAccess("permitAll()")
				//主要是让/oauth/token支持client_id以及client_secret作登录认证 否则会报Unauthorized
		.allowFormAuthenticationForClients()//允许表单认证
		;
	}

	
}
