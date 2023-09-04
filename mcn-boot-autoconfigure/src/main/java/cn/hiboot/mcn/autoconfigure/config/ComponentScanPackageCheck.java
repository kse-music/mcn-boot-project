package cn.hiboot.mcn.autoconfigure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * ComponentScanPackageCheck
 *
 * @author DingHao
 * @since 2022/7/14 17:41
 */
public class ComponentScanPackageCheck {
    private final Logger log = LoggerFactory.getLogger(ComponentScanPackageCheck.class);

    private static final Set<String> PROBLEM_PACKAGES = new HashSet<>();

    public ComponentScanPackageCheck(){
        PROBLEM_PACKAGES.add("cn");
        PROBLEM_PACKAGES.add("cn.hiboot");
    }

    public void check(BeanDefinitionRegistry registry) {
        String message = getWarning(registry);
        if (StringUtils.hasLength(message)) {
            log.warn(String.format("%n%n** WARNING ** : %s%n%n", message));
        }
    }

    private String getWarning(BeanDefinitionRegistry registry) {
        Set<String> scannedPackages = getComponentScanningPackages(registry);
        List<String> problematicPackages = getProblematicPackages(scannedPackages);
        if (problematicPackages.isEmpty()) {
            return null;
        }
        return "Your ApplicationContext is unlikely to start due to a @ComponentScan of "
                + StringUtils.collectionToDelimitedString(problematicPackages, ", ") + ".";
    }

    private Set<String> getComponentScanningPackages(BeanDefinitionRegistry registry) {
        Set<String> packages = new LinkedHashSet<>();
        String[] names = registry.getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition definition = registry.getBeanDefinition(name);
            if (definition instanceof AnnotatedBeanDefinition annotatedDefinition) {
                addComponentScanningPackages(packages, annotatedDefinition.getMetadata());
            }
        }
        return packages;
    }

    private void addComponentScanningPackages(Set<String> packages, AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(metadata.getAnnotationAttributes(ComponentScan.class.getName(), true));
        if (attributes != null) {
            addPackages(packages, attributes.getStringArray("value"));
            addPackages(packages, attributes.getStringArray("basePackages"));
            addClasses(packages, attributes.getStringArray("basePackageClasses"));
            if (packages.isEmpty()) {
                packages.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
        }
    }

    private void addPackages(Set<String> packages, String[] values) {
        if (values != null) {
            Collections.addAll(packages, values);
        }
    }

    private void addClasses(Set<String> packages, String[] values) {
        if (values != null) {
            for (String value : values) {
                packages.add(ClassUtils.getPackageName(value));
            }
        }
    }

    private List<String> getProblematicPackages(Set<String> scannedPackages) {
        List<String> problematicPackages = new ArrayList<>();
        for (String scannedPackage : scannedPackages) {
            if (isProblematicPackage(scannedPackage)) {
                problematicPackages.add(getDisplayName(scannedPackage));
            }
        }
        return problematicPackages;
    }

    private boolean isProblematicPackage(String scannedPackage) {
        if (scannedPackage == null || scannedPackage.isEmpty()) {
            return true;
        }
        return PROBLEM_PACKAGES.contains(scannedPackage);
    }

    private String getDisplayName(String scannedPackage) {
        if (scannedPackage == null || scannedPackage.isEmpty()) {
            return "the default package";
        }
        return "'" + scannedPackage + "'";
    }

}
