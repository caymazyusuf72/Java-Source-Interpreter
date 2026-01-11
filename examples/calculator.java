class Calculator {
    int add(int a, int b) {
        return a + b;
    }
    
    int multiply(int a, int b) {
        return a * b;
    }
    
    int subtract(int a, int b) {
        return a - b;
    }
}

class Main {
    void main() {
        Calculator calc = new Calculator();
        
        int sum = calc.add(5, 3);
        System.out.println(sum);
        
        int product = calc.multiply(sum, 2);
        System.out.println(product);
        
        int diff = calc.subtract(product, 5);
        System.out.println(diff);
    }
}