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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

public abstract class JavaClassGenerator extends ClassGenerator {
    protected String javaPackage;
    protected String outerClassName;

    protected JavaClassGenerator(PluginProtos.CodeGeneratorResponse.Builder response) {
        super(response);
    }

    @Override
    public void generate(DescriptorProtos.FileDescriptorProto descriptor) {
        javaPackage = descriptor.getOptions().getJavaPackage();
        outerClassName = descriptor.getOptions().getJavaOuterClassname();
        for (DescriptorProtos.ServiceDescriptorProto service : descriptor.getServiceList()) {
            JavaFile javaFile = generateServiceClasses(service);
            PluginProtos.CodeGeneratorResponse.File.Builder responseFile = PluginProtos.CodeGeneratorResponse.File.newBuilder();
            String fullPath = javaPackage.replace(".", "/") + "/" + javaFile.typeSpec.name + ".java";
            responseFile.setName(fullPath);
            responseFile.setContent(javaFile.toString());
            response.addFile(responseFile);
        }
    }

    protected abstract JavaFile generateServiceClasses(DescriptorProtos.ServiceDescriptorProto service);

    protected ClassName constantInterfaceName(DescriptorProtos.ServiceDescriptorProto service) {
        return ClassName.get(javaPackage, service.getName() + "Constants");
    }

    protected ClassName rpcInterfaceName(DescriptorProtos.ServiceDescriptorProto service) {
        return ClassName.get(javaPackage, service.getName() + "Rpc");
    }

    protected TypeName messageTypeName(String name) {
        if (outerClassName != null) return TypeVariableName.get(outerClassName + name);
        else return TypeVariableName.get(name);
    }

    protected ClassName methodType(String type) {
        ClassName cn;
        if (outerClassName != null) {
            cn = ClassName.get(javaPackage, outerClassName, type.substring(1));
        }
        else {
            cn = ClassName.get(javaPackage, type.substring(1));
        }
        return cn;
    }
}
