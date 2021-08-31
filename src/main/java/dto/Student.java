package dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

public class Student {
    @JSONField(ordinal = 0)
    public String name;

    @JSONField(ordinal = 1)
    public String age;

    @JSONField(ordinal = 2)
    public Map<Integer, Student> friends = new HashMap();

    public Student(String name, String age) {
        this.name = name;
        this.age = age;

    }

    public static void main(String[] args) {
        Student yanglei = new Student("yanglei", "24");
        yanglei.friends.put(0, new Student("linkui", "24"));
        yanglei.friends.put(1, new Student("yangzy", "24"));

        Object o = JSONObject.toJSON(yanglei);
        System.out.println(o);

        String s = JSON.toJSONString(yanglei);
        System.out.println(s);

        Student student = JSON.parseObject(s, Student.class);
        System.out.println(student.name);
    }
}
