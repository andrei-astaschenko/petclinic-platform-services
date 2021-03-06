package com.epam.petclinic.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    /**
     * Configures permissions for clients in memory of authentication server and passes the authorities for
     * resource server.
     * If user obtains credentials as 'client_credentials' grant type, his authorities base only on 'clients'.
     * If user obtains credentials as 'password' grant type, his authorities base only
     * on {@link AuthenticationManagerBuilder} configuration (see {@link WebSecurityConfig}).
     *
     * @param clients {@link ClientDetailsServiceConfigurer}
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //TODO encrypt the value for 'secret'
        clients.inMemory()
                .withClient("ui")
                .secret("123")
                .resourceIds("ORDER-SERVICE", "CLINIC-SERVICE", "GATEWAY-SERVICE")
                // TODO: investigate 'scopes' influence on client permissions
                .scopes("create", "read", "update", "delete")
                .autoApprove(true)
                .authorities("admin")
                .authorizedGrantTypes("implicit", "refresh_token", "password", "authorization_code",
                        "client_credentials")
                .refreshTokenValiditySeconds(600)
                .accessTokenValiditySeconds(300);
    }

    /**
     * Configures the OAuth2 endpoints to adjust the authentication manager which will represent the web-security users,
     * and JWT token store configuration.
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore()).tokenEnhancer(jwtAccessTokenConverter).authenticationManager(authenticationManager);
    }

    @Bean
    protected TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        //TODO encrypt 'mySecretKey'
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"),
                "mySecretKey".toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt"));
        return converter;
    }
}
