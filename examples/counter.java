class Counter {
    int count;
    
    void increment() {
        count = count + 1;
    }
    
    void decrement() {
        count = count - 1;
    }
    
    int getValue() {
        return count;
    }
}

class Main {
    void main() {
        Counter c = new Counter();
        
        System.out.println(c.getValue());
        
        c.increment();
        c.increment();
        c.increment();
        System.out.println(c.getValue());
        
        c.decrement();
        System.out.println(c.getValue());
        
        int i = 0;
        while (i < 5) {
            c.increment();
            i = i + 1;
        }
        System.out.println(c.getValue());
    }
}