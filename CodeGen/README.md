# ProtoBLE Protoc Plugin

The `codegen` subproject is a protoc plugin that generates custom
BLE Central and Peripheral (client and server) RPC interfaces to match
the Protobuf definition file.

To build this project run the `:codegen:assembleDist` or
`codegen:installDist` gradle target. These targets will compile the
protoc code generator plugin binary and scripts to run it. The scripts
can be installed anywhere with by extracting the packages in the
build/distributions direcory.

The build process generates three scripts. LinuxServerPlugin generates
the BLE Peripheral (server) code that runs on Linux. AndroidClientPlugin
generates the Java BLE Central (client) that runs on Android. Finally,
SwiftClientPlugin generates the BLE Central (client) that runs on iOS.

## Using the Plugins

You can invoke the Android and Linux java builds using the Gradle protoc
plugin or using the protoc command line tool.

### build.gradle protoc plugin
Create a `protoble` section in the `protobuf` block. Specify
the full or relative path to `LinuxServerPlugin`, or
`AndroidClientPlugin` using the `path` property. Add
`protoble` to the `generateProtoTasks` section. Depending on
how you set up your project, you may also need suppress the default
generation of the java classes using `task.builtins.remove java` in
that same section.

See the build.gradle files
in the example-android-central and example-linux-server projects along
with the protobuf-gradle-plugin documentation for more details.

### command line using protoc executable
To invoke on the command line, add the directories containing your 
.proto files along with the path to codegen/src/main/proto/ using the 
`--proto_path` option. Then add
`--plugin=protoc-gen-protoble=` with the path to the proto-ble
plugin. specify the `--protoble_out=Path` and your .proto file(s).

See the GenerateProto shell script in the Example for ProtoBLEiOS 
project, the protoc man pages and Google's Protobuf guides for more
details.

To generate Swift code using the `SwiftClientPlugin`, use the protoc
from Apple: https://github.com/apple/swift-protobuf

## Enhancing the Code Generators

The following notes are mostly for those who need to contribute to the codegen
subproject. Refer to the main README to details on how to use codegen as
part of proto-ble in your own project.

### Project details

Source code is set up as a standard gradle/maven project. The source
that gets built into the distribution is under src/main. The protobuf
BLE Extensions are defined in src/main/proto/BleExtensions.proto.

The project uses SLF4j for logging.

There are three Main classes that correspond to the three plugin scripts.
they are in the android, server, and swift subpackages and extend the
`BaseGenerator` class.

### Debugging

To avoid having to re-run the full protoc against the test protobuf file
during debug. The file test/resources/stream.bin can be used. It's the
protobuf stream from a run against the example-api-hello protobuf file.

It can be passed int as the first argument to any to the Main classes.


 
