package com.hezd.bindview.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import bindview.annotations.BindView;

/**
 * 1.获取所有使用BindView注解的Element
 * 2.将Element按照Activity进行分组
 * 3.生成xxx_Binding辅助类以及bind方法
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {
    RoundEnvironment environment;
    ProcessingEnvironment processingEnvironment;
    private Filer filer;

    /**
     *
     * @param processingEnv 提供了一些工具集合
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        environment = roundEnvironment;
        Messager messager = processingEnvironment.getMessager();
        System.out.println();
        // 获取BindView注解集合
        Set<? extends Element> elementsAnnotated = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        // 按照Activity进行分组
        HashMap<String, List<VariableElement>> elementMap =  new HashMap<>();
        for (Element element : elementsAnnotated) {
            if (element.getKind() == ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.NOTE, "element:" + element);
            }
            VariableElement variableElement = (VariableElement)element;
            TypeElement enclosingElement = (TypeElement) variableElement.getEnclosingElement();
            String activityName = enclosingElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"enclosingElement:"+activityName);
            List<VariableElement> elements = elementMap.get(activityName);
            if(elements == null){
                elements = new ArrayList<>();
                elementMap.put(activityName,elements);
            }
            elements.add(variableElement);
        }

        // 生成xxxBinding辅助类代码
        for(Map.Entry<String,List<VariableElement>> entry:elementMap.entrySet()){
            String activityName = entry.getKey();
            List<VariableElement> variableList = entry.getValue();
            TypeElement activityElement = (TypeElement) variableList.get(0).getEnclosingElement();
            String packageName = processingEnv.getElementUtils().getPackageOf(activityElement).toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"packageName:"+packageName);
            messager.printMessage(Diagnostic.Kind.NOTE,"activityName:"+activityName);
            String simpleName = activityElement.getSimpleName().toString();
            String className = simpleName+ConstantValues.SUFFIX_BINDING_CLASS;
            messager.printMessage(Diagnostic.Kind.NOTE,"binding class:"+className);
            Writer writer = null;
            try {
                JavaFileObject javaFileObject = filer.createSourceFile(className);
                writer = javaFileObject.openWriter();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("package "+packageName+";\n");
                stringBuffer.append("import com.hezd.bindview.IBinder;\n");
                stringBuffer.append("public class "+className+" implements IBinder<"+activityName+">{\n");
                stringBuffer.append("\tpublic void bind("+activityName+" target) {\n");
                for(VariableElement variableElement:variableList){
                    String variableName = variableElement.getSimpleName().toString();
                    int resourceId = variableElement.getAnnotation(BindView.class).value();
                    stringBuffer.append("\t\ttarget."+variableName+"=target.findViewById("+resourceId+");\n");
                }
                stringBuffer.append("\t}\n");
                stringBuffer.append("}\n");
                writer.write(stringBuffer.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                if(writer!=null){
                    try {
                        writer.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnvironment.getSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportAnnotationTypes = new HashSet<>();
        supportAnnotationTypes.add(BindView.class.getCanonicalName());
        return supportAnnotationTypes;
    }
}