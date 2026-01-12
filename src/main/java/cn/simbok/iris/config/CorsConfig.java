package cn.simbok.iris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局CORS跨域配置
 * 解决浏览器跨域访问问题，特别是预检请求（OPTIONS）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源（生产环境建议指定具体域名）
        config.addAllowedOriginPattern("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有HTTP方法（GET, POST, PUT, DELETE, OPTIONS等）
        config.addAllowedMethod("*");
        
        // 允许携带凭证（cookies、authorization headers等）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒），减少OPTIONS请求次数
        config.setMaxAge(3600L);
        
        // 注册CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
