package org.qiyi.video.svg.plugin.service

import com.android.build.gradle.AppExtension
import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.extension.DispatcherExtension
import org.qiyi.video.svg.plugin.manifest.IManifestParser
import org.qiyi.video.svg.plugin.manifest.ManifestParser

public class StubServiceGenerator implements IServiceGenerator {

    def static final NAME = 'android:name'
    def static final PROCESS = 'android:process'
    def static final EXPORTED = 'android:exported'
    def static final ENABLED = 'android:enabled'
    def static final FALSE = 'false'
    def static final TRUE = 'true'
    def static final STUB_SERVICE = 'org.qiyi.video.svg.stub.CommuStubService$CommuStubService'

    def static final AUTHORITIES="android:authorities"
    def static final DISPATCHER_AUTHORITY="org.qiyi.video.svg.dispatcher"

    def static final DISPATCHER_SERVICE='org.qiyi.video.svg.dispatcher.DispatcherService'
    def static final DISPTACHER_PROVIDER='org.qiyi.video.svg.dispatcher.DispatcherProvider'

    def public static final MATCH_DIR = "AndroMatch"
    def public static final MATCH_FILE_NAME = "match_stub.txt"

    private Map<String, String> matchedServices

    private String rootDirPath
    //private String dispatcherProcess
    def dispatcher

    @Override
    public void injectStubServiceToManifest(Project project) {

        println "injectStubServiceToManifest"

        //this.dispatcherProcess=dispatcherProcess

        println "rootDir:" + project.rootDir.absolutePath
        rootDirPath = project.rootDir.absolutePath

        //TODO 要找到别的办法来获取Manifest文件
        def android = project.extensions.getByType(AppExtension)
        this.dispatcher=project.extensions.getByType(DispatcherExtension)

        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                variant.outputs.each { output ->

                    //injectManifestFile(output.processManifest.manifestOutputDirectory)

                    output.processManifest.doLast {
                        //TODO 注意:Instant run时processManifest有可能不执行,另外，要保证各种buildVariants下都能运行

                        println "processManifest-->doLast"

                        println "manifestOutputDirectory:" + output.processManifest.manifestOutputDirectory.absolutePath

                        //output.getProcessManifest().manifestOutputDirectory
                        output.processManifest.outputs.files.each { File file ->
                            //在gradle plugin 3.0.0之前，file是文件，且文件名为AndroidManifest.xml
                            //在gradle plugin 3.0.0之后，file是目录，且不包含AndroidManifest.xml，需要自己拼接
                            //除了目录和AndroidManifest.xml之外，还可能会包含manifest-merger-debug-report.txt等不相干的文件，过滤它
                            if ((file.name.equalsIgnoreCase("AndroidManifest.xml") && !file.isDirectory()) || file.isDirectory()) {
                                if (file.isDirectory()) {
                                    //3.0.0之后，自己拼接AndroidManifest.xml
                                    injectManifestFile(new File(file, "AndroidManifest.xml"))
                                } else {
                                    //3.0.0之前，直接使用
                                    injectManifestFile(file)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    //TODO 这样的话会不会每次都往其中加入相应的CommuStubService?
    private void injectManifestFile(File manifestFile) {

        println "injectManifestFile"

        //def manifestFile = new File(manifestDir, "AndroidManifest.xml")

        //检测文件是否存在
        if (manifestFile != null && manifestFile.exists()) {

            println "manifest: ${manifestFile}"

            String serviceManifest = addServiceItem(manifestFile.absolutePath)

            println "serviceManifest:$serviceManifest"

            //writeStubService2File("./app/build/",MATCH_FILE_NAME)
            writeStubService2File(rootDirPath + File.separator + MATCH_DIR, MATCH_FILE_NAME)

            String newManifestContent = manifestFile.getText("UTF-8")
            int index = newManifestContent.lastIndexOf("</application>")
            newManifestContent = newManifestContent.substring(0, index) + serviceManifest + newManifestContent.substring(index)
            manifestFile.write(newManifestContent, 'UTF-8')

        } else {
            println "Attention!manifest file may not exist!"
        }
    }


    @Override
    Map<String, String> getMatchServices() {
        return matchedServices
    }
    //注意:闭包中只能调用static方法
    private String addServiceItem(String manifestPath) {
        IManifestParser manifestParser = new ManifestParser()
        Set<String> customProcessNames = manifestParser.getCustomProcessNames(manifestPath)

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.application {

            int index = 0
            customProcessNames.each {

                String serviceName = "${STUB_SERVICE}" + index.toString()

                service("${NAME}": serviceName,
                        "${ENABLED}": "${TRUE}",
                        "${EXPORTED}": "${FALSE}",
                        "${PROCESS}": it
                )

                if(matchedServices==null){
                    matchedServices=new HashMap<>()
                }
                matchedServices.put(it, serviceName)

                ++index
            }

            //之后，写入DispatcherService和DispatcherProvider
            def dispatcherProcess=dispatcher.process
            println "dispatcher.process:"+dispatcher.process
            if(dispatcherProcess!=null&&dispatcherProcess.length()>0){
                service("${NAME}":DISPATCHER_SERVICE,
                        "${ENABLED}":"${TRUE}",
                        "${EXPORTED}":"${FALSE}",
                        "${PROCESS}":dispatcherProcess
                )

                provider(
                        "${AUTHORITIES}":DISPATCHER_AUTHORITY,
                        "${EXPORTED}":"${FALSE}",
                        "${NAME}":DISPTACHER_PROVIDER,
                        "${ENABLED}":"${TRUE}",
                        "${PROCESS}":dispatcherProcess
                )

            }else{
                service("${NAME}":DISPATCHER_SERVICE,
                        "${ENABLED}":"${TRUE}",
                        "${EXPORTED}":"${FALSE}"
                )

                provider(
                        "${AUTHORITIES}":DISPATCHER_AUTHORITY,
                        "${EXPORTED}":"${FALSE}",
                        "${NAME}":DISPTACHER_PROVIDER,
                        "${ENABLED}":"${TRUE}"
                )

            }


        }

        // 删除 application 标签
        def normalStr = writer.toString().replace("<application>", "").replace("</application>", "")

        return normalStr
    }

    private void writeStubService2File(String dirPath, String fileName) {

        println "dirPath:" + dirPath + ",fileName:" + fileName

        File dir = new File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        File file = new File(dir, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

        BufferedOutputStream osm = file.newOutputStream()
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(osm))
        matchedServices.each {
            writer.writeLine(it.getKey() + "," + it.getValue())
        }
        writer.close()
        osm.close()
    }

}