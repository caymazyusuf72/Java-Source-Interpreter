class Fibonacci {
    int calculate(int n) {
        if (n <= 1) {
            return n;
        }
        return calculate(n - 1) + calculate(n - 2);
    }
}

class Main {
    void main() {
        Fibonacci fib = new Fibonacci();
        
        System.out.println(fib.calculate(0));
        System.out.println(fib.calculate(1));
        System.out.println(fib.calculate(5));
        System.out.println(fib.calculate(10));
    }
}