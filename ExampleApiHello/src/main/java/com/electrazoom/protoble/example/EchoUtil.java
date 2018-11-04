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

package com.electrazoom.protoble.example;

/**
 * Constants for Echo utility. This utility if for testing low level transfer of bytes arrays of
 * various lengths. But it doesn't use Protobuf definitions, so the constants are defined here.
 */
public interface EchoUtil {

    public static final String BULK_SERVICE_GUID = "b8c22c61-5e63-440b-8876-62f25153d9ac";
    public static final String BULK_INPUT_GUID = "2986d2cc-a576-4500-acdb-936805adee57";
    public static final String BULK_OUTPUT_GUID = "4d34f3e7-9480-41a6-9faa-cfa67a410852";

}
