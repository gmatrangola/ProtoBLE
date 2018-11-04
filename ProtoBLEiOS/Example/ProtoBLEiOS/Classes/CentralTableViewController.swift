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
import ProtoBLEiOS

let EchoService_UUID = CBUUID(string: "b8c22c61-5e63-440b-8876-62f25153d9ac")
let EchoInputCharacteristic_UUID = CBUUID(string: "2986d2cc-a576-4500-acdb-936805adee57")
let EchoOutputCharacteristic_UUID = CBUUID(string: "4d34f3e7-9480-41a6-9faa-cfa67a410852")

class CentralTableViewController: UITableViewController, CBCentralManagerDelegate {
    var timer = Timer()
    var peripherals: [UUID: DeviceInformation] = [:]
    var deviceArray: [DeviceInformation] = []
    weak var deviceDelegate: DeviceDelegate?
    var centralManager: ProtoBLECentralManager!
    var selected: DeviceInformation?

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        centralManager = ProtoBLECentralManager.shared
        centralManager.serviceUUIDs = [HelloWorldService_UUID, EchoService_UUID]
        centralManager.listener = self
        self.refreshControl?.addTarget(self, action: #selector(CentralTableViewController.handleRefresh(_:)), for: UIControlEvents.valueChanged)
        self.refreshControl?.beginRefreshing()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    override func viewWillDisappear(_ animated: Bool) {
        print("Stop Scanning")
        cancelScan()
        selected = nil
        super.viewWillDisappear(animated)
    }
    @objc func handleRefresh(_ refreshControl: UIRefreshControl) {
        if !centralManager.centralManager.isScanning {
            startScan()
        }
    }
    
    // MARK: -- BLE Stuff
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == CBManagerState.poweredOn {
            print("Bluetooth Enabled")
            startScan()
        }
        else {
            print("Bluetooth Disabled - Turn it on, please")
            let alertVC = UIAlertController(title: "Bluetooth is not enabled", message: "Make sure that your bluetooth is turned on", preferredStyle: UIAlertControllerStyle.alert)
            let action = UIAlertAction(title: "ok", style: UIAlertActionStyle.default, handler: { (action: UIAlertAction) -> Void in
                self.dismiss(animated: true, completion: nil)
            })
            alertVC.addAction(action)
            self.present(alertVC, animated: true, completion: nil)
        }
    }
    
    func startScan() {
        print("Scanning for BLE...")
        self.timer.invalidate()
        centralManager.scanForServices()
        Timer.scheduledTimer(timeInterval: 17, target: self, selector: #selector(self.cancelScan), userInfo: nil, repeats: false)
    }
    
    @objc func cancelScan() {
        centralManager.stopScan()
        self.refreshControl?.endRefreshing()
        print("Scan Stopped Found: \(peripherals.count)")
    }
    
    /*
     Called when the central manager discovers a peripheral while scanning. Also, once peripheral is connected, cancel scanning.
     */
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        self.peripherals[peripheral.identifier] = DeviceInformation(peripheral: peripheral, rssi: RSSI, advertisementData: advertisementData)
        deviceArray = peripherals.values.sorted(by: {$0.rssi.floatValue > $1.rssi.floatValue})
        self.tableView.reloadData()
    }
    
    // MARK: - Table view data source
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return peripherals.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "PeripheralTableViewCell"
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PeripheralTableViewCell else {
            fatalError("Wrong cell type in table")
        }
        
        // Configure the cell...
        let device = deviceArray[indexPath.row]
        cell.name.text = device.peripheral.name
        cell.rssi.text = "\(device.rssi) dB"
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let device = deviceArray[indexPath.row]
        cancelScan()
        let services = device.advertisementData[CBAdvertisementDataServiceUUIDsKey] as? NSArray
        if services != nil {
            print ("serivce = \(services![0]) UUID = \(HelloWorldService_UUID) eq? \(services![0] as! CBUUID == HelloWorldService_UUID)")
            if selected != nil {
                centralManager.centralManager.cancelPeripheralConnection(selected!.peripheral)
            }
            selected = device
            if (services?.contains {$0 as! CBUUID == HelloWorldService_UUID })! {
                self.performSegue(withIdentifier: "helloView", sender: self)
                // splitViewController?.showDetailViewController
            }
            else if (services?.contains {$0 as! CBUUID == EchoService_UUID})! {
                self.performSegue(withIdentifier: "echoView", sender: self)
            }
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        print ("prepare for segue \(segue.destination)")
        if segue.identifier == "helloView" {
            let destinationNavigationController = segue.destination as! UINavigationController
            let dest = destinationNavigationController.topViewController
            print ("dest = \(String(describing: dest?.title))")
            if let nextScene = dest as? HelloViewController {
                nextScene.device = selected
            }
        }
    }

}

