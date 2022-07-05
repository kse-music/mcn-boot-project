package cn.hiboot.mcn.autoconfigure.mongo;

import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * MongoExtensionAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/5 13:08
 */
@AutoConfiguration(before = MongoAutoConfiguration.class)
@EnableConfigurationProperties(MongoExtensionProperties.class)
@ConditionalOnClass(MongoClient.class)
@ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDatabaseFactory")
public class MongoExtensionAutoConfiguration {

    @Bean
    MongoClientSettingsBuilderCustomizer defaultMongoClientSettingsBuilderCustomizer(MongoExtensionProperties mongo){
        return builder -> {

            builder.applyToConnectionPoolSettings(connectionPool -> {
                connectionPool.maxSize(mongo.getMaxSize());
                connectionPool.minSize(mongo.getMinSize());
                connectionPool.maxWaitTime(mongo.getMaxWaitTime(),TimeUnit.MILLISECONDS);
            });

            builder.readPreference(ReadPreference.valueOf(mongo.getReadPreference().name()));

            builder.applyToSocketSettings(socket -> {
                socket.connectTimeout(mongo.getConnectTimeout(),TimeUnit.MILLISECONDS);
                socket.readTimeout(mongo.getReadTimeout(),TimeUnit.MILLISECONDS);
            });

        };
    }

}
