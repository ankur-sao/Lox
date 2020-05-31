package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst{
    public static void main(String [] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary    : Expr left, Token operator, Expr right",
                "Grouping  : Expr expression",
                "Unary     : Token operator, Expr right",
                "Literal   : Object value",
                "Variable  : Token name",
                "Assign    : Token name, Expr Value",
                "Logical   : Token operator, Expr left, Expr right",
                "call      : Expr callee, Token paren, List<Expr> Arguments"

        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print : Expr expression",
                "Var: Token name, Expr initializer",
                "Block: List<Stmt> statements",
                "If: Expr expr, Stmt.Block ifBlock, Stmt.Block elseBlock",
                "While : Expr condition, Stmt body",
                "Break : Stmt destBlock",
                "Function  : Token name, List<Token> params, List<Stmt> body"
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List <String> types) throws IOException {
        String path =   outputDir + "/" + baseName + ".java";
        PrintWriter fileWriter = new PrintWriter(path, "UTF-8");

        fileWriter.println("package com.craftinginterpreters.Lox;");
             fileWriter.println();
        fileWriter.println("import java.util.List;");
        fileWriter.println();
        fileWriter.println("abstract class " + baseName + " {");

        defineVisitor(fileWriter, baseName, types);

        for (String type : types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(fileWriter, className, baseName, fields);
        }

        // accept method in base class
        fileWriter.println();
        fileWriter.println("  abstract <R> R accept(Visitor<R> visitor);");

        fileWriter.println("}");
        fileWriter.close();
    }

    private  static void defineType(
            PrintWriter fileWriter, String className, String baseName, String fields){
        fileWriter.println("static  class " + className + " extends " + baseName + " {");

        //constructor
        fileWriter.println("  " +  className + " ( " + fields + "  ) {");

        //store parameters in fields

        String [] fieldList = fields.split(", ");
        for (String field  : fieldList){
            String name = field.split(" ")[1];
            fileWriter.println("    this." + name + " = " + name + ";");
        }

        fileWriter.println("}");

        //visitor pattern in types classes
        fileWriter.println();
        fileWriter.println("    @Override");
        fileWriter.println("    <R> R accept(Visitor<R> visitor) {");
        fileWriter.println("    return visitor.visit" +
                className + baseName+ "(this);");
        fileWriter.println("    }");


        //Fields
        fileWriter.println();
        for  (String field : fieldList){
            fileWriter.println("    final " +  field + ";");
        }

        fileWriter.println("} ");
    }

    private static void defineVisitor(
            PrintWriter fileWriter, String baseName, List<String> types){
        fileWriter.println("    interface  Visitor<R> { ");

        for(String type: types){
            String typeName = type.split(":")[0].trim();
            fileWriter.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        fileWriter.println(" }");


    }
}






























