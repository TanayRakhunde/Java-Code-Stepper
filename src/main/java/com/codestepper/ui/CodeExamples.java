package com.codestepper.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public class CodeExamples {
    
    public static final Map<String, String> EXAMPLES = new LinkedHashMap<>();
    
    static {
        EXAMPLES.put("1. Hello World", 
            "public class HelloWorld {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello World!\");\n" +
            "        int a = 5;\n" +
            "        int b = 10;\n" +
            "        int sum = a + b;\n" +
            "        System.out.println(\"Sum: \" + sum);\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("2. FizzBuzz", 
            "public class FizzBuzz {\n" +
            "    public static void main(String[] args) {\n" +
            "        for (int i = 1; i <= 15; i++) {\n" +
            "            if (i % 3 == 0 && i % 5 == 0) {\n" +
            "                System.out.println(\"FizzBuzz\");\n" +
            "            } else if (i % 3 == 0) {\n" +
            "                System.out.println(\"Fizz\");\n" +
            "            } else if (i % 5 == 0) {\n" +
            "                System.out.println(\"Buzz\");\n" +
            "            } else {\n" +
            "                System.out.println(i);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("3. Fibonacci", 
            "public class Fibonacci {\n" +
            "    public static int fib(int n) {\n" +
            "        if (n <= 1) {\n" +
            "            return n;\n" +
            "        }\n" +
            "        return fib(n - 1) + fib(n - 2);\n" +
            "    }\n\n" +
            "    public static void main(String[] args) {\n" +
            "        int result = fib(5);\n" +
            "        System.out.println(\"Fib(5) = \" + result);\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("4. Bubble Sort", 
            "public class BubbleSort {\n" +
            "    public static void main(String[] args) {\n" +
            "        int[] arr = {64, 34, 25, 12, 22, 11, 90};\n" +
            "        int n = arr.length;\n" +
            "        for (int i = 0; i < n - 1; i++) {\n" +
            "            for (int j = 0; j < n - i - 1; j++) {\n" +
            "                if (arr[j] > arr[j + 1]) {\n" +
            "                    // swap temp and arr[i]\n" +
            "                    int temp = arr[j];\n" +
            "                    arr[j] = arr[j + 1];\n" +
            "                    arr[j + 1] = temp;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        System.out.println(\"Sorted array:\");\n" +
            "        for (int i = 0; i < n; ++i) {\n" +
            "            System.out.print(arr[i] + \" \");\n" +
            "        }\n" +
            "        System.out.println();\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("5. Palindrome Check", 
            "public class Palindrome {\n" +
            "    public static void main(String[] args) {\n" +
            "        String str = \"racecar\";\n" +
            "        boolean isPalindrome = true;\n" +
            "        int n = str.length();\n" +
            "        for (int i = 0; i < n / 2; i++) {\n" +
            "            if (str.charAt(i) != str.charAt(n - i - 1)) {\n" +
            "                isPalindrome = false;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "        System.out.println(str + \" is palindrome? \" + isPalindrome);\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("6. Factorial", 
            "public class Factorial {\n" +
            "    public static int fact(int n) {\n" +
            "        if (n == 0) return 1;\n" +
            "        return n * fact(n - 1);\n" +
            "    }\n\n" +
            "    public static void main(String[] args) {\n" +
            "        int n = 5;\n" +
            "        System.out.println(\"Factorial of \" + n + \" is \" + fact(n));\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("7. Binary Search", 
            "public class BinarySearch {\n" +
            "    public static int binarySearch(int arr[], int x) {\n" +
            "        int l = 0, r = arr.length - 1;\n" +
            "        while (l <= r) {\n" +
            "            int m = l + (r - l) / 2;\n" +
            "            if (arr[m] == x) return m;\n" +
            "            if (arr[m] < x) l = m + 1;\n" +
            "            else r = m - 1;\n" +
            "        }\n" +
            "        return -1;\n" +
            "    }\n\n" +
            "    public static void main(String[] args) {\n" +
            "        int arr[] = {2, 3, 4, 10, 40};\n" +
            "        int x = 10;\n" +
            "        int result = binarySearch(arr, x);\n" +
            "        System.out.println(\"Element found at index \" + result);\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("8. ArrayList Demo", 
            "import java.util.ArrayList;\n\n" +
            "public class ArrayListDemo {\n" +
            "    public static void main(String[] args) {\n" +
            "        ArrayList list = new ArrayList();\n" +
            "        list.add(\"Apple\");\n" +
            "        list.add(\"Banana\");\n" +
            "        list.add(\"Cherry\");\n" +
            "        System.out.println(\"Size: \" + list.size());\n" +
            "        for (int i = 0; i < list.size(); i++) {\n" +
            "            System.out.println(\"Item \" + i + \": \" + list.get(i));\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("9. 2D Array Matrix", 
            "public class Matrix {\n" +
            "    public static void main(String[] args) {\n" +
            "        int[][] matrix = {\n" +
            "            {1, 2, 3},\n" +
            "            {4, 5, 6},\n" +
            "            {7, 8, 9}\n" +
            "        };\n" +
            "        int sum = 0;\n" +
            "        for (int i = 0; i < 3; i++) {\n" +
            "            for (int j = 0; j < 3; j++) {\n" +
            "                sum += matrix[i][j];\n" +
            "            }\n" +
            "        }\n" +
            "        System.out.println(\"Sum of matrix elements: \" + sum);\n" +
            "    }\n" +
            "}"
        );
        
        EXAMPLES.put("10. Simple Calculator", 
            "public class Calculator {\n" +
            "    public static void main(String[] args) {\n" +
            "        char operator = '+';\n" +
            "        double num1 = 10.5;\n" +
            "        double num2 = 5.0;\n" +
            "        double result = 0;\n" +
            "        switch (operator) {\n" +
            "            case '+': result = num1 + num2; break;\n" +
            "            case '-': result = num1 - num2; break;\n" +
            "            case '*': result = num1 * num2; break;\n" +
            "            case '/': result = num1 / num2; break;\n" +
            "            default: System.out.println(\"Invalid operator\"); return;\n" +
            "        }\n" +
            "        System.out.println(num1 + \" \" + operator + \" \" + num2 + \" = \" + result);\n" +
            "    }\n" +
            "}"
        );
    }
}
