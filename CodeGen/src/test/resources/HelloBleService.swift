//
//  HelloBleService.swift
//  ProtoBLEiOS_Example
//
//  This class will be generated this is the example that was written by hand.
//
//  Created by Geoffrey Matrangola on 9/16/18.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import Foundation
import ProtoBLEiOS
import CoreBluetooth

let HelloService_UUID = CBUUID(string: "b481e98f-ecc2-46cc-a91b-bf32ebd35b06")
let GreetingCharacteristic_UUID = CBUUID(string: "e9962334-d9b8-4b8c-8f55-405f0fa4da0d") // output from peripheral
let IntroCharacteristic_UUID = CBUUID(string: "3d9e21c9-c15b-4f56-be8b-e3cb9a885ff5") // input to peripheral

let EchoService_UUID = CBUUID(string: "b8c22c61-5e63-440b-8876-62f25153d9ac")
let EchoInputCharacteristic_UUID = CBUUID(string: "2986d2cc-a576-4500-acdb-936805adee57")
let EchoOutputCharacteristic_UUID = CBUUID(string: "4d34f3e7-9480-41a6-9faa-cfa67a410852")

protocol HelloWorldDelegate: class {
    func helloWorldDidComplete(_ greeting: Greeting)
    func getTimeDidComplete(_ timeResponse: TimeResponse)
    func wasConnected()
    func didError(_ error: String)
}

class HelloBleService {
    let bleService: ProtoBleService
    let helloWorldRpc: ProtoBleRpc

    class HelloWorldRpcDelegate: ProtoBleRpcDelegate {
        let delegate: HelloWorldDelegate
        init(_ delegate: HelloWorldDelegate) {
            self.delegate = delegate
        }

        func onMessageReceived(_ data: Data) {
            if let x = try? Greeting(serializedData: data) {
                delegate.helloWorldDidComplete(x)
            }
        }
        func onError(_ message: String) {
            print ("Got error from Hello World: \(message)")
        }
    }

    public init(delegate: HelloWorldDelegate) {
        let helloWorldRpcDelegate = HelloWorldRpcDelegate(delegate)
        helloWorldRpc = ProtoBleRpc(name: "helloWorld", paramUuid: IntroCharacteristic_UUID, resultUuid: GreetingCharacteristic_UUID, rpcDelegate: helloWorldRpcDelegate)
        bleService = ProtoBleService(serviceUuid: HelloService_UUID, rpc: [helloWorldRpc])

    }

    func helloWorld(introduction: Introduction) throws {
        try helloWorldRpc.writeValue(data: introduction.serializedData())
    }
}
