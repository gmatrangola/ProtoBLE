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

package com.electrazoom.codegen.server;

import com.electrazoom.codegen.BaseGenerator;
import com.google.protobuf.compiler.PluginProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Generates support classes that can be used to build a server that runs on Linux (including
 * Raspberry Pi).
 * Generates a ServiceRpcServer class that is for each Service defined in the protobuf file
 * Generates a ServiceRpc Interface that provides callbacks that the server can implement to
 * handle messages being sent.
 *
 * See example-linux-server for an example of how to use the generated files
 */
public class LinuxServerPlugin extends BaseGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxServerPlugin.class);

    public static void main(String args[]) {
        LOG.debug("main " + Arrays.toString(args));
        PluginProtos.CodeGeneratorResponse.Builder response = PluginProtos.CodeGeneratorResponse.newBuilder();

        LinuxServerPlugin generator = new LinuxServerPlugin();
        generator.addFileGenerator(new RpcInterfaceGenerator(response));
        generator.addFileGenerator(new RpcServerGenerator(response));
        generator.processInput(args, response);
    }
}
