package com.github.lemniscate.sql.injector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import javax.xml.bind.annotation.*;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author dave 1/22/15 1:17 PM
 */
public class SqlAnnotationPostProcessor implements BeanPostProcessor {

    private final Jaxb2Marshaller xmlMarshaller;
    private final ResourceLoader resourceLoader;


    public SqlAnnotationPostProcessor() {
        this(JaxB(), new DefaultResourceLoader());
    }

    @Inject
    public SqlAnnotationPostProcessor(Jaxb2Marshaller xmlMarshaller, ResourceLoader resourceLoader) {
        this.xmlMarshaller = xmlMarshaller;
        this.resourceLoader = resourceLoader;
    }

    public String readQuery(String fileName, String key){
        InputStream stream;
        try {
            Resource resource = resourceLoader.getResource(fileName);
            stream = resource.getInputStream();
        }catch(IOException e){
            throw new RuntimeException("Failed parsing resource: " + fileName);
        }
        Sql map = (Sql) xmlMarshaller.unmarshal(new StreamSource(stream));
        String value = map.get(key);
        return value;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @SuppressWarnings("unchecked")
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);
                if (field.getAnnotation(SqlQuery.class) != null) {
                    SqlQuery annotation = field.getAnnotation(SqlQuery.class);
                    String fileName = annotation.resourcePath().isEmpty()
                        ? "classpath:" + bean.getClass().getSimpleName() + ".xml"
                        : annotation.resourcePath();
                    String key = annotation.value().isEmpty() ? field.getName() : annotation.value();
                    String value = readQuery(fileName, key);
                    field.set(bean, value);
                }
            }
        });

        return bean;
    }

    // TODO Unmarshal directly to a map, so we can get rid of these ugly classes
    @XmlRootElement(name="sql")
    private static class Sql{
        @XmlElements( @XmlElement(name="query", type = Query.class) )
        private List<Query> queries;
        public String get(String key){
            for(Query q : queries){
                if( q.key.equals(key) ){
                    return q.value;
                }
            }
            return null;
        }
    }

    private static class Query{
        @XmlAttribute
        private String key;
        @XmlValue
        private String value;
    }

    private static Jaxb2Marshaller JaxB(){
        try {
            Jaxb2Marshaller jaxb = new Jaxb2Marshaller();
            // Add this class' package, so the nested objects are picked up
            jaxb.setPackagesToScan(new String[]{Sql.class.getPackage().getName()});
            jaxb.afterPropertiesSet();
            return jaxb;
        }catch(Exception e){
            throw new RuntimeException("Failed configuring JaxB");
        }
    }

}
