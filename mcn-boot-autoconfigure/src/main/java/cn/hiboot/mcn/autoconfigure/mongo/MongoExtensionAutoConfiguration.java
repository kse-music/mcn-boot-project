package cn.hiboot.mcn.autoconfigure.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * MongoExtensionAutoConfiguration
 *
 * @author DingHao
 * @since 2022/7/5 13:08
 */
@AutoConfiguration
@EnableConfigurationProperties(MongoExtensionProperties.class)
@ConditionalOnClass(MongoClient.class)
@ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDatabaseFactory")
public class MongoExtensionAutoConfiguration {

    private static final Map<String, ReadConcern> NAMED_CONCERNS;

    static {
        NAMED_CONCERNS = new HashMap<>();
        for (final Field f : ReadConcern.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(ReadConcern.class)) {
                String key = f.getName().toLowerCase();
                try {
                    NAMED_CONCERNS.put(key, (ReadConcern) f.get(null));
                } catch (IllegalAccessException e) {
                    //ignore;
                }
            }
        }
    }

    @Bean
    MongoClientSettingsBuilderCustomizer defaultMongoClientSettingsBuilderCustomizer(MongoExtensionProperties mongo){
        return builder -> {

            builder.applyToConnectionPoolSettings(connectionPool -> {
                connectionPool.maxSize(mongo.getPool().getMaxSize());
                connectionPool.minSize(mongo.getPool().getMinSize());
                connectionPool.maxWaitTime(mongo.getPool().getMaxWaitTime(),TimeUnit.MILLISECONDS);
            });

            builder.readPreference(ReadPreference.valueOf(mongo.getReadPreference().name()));
            builder.writeConcern(WriteConcern.valueOf(mongo.getWriteConcern().name()));
            builder.readConcern(Objects.isNull(mongo.getReadConcern()) ? ReadConcern.DEFAULT : NAMED_CONCERNS.get(mongo.getReadConcern().name()));

            builder.applyToSocketSettings(socket -> {
                socket.connectTimeout(mongo.getSocket().getConnectTimeout(),TimeUnit.MILLISECONDS);
                socket.readTimeout(mongo.getSocket().getReadTimeout(),TimeUnit.MILLISECONDS);
            });

            if(mongo.isAutoPojo()){
                builder.codecRegistry(CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())));
            }

        };
    }

}
