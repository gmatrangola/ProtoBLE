#!/bin/sh

#  GenerateProto.sh
#  ProtoBLEiOS
#
#  Created by Geoffrey Matrangola on 8/24/18.
protoc --proto_path=../../../CodeGen/src/main/proto/ --proto_path=../../../ExampleApiHello/src/main/proto --plugin=protoc-gen-protoble=../../../codegen/build/install/codegen/bin/SwiftClientPlugin --swift_out=Generated --protoble_out=Generated hello.proto
