/*
 * ProtoBLE - Protobuf RPC over Bluetooth Low Energy
 * Copyright (c) 2018. Geoffrey Matrangola, electrazoom.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 *     This program is also available under a commercial license. If you wish
 *     to redistribute this library and derivative work for commercial purposes
 *     please see ProtoBLE.com to obtain a proprietary license that will fit
 *     your needs.
 */

package com.electrazoom.codegen;

import com.electrazoom.protoble.BleExtensions;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.compiler.PluginProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BaseGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BaseGenerator.class);
    private List<ClassGenerator> fileGenerators = new ArrayList<>();
    private boolean logData = false;

    public void processInput(String[] args, PluginProtos.CodeGeneratorResponse.Builder response) {
        PluginProtos.CodeGeneratorRequest request;

        final InputStream input;

        try {
            if (args.length == 0) input = System.in;
            else input = new FileInputStream(args[0]);

            ExtensionRegistryLite registryLite = ExtensionRegistryLite.newInstance();
            BleExtensions.registerAllExtensions(registryLite);
            request = PluginProtos.CodeGeneratorRequest.parseFrom(input, registryLite);
            if (logData) {
                LOG.debug("request = " + request);
                FileOutputStream ofs = new FileOutputStream("stream.bin");
                request.writeTo(ofs);
                ofs.close();
            }

            for (DescriptorProtos.FileDescriptorProto fileDescriptorProto : request.getProtoFileList()) {
                for (ClassGenerator fileGenerator : fileGenerators) {
                    fileGenerator.generate(fileDescriptorProto);
                }
            }
            if (logData) LOG.debug("response: " + response);
            response.build().writeTo(System.out);
        } catch (IOException e) {
            LOG.error("Unable to deal with input", e);
        }
    }

    public boolean isLogData() {
        return logData;
    }

    public void setLogData(boolean logData) {
        this.logData = logData;
    }

    public void addFileGenerator(ClassGenerator fileGenerator) {
        this.fileGenerators.add(fileGenerator);
    }
}
