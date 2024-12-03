//package ee.ctob.config;
//
//import org.springdoc.core.SpringDocUtils;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.PostConstruct;
//
//@Configuration
//public class SwaggerConfig {
//
//    @PostConstruct
//    public void init() {
//        // Disable required field validation for some classes globally if needed
//        SpringDocUtils.getConfig().addRequestWrapperToIgnore(MyRequestModel.class);
//    }
//}