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
package org.qiyi.video.svg.plugin.manifest


class ComponentReader implements IComponentReader {

    def manifest

    ComponentReader(String filePath) {
        manifest = new XmlSlurper().parse(filePath)
    }

    @Override
    void readActivities(Set<String> processNames) {
        manifest.application.activity.each {
            addProcess(processNames, it)
        }
    }

    @Override
    void readServices(Set<String> processNames) {
        manifest.application.service.each {
            addProcess(processNames, it)
        }
    }

    private void addProcess(Set<String> processNames, def it) {
        String processName = it.'@android:process'
        if (processName != null && processName.length() > 0) {
            processNames.add(processName)
        }
    }

    @Override
    void readBroadcastReceivers(Set<String> processNames) {
        manifest.application.receiver.each {
            addProcess(processNames, it)
        }
    }

    @Override
    void readProviders(Set<String> processNames) {
        manifest.application.provider.each {
            addProcess(processNames, it)
        }
    }
}