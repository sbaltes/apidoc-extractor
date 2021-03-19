package dev.baltes.apidoc.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class Main {
    public static void main(String[] args) {

        // System.out.println("Reading properties...");
        Properties properties = new Properties();
        try (FileInputStream config = new FileInputStream("config.properties")) {
            properties.load(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String inputDirectory = properties.getProperty("InputDirectory");
        // System.out.printf("Reading Java files from directory '%s'...%n", inputDirectory);
        try (Stream<Path> files = Files.list(Paths.get(inputDirectory))) {

            // System.out.println("Extracting API documentation...");
            System.out.println("\"repo\",\"file\",\"method\",\"path\",\"documentation\",\"notes\"");

            files.forEach(file -> {
                try {
                    ApiDocumentation apiDocumentation = new ApiDocumentation();
                    apiDocumentation.setFilename(file.getFileName().toString());

                    CompilationUnit cu = StaticJavaParser.parse(file);
                    new ClassAnnotationVisitor().visit(cu, apiDocumentation);
                    new MethodAnnotationVisitor().visit(cu, apiDocumentation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClassAnnotationVisitor extends VoidVisitorAdapter<ApiDocumentation> {
        @Override
        public void visit(final ClassOrInterfaceDeclaration n, final ApiDocumentation arg) {

            for (AnnotationExpr annotation : n.getAnnotations()) {
                annotation.toAnnotationExpr().ifPresent(annotationExpr -> {

                    // Api:
                    // https://docs.swagger.io/swagger-core/v1.5.X/apidocs/io/swagger/annotations/Api.html
                    if (annotationExpr.getName().asString().equals("Api")) {
                        annotationExpr.toSingleMemberAnnotationExpr().ifPresent(singleMemberAnnotationExpr -> {
                            if (!singleMemberAnnotationExpr.getMemberValue().toString().contains(" ")) {
                                arg.setClasspath(singleMemberAnnotationExpr.getMemberValue().toString());
                            }
                        });

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (!pair.getValue().toString().contains(" ")
                                            && (pair.getName().toString().equals("basePath")
                                                    || pair.getName().toString().equals("value"))) {
                                        arg.setClasspath(pair.getValue().toString());
                                    }
                                }));
                    }
                });
            }

            for (AnnotationExpr annotation : n.getAnnotations()) {
                annotation.toAnnotationExpr().ifPresent(annotationExpr -> {

                    switch (annotationExpr.getName().asString()) {

                    // RequestMapping:
                    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html
                    case "RequestMapping":
                        annotationExpr.toSingleMemberAnnotationExpr().ifPresent(singleMemberAnnotationExpr -> arg
                                .setClasspath(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("path")
                                            || pair.getName().toString().equals("value")) {
                                        arg.setClasspath(pair.getValue().toString());
                                    }
                                }));
                        break;

                    // Path:
                    // https://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html
                    case "Path":
                    case "javax.ws.rs.Path":
                        annotationExpr.toSingleMemberAnnotationExpr().ifPresent(singleMemberAnnotationExpr -> arg
                                .setClasspath(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("value")) {
                                        arg.setClasspath(pair.getValue().toString());
                                    }
                                }));
                        break;

                    default:
                        break;
                    }
                });
            }
        }
    }

    private static class MethodAnnotationVisitor extends VoidVisitorAdapter<ApiDocumentation> {
        @Override
        public void visit(final MethodDeclaration n, final ApiDocumentation arg) {

            ApiDocumentation apiDocumentation = new ApiDocumentation();

            apiDocumentation.setFilename(arg.getFilename());
            apiDocumentation.setClasspath(arg.getClasspath());

            // System.out.printf("Visiting method %s...%n", n.getName());
            for (AnnotationExpr annotation : n.getAnnotations()) {
                // System.out.printf("Visiting annotation %s...%n", annotation.toString());
                annotation.toAnnotationExpr().ifPresent(annotationExpr -> {

                    String annotationName = annotationExpr.getName().asString();

                    switch (annotationName) {

                    // Methods:
                    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMethod.html
                    case "GET":
                    case "HEAD":
                    case "POST":
                    case "PUT":
                    case "DELETE":
                    case "OPTIONS":
                    case "PATCH":
                    case "TRACE":
                        apiDocumentation.setMethod(annotationName);
                        break;

                    // Mappings:
                    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/GetMapping.html
                    case "GetMapping":
                    case "PostMapping":
                    case "PutMapping":
                    case "DeleteMapping":
                    case "PatchMapping":
                        apiDocumentation.setMethod(annotationName.replace("Mapping", "").toUpperCase());

                        annotationExpr.toSingleMemberAnnotationExpr()
                                .ifPresent(singleMemberAnnotationExpr -> apiDocumentation
                                        .setPath(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("path")
                                            || pair.getName().toString().equals("value")) {
                                        apiDocumentation.setPath(pair.getValue().toString());
                                    }
                                }));
                        break;

                    // RequestMapping:
                    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html
                    case "RequestMapping":
                        annotationExpr.toSingleMemberAnnotationExpr()
                                .ifPresent(singleMemberAnnotationExpr -> apiDocumentation
                                        .setPath(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("method")) {
                                        String[] methods = pair.getValue().toString()
                                                .replaceAll("\\{ |RequestMethod.| \\}", "").split(", ");
                                        Arrays.sort(methods);
                                        apiDocumentation.setMethod(String.join(", ", methods));
                                    } else if (pair.getName().toString().equals("path")
                                            || pair.getName().toString().equals("value")) {
                                        apiDocumentation.setPath(pair.getValue().toString());
                                    }
                                }));
                        break;

                    // Path:
                    // https://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html
                    case "Path":
                    case "javax.ws.rs.Path":
                        annotationExpr.toSingleMemberAnnotationExpr()
                                .ifPresent(singleMemberAnnotationExpr -> apiDocumentation
                                        .setPath(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("value")) {
                                        apiDocumentation.setPath(pair.getValue().toString());
                                    }
                                }));
                        break;

                    // Operation:
                    // https://docs.swagger.io/swagger-core/v2.1.7/apidocs/io/swagger/v3/oas/annotations/Operation.html
                    case "Operation":
                        annotationExpr.toSingleMemberAnnotationExpr()
                                .ifPresent(singleMemberAnnotationExpr -> apiDocumentation
                                        .setDocumentation(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("method")) {
                                        apiDocumentation.setMethod(pair.getValue().toString());
                                    } else if (pair.getName().toString().equals("summary")) {
                                        apiDocumentation.setDocumentation(pair.getValue().toString());
                                    } else if (pair.getName().toString().equals("description")) {
                                        apiDocumentation.setNotes(pair.getValue().toString());
                                    }
                                }));
                        break;

                    // ApiOperation:
                    // https://docs.swagger.io/swagger-core/v1.5.X/apidocs/io/swagger/annotations/ApiOperation.html
                    case "ApiOperation":
                        annotationExpr.toSingleMemberAnnotationExpr()
                                .ifPresent(singleMemberAnnotationExpr -> apiDocumentation
                                        .setDocumentation(singleMemberAnnotationExpr.getMemberValue().toString()));

                        annotationExpr.toNormalAnnotationExpr()
                                .ifPresent(normalAnnotationExpr -> normalAnnotationExpr.getPairs().forEach(pair -> {
                                    if (pair.getName().toString().equals("httpMethod")) {
                                        apiDocumentation.setMethod(pair.getValue().toString());
                                    } else if (pair.getName().toString().equals("value")) {
                                        apiDocumentation.setDocumentation(pair.getValue().toString());
                                    } else if (pair.getName().toString().equals("notes")) {
                                        apiDocumentation.setNotes(pair.getValue().toString());
                                    }
                                }));
                        break;

                    default:
                        break;
                    }
                });
            }

            if (!apiDocumentation.isEmpty()) {
                System.out.println(apiDocumentation);
            }
        }
    }
}
