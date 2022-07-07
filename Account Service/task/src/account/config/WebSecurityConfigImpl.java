package account.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class WebSecurityConfigImpl extends WebSecurityConfigurerAdapter {
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    public void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .accessDeniedHandler(getAccessDeniedHandler());

        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handle auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests()// manage access
                .mvcMatchers(HttpMethod.POST, "/api/auth/changepass")
                    .hasAnyRole("ADMINISTRATOR", "ACCOUNTANT", "USER")
                .mvcMatchers(HttpMethod.GET, "api/empl/payment")
                    .hasAnyRole( "ACCOUNTANT", "USER")
                .mvcMatchers(HttpMethod.PUT, "api/acct/payments")
                    .hasRole( "ACCOUNTANT")
                .mvcMatchers(HttpMethod.POST, "/api/acct/payments")
                    .hasRole( "ACCOUNTANT")
                .mvcMatchers("/api/admin/user/**").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "api/security/events")
                .hasRole("AUDITOR")
                .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
    }

    @Bean
    public AccessDeniedHandler getAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

}
