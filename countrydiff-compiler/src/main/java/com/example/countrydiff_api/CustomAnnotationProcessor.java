package com.example.countrydiff_api;

import com.example.countrydiff_annotation.CountryAnnotation;
import com.example.countrydiff_annotation.MetaLoad;
import com.example.countrydiff_annotation.MetaSet;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author dan.tang
 * @ClassName CustomAnnotationProcessor
 * @date 2022/7/1 4:50 下午
 */
@AutoService(Processor.class)
public class CustomAnnotationProcessor extends AbstractProcessor {
    public static final String PROJECT = "CountryDiff";
    public static final String SEPARATOR = "$$";
    public static final String NAME_OF_ROOT = PROJECT + SEPARATOR + "Root";
    public static final String PACKAGE_OF_GENERATE_FILE = "com.example.myapplication.countrydiff";
    public static final String KEY_MODULE_NAME = "COUNTRY_DIFF_MODULE_NAME";

    Filer mFiler;
    Elements elementUtils;
    Types typeUtil;
    String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtil = processingEnv.getTypeUtils();

        if (processingEnv.getOptions() != null && !processingEnv.getOptions().isEmpty()) {
            moduleName = processingEnv.getOptions().get(KEY_MODULE_NAME);
        }

        if (moduleName != null && !moduleName.isEmpty()) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");

            System.out.println("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            throw new RuntimeException("Country Diff::Compiler >>> No module name, for more information, look at gradle log.");
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        Map<ClassName, Set<ClassName>> diffMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(CountryAnnotation.class)) {
            TypeElement typeElement = null;

            ClassName subClass;
            if (element.getKind() == ElementKind.METHOD) {
                typeElement = (TypeElement) element.getEnclosingElement();
            } else if (element.getKind() == ElementKind.CLASS) {
                typeElement = (TypeElement) element;
            }
            subClass = ClassName.get(typeElement);

            ClassName interfacesElement = null;
            List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            //一个element一般只对应一个接口
            if (interfaces != null && interfaces.size() == 1) {
                TypeMirror typeMirror = interfaces.get(0);
                interfacesElement = ClassName.get((TypeElement) typeUtil.asElement(typeMirror));
            }

            if (interfacesElement != null && subClass != null) {
                if (diffMap.get(interfacesElement) == null) {
                    Set<ClassName> set = new HashSet<>();
                    diffMap.put(interfacesElement, set);
                }
                //diffmap存以接口名为k，类名为v的东西；(因为使用方法就是通过继承接口的方式)
                diffMap.get(interfacesElement).add(subClass);
            }


            ParameterizedTypeName parameterizedMap = ParameterizedTypeName.get(ClassName.get(HashMap.class),
                    ClassName.get(Class.class), ParameterizedTypeName.get(MetaSet.class));
            //调用MetaLoad$load方法,传入一个参数(任意名称，重要的是类型),这里指定HashMap类型
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("load")
                    //我理解这第二个参数可任意命名，只需满足和下面javapoet调用参数一致即可；
                    .addParameter(ParameterSpec.builder(parameterizedMap, "map").build())
                    .addModifiers(Modifier.PUBLIC);

            //遍历diffmap里的类,并拼接一段代码，通过插桩来启动往文件中写入这
            //段代码，实现完成api类中的HashMap<Class, MetaSet> mCountryDiffs赋值；
            int index = 0;
            for (Map.Entry<ClassName, Set<ClassName>> meta : diffMap.entrySet()) {
                String cMeta = "metaSet" + index;
                //① MetaSet metaSet = new MetaSet();
                methodBuilder.addStatement("$T " + cMeta + "= new $T()", ParameterizedTypeName.get(MetaSet.class),
                        ParameterizedTypeName.get(MetaSet.class));
                for (ClassName className: meta.getValue()){
                    //② metaSet.add(MetaSet.class);
                    methodBuilder.addStatement(cMeta + ".add($T.class)",className);
                }
                //③ map.put(MetaSet.class, metaSet);
                methodBuilder.addStatement("map.put($T.class, " + cMeta + ")", meta.getKey());
            }

            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(NAME_OF_ROOT + SEPARATOR + moduleName)
                                .addModifiers(Modifier.PUBLIC)
                                .addMethod(methodBuilder.build())
                                //addSuperInterface:MetaLoad 这点比较重要，加了自定义注解的自动会默认继承MetaLoad接
                                //口,方便插桩的时候遍历查询类名；(本来读取注解这些都可以用插桩实现，但是插入字节码量大难度较大)
                                .addSuperinterface(MetaLoad.class)
                                .build()).build().writeTo(mFiler);
            } catch (IOException e) {

            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new HashSet<>();
        supportTypes.add(CountryAnnotation.class.getCanonicalName());
        return supportTypes;
    }
}
