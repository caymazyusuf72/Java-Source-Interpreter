class Main {
    void main() {
        // Test if/else
        int x = 10;
        if (x > 5) {
            System.out.println(1);
        } else {
            System.out.println(0);
        }
        
        // Test while loop
        int i = 0;
        while (i < 5) {
            System.out.println(i);
            i = i + 1;
        }
        
        // Test for loop
        int sum = 0;
        for (int j = 1; j <= 10; j = j + 1) {
            sum = sum + j;
        }
        System.out.println(sum);
    }
}