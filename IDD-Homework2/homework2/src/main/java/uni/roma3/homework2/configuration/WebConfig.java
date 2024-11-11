package uni.roma3.homework2.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/all_htmls/**")
                .addResourceLocations("file:C:/Users/gi.gaglione/Desktop/IDD-Homeworks/IDD-Homeworks/IDD-Homework2/urls_htmls_tables/all_htmls/");
    }
}
