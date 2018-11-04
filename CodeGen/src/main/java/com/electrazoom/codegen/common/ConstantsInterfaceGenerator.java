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

package com.electrazoom.codegen.common;

import com.electrazoom.codegen.JavaClassGenerator;
import com.electrazoom.protoble.BleExtensions;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

public class ConstantsInterfaceGenerator extends JavaClassGenerator {

    public ConstantsInterfaceGenerator(PluginProtos.CodeGeneratorResponse.Builder response) {
        super(response);
    }

    @Override
    protected JavaFile generateServiceClasses(DescriptorProtos.ServiceDescriptorProto service) {

        // Generate Interface
        ClassName interfaceName = constantInterfaceName(service);
        TypeSpec.Builder constantInterface = TypeSpec.interfaceBuilder(interfaceName);
        constantInterface.addModifiers(Modifier.PUBLIC);

        // Generate Constants
        DescriptorProtos.ServiceOptions options = service.getOptions();
        String pathExt = options.getExtension(BleExtensions.appPath);
        String serviceUuidExt = options.getExtension(BleExtensions.uuid);
        FieldSpec.Builder appPathConstant = FieldSpec.builder(String.class, "APP_PATH")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", pathExt);
        FieldSpec.Builder serviceUuidConstant = FieldSpec.builder(String.class, "SERVICE_UUID")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", serviceUuidExt);
        constantInterface.addField(appPathConstant.build());
        constantInterface.addField(serviceUuidConstant.build());

        for (DescriptorProtos.MethodDescriptorProto method : service.getMethodList()) {
            generateMethod(constantInterface, method);
        }
        return JavaFile.builder(javaPackage, constantInterface.build()).build();
    }

    private void generateMethod(TypeSpec.Builder constantInterface, DescriptorProtos.MethodDescriptorProto method) {
        String parameterUuid = method.getOptions().getExtension(BleExtensions.parameterUuid);
        String returnUuid = method.getOptions().getExtension(BleExtensions.returnUuid);
        FieldSpec.Builder parameterUuidField = FieldSpec.builder(String.class,
                paramUuidName(method.getName()))
                .initializer("$S", parameterUuid)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        constantInterface.addField(parameterUuidField.build());
        FieldSpec.Builder returnUuidField = FieldSpec.builder(String.class,
                returnUuidName(method.getName()))
                .initializer("$S", returnUuid)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        constantInterface.addField(returnUuidField.build());

    }
}
