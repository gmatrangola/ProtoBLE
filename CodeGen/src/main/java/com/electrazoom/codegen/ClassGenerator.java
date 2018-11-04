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

public abstract class ClassGenerator {
    protected final PluginProtos.CodeGeneratorResponse.Builder response;

    public ClassGenerator(PluginProtos.CodeGeneratorResponse.Builder response) {
        this.response = response;
    }

    public abstract void generate(DescriptorProtos.FileDescriptorProto descriptor);

    public String returnUuidName(String name) {
        if (name.startsWith(".")) name = name.substring(1);
        return uuidName(name, "RETURN_UUID");
    }

    public String paramUuidName(String name) {
        if (name.startsWith(".")) name = name.substring(1);
        return uuidName(name, "PARAM_UUID");
    }

    private String uuidName(String name, String direction) {
        if (name.startsWith(".")) name = name.substring(1);
        return name.toUpperCase() + "_" + direction;
    }

    protected String initialCaps(String text) {
        if (text.startsWith(".")) text = text.substring(1);
        return text.substring(0,1).toUpperCase() + text.substring(1);
    }

    protected String initialLower(String text) {
        if (text.startsWith(".")) text = text.substring(1);
        return text.substring(0,1).toLowerCase() + text.substring(1);
    }
}
