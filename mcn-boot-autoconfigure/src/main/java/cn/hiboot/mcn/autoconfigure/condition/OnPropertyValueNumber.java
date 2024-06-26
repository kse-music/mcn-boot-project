package cn.hiboot.mcn.autoconfigure.condition;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * OnPropertyValueNumber
 *
 * @author DingHao
 * @since 2022/1/7 10:06
 */
class OnPropertyValueNumber extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> map = metadata.getAllAnnotationAttributes(ConditionalOnPropertyValueNumber.class.getName());
        List<AnnotationAttributes> allAnnotationAttributes = Collections.emptyList();
        if (map != null) {
            allAnnotationAttributes = annotationAttributesFromMultiValueMap(map);
        }
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();
        for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
            ConditionOutcome outcome = determineOutcome(annotationAttributes, context.getEnvironment());
            (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
        }
        if (!noMatch.isEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
        }
        return ConditionOutcome.match(ConditionMessage.of(match));
    }

    private List<AnnotationAttributes> annotationAttributesFromMultiValueMap(MultiValueMap<String, Object> multiValueMap) {
        List<Map<String, Object>> maps = new ArrayList<>();
        multiValueMap.forEach((key, value) -> {
            for (int i = 0; i < value.size(); i++) {
                Map<String, Object> map;
                if (i < maps.size()) {
                    map = maps.get(i);
                }
                else {
                    map = new HashMap<>();
                    maps.add(map);
                }
                map.put(key, value.get(i));
            }
        });
        List<AnnotationAttributes> annotationAttributes = new ArrayList<>(maps.size());
        for (Map<String, Object> map : maps) {
            annotationAttributes.add(AnnotationAttributes.fromMap(map));
        }
        return annotationAttributes;
    }

    private ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes, PropertyResolver resolver) {
        return new OnPropertyValueNumber.Spec(annotationAttributes).check(resolver);
    }

    private static class Spec {

        private final String prefix;
        private final String name;
        private final int min;
        private final int max;

        Spec(AnnotationAttributes annotationAttributes) {
            String prefix = annotationAttributes.getString("prefix").trim();
            if (StringUtils.hasText(prefix) && !prefix.endsWith(".")) {
                prefix = prefix + ".";
            }
            this.prefix = prefix;
            this.min = annotationAttributes.getNumber("min");
            this.max = annotationAttributes.getNumber("max");
            this.name = annotationAttributes.getString("name").trim();
            Assert.state(McnUtils.isNotNullAndEmpty(name),"The name or value attribute of @ConditionalOnPropertyValueNumber must be specified");
        }

        private ConditionOutcome check(PropertyResolver resolver) {
            String key = this.prefix + name;
            String[] dbs = resolver.getProperty(key, String[].class);
            if(dbs != null && dbs.length >= min && dbs.length <= max){
                return ConditionOutcome.match("match " + dbs.length + " value");
            }
            return ConditionOutcome.noMatch("no value number >= " + min + " and number <= " + max);
        }


        @Override
        public String toString() {
            return "(" +
                    this.prefix + this.name +
                    ",min = " + this.min +
                    ",max = " + this.max +
                    ")";
        }

    }
}
