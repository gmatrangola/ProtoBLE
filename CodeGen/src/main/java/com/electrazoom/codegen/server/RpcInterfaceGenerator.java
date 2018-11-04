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

import com.electrazoom.codegen.JavaClassGenerator;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

class RpcInterfaceGenerator extends JavaClassGenerator {

    private TypeSpec.Builder rpcInterface;

    public RpcInterfaceGenerator(PluginProtos.CodeGeneratorResponse.Builder response) {
        super(response);
    }

    @Override
    protected JavaFile generateServiceClasses(DescriptorProtos.ServiceDescriptorProto service) {
        // Generate interface
        rpcInterface = TypeSpec.interfaceBuilder(rpcInterfaceName(service));
        rpcInterface.addModifiers(Modifier.PUBLIC);

        // Generate Method definitions for the Service methods
        for (DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
            buildMethod(method);
        }

        // Generate the callbacks for system events
        MethodSpec.Builder onConnectSpec = MethodSpec.methodBuilder("onConnect");
        onConnectSpec.addParameter(String.class, "characteristic", Modifier.FINAL);
        onConnectSpec.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        rpcInterface.addMethod(onConnectSpec.build());

        MethodSpec.Builder onErrorSpec = MethodSpec.methodBuilder("onError");
        onErrorSpec.addParameter(String.class, "source", Modifier.FINAL);
        onErrorSpec.addParameter(String.class, "message", Modifier.FINAL);
        onErrorSpec.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        rpcInterface.addMethod(onErrorSpec.build());

        return JavaFile.builder(javaPackage, rpcInterface.build()).build();
    }

    private void buildMethod(DescriptorProtos.MethodDescriptorProto method) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.getName());
        methodSpec.returns(methodType(method.getOutputType()));
        methodSpec.addParameter(methodType(method.getInputType()), "in", Modifier.FINAL);
        methodSpec.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        rpcInterface.addMethod(methodSpec.build());
    }
}
