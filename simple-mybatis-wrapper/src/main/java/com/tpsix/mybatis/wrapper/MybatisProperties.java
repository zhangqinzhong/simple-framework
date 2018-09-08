package com.tpsix.mybatis.wrapper;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 扩展 MyBatis 配置类, 继承官方配置类
 *
 * <ul>
 *     <li>排除 Mapper</li>
 * </ul>
 *
 * @author zhangyin
 * @see org.mybatis.spring.boot.autoconfigure.MybatisProperties
 * @since 1.0
 */
@Getter
@Setter
public class MybatisProperties extends org.mybatis.spring.boot.autoconfigure.MybatisProperties {


    /**
     * 默认 Mapper 路径
     */
    private static final String defaultMapperLocations = "classpath:mappers/*/*.xml";

    private ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();

    /**
     * 要排除的 mapper.xml 路径
     */
    private String[] excludeMapperLocations;

    /**
     * Class 形式配置, 需要实现 MybatisMapperProcess 并注册为 Bean
     * @see MybatisMapperProcess
     */
    @Autowired(required = false)
    private List<MybatisMapperProcess> mybatisMapperProcesses;


    /**
     * 重写 resolveMapperLocations , 排除不需要的 Mapper
     * @return
     */
    @Override
    public Resource[] resolveMapperLocations() {

        // 记录需要的 Mapper
        Map<String, Resource> resources = new HashMap<>(50);

        // 导入的 Mapper
        Set<String> locations;

        // 排除的 Mapper
        Set<String> excludes = new HashSet<>();

        // mapperLocations
        String[] mybatisMapperLocations = getMapperLocations();

        if (null != mybatisMapperLocations) {
            // 已经配置, 转为 Set
            locations = Arrays.stream(mybatisMapperLocations).collect(Collectors.toSet());
        } else {
            locations = new HashSet<>();
        }

        // 添加默认路径
        locations.add(defaultMapperLocations);

        if (null != mybatisMapperProcesses) {
            // 需要导入的
            mybatisMapperProcesses.stream()
                    // 获取需要导入的 mapper 路径
                    .map(MybatisMapperProcess::includes)
                    // 转换为 String
                    .flatMap(Arrays::stream)
                    // 添加到 locations
                    .forEach(locations::add);

            // 需要排除的
            mybatisMapperProcesses.stream()
                    .map(MybatisMapperProcess::excludes)
                    .flatMap(Arrays::stream)
                    .forEach(excludes::add);
        }

        // 添加需要的配置
        locations.forEach(location -> {
            Resource[] mappers;
            try {
                mappers = resourceLoader.getResources(location);
                for (Resource mapper : mappers) {
                    String mapperUrl = mapper.getURL().toString();
                    resources.put(mapperUrl, mapper);
                }
            } catch (IOException e) {}
        });


        // properties 文件中如果配置 excludeMapperLocations, 则会执行
        if (excludeMapperLocations != null
                &&  excludeMapperLocations.length > 0) {
            for (String excludeMapperLocation : excludeMapperLocations) {
                excludes.add(excludeMapperLocation);
            }
        }

        // 排除不需要的配置
        excludes.forEach(exclude -> {
            try {
                Resource[] excludeMappers = resourceLoader.getResources(exclude);
                for (Resource excludeMapper : excludeMappers) {
                    String mapperUrl = excludeMapper.getURL().toString();
                    resources.remove(mapperUrl);
                }
            } catch (IOException e) {}
        });
        Resource[] mapperLocations = new Resource[resources.size()];
        return resources.values().toArray(mapperLocations);


    }
}
