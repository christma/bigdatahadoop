package lession05;

public class DecoratePatterDemo {

    public static void main(String[] args) {
        SuperPerson superPerson = new SuperPerson(new Person());
        superPerson.superEat();
    }


    public static class Person {
        public void eat() {
            System.out.println("吃饭");
        }
    }

    public static class SuperPerson {
        private Person person;

        public SuperPerson(Person person) {
            this.person = person;
        }

        public void superEat() {
            System.out.println("先跑两圈");
            person.eat();
            System.out.println("再跑两圈");
        }
    }

}
