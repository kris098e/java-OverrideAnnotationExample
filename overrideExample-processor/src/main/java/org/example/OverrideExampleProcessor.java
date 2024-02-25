package org.example;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

public class OverrideExampleProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(OverrideExampleAnnotation.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                TypeElement classElement = (TypeElement) method.getEnclosingElement();
                checkIfMethodOverrides(method, classElement);
            }
        }
        return true; // No further processing of this annotation type
    }

    private void checkIfMethodOverrides(ExecutableElement method, TypeElement classElement) {
        TypeMirror superclass = classElement.getSuperclass();
        boolean overrides = false;
        while (superclass.getKind() != TypeKind.NONE) {
            TypeElement superElement = (TypeElement) typeUtils.asElement(superclass);
            for (Element enclosed : superElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD) {
                    ExecutableElement superMethod = (ExecutableElement) enclosed;
                    if (elementUtils.overrides(method, superMethod, classElement)) {
                        overrides = true;
                        break;
                    }
                }
            }
            if (overrides) break;
            superclass = superElement.getSuperclass();
        }

        if (!overrides) {
            // If the method does not override a method from its superclass, report an error
            Messager messager = processingEnv.getMessager();
            messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Method does not override any method from its superclass",
                    method
            );
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(OverrideExampleAnnotation.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
