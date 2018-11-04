// ProtoBLE - Protobuf RPC over Bluetooth Low Energy
// Copyright (c) 2018. ElectraZoom.com
//
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, version 3 of the License.
//
//     This program is distributed in the hope that it will be useful,
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//     GNU General Public License for more details.
//
//     You should have received a copy of the GNU General Public License
//     along with this program.  If not, see <https://www.gnu.org/licenses/>
//
//     This program is also available under a commercial license. If you wish
//     to redistribute this library and derivative work for commercial purposes
//     please see ProtoBLE.com to obtain a proprietary license that will fit
//     your needs.


import Foundation
import CoreBluetooth

public struct ProtoBleError: Error, LocalizedError {
    public init(kind:ErrorKind, message:String) {
        self.kind = kind
        self.message = message
    }
    
    public enum ErrorKind {
        case servicesNotFound;
        case notSubscribed;
        case dataRead;
        case dataWrite;
        case characteristicRead;
        case invalidMessage;
    }
    public let kind: ErrorKind
    public let message: String
    
    public var errorDescription: String? {
        return NSLocalizedString("ErrorMessage", comment: "Error: \(kind): \(message)")
    }
}

public protocol ProtoBleRpcDelegate: AnyObject {
    func messageWasReceived(_ data: Data)
    func messageProgress(current:Int, total:Int)
    func rpcDidError(_ errorType: Error)
}

public extension Data {
    var hexDescription: String {
        return reduce("") {$0 + String(format: "%02x", $1)}
    }
}

class ReadData {
    var message = Data()
    var size = 0
}

class WriteData {
    let data: Data
    let size: Int
    var bytesWritten: Int
    let writeCharacteristic: CBCharacteristic
    init(characteristic: CBCharacteristic, data: Data) {
        self.data = data
        self.size = data.count
        bytesWritten = 0
        self.writeCharacteristic = characteristic
    }
}

public class ProtoBleRpc: NSObject {
    let FIRST_BUFFSIZE = 19

    public let name: String
    public let rpcDelegate: ProtoBleRpcDelegate
    public let paramUuid: CBUUID
    public let resultUuid: CBUUID
    public var currentMessageLen: Int = 0
    
    var peripheral: CBPeripheral?
    public var readCharacteristic: CBCharacteristic? {
        didSet {
            if let rc = readCharacteristic {
                peripheral?.setNotifyValue(true, for: rc)
            }
        }
    }
    public var writeCharacteristic: CBCharacteristic?

    var currentReadData: ReadData? = nil
    var currentWriteMessage: WriteData? = nil

    public init(name: String, paramUuid : CBUUID, resultUuid :CBUUID, rpcDelegate: ProtoBleRpcDelegate) {
        self.name = name
        self.paramUuid = paramUuid
        self.resultUuid = resultUuid
        self.rpcDelegate = rpcDelegate
    }
    
    func messageWritten(_ characteristic: CBCharacteristic) {
        if let current = currentWriteMessage {
            current.bytesWritten += FIRST_BUFFSIZE
            if (current.bytesWritten < current.size) {
                writeNext()
            }
            else {
                print ("Done writing for \(characteristic.uuid)")
                currentWriteMessage = nil
            }
        }
    }
    
    public func writeValue(data : Data) {
        var sizeUint16 = UInt16(data.count)
        let sizeBytes = withUnsafeBytes(of: &sizeUint16) { Array($0) }
        
        var binary = Data(data)
        binary.insert(sizeBytes[0], at: 0)
        binary.insert(sizeBytes[1], at: 0)
        if (sizeUint16 < FIRST_BUFFSIZE) {
            peripheral?.writeValue(binary, for: (writeCharacteristic)!, type: CBCharacteristicWriteType.withResponse)
        }
        else {
            print("Write size of \(sizeUint16)")
            if let wc = writeCharacteristic {
                currentWriteMessage = WriteData(characteristic: wc, data: binary)
                writeNext()
            }
        }
    }
    
    func writeNext() {
        print( "writeNext \(String(describing: currentWriteMessage))")
        if let current = currentWriteMessage {
            if (current.bytesWritten < current.size) {
                let upperBound = current.bytesWritten + min(FIRST_BUFFSIZE, current.size - current.bytesWritten)
                let data = current.data.subdata(in:current.bytesWritten..<upperBound)
                peripheral?.writeValue(data, for: current.writeCharacteristic, type: CBCharacteristicWriteType.withResponse)
            }
            else {
                error(ProtoBleError(kind: .dataWrite, message: "writeNext Error \(current.bytesWritten) >= \(current.size)"))
                currentWriteMessage = nil
            }
        }
    }
    
    public func read() {
        if let read = readCharacteristic {
            peripheral?.readValue(for: read)
        }
        else {
            error(ProtoBleError(kind: .dataRead, message: "Read Characteristic not set yet"))
        }
    }
    
    func readFistBuffer(_ data: Data) -> Bool {
        var isEom = false;
        if data.isEmpty {
            return true
        }
        let sizeData = Data(data.subdata(in: 0..<2).reversed())
        print("sizeData = \(sizeData.hexDescription)")
        if let size:UInt16 = sizeData.withUnsafeBytes({$0.pointee}) {
            print ("Size = \(size)")
            let msgData = data.dropFirst().dropFirst()
            print("msgData \(msgData.hexDescription)")
            if size < FIRST_BUFFSIZE {
                rpcDelegate.messageWasReceived(msgData)
                currentReadData = nil
                isEom = true
            }
            else {
                let rd = ReadData()
                rd.size = Int(size)
                rd.message.append(msgData)
                currentReadData = rd
            }
        }
        return isEom
    }
    
    func readCharacteristicValue(_ characteristic: CBCharacteristic) {
        print("---- readCharacteristic -----")
        if let data = characteristic.value {
            if let crd = currentReadData {
                if !data.isEmpty {
                    print("data = \(data.hexDescription)")
                    if crd.message.isEmpty {
                        if !readFistBuffer(data) {
                            read()
                        }
                    }
                    else {
                        crd.message.append(data)
                        if (crd.message.count < crd.size) {
                            read()
                            rpcDelegate.messageProgress(current: crd.message.count, total: crd.size)
                        }
                        else {
                            rpcDelegate.messageWasReceived(crd.message)
                            currentReadData = nil
                        }
                    }
                }
                else {
                    error(ProtoBleError(kind: .dataRead, message: "Error: No data to read"))
                }
            }
            else {
                if !readFistBuffer(data) {
                    read()
                }
            }
        }
        else {
            error(ProtoBleError(kind:.characteristicRead, message: "Error reading characteristic \(paramUuid)"))
        }
    }
    
    func error( _ err: ProtoBleError) {
        rpcDelegate.rpcDidError(err)
        print(err.localizedDescription)
    }
}
