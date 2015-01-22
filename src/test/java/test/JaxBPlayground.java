package test;

import com.github.lemniscate.sql.injector.SqlAnnotationPostProcessor;
import com.github.lemniscate.sql.injector.SqlQuery;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.junit.Assert.assertEquals;

/**
 * @Author dave 1/22/15 1:24 PM
 */
public class JaxBPlayground {

    public static class Example{
        @SqlQuery
        public String dummyQuery;

        @SqlQuery
        public String dummyQuery2;

        @SqlQuery("customKey")
        public String thisIsDifferent;
    }

    public static class Example2{
        @SqlQuery(resourcePath = "classpath:Example.xml")
        public String dummyQuery;

        @SqlQuery(resourcePath = "classpath:Example.xml")
        public String dummyQuery2;

        @SqlQuery(value="customKey", resourcePath = "classpath:Example.xml")
        public String thisIsDifferent;
    }

    @Test
    public void testInjection(){
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("example", new RootBeanDefinition(Example.class));
        bf.addBeanPostProcessor(new SqlAnnotationPostProcessor());

        Example ex = bf.getBean(Example.class);
        assertEquals(ex.dummyQuery, "select 1;");
        assertEquals(ex.dummyQuery2.replaceAll("\\n", "").trim(), "select 2;");
        assertEquals(ex.thisIsDifferent, "select 3;");
    }

    @Test
    public void testInjectionAlternatePath(){
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("example", new RootBeanDefinition(Example2.class));
        bf.addBeanPostProcessor(new SqlAnnotationPostProcessor());

        Example2 ex = bf.getBean(Example2.class);
        assertEquals(ex.dummyQuery, "select 1;");
        assertEquals(ex.dummyQuery2.replaceAll("\\n", "").trim(), "select 2;");
        assertEquals(ex.thisIsDifferent, "select 3;");
    }
}
