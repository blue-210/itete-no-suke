package app.itetenosuke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Autowired private UserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/webjars/**", "/css/**", "/images/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/webjars/**")
        .permitAll()
        .antMatchers("/js/**")
        .permitAll()
        .antMatchers("/css/**")
        .permitAll()
        .antMatchers("/images/**")
        .permitAll()
        .antMatchers("/login")
        .permitAll()
        .antMatchers("/signup")
        .permitAll()
        // テスト用に一時的に追加
        .antMatchers("/v1/**")
        .permitAll()
        .antMatchers("/actuator/health")
        .permitAll()
        .anyRequest()
        .authenticated();

    http.formLogin()
        .loginProcessingUrl("/login")
        .loginPage("/login")
        .failureUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .defaultSuccessUrl("/home", true);

    http.logout()
        // .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login")
        .and()
        .rememberMe()
        .alwaysRemember(false)
        .useSecureCookie(true);
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
