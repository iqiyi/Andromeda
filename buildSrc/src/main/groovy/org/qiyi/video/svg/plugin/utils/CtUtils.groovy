
package org.qiyi.video.svg.plugin.utils

import com.android.SdkConstants
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.apache.commons.io.FileUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher

class CtUtils{

    static List<CtClass>getCtClasses(Collection<TransformInput>inputs, ClassPool classPool){
        List<String>classNames=new ArrayList<>()
        List<String>allClasses=new ArrayList<>()
        inputs.each{
            it.directoryInputs.each{
                processSingleDirInput(it,classPool,classNames)
            }

            it.jarInputs.each{
                processSingleJarInput(it,classPool,classNames)
            }
        }

        classNames.each{
            try{
                allClasses.add(classPool.get(it))
            }catch(NotFoundException ex){
                println "Attention:class "+it+" not found in ClassPool!";
            }
        }

        Collections.sort(allClasses,new Comparator<CtClass>(){
            @Override
            int compare(CtClass o1, CtClass o2) {
                return o1.getName()<=>o2.getName()
            }
        })

        return allClasses
    }

    private static void processSingleJarInput(JarInput jarInput,ClassPool classPool,List<String>classNames){

        println "processSingleJarInput()"

        classPool.insertClassPath(jarInput.file.absolutePath)
        def jarFile=new JarFile(jarInput.file)
        Enumeration<JarEntry>jarEntries=jarFile.entries()
        while(jarEntries.hasMoreElements()){
            JarEntry jarEntry=jarEntries.nextElement()
            String className=jarEntry.getName()
            if(className.endsWith(SdkConstants.DOT_CLASS)){
                className=className.substring(0,className.length()-SdkConstants.DOT_CLASS.length()).replaceAll('/','.')

                println "className:"+className
                /*
                if(!classNames.contains(className)){
                    classNames.add(className)
                }
                */

                if(classNames.contains(className)){
                    throw new RuntimeException("You have duplicate classes with the same name : "+className+" please remove duplicate classes ")
                }
                classNames.add(className)
            }
        }

    }



    private static void processSingleDirInput(DirectoryInput dirInput,ClassPool classPool,List<String>classNames){

        println "processSingleDirInput()"

        def dirPath=dirInput.file.absolutePath
        classPool.insertClassPath(dirPath)
        FileUtils.listFiles(dirInput.file,null,true).each{
            if(it.absolutePath.endsWith(SdkConstants.DOT_CLASS)){
                 def className=it.absolutePath.substring(dirPath.length()+1,
                         it.absolutePath.length()-SdkConstants.DOT_CLASS.length()).replaceAll(Matcher.quoteReplacement(File.separator),'.')

                 if(classNames.contains(className)){
                     throw new RuntimeException("You have duplicate classes with the same name : "+className+" please remove duplicate classes ")
                 }
                 classNames.add(className)
            }
        }
    }

}