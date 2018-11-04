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


import UIKit
import CoreBluetooth

public class ProtoBLECentralManager: NSObject, CBCentralManagerDelegate {
    public static let shared = ProtoBLECentralManager()

    public var serviceUUIDs : [CBUUID]?
    public var centralManager : CBCentralManager!
    public var listener : CBCentralManagerDelegate?
    public var connectedDevice: DeviceInformation?
    var service : ProtoBleService?

    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    public func connect(_ bleService: ProtoBleService) {
        service = bleService
        if let peripheral = connectedDevice?.peripheral {
            centralManager.connect(peripheral)
        }
        else {
            print("Error: No selected device:")
        }
    }
    
    public func scanForServices() {
        centralManager.scanForPeripherals(withServices: serviceUUIDs!, options: [CBCentralManagerScanOptionAllowDuplicatesKey:false])
    }
    
    public func stopScan() {
        centralManager.stopScan()
    }
    
    // - Delegate Stuff
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
        listener?.centralManagerDidUpdateState(centralManager)
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print("Found Peripheral name: \(String(describing: peripheral.name)) Advertisement Data : \(advertisementData)")
        if let l = listener {
            l.centralManager!(central, didDiscover: peripheral, advertisementData: advertisementData, rssi: RSSI)
        }
    }
    
    //-Connected
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("Manager: Connection complete Peripheral info: \(String(describing: peripheral))")
        
        connectedDevice?.peripheral = peripheral
        service?.device = connectedDevice
        service?.discover()
    }
    
    public func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("*****************")
        print("Manager: failed to connect \(String(describing: error))")
        if let l = listener {
            l.centralManager!(central, didFailToConnect: peripheral, error: error)
        }
    }
    
    public func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("Disconnected \(peripheral.identifier)")
        if let l = listener {
            l.centralManager!(central, didDisconnectPeripheral: peripheral, error: error)
        }
    }
}
