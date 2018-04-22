/*
* Copyright (c) 2018-present, iQIYI, Inc. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
*
*        1. Redistributions of source code must retain the above copyright notice,
*        this list of conditions and the following disclaimer.
*
*        2. Redistributions in binary form must reproduce the above copyright notice,
*        this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*
*        3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived
*        from this software without specific prior written permission.
*
*        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
*        INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
*        IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*        OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
*        OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
*        EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package org.qiyi.video.svg.plugin.service

import com.android.build.gradle.AppExtension
import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import org.qiyi.video.svg.plugin.extension.DispatcherExtension
import org.qiyi.video.svg.plugin.manifest.IManifestParser
import org.qiyi.video.svg.plugin.manifest.ManifestParser

class StubServiceGenerator implements IServiceGenerator {

    def static final NAME = 'android:name'
    def static final PROCESS = 'android:process'
    def static final EXPORTED = 'android:exported'
    def static final ENABLED = 'android:enabled'
    def static final FALSE = 'false'
    def static final TRUE = 'true'
    def static final STUB_SERVICE = 'org.qiyi.video.svg.stub.CommuStubService$CommuStubService'

    def static final AUTHORITIES = "android:authorities"
    def static final DISPATCHER_AUTHORITY = "qiyi.svg.dispatcher"

    def static final DISPATCHER_SERVICE = 'org.qiyi.video.svg.dispatcher.DispatcherService'
    def static final DISPTACHER_PROVIDER = 'org.qiyi.video.svg.dispatcher.DispatcherProvider'

    def public static final MATCH_DIR = "build"
    def public static final MATCH_FILE_NAME = "match_stub.txt"

    private Map<String, String> matchedServices

    private String rootDirPath
    def dispatcher
    private String pkgName

    @Override
    void injectStubServiceToManifest(Project project) {

        println "injectStubServiceToManifest"

        rootDirPath = project.rootDir.absolutePath

        def android = project.extensions.getByType(AppExtension)

        this.dispatcher = project.extensions.getByType(DispatcherExtension)

        project.afterEvaluate {
            android.applicationVariants.all { variant ->

                if (pkgName == null) {
                    pkgName = getPackageName(variant)
                    println "pkgName:" + pkgName
                }

                variant.outputs.each { output ->

                    output.processManifest.doLast {

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

    private void injectManifestFile(File manifestFile) {

        println "injectManifestFile"

        //检测文件是否存在
        if (manifestFile != null && manifestFile.exists()) {

            String serviceManifest = addServiceItem(manifestFile.absolutePath)

            writeStubService2File(rootDirPath + File.separator + MATCH_DIR, MATCH_FILE_NAME)

            String newManifestContent = manifestFile.getText("UTF-8")
            int index = newManifestContent.lastIndexOf("</application>")
            newManifestContent = newManifestContent.substring(0, index) + serviceManifest + newManifestContent.substring(index)
            manifestFile.write(newManifestContent, 'UTF-8')

        } else {
            println "Attention!manifest file may not exist!"
        }
    }

    def getPackageName(variant) {
        if (null == variant) {
            return null
        }
        [variant.mergedFlavor.applicationId, variant.buildType.applicationIdSuffix].findAll().join()
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

                if (matchedServices == null) {
                    matchedServices = new HashMap<>()
                }
                matchedServices.put(it, serviceName)

                ++index
            }

            //之后，写入DispatcherService和DispatcherProvider
            def dispatcherProcess = dispatcher.process
            println "dispatcher.process:" + dispatcher.process
            if (dispatcherProcess != null && dispatcherProcess.length() > 0) {
                service("${NAME}": DISPATCHER_SERVICE,
                        "${ENABLED}": "${TRUE}",
                        "${EXPORTED}": "${FALSE}",
                        "${PROCESS}": dispatcherProcess
                )

                provider(
                        "${AUTHORITIES}": getAuthority(),
                        "${EXPORTED}": "${FALSE}",
                        "${NAME}": DISPTACHER_PROVIDER,
                        "${ENABLED}": "${TRUE}",
                        "${PROCESS}": dispatcherProcess
                )

            } else {
                service("${NAME}": DISPATCHER_SERVICE,
                        "${ENABLED}": "${TRUE}",
                        "${EXPORTED}": "${FALSE}"
                )

                provider(
                        "${AUTHORITIES}": getAuthority(),
                        "${EXPORTED}": "${FALSE}",
                        "${NAME}": DISPTACHER_PROVIDER,
                        "${ENABLED}": "${TRUE}"
                )

            }


        }

        // 删除 application 标签
        def normalStr = writer.toString().replace("<application>", "").replace("</application>", "")

        return normalStr
    }

    private String getAuthority() {
        return pkgName + "." + DISPATCHER_AUTHORITY
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