package lession05;

public class BuilderPatterDemo {

    public static void main(String[] args) {
        Student build = new ConCreateStudent()
                .setName("张三")
                .setAge(28)
                .setHeight(199)
                .build();
        System.out.println(build);
    }

    public static class Student {
        private String name;
        private int age;
        private int height;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }


        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", height=" + height +
                    '}';
        }
    }


    public interface Builder {
        Builder setName(String name);

        Builder setAge(int age);

        Builder setHeight(int height);

        Student build();
    }


    public static class ConCreateStudent implements Builder {

        Student student = new Student();

        public Builder setName(String name) {
            if("张三".equals(name)){
                name = "超级张三";
            }
            student.setName(name);
            return this;
        }

        public Builder setAge(int age) {
            System.out.println("非常复杂的逻辑2");
            student.setAge(age);
            return this;
        }

        public Builder setHeight(int height) {
            System.out.println("非常复杂的逻辑3");
            student.setHeight(height);
            return this;
        }

        public Student build() {
            return student;
        }
    }

}
