package com.codestepper.interpreter;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

import javax.swing.*;
import java.util.*;

public class JavaASTInterpreter {

    private final ExecutionTrace trace;
    private final int MAX_STEPS = 10000;
    private int stepCount = 0;
    private Environment globalEnv;
    private List<String> callStack;
    private StringBuilder consoleOut;
    private boolean isError = false;

    // Class definitions
    private final Map<String, ClassOrInterfaceDeclaration> classDecls = new HashMap<>();
    private final Map<String, MethodDeclaration> globalMethods = new HashMap<>();

    public JavaASTInterpreter() {
        this.trace = new ExecutionTrace();
        this.globalEnv = new Environment();
        this.callStack = new ArrayList<>();
        this.consoleOut = new StringBuilder();
    }

    public ExecutionTrace parseAndExecute(String code, String mode) {
        try {
            if ("Code Snippet".equals(mode)) {
                BlockStmt block = StaticJavaParser.parseBlock("{ " + code + " }");
                callStack.add("main() snippet");
                executeBlock(block, globalEnv);
            } else if ("Main Method Only".equals(mode)) {
                MethodDeclaration method = (MethodDeclaration) StaticJavaParser.parseBodyDeclaration(code);
                globalMethods.put(method.getNameAsString(), method);
                callStack.add(method.getNameAsString() + "()");
                if (method.getBody().isPresent()) {
                    executeBlock(method.getBody().get(), globalEnv);
                }
            } else {
                CompilationUnit cu = StaticJavaParser.parse(code);
                for (TypeDeclaration<?> type : cu.getTypes()) {
                    if (type instanceof ClassOrInterfaceDeclaration) {
                        ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) type;
                        classDecls.put(cid.getNameAsString(), cid);
                        
                        // Find main method
                        MethodDeclaration mainMethod = null;
                        for (MethodDeclaration method : cid.getMethods()) {
                            if (method.getNameAsString().equals("main") && method.isStatic()) {
                                mainMethod = method;
                            }
                        }
                        if (mainMethod != null && mainMethod.getBody().isPresent()) {
                            callStack.add(cid.getNameAsString() + ".main()");
                            executeBlock(mainMethod.getBody().get(), globalEnv);
                        } else if (!cid.getMethods().isEmpty()) {
                            // execute first method if no main
                             MethodDeclaration m = cid.getMethods().get(0);
                             if (m.getBody().isPresent()) {
                                callStack.add(cid.getNameAsString() + "." + m.getNameAsString() + "()");
                                executeBlock(m.getBody().get(), globalEnv);
                             }
                        }
                    }
                }
            }
        } catch (ReturnException re) {
            // normal termination of top level
        } catch (Exception e) {
            recordErrorStep(-1, "RuntimeException: " + e.getMessage());
        }
        return trace;
    }

    private void recordStep(Node node, String description, Environment env) {
        if (stepCount++ >= MAX_STEPS) {
            throw new RuntimeException("Maximum execution steps exceeded (" + MAX_STEPS + "). Potential infinite loop.");
        }
        int line = node.getBegin().isPresent() ? node.getBegin().get().line : -1;
        trace.addStep(new ExecutionStep(
                line,
                description,
                env.getSnapshot(),
                new ArrayList<>(callStack),
                consoleOut.toString(),
                ExecutionStep.HighlightType.CURRENT,
                false
        ));
    }

    private void recordErrorStep(int line, String message) {
        isError = true;
        trace.addStep(new ExecutionStep(
                line,
                message,
                globalEnv.getSnapshot(),
                new ArrayList<>(callStack),
                consoleOut.toString(),
                ExecutionStep.HighlightType.ERROR,
                true
        ));
    }

    // Execution methods
    private void executeStmt(Statement stmt, Environment env) {
        if (isError) return;

        if (stmt instanceof BlockStmt) {
            executeBlock((BlockStmt) stmt, new Environment(env));
        } else if (stmt instanceof ExpressionStmt) {
            Expression expr = ((ExpressionStmt) stmt).getExpression();
            recordStep(stmt, "Evaluating expression", env);
            evaluate(expr, env);
        } else if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;
            recordStep(ifStmt.getCondition(), "Checking if condition", env);
            Value cond = evaluate(ifStmt.getCondition(), env);
            if (cond.asBoolean()) {
                recordStep(ifStmt.getThenStmt(), "Taking 'then' branch", env);
                executeStmt(ifStmt.getThenStmt(), env);
            } else if (ifStmt.getElseStmt().isPresent()) {
                recordStep(ifStmt.getElseStmt().get(), "Taking 'else' branch", env);
                executeStmt(ifStmt.getElseStmt().get(), env);
            }
        } else if (stmt instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt) stmt;
            while (true) {
                recordStep(whileStmt.getCondition(), "Checking while condition", env);
                Value cond = evaluate(whileStmt.getCondition(), env);
                if (!cond.asBoolean()) break;
                executeStmt(whileStmt.getBody(), env);
            }
        } else if (stmt instanceof DoStmt) {
            DoStmt doStmt = (DoStmt) stmt;
            while (true) {
                executeStmt(doStmt.getBody(), env);
                recordStep(doStmt.getCondition(), "Checking do-while condition", env);
                Value cond = evaluate(doStmt.getCondition(), env);
                if (!cond.asBoolean()) break;
            }
        } else if (stmt instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) stmt;
            Environment forEnv = new Environment(env);
            for (Expression init : forStmt.getInitialization()) {
                recordStep(init, "For loop initialization", forEnv);
                evaluate(init, forEnv);
            }
            while (true) {
                if (forStmt.getCompare().isPresent()) {
                    recordStep(forStmt.getCompare().get(), "Checking for loop condition", forEnv);
                    Value cond = evaluate(forStmt.getCompare().get(), forEnv);
                    if (!cond.asBoolean()) break;
                }
                executeStmt(forStmt.getBody(), forEnv);
                for (Expression update : forStmt.getUpdate()) {
                    recordStep(update, "For loop update", forEnv);
                    evaluate(update, forEnv);
                }
            }
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt retStmt = (ReturnStmt) stmt;
            Value retVal = Value.NULL;
            if (retStmt.getExpression().isPresent()) {
                recordStep(retStmt, "Evaluating return expression", env);
                retVal = evaluate(retStmt.getExpression().get(), env);
            }
            recordStep(retStmt, "Returning from method", env);
            throw new ReturnException(retVal);
        } else if (stmt instanceof SwitchStmt) {
             SwitchStmt sw = (SwitchStmt) stmt;
             recordStep(sw.getSelector(), "Evaluating switch selector", env);
             Value selector = evaluate(sw.getSelector(), env);
             boolean matched = false;
             for (SwitchEntry entry : sw.getEntries()) {
                 if (!matched) {
                     if (entry.getLabels().isEmpty()) {
                         matched = true; // default
                     } else {
                         for (Expression label : entry.getLabels()) {
                             Value labelVal = evaluate(label, env);
                             if (Objects.equals(selector.getUnderlying(), labelVal.getUnderlying())) {
                                 matched = true;
                                 break;
                             }
                         }
                     }
                 }
                 if (matched) {
                     for (Statement s : entry.getStatements()) {
                         if (s instanceof BreakStmt) {
                             return; // break out of switch
                         }
                         executeStmt(s, env);
                     }
                 }
             }
        } else {
            // Unsupported or empty statement
            if (!(stmt instanceof EmptyStmt)) {
                recordStep(stmt, "Skipped unsupported statement: " + stmt.getClass().getSimpleName(), env);
            }
        }
    }

    private void executeBlock(BlockStmt block, Environment env) {
        for (Statement stmt : block.getStatements()) {
            executeStmt(stmt, env);
            if (isError) break;
        }
    }

    private Value evaluate(Expression expr, Environment env) {
        if (expr instanceof IntegerLiteralExpr) {
            return new Value("int", ((IntegerLiteralExpr) expr).asNumber().intValue());
        } else if (expr instanceof DoubleLiteralExpr) {
            return new Value("double", ((DoubleLiteralExpr) expr).asDouble());
        } else if (expr instanceof BooleanLiteralExpr) {
            return new Value("boolean", ((BooleanLiteralExpr) expr).getValue());
        } else if (expr instanceof StringLiteralExpr) {
            return new Value("String", ((StringLiteralExpr) expr).getValue());
        } else if (expr instanceof CharLiteralExpr) {
            return new Value("char", ((CharLiteralExpr) expr).getValue().charAt(0));
        } else if (expr instanceof NameExpr) {
            String name = ((NameExpr) expr).getNameAsString();
            return env.get(name);
        } else if (expr instanceof VariableDeclarationExpr) {
            VariableDeclarationExpr vde = (VariableDeclarationExpr) expr;
            for (VariableDeclarator vd : vde.getVariables()) {
                Value initVal = Value.NULL;
                if (vd.getInitializer().isPresent()) {
                    initVal = evaluate(vd.getInitializer().get(), env);
                }
                env.define(vd.getNameAsString(), initVal);
                recordStep(expr, "Declared variable " + vd.getNameAsString(), env);
            }
            return Value.NULL;
        } else if (expr instanceof AssignExpr) {
            AssignExpr assign = (AssignExpr) expr;
            Value right = evaluate(assign.getValue(), env);
            if (assign.getTarget() instanceof NameExpr) {
                String name = ((NameExpr) assign.getTarget()).getNameAsString();
                
                // handle +=, -= etc
                if (assign.getOperator() != AssignExpr.Operator.ASSIGN) {
                    Value left = env.get(name);
                    right = performBinaryMath(left, right, getMathOp(assign.getOperator()));
                }
                
                env.assign(name, right);
                recordStep(assign, "Assigned to " + name, env);
            } else if (assign.getTarget() instanceof ArrayAccessExpr) {
                ArrayAccessExpr acc = (ArrayAccessExpr) assign.getTarget();
                Value arrayVal = evaluate(acc.getName(), env);
                Value indexVal = evaluate(acc.getIndex(), env);
                
                if (assign.getOperator() != AssignExpr.Operator.ASSIGN) {
                    Value left = getArrayElement(arrayVal, indexVal.asNumber().intValue());
                    right = performBinaryMath(left, right, getMathOp(assign.getOperator()));
                }
                
                setArrayElement(arrayVal, indexVal.asNumber().intValue(), right);
                recordStep(assign, "Assigned to array element", env);
            }
            return right;
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr bin = (BinaryExpr) expr;
            Value left = evaluate(bin.getLeft(), env);
            
            // Short-circuit
            if (bin.getOperator() == BinaryExpr.Operator.AND) {
                if (!left.asBoolean()) return new Value("boolean", false);
                return new Value("boolean", evaluate(bin.getRight(), env).asBoolean());
            } else if (bin.getOperator() == BinaryExpr.Operator.OR) {
                if (left.asBoolean()) return new Value("boolean", true);
                return new Value("boolean", evaluate(bin.getRight(), env).asBoolean());
            }
            
            Value right = evaluate(bin.getRight(), env);
            return performBinaryOperation(left, right, bin.getOperator());
        } else if (expr instanceof UnaryExpr) {
            UnaryExpr un = (UnaryExpr) expr;
            Value val = evaluate(un.getExpression(), env);
            if (un.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
                return new Value("boolean", !val.asBoolean());
            } else if (un.getOperator() == UnaryExpr.Operator.MINUS) {
                if (val.getUnderlying() instanceof Integer) return new Value("int", -val.asNumber().intValue());
                return new Value("double", -val.asNumber().doubleValue());
            } else if (un.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT || un.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT) {
                int newVal = val.asNumber().intValue() + 1;
                Value res = new Value("int", newVal);
                if (un.getExpression() instanceof NameExpr) {
                    env.assign(((NameExpr)un.getExpression()).getNameAsString(), res);
                }
                return un.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT ? val : res;
            } else if (un.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT || un.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT) {
                int newVal = val.asNumber().intValue() - 1;
                Value res = new Value("int", newVal);
                if (un.getExpression() instanceof NameExpr) {
                    env.assign(((NameExpr)un.getExpression()).getNameAsString(), res);
                }
                return un.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT ? val : res;
            }
        } else if (expr instanceof MethodCallExpr) {
            return evaluateMethodCall((MethodCallExpr) expr, env);
        } else if (expr instanceof ArrayCreationExpr) {
            ArrayCreationExpr ac = (ArrayCreationExpr) expr;
            if (ac.getInitializer().isPresent()) {
                return evaluate(ac.getInitializer().get(), env);
            }
            // default init
            int size = evaluate(ac.getLevels().get(0).getDimension().get(), env).asNumber().intValue();
            String type = ac.getElementType().asString();
            if (type.equals("int")) return new Value("int[]", new int[size]);
            if (type.equals("double")) return new Value("double[]", new double[size]);
            if (type.equals("boolean")) return new Value("boolean[]", new boolean[size]);
            if (type.equals("String")) return new Value("String[]", new String[size]);
            return new Value(type + "[]", new Object[size]);
        } else if (expr instanceof ArrayInitializerExpr) {
            ArrayInitializerExpr ai = (ArrayInitializerExpr) expr;
            NodeList<Expression> values = ai.getValues();
            if (values.isEmpty()) return new Value("Object[]", new Object[0]);
            
            Value first = evaluate(values.get(0), env);
            if (first.getType().equals("int")) {
                int[] arr = new int[values.size()];
                for (int i=0; i<values.size(); i++) arr[i] = evaluate(values.get(i), env).asNumber().intValue();
                return new Value("int[]", arr);
            } else {
                Object[] arr = new Object[values.size()];
                for (int i=0; i<values.size(); i++) arr[i] = evaluate(values.get(i), env).getUnderlying();
                return new Value("Object[]", arr);
            }
        } else if (expr instanceof ArrayAccessExpr) {
            ArrayAccessExpr acc = (ArrayAccessExpr) expr;
            Value arrayVal = evaluate(acc.getName(), env);
            Value indexVal = evaluate(acc.getIndex(), env);
            return getArrayElement(arrayVal, indexVal.asNumber().intValue());
        } else if (expr instanceof ObjectCreationExpr) {
             ObjectCreationExpr oce = (ObjectCreationExpr) expr;
             String type = oce.getTypeAsString();
             if (type.equals("ArrayList")) {
                 return new Value("ArrayList", new ArrayList<>());
             } else if (type.equals("HashMap")) {
                 return new Value("HashMap", new HashMap<>());
             } else if (type.equals("Scanner")) {
                 return new Value("Scanner", "ScannerInstance");
             }
             return new Value(type, "Object@" + System.identityHashCode(new Object()));
        }
        
        return Value.NULL;
    }

    private Value evaluateMethodCall(MethodCallExpr call, Environment env) {
        String name = call.getNameAsString();
        
        // System.out.print / println
        if (call.getScope().isPresent() && call.getScope().get().toString().equals("System.out")) {
            if (name.equals("println") || name.equals("print")) {
                String out = "";
                if (!call.getArguments().isEmpty()) {
                    out = evaluate(call.getArguments().get(0), env).toString();
                    if (out.startsWith("\"") && out.endsWith("\"")) out = out.substring(1, out.length()-1);
                }
                if (name.equals("println")) out += "\n";
                consoleOut.append(out);
                recordStep(call, "Console output: " + out.trim(), env);
                return Value.NULL;
            }
        }
        
        // Scanner
        if (call.getScope().isPresent()) {
            Value scope = evaluate(call.getScope().get(), env);
            if (scope.getType().equals("Scanner")) {
                String input = JOptionPane.showInputDialog(null, "Program is requesting input (" + name + "):");
                if (input == null) input = "";
                if (name.equals("nextLine")) return new Value("String", input);
                if (name.equals("nextInt")) return new Value("int", Integer.parseInt(input.trim()));
                if (name.equals("nextDouble")) return new Value("double", Double.parseDouble(input.trim()));
            }
            
            // ArrayList methods
            if (scope.getType().equals("ArrayList") && scope.getUnderlying() instanceof ArrayList) {
                 ArrayList list = (ArrayList) scope.getUnderlying();
                 if (name.equals("add")) {
                     list.add(evaluate(call.getArguments().get(0), env).getUnderlying());
                     return Value.NULL;
                 } else if (name.equals("get")) {
                     int idx = evaluate(call.getArguments().get(0), env).asNumber().intValue();
                     Object val = list.get(idx);
                     return new Value(val != null ? val.getClass().getSimpleName() : "Object", val);
                 } else if (name.equals("size")) {
                     return new Value("int", list.size());
                 }
            }
            
            // String methods
            if (scope.getType().equals("String") && scope.getUnderlying() instanceof String) {
                String str = (String) scope.getUnderlying();
                if (name.equals("length")) return new Value("int", str.length());
                if (name.equals("charAt")) return new Value("char", str.charAt(evaluate(call.getArguments().get(0), env).asNumber().intValue()));
                if (name.equals("equals")) return new Value("boolean", str.equals(evaluate(call.getArguments().get(0), env).getUnderlying()));
                if (name.equals("substring")) {
                    int start = evaluate(call.getArguments().get(0), env).asNumber().intValue();
                    if (call.getArguments().size() == 2) {
                        int end = evaluate(call.getArguments().get(1), env).asNumber().intValue();
                        return new Value("String", str.substring(start, end));
                    }
                    return new Value("String", str.substring(start));
                }
                if (name.equals("toUpperCase")) return new Value("String", str.toUpperCase());
                if (name.equals("toLowerCase")) return new Value("String", str.toLowerCase());
            }
            
            // Math methods
            if (call.getScope().get().toString().equals("Math")) {
                if (name.equals("max")) {
                    Number a = evaluate(call.getArguments().get(0), env).asNumber();
                    Number b = evaluate(call.getArguments().get(1), env).asNumber();
                    return new Value("double", Math.max(a.doubleValue(), b.doubleValue()));
                }
                if (name.equals("min")) {
                    Number a = evaluate(call.getArguments().get(0), env).asNumber();
                    Number b = evaluate(call.getArguments().get(1), env).asNumber();
                    return new Value("double", Math.min(a.doubleValue(), b.doubleValue()));
                }
                if (name.equals("abs")) return new Value("double", Math.abs(evaluate(call.getArguments().get(0), env).asNumber().doubleValue()));
                if (name.equals("sqrt")) return new Value("double", Math.sqrt(evaluate(call.getArguments().get(0), env).asNumber().doubleValue()));
                if (name.equals("pow")) return new Value("double", Math.pow(evaluate(call.getArguments().get(0), env).asNumber().doubleValue(), evaluate(call.getArguments().get(1), env).asNumber().doubleValue()));
                if (name.equals("random")) return new Value("double", Math.random());
            }
        }
        
        // Custom method call
        MethodDeclaration method = findMethod(name);
        if (method != null) {
            Environment localEnv = new Environment(globalEnv);
            for (int i = 0; i < method.getParameters().size(); i++) {
                String paramName = method.getParameter(i).getNameAsString();
                Value argVal = evaluate(call.getArguments().get(i), env);
                localEnv.define(paramName, argVal);
            }
            callStack.add(0, name + "(...)");
            try {
                recordStep(method, "Entered method " + name, localEnv);
                if (method.getBody().isPresent()) {
                    executeBlock(method.getBody().get(), localEnv);
                }
            } catch (ReturnException re) {
                callStack.remove(0);
                return re.getValue();
            }
            callStack.remove(0);
            return Value.NULL;
        }

        recordStep(call, "Skipped unsupported method call: " + name, env);
        return Value.NULL;
    }

    private MethodDeclaration findMethod(String name) {
        if (globalMethods.containsKey(name)) return globalMethods.get(name);
        for (ClassOrInterfaceDeclaration c : classDecls.values()) {
            for (MethodDeclaration m : c.getMethods()) {
                if (m.getNameAsString().equals(name)) return m;
            }
        }
        return null;
    }

    private Value performBinaryOperation(Value left, Value right, BinaryExpr.Operator op) {
        if (op == BinaryExpr.Operator.EQUALS) {
            return new Value("boolean", Objects.equals(left.getUnderlying(), right.getUnderlying()));
        }
        if (op == BinaryExpr.Operator.NOT_EQUALS) {
            return new Value("boolean", !Objects.equals(left.getUnderlying(), right.getUnderlying()));
        }

        if (left.isString() || right.isString()) {
            if (op == BinaryExpr.Operator.PLUS) {
                String s1 = left.isNull() ? "null" : (left.isString() ? left.asString() : left.toString());
                String s2 = right.isNull() ? "null" : (right.isString() ? right.asString() : right.toString());
                return new Value("String", s1 + s2);
            }
        }

        if (left.isNumber() && right.isNumber()) {
            double l = left.asNumber().doubleValue();
            double r = right.asNumber().doubleValue();
            
            switch (op) {
                case PLUS: return new Value("double", l + r);
                case MINUS: return new Value("double", l - r);
                case MULTIPLY: return new Value("double", l * r);
                case DIVIDE: return new Value("double", l / r);
                case REMAINDER: return new Value("double", l % r);
                case LESS: return new Value("boolean", l < r);
                case LESS_EQUALS: return new Value("boolean", l <= r);
                case GREATER: return new Value("boolean", l > r);
                case GREATER_EQUALS: return new Value("boolean", l >= r);
            }
        }

        return Value.NULL;
    }
    
    private BinaryExpr.Operator getMathOp(AssignExpr.Operator op) {
        switch(op) {
            case PLUS: return BinaryExpr.Operator.PLUS;
            case MINUS: return BinaryExpr.Operator.MINUS;
            case MULTIPLY: return BinaryExpr.Operator.MULTIPLY;
            case DIVIDE: return BinaryExpr.Operator.DIVIDE;
            case REMAINDER: return BinaryExpr.Operator.REMAINDER;
            default: return BinaryExpr.Operator.PLUS;
        }
    }
    
    private Value getArrayElement(Value arrayVal, int idx) {
        if (arrayVal.getUnderlying() instanceof int[]) return new Value("int", ((int[])arrayVal.getUnderlying())[idx]);
        if (arrayVal.getUnderlying() instanceof double[]) return new Value("double", ((double[])arrayVal.getUnderlying())[idx]);
        if (arrayVal.getUnderlying() instanceof Object[]) {
            Object obj = ((Object[])arrayVal.getUnderlying())[idx];
            return new Value(obj != null ? obj.getClass().getSimpleName() : "Object", obj);
        }
        return Value.NULL;
    }
    
    private void setArrayElement(Value arrayVal, int idx, Value right) {
        if (arrayVal.getUnderlying() instanceof int[]) ((int[])arrayVal.getUnderlying())[idx] = right.asNumber().intValue();
        if (arrayVal.getUnderlying() instanceof double[]) ((double[])arrayVal.getUnderlying())[idx] = right.asNumber().doubleValue();
        if (arrayVal.getUnderlying() instanceof Object[]) ((Object[])arrayVal.getUnderlying())[idx] = right.getUnderlying();
    }

    private static class ReturnException extends RuntimeException {
        private final Value value;
        public ReturnException(Value value) {
            this.value = value;
        }
        public Value getValue() { return value; }
    }
}
