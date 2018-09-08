package com.tpsix.mybatis.wrapper;

/**
 * mybatis mapper locations 处理
 *
 * @see MybatisProperties
 * @author zhangyin
 * @since 1.0
 */
public interface MybatisMapperProcess {

    /**
     * 需要导入的 location
     * @return
     */
    String[] includes();

    /**
     * 需要排除的 location
     * @return
     */
    String[] excludes();

}
