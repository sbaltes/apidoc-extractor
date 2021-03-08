package dev.baltes.apidoc.extractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println("Reading properties...");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputDirectory = properties.getProperty("InputDirectory");
        System.out.printf("Reading Java files from directory '%s'...\n", inputDirectory);
        System.out.println("Extracting API documentation...");
        try {
            Files.list(Paths.get(inputDirectory)).forEach(
                    file -> {
                        try {
                            CompilationUnit cu  = StaticJavaParser.parse(file);
                            new MethodAnnotationVisitor().visit(cu, null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MethodAnnotationVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(final MethodDeclaration n, final Void arg) {
            ApiDocumentation apiDocumentation = new ApiDocumentation();
            //System.out.printf("Visiting method %s...\n", n.getName());
            for (AnnotationExpr annotation: n.getAnnotations()) {
                //System.out.printf("Visiting annotation %s...\n", annotation.toString());
                annotation.toAnnotationExpr().ifPresent(
                        annotationExpr -> {
                            switch (annotationExpr.getName().asString()) {
                                case "GET":
                                    apiDocumentation.setMethod("GET");
                                    break;
                                case "POST":
                                    apiDocumentation.setMethod("POST");
                                    break;
                                case "PUT":
                                    apiDocumentation.setMethod("PUT");
                                    break;
                                case "DELETE":
                                    apiDocumentation.setMethod("DELETE");
                                    break;
                                case "Path":
                                    annotationExpr.toSingleMemberAnnotationExpr().ifPresent(
                                            singleMemberAnnotationExpr -> apiDocumentation.setPath(singleMemberAnnotationExpr.getMemberValue().toString())
                                    );
                                    break;
                                case "ApiOperation":
                                    annotationExpr.toNormalAnnotationExpr().ifPresent(
                                           normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(
                                                   pair -> {
                                                       if (pair.getName().toString().equals("value")) {
                                                           apiDocumentation.setDocumentation(pair.getValue().toString());
                                                       } else if (pair.getName().toString().equals("notes")) {
                                                           apiDocumentation.setNotes(pair.getValue().toString());
                                                       }
                                                   }
                                           )
                                    );
                                    break;
                            }
                        }
                );
            }
            if (!apiDocumentation.isEmpty()) {
                System.out.println(apiDocumentation);
            }
        }
    }
}
