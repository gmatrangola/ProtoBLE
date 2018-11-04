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

public protocol ProtoBleServiceDelegate: AnyObject {
    func bleDidDiscoverCharacteristics()
    func bleDidError( _ error: Error)
}

public class ProtoBleService: NSObject, CBPeripheralDelegate {
    var device : DeviceInformation? {
        didSet {
            device?.peripheral.delegate = self
            protoRpc.forEach {
                $0.peripheral = device?.peripheral
            }
        }
    }
    public var serviceDelegate : ProtoBleServiceDelegate?
    let serviceUuid : CBUUID
    let protoRpc: [ProtoBleRpc]
    
    public init(serviceUuid : CBUUID, rpc: [ProtoBleRpc]) {
        self.serviceUuid = serviceUuid
        self.protoRpc = rpc
        serviceDelegate = nil
        super.init()
    }
    
    func discover() {
        device?.peripheral.discoverServices([serviceUuid])
    }
    
    // MARK: - BLE Stuff
    
    /*
     Invoked when you discover the peripheral’s available services.
     This method is invoked when your app calls the discoverServices(_:) method. If the services of the peripheral are successfully discovered, you can access them through the peripheral’s services property. If successful, the error parameter is nil. If unsuccessful, the error parameter returns the cause of the failure.
     */
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("didDiscoverServices")
        
        if error != nil {
            reportError(error!)
            return
        }
        guard let services = peripheral.services else {
            reportError(ProtoBleError(kind: .servicesNotFound, message: "\(String(describing: peripheral.name))"))
            return
        }
        
        //We need to discover the all characteristics
        for service in services {
            print("Discovered Services: \(service)")
            if (service.uuid == serviceUuid) {
                let uuids = (protoRpc.map {$0.paramUuid}) + (protoRpc.map {$0.resultUuid})
                peripheral.discoverCharacteristics(uuids, for: service)
            }
        }
    }
    
    /*
     Invoked when you discover the characteristics of a specified service.
     This method is invoked when your app calls the discoverCharacteristics(_:for:) method. If the characteristics of the specified service are successfully discovered, you can access them through the service's characteristics property. If successful, the error parameter is nil. If unsuccessful, the error parameter returns the cause of the failure.
     */
    
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        
        print("******** didDiscoverCharacteristicsFor ***********")
        
        if error != nil {
            reportError(error!)
            return
        }
        
        guard let characteristics = service.characteristics else {
            return
        }
        
        print("Found \(characteristics.count) characteristics!")
        
        for characteristic in characteristics {
            //looks for the right characteristic
            if let readCharacteristicRpc = protoRpc.first(where: {characteristic.uuid.isEqual($0.resultUuid)}) {
                print("Read Characteristic: \(characteristic.uuid)")
                readCharacteristicRpc.readCharacteristic = characteristic
            }
            else if let writeCharacteristicRpc = protoRpc.first(where: {characteristic.uuid.isEqual($0.paramUuid)}) {
                print("Write Characteristic: \(characteristic.uuid)")
                writeCharacteristicRpc.writeCharacteristic = characteristic
            }
            // peripheral.discoverDescriptors(for: characteristic)
        }
        serviceDelegate?.bleDidDiscoverCharacteristics()
    }

    public func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        print("peripheral didiUpdateValueFor \(characteristic.uuid)")
        if let readRpc = protoRpc.first(where: {$0.resultUuid.isEqual(characteristic.uuid)}) {
            readRpc.readCharacteristicValue(characteristic)
        }
    }
    
    /* Handle Descriptors */
    public func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        if error != nil {
            reportError(error!)
            return
        }
        if characteristic.descriptors != nil {
            for x in characteristic.descriptors!{
                let descript = x as CBDescriptor?
                print("function name: DidDiscoverDescriptorForChar \(String(describing: descript?.description))")
            }
        }
    }
    
    // Handle Update Notifications
    public func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if error != nil {
            reportError(error!)
        } else {
            print("Characteristic \(characteristic.uuid) value subscribed")
        }
        
        if !characteristic.isNotifying {
            reportError(ProtoBleError(kind: ProtoBleError.ErrorKind.notSubscribed, message:"\(characteristic.uuid)"))
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        guard error == nil else {
            reportError(error!)
            return
        }
        print("Message sent for \(characteristic.uuid) ")
        if let writeRpc = protoRpc.first(where: {$0.paramUuid.isEqual(characteristic.uuid)}) {
            writeRpc.messageWritten(characteristic)
        }
    }
    
    public func peripheral(_ peripheral: CBPeripheral, didWriteValueFor descriptor: CBDescriptor, error: Error?) {
        guard error == nil else {
            reportError(error!)
            return
        }
        print("Succeeded!")
    }
    
    func reportError(_ err: Error) {
        serviceDelegate?.bleDidError(err)
        print("ProtoBleService Error \(err.localizedDescription)")
    }
}
