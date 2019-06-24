package com.yoloho.enhanced.common.util;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.common.util.BeansUtil;

public class BeansUtilTest {
    public static class Demo {
        private int id;
        private long parentId;
        private String name;
        //没有getter/setter
        public String content;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public long getParentId() {
            return parentId;
        }
        public void setParentId(long parentId) {
            this.parentId = parentId;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
    public static class Demo1 {
        private Integer id;
        private Long parentId;
        private String name;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public Long getParentId() {
            return parentId;
        }
        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
    @Test
    public void normalTest() {
        Demo demo1 = new Demo();
        demo1.setId(1);
        demo1.setParentId(2);
        demo1.setName("test");
        demo1.content = "demo";
        Demo demo2 = new Demo();
        BeansUtil.copyBean(demo1, demo2);
        Assert.assertEquals(demo1.getId(), demo2.getId());
        Assert.assertEquals(demo1.getParentId(), demo2.getParentId());
        Assert.assertEquals(demo1.getName(), demo2.getName());
        Assert.assertNotEquals(demo1.content, demo2.content);
    }
    
    @Test
    public void primativeTest() {
        Demo demo1 = new Demo();
        demo1.setId(1);
        demo1.setParentId(2);
        demo1.setName("test");
        demo1.content = "demo";
        Demo1 demo2 = new Demo1();
        BeansUtil.copyBean(demo1, demo2);
        Assert.assertEquals(Integer.valueOf(demo1.getId()), demo2.getId());
        Assert.assertEquals(Long.valueOf(demo1.getParentId()), demo2.getParentId());
        Assert.assertEquals(demo1.getName(), demo2.getName());
    }

}
